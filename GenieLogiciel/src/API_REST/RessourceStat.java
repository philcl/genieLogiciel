package API_REST;

import DataBase.*;
import Modele.Client.Client;
import Modele.Client.ClientList;
import Modele.Staff.Token;
import Modele.Stat.ClientTicket;
import Modele.Stat.Map;
import Modele.Stat.StatutTicket;
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
import java.util.HashMap;
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
        String token = "";
        ClientTicket res = new ClientTicket();
        //int clientId = -1;
        Transaction tx = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token)", false, null, null);
        }

        if (!Token.tryToken(token))
            return Token.tokenNonValide();

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            List clients = session.createQuery("FROM ClientEntity c WHERE c.actif = 1").list();

            for(Object o : clients)
            {
                ClientEntity client = (ClientEntity) o;

                Long nbTicket = 0L;
                try{nbTicket = (Long) session.createQuery("SELECT COUNT(t.id) FROM TicketEntity t").getSingleResult();}
                catch(NoResultException ignored){}

                res.doughnutChartLabels.add(client.getNom());
                res.doughnutChartData.add(nbTicket);
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
        StatutTicket res = new StatutTicket();
        Transaction tx = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token)", false, null, null);
        }

        if (!Token.tryToken(token))
            return Token.tokenNonValide();

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            List statuts = session.createQuery("FROM StatutTicketEntity c").list();

            List tickets = session.createQuery("FROM TicketEntity t").list();

            for(Object o : statuts)
            {
                StatutTicketEntity statutTicketEntity = (StatutTicketEntity) o;

                res.radarChartLabels.add(statutTicketEntity.getIdStatusTicket());
            }

            for (Object o : tickets)
            {
                TicketEntity ticketEntity = (TicketEntity) o;

                if(!res.radarChartData.contains(ticketEntity.getDate().toLocalDateTime().getYear()))
                {
                    res.radarChartData.add(new Map(String.valueOf(ticketEntity.getDate().toLocalDateTime().getYear())));
                }
            }

            for (Object o : tickets)
            {
                TicketEntity ticketEntity = (TicketEntity) o;

                String statut = ticketEntity.getStatut();

                int pos = res.radarChartLabels.indexOf(statut);


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