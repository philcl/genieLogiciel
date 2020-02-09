package API_REST;

import DataBase.*;
import Modele.Staff.Staff;
import Modele.Staff.StaffList;
import Modele.Staff.Token;
import Modele.Staff.UserConnection;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.query.Query;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("JpaQlInspection") //Enleve les erreurs pour les requetes SQL elles peuvent etre juste
@Path("/user")
public class Login {
    private static final String key = "18735496297587485219643518542546";

    @Path("/login")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response login(String jsonStr) {
        Transaction tx = null;
        byte[] bytes = null;
        boolean userFind = false;
        UserConnection user = null;

        try (Session session = CreateSession.getSession()) {
            //Parse du String en JSON pour lire les données
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(jsonStr);
            String pass = obj.get("userPassword").toString();

            System.err.println("Votre password est " + pass);

            //Chiffrage du mot de passe d'origine pour verification sur la base de donnees
            bytes = encrypt(pass);

            //Debut de la partie requete SQL (test de l'ID et du password)
            tx = session.beginTransaction();
            StaffEntity userEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.login = " + obj.get("staffUserName")).getSingleResult();
            if (userEntity == null)
                return Response.status(406)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                        .allow("OPTIONS")
                        .entity("user not found")
                        .build();

            if (Arrays.equals(bytes, userEntity.getMdp()))
                user = getUser(userEntity.getId());
            else
                return Response.status(401)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                        .allow("OPTIONS")
                        .entity(new String("Wrong password "))
                        .build();

            tx.commit();
            session.clear();
            session.close();
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("Erreur parse sur le login");
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity(user)
                .build();
    }

    @Path("/create")
    @POST
    @Consumes("text/plain")
    public Response createUser(String jsonStr) {
        Transaction tx = null;
        JSONObject obj = null;
        Staff p;

        //Parse du String en JSON pour lire les données
        try {
            JSONParser parser = new JSONParser();
            obj = (JSONObject) parser.parse(jsonStr);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return ReponseType.getNOTOK("Erreur lors du parsing du staff", false, null, null);
        }

        p = getStaffFromJSON(obj.toJSONString()); //Transformation du json en modele Staff
        if (p == null)
            return ReponseType.getNOTOK("L'objet staff n'est pas correctement rempli veuillez verifier", false, null, null);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            session.createQuery("FROM StaffEntity s WHERE s.login = '" + p.staffUserName + "'").getSingleResult();
            return ReponseType.getNOTOK("Le login ou le staffId existe deja veuillez les changer", true, tx, session);
        } catch (NoResultException ignored) {}

        //Recuperation du staffEntity
        StaffEntity user = setStaffEntity(p);

        //Recuperation des competences
        ArrayList<JonctionStaffCompetenceEntity> competenceToAdd = getCompetences(p, user.getId());
        if(competenceToAdd == null)
            return ReponseType.getNOTOK("L'une des competences n'existe pas", false, null, null);

        //Verification de l'existance des roles
        ArrayList<JonctionStaffPosteEntity> posteId = getPosteId(p.staffRole, user.getId());
        if(posteId == null)
            return ReponseType.getNOTOK("L'un des postes n'existe pas", false, null, null);

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            //Ajout du staff sur la base pour avoir les foreign key sur competence et poste
            session.save(user);
            tx.commit();
            session.clear();
            tx = session.beginTransaction();

            try {
                //Ajout des competences
                for (JonctionStaffCompetenceEntity j : competenceToAdd)
                    session.save(j);

                //Ajout des postes
                for (JonctionStaffPosteEntity poste : posteId)
                    session.save(poste);

                tx.commit();
                session.clear();
            } catch(Exception e) {
                //Suppression du user avant de partir
                tx.rollback();
                tx = session.beginTransaction();
                session.delete(user);
                tx.commit();
                session.clear();
                session.close();
                e.printStackTrace();
                return ReponseType.getNOTOK("Erreur lors de l'ajout du staff", false, null, null);
            }
            session.close();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            System.err.println("Erreur sur la base lors de l'ajout d'un staff");
            e.printStackTrace();
        }
        return ReponseType.getOK("");
    }

    @Path("/modify")
    @POST
    @Consumes("text/plain")
    public Response modifyPassword(String jsonStr) {
        Transaction tx = null;
        JSONObject obj = null, json = null;
        Staff p;
        String token = "";

        //Parse du String en JSON pour lire les données
        try {
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(jsonStr);
            obj = (JSONObject) json.get("staff");
            token = (String)json.get("token");
        } catch (ParseException ex) {
            ex.printStackTrace();
            return ReponseType.getNOTOK("Erreur lors du parsing du staff", false, null, null);
        }
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        p = getStaffFromJSON(obj.toJSONString()); //Transformation du json en modele Staff
        if (p == null)
            return ReponseType.getNOTOK("L'objet staff n'est pas correctement rempli veuillez verifier", false, null, null);

        //Recuperation du staffEntity
        StaffEntity user = setStaffEntity(p);

        //Recuperation des competences
        ArrayList<JonctionStaffCompetenceEntity> competenceToAdd = getCompetences(p, user.getId());
        if(competenceToAdd == null)
            return ReponseType.getNOTOK("L'une des competences n'existe pas", false, null, null);

        //todo Verifier les roles
        //Verification de l'existance des roles et ajout

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            //Suppression des competences
            Query query = session.createQuery("DELETE JonctionStaffCompetenceEntity j WHERE j.staffId = " + p.staffId );
            query.executeUpdate();

            //Ajout des nouvelles competences
            for (JonctionStaffCompetenceEntity j : competenceToAdd)
                session.save(j);

            //Ajout du staff sur la base
            session.save(user);
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            System.err.println("Erreur sur la base lors de la modification d'un staff");
            e.printStackTrace();
        }
        return ReponseType.getOK("");
    }

    @Path("/list")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getStaffList(String jsonStr) {
        Transaction tx = null;
        ArrayList<StaffList> staffList = new ArrayList<>();

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            List result = session.createQuery("FROM StaffEntity s WHERE s.actif = 1").list();
            tx.commit();
            session.clear();

            for(Object o : result) {
                StaffEntity staff = (StaffEntity) o;
                staffList.add(getStaff(session, staff.getId()));
            }
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK(staffList);
    }

    private byte[] encrypt(String toEncrypt) {
        byte[] bytes = null;
        SecretKeySpec specification = new SecretKeySpec(key.getBytes(), "AES");
        try {
            Cipher chiffreur = Cipher.getInstance("AES");
            chiffreur.init(Cipher.ENCRYPT_MODE, specification);
            bytes = chiffreur.doFinal(toEncrypt.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            System.err.println("Erreur lors l'encryptage du mot de passe");
        }
        return bytes;
    }

    private StaffList getStaff(Session session, int idStaff) {
        StaffList staff = new StaffList();
        Transaction tx = session.beginTransaction();

        StaffEntity staffEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + idStaff).getSingleResult();
        staff.staffTel = staffEntity.getTelephone();
        staff.staffName = staffEntity.getNom();
        staff.staffSurname = staffEntity.getPrenom();
        staff.staffId = idStaff;
        staff.staffMail = staffEntity.getMail();

        AdresseEntity adresseEntity = (AdresseEntity) session.createQuery("FROM AdresseEntity a WHERE a.idAdresse = " + staffEntity.getAdresse()).getSingleResult();
        staff.staffAdress.numero = adresseEntity.getNumero();
        staff.staffAdress.codePostal = adresseEntity.getCodePostal();
        staff.staffAdress.ville = adresseEntity.getVille();
        staff.staffAdress.rue = adresseEntity.getRue();
        staff.staffRole.addAll(getPoste(session, idStaff));

        tx.commit();
        session.clear();
        return staff;
    }

    /**
     * @param userId id du l'utilisateur apres verification du login et mot de passe
     * @return Renvoie un UserConnection correspondant aux informations de connection
     */
    private UserConnection getUser(int userId) {
        UserConnection user = new UserConnection();
        Transaction tx = null;

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            StaffEntity staffEntity = (StaffEntity) session.createQuery("FROM StaffEntity p WHERE p.id = " + userId).getSingleResult();
            user.token = Token.addUID();
            user.firstName = staffEntity.getNom();
            user.lastName = staffEntity.getPrenom();
            user.id = userId;
            user.role.addAll(getPoste(session, userId));

            tx.commit();
            session.clear();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return user;
    }

    /**
     * @param jsonStr
     * @return Renvoi un objet Staff contenant les informations du JSON
     */
    private Staff getStaffFromJSON(String jsonStr) {
        Staff staff = new Staff();
        JSONObject json = null;
        try {
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(jsonStr);
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("Impossible de parse le staff");
        }

        //Extraction des informations du JSON
        try {
            //ajout de l'adresse
            JSONObject adresse = (JSONObject) json.get("staffAdress");
            staff.staffAdress.numero = Integer.parseInt(((Long) adresse.get("numero")).toString());
            staff.staffAdress.codePostal = (String) adresse.get("codePostal");
            staff.staffAdress.rue = (String) adresse.get("rue");
            staff.staffAdress.ville = (String) adresse.get("ville");

            //Ajout du reste de l'objet staff
            staff.staffId = Integer.parseInt(((Long) json.get("staffId")).toString());
            staff.staffSurname = (String) json.get("staffSurname");
            staff.staffName = (String) json.get("staffName");
            staff.staffTel = (String) json.get("staffTel");
            staff.staffMail = (String) json.get("staffMail");
            staff.staffUserName = (String) json.get("staffUserName");
            staff.staffPassword = (String) json.get("staffPassword");
            staff.staffRole.addAll((ArrayList<String>) json.get("staffRole"));
            staff.staffCompetency.addAll((ArrayList<String>) json.get("staffCompetency"));
        } catch (NullPointerException e) {
            return null;
        }
        return staff;
    }

    /**
     * @param p
     * @return Renvoie le staffEntity initialise avec p
     */
    private StaffEntity setStaffEntity(Staff p) {
        Transaction tx = null;
        StaffEntity user = new StaffEntity();

        //Set du staff pour ajout
        user.setLogin(p.staffUserName); //Seulement pour la création
        user.setMdp(encrypt(p.staffPassword));
        user.setActif(1);
        user.setMail(p.staffMail);
        user.setNom(p.staffName);
        user.setPrenom(p.staffSurname);
        user.setTelephone(p.staffTel);

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            //Recupération de l'id max et set
            int id = (int) session.createQuery("SELECT MAX(s.id) FROM StaffEntity s").getSingleResult();
            user.setId(id+1);

            //Ajout de l'adresse
            int adr = p.staffAdress.getId();
            if(adr == -1) //L'adresse n'existe pas lors de la modification ou de l'ajout d'un staff alors ajout direct de l'adresse
            {
                AdresseEntity adresseEntity = new AdresseEntity();
                adresseEntity.setCodePostal(p.staffAdress.codePostal);
                adresseEntity.setNumero(p.staffAdress.numero);
                adresseEntity.setRue(p.staffAdress.rue);
                adresseEntity.setVille(p.staffAdress.ville);
                id = (int) session.createQuery("SELECT MAX(a.idAdresse) FROM AdresseEntity a").getSingleResult();
                adresseEntity.setIdAdresse(id+1);
                session.save(adresseEntity);
            }
            tx.commit();
            session.clear();
            session.close();
            //Je le met apres pour etre sur que le save de l'adresse est bien passe
            user.setAdresse(p.staffAdress.getId());
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return user;
    }

    /**
     * @param p le staff rempli
     * @return Renvoie l'objet si tout c'est bien passe sinon null
     */
    private ArrayList<JonctionStaffCompetenceEntity> getCompetences(Staff p, int staffId) {
        Transaction tx = null;
        List result = null;
        try (Session session = CreateSession.getSession()) {
            //Verification de l'existance des competences et ajout
            tx = session.beginTransaction();
            result = session.createQuery("FROM CompetencesEntity c").list();
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
        }

        HashMap<String, Integer> competences = new HashMap<>();
        ArrayList<JonctionStaffCompetenceEntity> comptenceToAdd = new ArrayList<>();

        //Enregistrement des competences qui sont sur la base
        for (Object o : result) {
            CompetencesEntity c = (CompetencesEntity) o;
            competences.put(c.getCompetence(), c.getIdCompetences());
        }

        //Verification entre les competences entrees et celles sur la base
        for (String s : p.staffCompetency) {
            if (!competences.containsKey(s))
                return null;
            else {
                JonctionStaffCompetenceEntity jonction = new JonctionStaffCompetenceEntity();
                jonction.setCompetenceId(competences.get(s));
                jonction.setStaffId(staffId);
                comptenceToAdd.add(jonction);
            }
        }
        return comptenceToAdd;
    }

    private ArrayList<String> getPoste(Session session, int userId) {
        ArrayList<String> staffPostes = new ArrayList<>();
        List result = session.createQuery("FROM PosteEntity").list();
        HashMap<Integer, String> postes = new HashMap<>();
        for(Object o : result) {
            PosteEntity poste = (PosteEntity) o;
            postes.put(poste.getIdPoste(), poste.getPoste());
        }

        result = session.createQuery("FROM JonctionStaffPosteEntity sp WHERE sp.idStaff = " + userId).list();
        for(Object o : result) {
            JonctionStaffPosteEntity staffPoste = (JonctionStaffPosteEntity) o;
            String poste = postes.get(staffPoste.getIdPoste());
            staffPostes.add(poste);
        }
        return staffPostes;
    }

    private ArrayList<JonctionStaffPosteEntity> getPosteId(ArrayList<String> postes, int staffId) {
        Transaction tx = null;
        ArrayList<JonctionStaffPosteEntity> postesId = new ArrayList<>();
        try(Session session = CreateSession.getSession()) {
             tx = session.beginTransaction();
            for (String s : postes) {
                PosteEntity posteEntity = null;
                try{posteEntity = (PosteEntity) session.createQuery("FROM PosteEntity p WHERE p.poste = '" + s + "'").getSingleResult();}
                catch(NoResultException e) {
                    tx.rollback();
                    session.clear();
                    session.close();
                    return null;
                }
                JonctionStaffPosteEntity poste = new JonctionStaffPosteEntity();
                poste.setIdPoste(posteEntity.getIdPoste());
                poste.setIdStaff(staffId);
                postesId.add(poste);
            }
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return postesId;
    }
}
