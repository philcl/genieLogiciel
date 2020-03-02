package API_REST;

import DataBase.DemandeurEntity;
import DataBase.JonctionSirensiretEntity;
import Modele.Client.Demandeur;
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
import java.sql.Timestamp;
import java.time.Instant;

@Path("/demandeur")
public class RessourceDemandeur {

    static JSONObject demandeurJSON;
    static Demandeur demandeur = new Demandeur();
    static int clientSIREN = -1;

    @Path("create")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public static Response createDemandeur(String jsonStr) {
        Transaction tx = null;

        Response response = doInitDemandeur(jsonStr);
        if(response != null)
            return  response;

        DemandeurEntity demandeurEntity = new DemandeurEntity();
        demandeurEntity.setActif((byte) 1);
        demandeurEntity.setDebut(Timestamp.from(Instant.now()));
        demandeurEntity.setNom(demandeur.demandeur.nom);
        demandeurEntity.setPrenom(demandeur.demandeur.prenom);
        demandeurEntity.setSexe(demandeur.demandeur.sexe);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            int idAdresse;
            try{idAdresse = (int) session.createQuery("SELECT a.id FROM AdresseEntity a WHERE a.id = " + demandeur.idAdresse).getSingleResult();}
            catch (NoResultException e) {
                //todo faire la suppression des SIRET dans JonctionSIRET / SIREN
                return ReponseType.getNOTOK("L'adresse avec l'id " + demandeur.idAdresse + " n'existe pas", true, tx, session);
            }
            demandeurEntity.setAdresse(idAdresse);

            try{
                session.createQuery("FROM DemandeurEntity d WHERE d.telephone = '" + demandeur.telephone + "' and d.actif = 1").getSingleResult();
                return ReponseType.getNOTOK("Le telephone "+ demandeur.telephone + " appartient deja a un demandeur veuillez le changer", true, tx, session);
            }
            catch (NoResultException e) {demandeurEntity.setTelephone(demandeur.telephone);}

            int maxID;
            if(demandeur.demandeur.id == -1)
                maxID = (int) session.createQuery("SELECT MAX(d.id) FROM DemandeurEntity d").getSingleResult()+1;
            else
                maxID = demandeur.demandeur.id;
            demandeurEntity.setIdPersonne(maxID);

            tx = createJonctionSiretSiren(tx, session);

            demandeurEntity.setSiret(demandeur.SIRET);

            session.save(demandeurEntity);
            tx.commit();
            session.clear();
            session.close();
        }
        catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            System.err.println("erreur sur la sauvegarde du demandeur pour la creation SIRET = " + demandeur.SIRET + " SIREN = " + clientSIREN);
            e.printStackTrace();
        }
        return ReponseType.getOK("");
    }

    @Path("/modify")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public static Response modifyDemandeurs(String jsonStr) {
        Transaction tx = null;

        Response response = doInitDemandeur(jsonStr);
        if(response != null)
            return response;

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            DemandeurEntity demandeurEntity;
            try{demandeurEntity = (DemandeurEntity) session.createQuery("FROM DemandeurEntity d WHERE d.idPersonne = " + demandeur.demandeur.id + " and d.actif = 1").getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("Le demandeur avec l'id " + demandeur.demandeur.id + " n'existe pas", true, tx, session);}

            try{session.createQuery("FROM AdresseEntity a WHERE a.idAdresse = " + demandeur.idAdresse).getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("L'adresse " + demandeur.idAdresse + " n'existe pas", true, tx, session);}
            demandeurEntity.setAdresse(demandeur.idAdresse);

            tx = createJonctionSiretSiren(tx, session);

            try{
                DemandeurEntity demandeurEntity1 = (DemandeurEntity) session.createQuery("FROM DemandeurEntity d WHERE d.telephone = '" + demandeur.telephone + "' and d.actif = 1").getSingleResult();
                if(demandeurEntity1.getIdPersonne() != demandeurEntity.getIdPersonne())
                    return ReponseType.getNOTOK("Le telephone " + demandeur.telephone + " existe deja veuillez le changer", true, tx, session);
            }
            catch (NoResultException e) {demandeurEntity.setTelephone(demandeur.telephone);}

            demandeurEntity.setSiret(demandeur.SIRET);
            demandeurEntity.setPrenom(demandeur.demandeur.prenom);
            demandeurEntity.setNom(demandeur.demandeur.nom);
            demandeurEntity.setSexe(demandeur.demandeur.sexe);

            session.saveOrUpdate(demandeurEntity);
            tx.commit();
            session.clear();
            session.close();
        }
        catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
            return ReponseType.getNOTOK("Le demandeur avec l'id " + demandeur.demandeur.id + " n'a pas pu etre modifier", false, null, null);
        }
        return ReponseType.getOK("");
    }

    @Path("/delete")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public static Response deleteDemandeurAPI(String jsonStr) {
        String token = "";
        int actif = -1, idDemandeur = -1;

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            actif = Integer.parseInt(((Long) json.get("actif")).toString());
            idDemandeur = Integer.parseInt(((Long) json.get("idDemandeur")).toString());
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, actif, idDemandeur)", false, null, null);
        }
        if (!Token.tryToken(token))
            return Token.tokenNonValide();
        if(actif != 0)
            return ReponseType.getNOTOK("Le parametre actif ne permet pas la suppression", false, null, null);

        if(!Demandeur.deleteDemandeur(idDemandeur))
            return ReponseType.getNOTOK("Impossible de delete le demandeur avec l'id " + idDemandeur, false, null, null);

        return ReponseType.getOK("");
    }

    private static Transaction createJonctionSiretSiren(Transaction tx, Session session) {
        try{session.createQuery("FROM JonctionSirensiretEntity j WHERE j.siret = " + demandeur.SIRET + " and j.siren = " + clientSIREN + " and j.actif = 1").getSingleResult();}
        catch (NoResultException e) {
            JonctionSirensiretEntity j = new JonctionSirensiretEntity();
            j.setSiren(clientSIREN);
            j.setSiret(demandeur.SIRET);
            j.setActif(1);
            j.setDebut(Timestamp.from(Instant.now()));
            session.save(j);
            tx.commit();
            session.clear();
            tx = session.beginTransaction();
        }
        return tx;
    }

    private static Response doInitDemandeur(String jsonStr) {
        String token = "";
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            demandeurJSON = (JSONObject) json.get("demandeur");
            clientSIREN = Integer.parseInt(((Long) json.get("clientID")).toString());
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, demandeur, clientSIREN)", false, null, null);
        }
        if (!Token.tryToken(token))
            return Token.tokenNonValide();

        if(!demandeur.RecupererDemandandeurDepuisJson(demandeurJSON))
            return ReponseType.getNOTOK("Le demandeur est mal rempli", false, null, null);

        return null;
    }
}
