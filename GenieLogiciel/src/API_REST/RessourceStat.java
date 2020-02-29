package API_REST;

import Modele.Staff.Token;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

//- temps par compétences/tech
//- Nombre tickets par clients
//- Nombre tickets par statut
//- Nombre de taches avec chaque compétences pour tout les tickets et toutes les
//compétences.

@SuppressWarnings("JpaQlInspection") //Enleve les erreurs pour les requetes SQL elles peuvent etre juste
@Path("/stat")
public class RessourceStat {

    @Path("/statTruc")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getClientId(String jsonStr) {
        String token = "", statistique = "";
        int clientId = -1;
        Transaction tx = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            statistique = (String) json.get("statistique");
            if (Security.test(statistique) == null)
                return ReponseType.getNOTOK("Le clientName contient des commandes SQL veuillez corriger", false, null, null);
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, statistique)", false, null, null);
        }

        if (!Token.tryToken(token))
            return Token.tokenNonValide();

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            //clientId = (int) session.createQuery("SELECT c.siren FROM ClientEntity c WHERE c.nom = '" + clientName.replace("'", "''") + "'").getSingleResult();
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
        } catch (NoResultException e) {
            //return ReponseType.getNOTOK("Le client " + clientName + " n'existe pas", false, null, null);
        }

        //return ReponseType.getOK(clientId);
        return ReponseType.getNOTOK("Wesh ça existe pas bro",false,null,null);
    }
}