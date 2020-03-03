package API_REST;

import DataBase.*;
import Modele.Staff.Token;
import Modele.Ticket.Tache;
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
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//todo mettre la date de fin à -1 si le statut n'est pas en Resolu


@Path("tache")
public class RessourceTache {

    static JSONObject tacheJSON;
    static Tache tache = new Tache();

    @Path("/create")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public static Response createTache(String jsonStr) {
        Transaction tx = null;
        Response resp = getInitTask(jsonStr);
        if(resp != null)
            return resp;

        TacheEntity tacheEntity = new TacheEntity();
        tacheEntity.setDebut(Timestamp.from(Instant.now()));
        tacheEntity.setDescription(tache.description);
        tacheEntity.setObjet(tache.objet);
        tacheEntity.setFin(Timestamp.from(Instant.now()));
        if(tache.tempsEstime != -1)
            tacheEntity.setDureeEstimee(tache.tempsEstime);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            resp = verifyTask(session, tx, tacheEntity);
            if(resp != null)
                return resp;

            int maxID = (int) session.createQuery("SELECT MAX(t.id) FROM TacheEntity t").getSingleResult() +1;
            tacheEntity.setId(maxID); //Rajout de l'increment

            session.save(tacheEntity);

            for(String competence : tache.competences) {
                int idCompetence;
                try{idCompetence = (int) session.createQuery("SELECT c.idCompetences FROM CompetencesEntity c WHERE c.competence = '" + competence + "'").getSingleResult();}
                catch (NoResultException e) {return ReponseType.getNOTOK("La competence " + competence + " n'existe pas",true, tx, session);}

                JonctionTacheCompetenceEntity jct = new JonctionTacheCompetenceEntity();
                jct.setCompetence(idCompetence);
                jct.setTache(maxID);
                jct.setActif(1);
                jct.setDebut(Timestamp.from(Instant.now()));
                session.save(jct);
            }

            TicketJonctionEntity jct = new TicketJonctionEntity();

            jct.setIdEnfant(maxID);
            jct.setIdParent(tache.ticketParent);
            jct.setActif(1);
            jct.setDebut(Timestamp.from(Instant.now()));
            session.save(jct);

            tx.commit();
            session.clear();
            session.close();
        }
        catch (HibernateException e)  {
            if(tx != null)
                tx.rollback();
            return ReponseType.getNOTOK("La date de fin est inferieur a la date de debut", false, null, null);
        }
        return ReponseType.getOK("");
    }

    @Path("/modify")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public static Response modifyTask(String jsonStr) {
        Transaction tx = null;

        Response resp = getInitTask(jsonStr);
        if(resp != null)
            return resp;
        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            TacheEntity tacheEntity;
            try{tacheEntity = (TacheEntity) session.createQuery("FROM TacheEntity t WHERE t.id = " + tache.id + " and t.statut != 'Resolu' and t.statut != 'Non resolu'").getSingleResult();}
            catch (NoResultException e) { return ReponseType.getNOTOK("La tache " + tache.id + " n'existe pas", true, tx, session);}

            tacheEntity.setStatut(tache.statut);
            tacheEntity.setDureeEstimee(tache.tempsEstime);
            tacheEntity.setTechnicien(tache.technicien.id);
            tacheEntity.setTicket(tache.ticketParent);
            tacheEntity.setObjet(tache.objet);
            tacheEntity.setDescription(tache.description);
            tacheEntity.setDureeReelle(tache.tempsPasse);

            resp = verifyTask(session, tx, tacheEntity);
            if(resp != null)
                return resp;
            System.err.println("statut = " + tache.statut);
            if(tache.statut.equals("Resolu")) {
                System.err.println("dans le if ");
                tacheEntity.setFin(Timestamp.from(Instant.now()));
            }

            System.err.println("tache fin " + tacheEntity.getFin());

            session.update(tacheEntity);
            tx.commit();
            session.clear();

            tx = session.beginTransaction();

            List result = session.createQuery("SELECT c.competence FROM JonctionTacheCompetenceEntity j, CompetencesEntity c WHERE j.actif = 1 and c.actif = 1 and j.tache = " + tache.id + " and c.idCompetences = j.competence").list();
            ArrayList<String> competencesOrigine = new ArrayList<>();
            for(Object o : result)
                competencesOrigine.add((String) o);



            for(String competence : tache.competences) {
                if(!competencesOrigine.contains(competence)) {
                    @SuppressWarnings("DuplicatedCode")
                    int idCompetence;
                    try {
                        idCompetence = (int) session.createQuery("SELECT c.idCompetences FROM CompetencesEntity c WHERE c.competence = '" + competence + "' and c.actif = 1").getSingleResult();
                    } catch (NoResultException e) {
                        return ReponseType.getNOTOK("La competence " + competence + " n'existe pas", true, tx, session);
                    }

                    JonctionTacheCompetenceEntity jct = new JonctionTacheCompetenceEntity();
                    jct.setCompetence(idCompetence);
                    jct.setTache(tacheEntity.getId());
                    jct.setActif(1);
                    jct.setDebut(Timestamp.from(Instant.now()));
                    session.save(jct);
                }
            }
            for(String competence : competencesOrigine) {
                if(!tache.competences.contains(competence)) {
                    JonctionTacheCompetenceEntity j = (JonctionTacheCompetenceEntity) session.createQuery("SELECT j FROM JonctionTacheCompetenceEntity j, CompetencesEntity c WHERE c.actif = 1 and j.actif = 1 and j.competence = c.idCompetences and c.competence = '" + competence + "'").getSingleResult();
                    j.setActif(0);
                    j.setFin(Timestamp.from(Instant.now()));
                    session.update(j);
                }
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



    @Path("/delete")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public static Response deleteTask(String jsonStr) {
        Transaction tx = null;

        Response resp =  getInitTask(jsonStr);
        if(resp != null)
            return resp;
        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            TacheEntity tacheEntity;
            try{tacheEntity = (TacheEntity) session.createQuery("FROM TacheEntity t WHERE t.id = " + tache.id + " and t.statut != 'Resoolu' and t.statut != 'Non resolu'").getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("La tache " + tache.id + " n'existe pas", true, tx, session);}

            tacheEntity.setStatut("Non resolu");
            tacheEntity.setFin(Timestamp.from(Instant.now()));
            session.update(tacheEntity);

            List result = session.createQuery("FROM JonctionTacheCompetenceEntity j WHERE j.actif = 1 and j.tache = " + tacheEntity.getId()).list();

            for(Object o : result) {
                JonctionTacheCompetenceEntity j = (JonctionTacheCompetenceEntity) o;
                j.setActif(0);
                j.setFin(Timestamp.from(Instant.now()));
                session.update(j);
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

    private static Response getInitTask(String jsonStr) {
        String token = "";
        Transaction tx = null;
        try {
            //Recuperation du JSON et parsing
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            tacheJSON = (JSONObject) json.get("tache");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, tache)", false, null, null);
        }
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        if(!tache.RecupererTacheDepuisJSON(tacheJSON))
            return ReponseType.getNOTOK("Parsing de la tache non reussi veuillez le format de la tache", false, null, null);

        System.err.println("--------------tikectParent = " + tache.ticketParent + "-------------------");
        return null;
    }

    private static Response verifyTask(Session session, Transaction tx, TacheEntity tacheEntity) {
        try{session.createQuery("FROM TicketEntity t WHERE t.id = " + tache.ticketParent + " and t.statut != 'Non Resolu' and t.statut != 'Resolu'").getSingleResult();}
        catch (NoResultException e) {return ReponseType.getNOTOK("Le ticket " + tache.ticketParent + "n'existe pas", true, tx, session);}
        tacheEntity.setTicket(tache.ticketParent);

        try{session.createQuery("FROM StaffEntity s WHERE s.id = " + tache.technicien.id + " and s.actif = 1").getSingleResult();}
        catch (NoResultException e) {return ReponseType.getNOTOK("Le technicien avec l'id " + tache.technicien.id + " n'existe pas", true, tx, session);}
        tacheEntity.setTechnicien(tache.technicien.id);

        try{session.createQuery("FROM StatutTicketEntity s WHERE s.idStatusTicket = '" + tache.statut.replace("'", "''") + "' and s.actif = 1").getSingleResult();}
        catch (NoResultException e) {return ReponseType.getNOTOK("Le statut : " + tache.statut + " n'existe pas", true, tx, session);}
        tacheEntity.setStatut(tache.statut);

        return null;
    }
}
