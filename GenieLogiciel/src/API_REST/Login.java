package API_REST;

import DataBase.PosteEntity;
import DataBase.StaffEntity;
import Modele.Staff;
import Modele.Token;
import Modele.UserConnection;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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

        try(Session session = CreateSession.getSession()) {
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
            if(userEntity == null)
                return Response.status(406)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                        .allow("OPTIONS")
                        .entity("user not found")
                        .build();

            if(Arrays.equals(bytes, userEntity.getMdp()))
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
        }catch(ParseException e){
            e.printStackTrace();
            System.err.println("Erreur parse sur le login");
        }catch (HibernateException e) {
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
        String pass = "", login = "";
        Transaction tx = null;
        Staff p = null;

        try(Session session = CreateSession.getSession()) {
            //Parse du String en JSON pour lire les données
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(jsonStr);
            try{pass = obj.get("userPassword").toString(); }
            catch (NullPointerException e) { return ReponseType.getNOTOK("Password non rempli", false, null, null);}
            try{login = (String)obj.get("staffUserName");}
            catch (NullPointerException e) { return ReponseType.getNOTOK("staffUserName non rempli", false, null, null);}

            tx = session.beginTransaction();
            StaffEntity user = new StaffEntity();
            user.setLogin(login);
            user.setMdp(encrypt(pass));
            user.setActif(1);
            user.setMail("");
            user.setNom("staffName");
            user.setPrenom("staffSurname");
            user.setTelephone("staffTel");
            user.setAdresse(1);
            session.save(user);
            tx.commit();
            session.clear();
            session.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ReponseType.getOK("");
    }

    @Path("/modify")
    @POST
    @Consumes("text/plain")
    public Response modifyPassword(String jsonStr) {
        String token = "";
        Transaction tx = null;

        try(Session session = CreateSession.getSession()){
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String)json.get("token");
            if(!Token.tryToken(token))
                return Token.tokenNonValide();

            tx = session.beginTransaction();
            byte[] pass = encrypt((String)json.get("userPassword"));
            String request = "UPDATE StaffEntity s SET s.mdp = '" + Arrays.toString(pass) + "' WHERE s.login = " + (String)json.get("staffUserName");
            Query update = session.createQuery(request);
            int nbLigne = update.executeUpdate();
            tx.commit();
            session.clear();
            session.close();
            if(nbLigne != 1)
                return ReponseType.getNOTOK("Erreur d'execution de la requete pour le changement de password", false, null, null);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch(HibernateException e) {
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

    private UserConnection getUser(int userId) {
        UserConnection user = new UserConnection();
        Transaction tx = null;

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            List result = session.createQuery("FROM StaffEntity p WHERE p.id = " + userId).list();
            for (Object o : result) {
                StaffEntity staffEntity = (StaffEntity) o;
                user.token = Token.addUID();
                user.firstName = staffEntity.getNom();
                user.lastName = staffEntity.getPrenom();
                user.id = staffEntity.getId();
            }
            tx.commit();
            session.clear();

            tx = session.beginTransaction();
            PosteEntity poste = (PosteEntity) session.createQuery("FROM PosteEntity p WHERE idPersonne = " + user.id).getSingleResult();
            if (poste.getAdmin() == 1)
                user.role.add("Admin");
            else if (poste.getRespTech() == 1)
                user.role.add("Responsable Technicien");
            else if (poste.getTechnicien() == 1)
                user.role.add("Technicien");
            else if (poste.getOperateur() == 1)
                user.role.add("Operateur");
            tx.commit();
            session.clear();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return user;
    }

    private Staff getStaff(int staffId) {
        Staff staff = new Staff();

        return staff;
    }
}
