package API_REST;

import DataBase.TacheEntity;
import DataBase.TicketJonctionEntity;
import Modele.Staff.Token;
import Modele.Ticket.Tache;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.sun.mail.imap.protocol.SearchSequence;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.Instant;

//todo competences des taches associé au ticket
//todo modification des competences du ticket seulement si aucune taches

//todo mettre la date de fin à -1 si le statut n'est pas en Resolu


@Path("tache")
public class RessourceTache {

    @Path("/create")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public static Response createTache(String jsonStr) {
        String token = "";
        JSONObject tacheJSON;
        Tache tache = new Tache();
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

        tache.RecupererTacheDepuisJSON(tacheJSON);

        TacheEntity tacheEntity = new TacheEntity();
        tacheEntity.setDebut(Timestamp.from(Instant.now()));
        tacheEntity.setDescription(tache.description);
        tacheEntity.setObjet(tache.objet);
        tacheEntity.setFin(Timestamp.from(Instant.now()));
        if(tache.tempsEstime != -1)
            tacheEntity.setDureeEstimee(tache.tempsEstime);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            try{session.createQuery("FROM TicketEntity t WHERE t.id = " + tache.ticketParent + " and t.statut != 'Non Resolu' and t.statut != 'Resolu'").getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("Le ticket " + tache.ticketParent + "n'existe pas", true, tx, session);}
            tacheEntity.setTicket(tache.ticketParent);

            try{session.createQuery("FROM StaffEntity s WHERE s.id = " + tache.technicien.id + " and s.actif = 1").getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("Le technicien avec l'id " + tache.technicien.id + " n'existe pas", true, tx, session);}
            tacheEntity.setTechnicien(tache.technicien.id);

            try{session.createQuery("FROM StatutTicketEntity s WHERE s.idStatusTicket = '" + tache.statut.replace("'", "''") + "'").getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("Le statut : " + tache.statut + " n'existe pas", true, tx, session);}
            tacheEntity.setStatut(tache.statut);

            int maxID = (int) session.createQuery("SELECT MAX(t.id) FROM TacheEntity t").getSingleResult() +1;
            tacheEntity.setId(maxID); //Rajout de l'increment

            session.save(tacheEntity);
            TicketJonctionEntity jct = new TicketJonctionEntity();

            jct.setIdEnfant(maxID);
            jct.setIdParent(tache.ticketParent);

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
}
