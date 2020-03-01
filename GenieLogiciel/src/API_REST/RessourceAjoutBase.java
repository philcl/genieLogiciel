package API_REST;

import Modele.Adresse;
import Modele.Staff.Token;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.File;

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
}
