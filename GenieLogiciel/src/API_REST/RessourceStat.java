package API_REST;

import DataBase.AdresseEntity;
import DataBase.ClientEntity;
import DataBase.DemandeurEntity;
import DataBase.StatutTicketEntity;
import Modele.Client.Client;
import Modele.Client.ClientList;
import Modele.Staff.Token;
import javafx.util.Pair;
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
import java.util.ArrayList;
import java.util.List;

//- temps par compétences/tech : KO
//- Nombre tickets par clients : OK
//- Nombre tickets par statut : OK
//- Nombre de taches avec chaque compétences pour tout les tickets et toutes les compétences : KO

@SuppressWarnings("JpaQlInspection") //Enleve les erreurs pour les requetes SQL elles peuvent etre juste
@Path("/stat")
public class RessourceStat {

    @Path("/statTicketParClient")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getTicketParClient(String jsonStr) {
        String token = "", statistique = "";
        ArrayList<Pair<Long, Client>> res = new ArrayList<>();
        //int clientId = -1;
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

            List clients = session.createQuery("FROM ClientEntity c WHERE c.actif = 1").list();

            for(Object o : clients)
            {
                ClientEntity client = (ClientEntity) o;
                Client myClient = new Client();

                myClient.SIREN = client.getSiren();

                AdresseEntity adresseEntity = (AdresseEntity) session.createQuery("SELECT t FROM AdresseEntity t WHERE t.idAdresse = " + client.getAdresse()).getSingleResult();

                myClient.adresse.numero = adresseEntity.getNumero();
                myClient.adresse.codePostal = adresseEntity.getCodePostal();
                myClient.adresse.rue = adresseEntity.getRue();
                myClient.adresse.ville = adresseEntity.getVille();

                Long nbTicketActif = 0L;
                try{nbTicketActif = (Long) session.createQuery("SELECT COUNT(t.id) FROM TicketEntity t").getSingleResult();}
                catch(NoResultException ignored){}

                res.add(new Pair<>(nbTicketActif,myClient));
            }

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

        return ReponseType.getOK(res);
    }

    @Path("/statTicketParCategorie")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getTicketParCategorie(String jsonStr) {
        String token = "", statistique = "";
        ArrayList<Pair<Long,String>> res = new ArrayList<>();
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

            List statuts = session.createQuery("FROM StatutTicketEntity c").list();

            for(Object o : statuts)
            {
                StatutTicketEntity statutTicketEntity = (StatutTicketEntity) o;
                String statut = statutTicketEntity.getIdStatusTicket();

                Long nbTicketActif = 0L;
                try{nbTicketActif = (Long) session.createQuery("SELECT COUNT(t.id) FROM TicketEntity t WHERE t.statut = '" + statut + "'").getSingleResult();}
                catch(NoResultException ignored){}

                res.add(new Pair<>(nbTicketActif,statut));
            }

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

        return ReponseType.getOK(res);
    }
}