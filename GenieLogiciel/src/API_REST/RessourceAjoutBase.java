package API_REST;

import DataBase.CompetencesEntity;
import Modele.Adresse;
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
import java.io.File;
import java.util.ArrayList;

@Path("/ajoutBase")
public class RessourceAjoutBase {

    //todo faire l'API pour ajouter des competences

    @Path("/adresse")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response createAdresseReturnId(String jsonStr) {
        String token = "";
        JSONObject adresseJSON;
        Transaction tx = null;

        try{
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            adresseJSON = (JSONObject) json.get("adresse");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, adresse)", false, null, null);
        }
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        Adresse adresse = new Adresse();
        if(!adresse.RecupererAdresseDepuisJson(adresseJSON))
            return ReponseType.getNOTOK("L'adresse est mal formee ou contient des requetes SQL veuillez verifier", false, null , null);
        if(adresse.addAdresse() == -1)
            return ReponseType.getNOTOK("Impossible de rajouter l'adresse", false, null, null);

        return ReponseType.getOK(adresse.getId());
    }

    @Path("/addCompetence")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response createCompetence(String jsonStr) {
        String token = "", competence = "";
        Transaction tx = null;

        try{
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            competence = (String) json.get("competence");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, competence)", false, null, null);
        }
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        ArrayList<String> roles = Token.getRolesFromToken(token);
        if(roles == null)
            return ReponseType.getNOTOK("Erreur interne au serveur", false, null, null);

        if(!roles.contains("Admin") && !roles.contains("RespTech"))
            return ReponseType.getUnanthorized("Vous n'etes pas autoriser pour le rajout d'une competence veuillez contacter un superieur", false, null, null);

        if(Security.test(competence) == null)
            return ReponseType.getNOTOK("La competence " + competence + " contient des requetes SQL", false, null, null);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            try{
                session.createQuery("FROM CompetencesEntity c WHERE c.competence = '" + competence + "'").getSingleResult();
            }
            catch (NoResultException e) {
                CompetencesEntity competencesEntity = new CompetencesEntity();
                competencesEntity.setCompetence(competence);
                session.save(competencesEntity);
            }

            tx.commit();
            session.clear();
            session.close();
        }
        catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK("");
    }
}
