package API_REST;

import DataBase.*;
import Modele.Staff.*;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.CORBA.TRANSACTION_MODE;

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
import java.sql.Time;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.Instant;
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

            //Chiffrage du mot de passe d'origine pour verification sur la base de donnees
            bytes = encrypt(pass);

            //Debut de la partie requete SQL (test de l'ID et du password)
            tx = session.beginTransaction();
            StaffEntity userEntity;

            //Test de securite SQL pour verifier que le string ne contient pas de commande SQL
            String login = (String) obj.get("staffUserName");
            if(Security.test(login) == null)
                return ReponseType.getNOTOK("Le login contient des commandes SQL ce n'est pas bien merci de corriger", true, tx, session);

            try{ userEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.login = '" + obj.get("staffUserName") + "' and s.actif = 1").getSingleResult();}
            catch(NoResultException e) {return ReponseType.getNOTOK("Utilisateur non trouve", true, tx, session);}

            if (Arrays.equals(bytes, userEntity.getMdp()))
                user = getUser(userEntity.getId());
            else
                return ReponseType.getNOTOK("Mauvais mot de passe", true, tx, session);

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
        JSONObject obj, json;
        Staff p;
        String token = "";

        //Parse du String en JSON pour lire les données
        try {
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(jsonStr);
            obj = (JSONObject) json.get("staff");
            token = (String) json.get("token");

            if(obj == null)
                throw new NullPointerException();

        } catch (ParseException | NullPointerException ex) {
            ex.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, staff)", false, null, null);
        }
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        System.err.println("staff = " + obj);
        p = getStaffFromJSON(obj.toJSONString(), true); //Transformation du json en modele Staff
        if (p == null)
            return ReponseType.getNOTOK("L'objet staff n'est pas correctement rempli ou contient des commandes SQL veuillez verifier", false, null, null);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            try{
                session.createQuery("FROM StaffEntity s WHERE s.login = '" + p.staffUserName + "'").getSingleResult();
                return ReponseType.getNOTOK("Le login existe deja veuillez le changer", true, tx, session);
            } catch (NoResultException ignored) {}

        //Recuperation du staffEntity
        StaffEntity user = setStaffEntity(p, true, session);
        if(user == null)
            return ReponseType.getNOTOK("Impossible de rajouter l'adresse du user", false, null, null);
        user.setActif(1);
        user.setDebut(Timestamp.from(Instant.now()));

            //Ajout du staff sur la base pour avoir les foreign key sur competence et poste
            try{session.save(user);
            tx.commit();
            }
            catch (Exception e) {
                System.err.println("Une erreur c'est produite lors de l'ajout du staff " + jsonStr);
                e.printStackTrace();
                return ReponseType.getNOTOK("Erreur interne", true, tx, session);
            }
            session.clear();
            tx = session.beginTransaction();

            int idUser = (int) session.createQuery("SELECT s.id FROM StaffEntity s WHERE s.login = '" + user.getLogin() + "' and s.actif = 1").getSingleResult();
            user.setId(idUser);

            //Recuperation des competences
            System.err.println("user id = " + user.getId());
            ArrayList<JonctionStaffCompetenceEntity> competenceToAdd = getCompetences(p, user.getId(), session);
            if(competenceToAdd == null)
                return ReponseType.getNOTOK("L'une des competences n'existe pas", false, null, null);

            //Verification de l'existance des roles
            ArrayList<JonctionStaffPosteEntity> posteId = getPosteId(p.staffRole, user.getId(), session);
            if(posteId == null)
                return ReponseType.getNOTOK("L'un des postes n'existe pas ou aucun poste attribué", false, null, null);

            try {
                //Ajout des competences
                for (JonctionStaffCompetenceEntity j : competenceToAdd)
                    session.save(j);

                tx.commit();
                session.clear();
                tx = session.beginTransaction();
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
        } catch (ParseException | NullPointerException ex) {
            ex.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, staff)", false, null, null);
        }
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        p = getStaffFromJSON(obj.toJSONString(), false); //Transformation du json en modele Staff
        if (p == null)
            return ReponseType.getNOTOK("L'objet staff n'est pas correctement rempli ou contient des commandes SQL veuillez verifier", false, null, null);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            try{
                session.createQuery("FROM StaffEntity s WHERE s.id = '" + p.staffId + "' and s.actif = 1").getSingleResult();
                session.createQuery("FROM StaffEntity s WHERE s.login = '" + p.staffUserName + "' and s.actif = 1").getSingleResult();
            }
            catch (NoResultException e) {return ReponseType.getNOTOK("Le login ou le staffId n'existe pas veuillez les changer", false, null, null);}

            //Recuperation du staffEntity
            StaffEntity user = setStaffEntity(p, false, session);

            if(user == null)
                return ReponseType.getNOTOK("Impossible de rajouter l'adresse du user", false, null, null);

            //Recuperation des competences
            ArrayList<JonctionStaffCompetenceEntity> competenceToAdd = getCompetences(p, user.getId(), session);
            if(competenceToAdd == null)
                return ReponseType.getNOTOK("L'une des competences n'existe pas", false, null, null);

            //Verification de l'existance des roles
            ArrayList<JonctionStaffPosteEntity> posteId = getPosteId(p.staffRole, user.getId(), session);
            if(posteId == null)
                return ReponseType.getNOTOK("L'un des postes n'existe pas", false, null, null);

            //Ajout du staff sur la base
            StaffEntity staffEntity;

            try{staffEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + user.getId() + " and s.actif = 1").getSingleResult();}
            catch(NoResultException e) {return ReponseType.getNOTOK("Staff non trouve " + user.getId(), true, tx, session);}

            staffEntity.setAdresse(user.getAdresse());
            if(user.getMdp() != null)
                staffEntity.setMdp(user.getMdp());
            staffEntity.setTelephone(user.getTelephone());
            staffEntity.setMail(user.getMail());
            staffEntity.setPrenom(user.getPrenom());
            staffEntity.setNom(user.getNom());

            session.update(staffEntity);

            //Ajout des nouvelles competences
            for (JonctionStaffCompetenceEntity j : competenceToAdd)
                session.saveOrUpdate(j);

            //Ajout des postes
            for (JonctionStaffPosteEntity poste : posteId)
                session.saveOrUpdate(poste);

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

    @Path("/init")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getInit(String jsonStr) {
        String token = "";
        int staffId = -1;
        StaffInit staffInit = new StaffInit();
        Transaction tx = null;

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            try{ staffId = Integer.parseInt(((Long)json.get("staffId")).toString());} catch(NullPointerException ignored){}
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manques des parametres (token, 'optionnel staffId')", false, null, null);
        }

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            List result = session.createQuery("SELECT c.competence FROM CompetencesEntity c WHERE c.actif = 1").list();
            for(Object o : result)
                staffInit.competencesList.add((String) o);

            result = session.createQuery("SELECT p.poste FROM PosteEntity p WHERE p.actif = 1").list();
            for(Object o : result)
                staffInit.fonctionsList.add((String) o);

            if(staffId != -1) {
                staffInit.staff = new Staff();
                StaffEntity staffEntity;
                try{staffEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + staffId + " and s.actif = 1").getSingleResult();}
                catch(NoResultException e) {return ReponseType.getNOTOK("Le user avec l'id " + staffId + " n'existe pas", true, tx,session);}

                AdresseEntity adresse;
                try{adresse = (AdresseEntity) session.createQuery("FROM AdresseEntity a WHERE a.id = " + staffEntity.getAdresse() + " and a.actif = 1").getSingleResult();}
                catch(NoResultException e) {return ReponseType.getNOTOK("Une erreur c'est produite sur la base de donnee", true, tx, session);}

                //Recuperation du staff
                staffInit.staff.staffPassword = "";
                staffInit.staff.staffUserName = staffEntity.getLogin();
                staffInit.staff.staffMail = staffEntity.getMail();
                staffInit.staff.staffTel = staffEntity.getTelephone();
                staffInit.staff.staffName = staffEntity.getNom();
                staffInit.staff.staffSurname = staffEntity.getPrenom();
                staffInit.staff.staffId = staffId;
                staffInit.staff.staffSexe = staffEntity.getSexe();

                //Recuperation de l'adresse
                staffInit.staff.staffAdress.ville = adresse.getVille();
                staffInit.staff.staffAdress.rue = adresse.getRue();
                staffInit.staff.staffAdress.codePostal = adresse.getCodePostal();
                staffInit.staff.staffAdress.numero = adresse.getNumero();

                //Recuperation des roles et competences
                staffInit.staff.staffCompetency = getCompetences(session, staffId);
                staffInit.staff.staffRole = getPoste(session, staffId);

                tx.commit();
                session.clear();
                session.close();
            }
        } catch(HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK(staffInit);
    }

    @Path("/list")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getStaffList(String jsonStr) {
        Transaction tx = null;
        String token = "";
        ArrayList<StaffList> staffList = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(jsonStr);
            token = (String) obj.get("token");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

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

    @Path("/delete")
    @POST
    @Consumes("text/plain")
    public Response deleteStaff(String jsonStr) {
        String token = "", staffUserName = "";
        int staffId = -1;
        JSONObject json;
        Transaction tx = null;
        try{
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            try{staffUserName = (String) json.get("staffUserName");} catch (NullPointerException ignored){}
            try{staffId = Integer.parseInt(((Long) json.get("staffId")).toString());} catch (NullPointerException ignored){}
            if(staffId == -1 && staffUserName.equals(""))
                throw new NullPointerException();

        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, 'staffUserName ou staffId')", false, null, null);
        }
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            StaffEntity staffEntity;

            if(staffUserName != null) {
                try {staffEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.login = '" + staffUserName + "' and s.actif = 1").getSingleResult();}
                catch (NoResultException e) {return ReponseType.getNOTOK("Le staff avec le login : " + staffUserName + " n'existe pas", true, tx, session);}
            }
            else {
                try {staffEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = '" + staffId + "' and s.actif = 1").getSingleResult();}
                catch (NoResultException e) {return ReponseType.getNOTOK("Le staff avec l'id : " + staffId + " n'existe pas", true, tx, session);}
            }

            //todo supprimer les competences et postes lies
            staffEntity.setActif(0);
            staffEntity.setFin(Timestamp.from(Instant.now()));
            session.update(staffEntity);

            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK("");
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

        StaffEntity staffEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + idStaff + " and s.actif = 1").getSingleResult();
        staff.staffTel = staffEntity.getTelephone();
        staff.staffName = staffEntity.getNom();
        staff.staffSurname = staffEntity.getPrenom();
        staff.staffId = idStaff;
        staff.staffMail = staffEntity.getMail();

        AdresseEntity adresseEntity = (AdresseEntity) session.createQuery("FROM AdresseEntity a WHERE a.idAdresse = " + staffEntity.getAdresse() + " and a.actif = 1").getSingleResult();
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
            StaffEntity staffEntity = (StaffEntity) session.createQuery("FROM StaffEntity p WHERE p.id = " + userId + " and p.actif = 1").getSingleResult();
            user.firstName = staffEntity.getNom();
            user.lastName = staffEntity.getPrenom();
            user.id = userId;
            user.role.addAll(getPoste(session, userId));
            user.token = Token.addUID(user.role);

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
    private Staff getStaffFromJSON(String jsonStr, boolean creation) {
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
            if(adresse == null) {
                System.err.println("l'adresse est mal remplie");
                return null;
            }

            if(!staff.staffAdress.RecupererAdresseDepuisJson(adresse))
                return null;

            //Ajout du reste de l'objet staff
            if(creation) {
                try {staff.staffId = Integer.parseInt(((Long) json.get("staffId")).toString());}
                catch (NullPointerException ignored) {}
            }
            else {
                staff.staffId = -1;
                staff.staffId = Integer.parseInt(((Long) json.get("staffId")).toString());
                if(staff.staffId == -1)
                    return null;
            }
            System.err.println("ok1");

            try {
                staff.staffSurname = Security.test((String) json.get("staffSurname"));
                staff.staffName = Security.test((String) json.get("staffName"));
                staff.staffTel = Security.test((String) json.get("staffTel"));
                staff.staffMail = Security.test((String) json.get("staffMail"));
                staff.staffSexe = Security.test((String) json.get("staffSexe"));
            }
            catch (ClassCastException e) {
                return null;
            }

            if(staff.staffSurname == null || staff.staffName == null ||staff.staffTel == null || staff.staffMail == null)
                return null;

            staff.staffUserName = Security.test ((String) json.get("staffUserName"));
            String password = (String) json.get("staffPassword");
            if(!password.equals("") || creation)
                staff.staffPassword = password;

            ArrayList<String> roles = Security.testArray((ArrayList<String>) json.get("staffRole"));
            ArrayList<String> competences = Security.testArray((ArrayList<String>) json.get("staffCompetency"));
            if(roles == null || competences == null)
                return null;

            staff.staffRole.addAll(roles);
            staff.staffCompetency.addAll(competences);

            System.err.println("userName : " + staff.staffUserName + " pwd : " + staff.staffPassword + " role : " + staff.staffRole.isEmpty() + " competences : " + staff.staffCompetency.isEmpty());

            //Pas de verification sur le password pour ne pas le changer.
            if(staff.staffUserName == null || staff.staffRole == null || staff.staffCompetency == null)
                return null;
        } catch (NullPointerException e) {
            System.err.println("renvoi null le format du json du staff n'est pas correct");
            return null;
        }
        System.err.println("ok3");
        return staff;
    }

    /**
     * @param p
     * @return Renvoie le staffEntity initialise avec p ou null si l'adresse n'a oas pu s'ajouter
     */
    private StaffEntity setStaffEntity(Staff p, boolean creation, Session session) {
        StaffEntity user = new StaffEntity();

        //Set du staff pour ajout
        user.setLogin(p.staffUserName); //Seulement pour la création
        if(p.staffPassword != null)
            user.setMdp(encrypt(p.staffPassword));
        user.setMail(p.staffMail);
        user.setNom(p.staffName);
        user.setPrenom(p.staffSurname);
        user.setTelephone(p.staffTel);
        user.setSexe(p.staffSexe);

        //Recupération de l'id max et set
        if(creation && p.staffId == 0) {
            user.setActif(1);
            user.setDebut(Timestamp.from(Instant.now()));
        }
        else
            user.setId(p.staffId);

        //Ajout de l'adresse
        int adr = p.staffAdress.getId();
        if(adr == -1) //L'adresse n'existe pas lors de la modification ou de l'ajout d'un staff alors ajout direct de l'adresse
            if(p.staffAdress.addAdresse() == -1)
                return null;

        //Je le met apres pour etre sur que le save de l'adresse est bien passe
        user.setAdresse(p.staffAdress.getId());
        return user;
    }

    /**
     * @param p le staff rempli
     * @return Renvoie l'objet si tout c'est bien passe sinon null
     */
    private ArrayList<JonctionStaffCompetenceEntity> getCompetences(Staff p, int staffId, Session session) {
        List result = null;
        //Verification de l'existance des competences et ajout
        result = session.createQuery("FROM CompetencesEntity c WHERE c.actif = 1").list();

        HashMap<String, Integer> competences = new HashMap<>();
        ArrayList<JonctionStaffCompetenceEntity> comptenceToAdd = new ArrayList<>();

        if(p.staffCompetency.isEmpty())
            return comptenceToAdd;

        //Enregistrement des competences qui sont sur la base
        for (Object o : result) {
            CompetencesEntity c = (CompetencesEntity) o;
            System.err.println("competence = " + c.getCompetence() + " id :" + c.getIdCompetences());
            competences.put(c.getCompetence(), c.getIdCompetences());
        }

        //Verification entre les competences entrees et celles sur la base
        for (String s : p.staffCompetency) {
            if(!s.equals("")) {
                if (!competences.containsKey(s))
                    return null;
                else {
                    //todo permettre la suppression des competences avec le staff
                    JonctionStaffCompetenceEntity jonction = new JonctionStaffCompetenceEntity();
                    jonction.setCompetenceId(competences.get(s));
                    jonction.setStaffId(staffId);
                    jonction.setActif(1);
                    jonction.setDebut(Timestamp.from(Instant.now()));

                    System.err.println("ajout de la competence = " + competences.get(s) + " pour le staff :" + staffId);
                    comptenceToAdd.add(jonction);
                }
            }
        }
        return comptenceToAdd;
    }

    private ArrayList<String> getCompetences(Session session, int userId) {
        ArrayList<String> staffCompetences = new ArrayList<>();
        List result = session.createQuery("FROM CompetencesEntity c WHERE c.actif = 1").list();
        HashMap<Integer, String> competences = new HashMap<>();
        for(Object o : result) {
            CompetencesEntity competence = (CompetencesEntity) o;
            competences.put(competence.getIdCompetences(), competence.getCompetence());
        }

        result = session.createQuery("FROM JonctionStaffCompetenceEntity sc WHERE sc.staffId = " + userId + " and sc.actif = 1").list();
        for(Object o : result) {
            JonctionStaffCompetenceEntity staffPoste = (JonctionStaffCompetenceEntity) o;
            String poste = competences.get(staffPoste.getCompetenceId());
            staffCompetences.add(poste);
        }
        return staffCompetences;
    }

    private ArrayList<String> getPoste(Session session, int userId) {
        ArrayList<String> staffPostes = new ArrayList<>();
        List result = session.createQuery("FROM PosteEntity").list();
        HashMap<Integer, String> postes = new HashMap<>();
        for(Object o : result) {
            PosteEntity poste = (PosteEntity) o;
            postes.put(poste.getIdPoste(), poste.getPoste());
        }

        result = session.createQuery("FROM JonctionStaffPosteEntity sp WHERE sp.idStaff = " + userId + " and sp.actif = 1").list();
        for(Object o : result) {
            JonctionStaffPosteEntity staffPoste = (JonctionStaffPosteEntity) o;
            String poste = postes.get(staffPoste.getIdPoste());
            staffPostes.add(poste);
        }
        return staffPostes;
    }

    private ArrayList<JonctionStaffPosteEntity> getPosteId(ArrayList<String> postes, int staffId, Session session) {
        Transaction tx = null;
        ArrayList<JonctionStaffPosteEntity> postesId = new ArrayList<>();
        //todo permettre la suppression des postes
        for (String s : postes) {
            PosteEntity posteEntity = null;
            try{posteEntity = (PosteEntity) session.createQuery("FROM PosteEntity p WHERE p.poste = '" + s + "' and p.actif = 1").getSingleResult();}
            catch(NoResultException e) {
                tx.rollback();
                session.clear();
                session.close();
                return null;
            }
            JonctionStaffPosteEntity poste = new JonctionStaffPosteEntity();
            poste.setIdPoste(posteEntity.getIdPoste());
            poste.setIdStaff(staffId);
            poste.setActif(1);
            poste.setDebut(Timestamp.from(Instant.now()));
            postesId.add(poste);
        }
        return postesId;
    }
}
