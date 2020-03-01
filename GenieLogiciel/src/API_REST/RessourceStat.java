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
//- Nombre de taches avec chaque compétences pour tout les tickets : OK

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
                try{nbTicket = (Long) session.createQuery("SELECT COUNT(t.id) FROM TicketEntity t WHERE t.siren = '" + client.getSiren() + "'").getSingleResult();}
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

    @Path("/statTicketParStatut")
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

                String string = String.valueOf(ticketEntity.getDate().toLocalDateTime().getYear());

                if(!(res.contient(string)))
                {
                    System.err.println("J'ajoute un élément : " + string);
                    res.radarChartData.add(new Map(string,res.radarChartLabels.size()));
                }
                else {
                    System.err.println("J'ajoute PAS d'élément : " + string);
                }
            }

            for (Object o : tickets)
            {
                TicketEntity ticketEntity = (TicketEntity) o;

                String statut = ticketEntity.getStatut();

                String string = String.valueOf(ticketEntity.getDate().toLocalDateTime().getYear());

                int pos = res.radarChartLabels.indexOf(statut);

                for(Map map : res.radarChartData)
                {
                    if(map.label.equals(string))
                    {
                        map.data.set(pos,map.data.get(pos)+1);
                    }
                }
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

    @Path("/statNombreTicketParCompetence")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getNbTicketParCompetence(String jsonStr) {
        String token = "", statistique = "";
        ClientTicket res = new ClientTicket();
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

            List competences = session.createQuery("FROM CompetencesEntity c").list();

            List tickets = session.createQuery("FROM TicketEntity t").list();

            for(Object o : competences)
            {
                CompetencesEntity competencesEntity = (CompetencesEntity) o;

                res.doughnutChartLabels.add(competencesEntity.getCompetence());
            }

            /*for (Object o : tickets)
            {
                TicketEntity ticketEntity = (TicketEntity) o;

                String string = String.valueOf(ticketEntity.getDate().toLocalDateTime().getYear());

                if(!(res.contient(string)))
                {
                    System.err.println("J'ajoute un élément : " + string);
                    res.radarChartData.add(new Map(string,res.radarChartLabels.size()));
                }
                else {
                    System.err.println("J'ajoute PAS d'élément : " + string);
                }
            }*/

            for (int i = 0;i<res.doughnutChartLabels.size();i++)
            {
                res.doughnutChartData.add(0L);
            }

            for (Object o : tickets)
            {
                TicketEntity ticketEntity = (TicketEntity) o;

                List idCompetencesTicket = session.createQuery("SELECT c.competence FROM JonctionTicketCompetenceEntity c WHERE c.idTicket = " + ticketEntity.getId()).list();

                for (Object p : idCompetencesTicket)
                {
                    int idCompetence = (Integer) p;

                    CompetencesEntity competencesEntity = (CompetencesEntity) session.createQuery("FROM CompetencesEntity c WHERE c.idCompetences = " + idCompetence).getSingleResult();

                    for (int i = 0;i<res.doughnutChartLabels.size();i++)
                    {
                        if(res.doughnutChartLabels.get(i).equals(competencesEntity.getCompetence()))
                        {
                            res.doughnutChartData.set(i,res.doughnutChartData.get(i)+1);
                        }
                    }
                }
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