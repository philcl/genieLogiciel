package API_REST;

import DataBase.LoginEntity;
import DataBase.PersonneEntity;
import DataBase.PosteEntity;
import Modele.Personne;
import Modele.Token;
import Modele.UserConnection;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
        UserConnection user = new UserConnection();
        LoginEntity userEntity = null;
        Transaction tx = null;
        byte[] bytes = null;
        boolean userFind = false;

        try(Session session = CreateSession.getSession()) {
            //Parse du String en JSON pour lire les données
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(jsonStr);
            String pass = obj.get("userPassword").toString();

            //Chiffrage du mot de passe d'origine pour verification sur la base de donnees
            bytes = encrypt(pass);

            //Debut de la partie requete SQL (test de l'ID et du password)
            tx = session.beginTransaction();
            List result = session.createQuery("FROM LoginEntity l WHERE l.id = " + obj.get("userId")).list();
            for(Object o : result) {
                userEntity = (LoginEntity) o;
                if(Arrays.equals(bytes, userEntity.getPassword()))
                    userFind = true;
                else
                    System.err.println("personne non trouvée id = " + userEntity.getId() + "password = " + Arrays.toString(userEntity.getPassword()) + " bytes= " + Arrays.toString(bytes));
            }
            tx.commit();
            session.clear();

            //Recuperation du user
            if(userFind) {
                tx = session.beginTransaction();
                result = session.createQuery("FROM PersonneEntity p WHERE p.id = " + userEntity.getId()).list();
                for (Object o : result) {
                    PersonneEntity personneEntity = (PersonneEntity) o;
                    user.token = Token.getInstance().addUID();
                    user.firstName = personneEntity.getNom();
                    user.lastName = personneEntity.getPrenom();
                    user.id = personneEntity.getIdPersonne();
                }
                tx.commit();
                session.clear();

                tx = session.beginTransaction();
                PosteEntity poste = (PosteEntity) session.createQuery("FROM PosteEntity p WHERE idPersonne = " + user.id).getSingleResult();
                if(poste.getAdmin() == 1)
                    user.role = "Admin";
                else if(poste.getRespTech() == 1)
                    user.role = "Responsable Technicien";
                else if(poste.getTechnicien() == 1)
                    user.role = "Technicien";
                else if(poste.getOperateur() == 1)
                    user.role = "Operateur";
                tx.commit();
                session.clear();
            }
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
        String pass = "";
        Transaction tx = null;
        boolean personFind = false;
        PersonneEntity p = null;

        try(Session session = CreateSession.getSession()) {
            //Parse du String en JSON pour lire les données
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(jsonStr);
            pass = obj.get("userPassword").toString();
            int userId = Integer.parseInt(obj.get("userId").toString());

            tx = session.beginTransaction();
            List result = session.createQuery("FROM PersonneEntity p WHERE p.id = " + userId).list();
            for(Object o : result) {
                p = (PersonneEntity) o;
                if (p.getIdPersonne() == userId) {
                    personFind = true;
                    break;
                }

            }
            tx.commit();
            session.clear();

            if(personFind) {
                tx = session.beginTransaction();
                LoginEntity user = new LoginEntity();
                user.setId(p.getIdPersonne());

                user.setPassword(encrypt(pass));
                session.save(user);

                tx.commit();
                session.clear();
            }
            session.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .build();
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
}
