package API_REST;

import DataBase.*;
import Modele.*;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JpaQlInspection") //Enleve les erreurs pour les requetes SQL elles peuvent etre juste
@Path("/ticket")
public class RessourceTicket {
    @Path("/init")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getInit(String jsonStr) {
        String token = "";
        long IdClient = 0;
        int IdTicket = -1;
        try {
            //Recuperation du JSON et parsing
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            IdClient = Long.parseLong((String) json.get("clientId"));
            try{IdTicket = Integer.parseInt (((Long) json.get("ticketId")).toString());} catch(NullPointerException ignored){}
            token = (String) json.get("token");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (clientId, token, 'optionnel ticketId')", false, null, null);
        }

        //Verification du token
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        //Init des objets
        ArrayList<Object> listInfos = new ArrayList<>();
        InitTicket answer = new InitTicket();
        Transaction tx = null;

        try(Session session = CreateSession.getSession()) {
            //Recuperation des types des demandes
            tx = session.beginTransaction();
            List result = session.createQuery("FROM TypeDemandesEntity ").list();
            for(Object o : result) {
                TypeDemandesEntity typeDemande = (TypeDemandesEntity) o;
                answer.demandeTypeList .add(typeDemande.getIdTypeDemandes());
            }

            //Recuperation de la liste des techniciens
            result = session.createQuery("FROM StaffEntity").list();
            for(Object o : result) {
                StaffEntity technicienEntity = (StaffEntity) o;
                answer.technicienList.add(new Personne(technicienEntity.getNom(), technicienEntity.getPrenom(), technicienEntity.getId()));
            }

            //Recuperation de la liste des demandeurs
            result = session.createQuery("FROM PersonneEntity p WHERE p.siret LIKE '" + IdClient + "%'").list();
            for(Object o : result) {
                PersonneEntity demandeurEntity = (PersonneEntity) o;
                answer.demandeurList .add(new Personne(demandeurEntity.getNom(), demandeurEntity.getPrenom(), demandeurEntity.getIdPersonne()));
            }

            //Recuperation de la liste des sites du client
            String request = "FROM JonctionAdresseSiretEntity A WHERE A.siret LIKE '" + IdClient + "%'";
            result = session.createQuery(request).list();
            if(result == null)
                return ReponseType.getNOTOK("L'id du client n'existe pas", true, tx, session);

            for(Object o : result) {
                JonctionAdresseSiretEntity adresse = (JonctionAdresseSiretEntity) o;
                if (adresse.getActif() == 1) {
                    AdresseEntity adr = (AdresseEntity) session.createQuery("FROM AdresseEntity a WHERE a.id = " + adresse.getIdAdresse()).getSingleResult();
                    Demandeur adresseClient = new Demandeur(adresse.getSiret(), new Adresse(adr.getNumero(), adr.getCodePostal(), adr.getRue(), adr.getVille()));
                    answer.clientSiteList.add(adresseClient);
                }
            }

            //Recuperation de la liste des categories
            result = session.createQuery("FROM CategorieEntity ").list();
            for(Object o : result) {
                CategorieEntity categorie = (CategorieEntity) o;
                answer.categorieList .add(categorie.getCategorie());
            }

            //Recuperation de la liste des statut
            result = session.createQuery("FROM StatutTicketEntity ").list();
            for (Object o : result) {
                StatutTicketEntity statut = (StatutTicketEntity) o;
                answer.statusList .add(statut.getIdStatusTicket());
            }

            // Recuperation de la liste des competences
            result = session.createQuery("FROM CompetencesEntity ").list();
            for (Object o : result) {
                CompetencesEntity competence = (CompetencesEntity) o;
                answer.skillsList .add(competence.getCompetence());
            }
            tx.commit();
            session.clear();

            //Ajout du ticket si son id est present
            if(IdTicket != -1) {
                //todo : verifier que le clientId et le client du ticket sont bien les mêmes
                answer.ticket = recuperationTicket(session, IdTicket);
                if(answer.ticket == null)
                    return ReponseType.getNOTOK("Le ticket avec l'id " + IdTicket + " n'exsite pas", true, null, session);
            }
            session.close();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK(answer);
    }

    @Path("/create")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response postCreation(String jsonStr) {
        String token = "", ticketJson = "";
        int ticketParent = 1;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String)json.get("token");
            ticketJson = ((JSONObject)json.get("ticket")).toJSONString();
            try{ticketParent = Integer.parseInt(((Long)json.get("ticketParent")).toString());} catch(NullPointerException ignored){}
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametre (token, ticket, 'optionnel ticketParent')", false,null, null);
        }
        //Verification du token
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        ArrayList<Object> list = new ArrayList<>();
        Transaction tx = null;
        TicketEntity ticketEntity = new TicketEntity();
        Ticket ticket = createObjectFromJson(ticketJson);

        if(ticket == null)
            return Response.status(406)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                    .allow("OPTIONS")
                    .entity("Le ticket n'est pas present dans la requete")
                    .build();

        ticketEntity.setCategorie(ticket.categorie);
        ticketEntity.setDate(Timestamp.from(Instant.now()));
        ticketEntity.setDescription(ticket.description);
        ticketEntity.setObjet(ticket.objet);
        ticketEntity.setStatut(ticket.statut);
        ticketEntity.setType(ticket.type);
        ticketEntity.setTicket(ticketParent);

        try(Session session = CreateSession.getSession()) {
            //Ajout de l'adresse (SIRET) des ID du demandeur et du technicien
            tx = session.beginTransaction();
            PersonneEntity demandeur= (PersonneEntity) session.createQuery("FROM PersonneEntity p WHERE p.id = " + ticket.demandeur.id).getSingleResult();
            ticketEntity.setAdresse(demandeur.getSiret());
            ticketEntity.setDemandeur(demandeur.getIdPersonne());

            StaffEntity tech = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + ticket.technicien.id).getSingleResult();
            ticketEntity.setTechnicien(tech.getId());

            int maxID = (int) session.createQuery("SELECT MAX(t.id) FROM TicketEntity t").getSingleResult();
            ticketEntity.setId(maxID+1); //Rajout de l'increment

            //Ajout en base de donnee du ticket
            session.save(ticketEntity);
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        list.add(ticket);
        list.add(null);
        list.add(null);
        list.add(ticketEntity);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity(list)
                .build();
    }

    @Path("/modify")
    @POST
    @Consumes("text/plain")
    public Response postModify(String jsonStr) {
        String token = "", ticketJson = "";
        long IdClient = 0;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            IdClient = (Long) json.get("clientId");
            token = (String)json.get("token");
            ticketJson = ((JSONObject)json.get("ticket")).toJSONString();

        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            System.err.println("impossible de parse");
            return ReponseType.getNOTOK("L'un des parametre n'est pas present (clientId, token, ticket)", false, null, null);
        }

        //Verification du token
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        Transaction tx = null;
        Ticket ticket = createObjectFromJson(ticketJson);
        if(ticket == null)
            return ReponseType.getNOTOK("Le ticket n'est pas present dans la requete", false, null, null);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            //Recuperation du tech
            StaffEntity tech = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + ticket.technicien.id).getSingleResult();
            if(tech == null)
                return ReponseType.getNOTOK("Le technicien n'existe pas", true, tx, session);

            //Recuperation du demandeur
            PersonneEntity demandeur = (PersonneEntity) session.createQuery("FROM PersonneEntity p WHERE p.id = " + ticket.demandeur.id).getSingleResult();
            if(demandeur == null)
                return ReponseType.getNOTOK("Le demandeur n'existe pas", true, tx, session);

            //Recuperation du client
            JonctionAdresseSiretEntity client = (JonctionAdresseSiretEntity) session.createQuery("FROM JonctionAdresseSiretEntity ac WHERE ac.siret = " + IdClient).getSingleResult();
            if(client == null)
                return ReponseType.getNOTOK("Le client n'existe pas", true, tx, session);
            tx.commit();
            session.clear();

            //Execution de la commande update
            tx =  session.beginTransaction();
            String request = "UPDATE TicketEntity t SET t.objet = '" + ticket.objet + "', t.categorie = '" + ticket.categorie + "', t.description ='" + ticket.description + "', t.statut ='" + ticket.statut + "', t.type = '" + ticket.type + "' WHERE t.id = " + ticket.id;
            Query update = session.createQuery(request);
            int nbLignes = update.executeUpdate();

            request = "UPDATE TicketEntity t SET t.adresse = " + client.getSiret() + ", t.demandeur = " + demandeur.getIdPersonne() + ", t.technicien = " + tech.getId() + " WHERE t.id = " + ticket.id;
            update = session.createQuery(request);
            nbLignes += update.executeUpdate();
            tx.commit();
            session.clear();

            if(nbLignes != 2)
                return ReponseType.getNOTOK("Erreur lors de l'execution de la requete", true, null, session);
            session.close();
        }catch (HibernateException e){
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK("");
    }

    @Path("/state")
    @POST
    @Consumes("text/plain")
    public Response changeState(String jsonStr) {
        Transaction tx = null;
        int ticketId = -1;
        String token = "", statut = "";
        try (Session session = CreateSession.getSession()){
            try {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(jsonStr);
                ticketId = Integer.parseInt((String) json.get("ticketId"));
                token = (String) json.get("token");
                statut = (String) json.get("statut");
            } catch (ParseException | NullPointerException e) {
                e.printStackTrace();
                return  ReponseType.getNOTOK("Il manque des parametres (ticketId, token, statut)", false, null, null);
            }

            if(!Token.tryToken(token))
                return Token.tokenNonValide();

            tx = session.beginTransaction();
            System.err.println("ticket id = " + ticketId);
            int id = -1;
            id = (int) session.createQuery("SELECT t.id FROM TicketEntity t WHERE t.id = " + ticketId).getSingleResult();
            tx.commit();
            session.clear();
            if(id == -1)
                return ReponseType.getNOTOK("Le ticketId n'existe pas", true, null, session);

            try {
                tx = session.beginTransaction();
                Query update = session.createQuery("UPDATE TicketEntity T set T.statut = '" + statut + "' WHERE T.id = " + ticketId);
                int nbLignes = update.executeUpdate();
                if (nbLignes != 1)
                    return ReponseType.getNOTOK("Erreur lors de la requete en base de donnees", true, tx, session);
                tx.commit();
                session.clear();
                session.close();
            } catch(Exception e) { if(tx != null) tx.rollback(); return ReponseType.getNOTOK("Le statut " + statut + " n'existe pas", true, tx, session);}
        } catch (HibernateException e){
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK("");
    }

    @Path("/list")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getList(String jsonStr) {
        Transaction tx = null;
        ArrayList<Ticket> tickets = new ArrayList<>();
        String token = "";
        int techId = -1;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String)json.get("token");
            try {techId = Integer.parseInt(((Long)json.get("userId")).toString());} catch(Exception ignored){}
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, 'optionnel userId')", false, null, null);
        }

        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        //S'il n'y a pas de technicien en parametre
        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            if(techId == -1) {
                List result = session.createQuery("SELECT t.id FROM TicketEntity t").list();
                tx.commit();
                session.clear();
                for (Object o : result)
                    tickets.add(recuperationTicket(session, (int) o));
            }
            else {
                List result = session.createQuery("SELECT t.id FROM TicketEntity t WHERE t.technicien = " + techId).list();
                tx.commit();
                session.clear();
                for(Object o : result)
                    tickets.add(recuperationTicket(session, (int) o));

            }
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK(tickets);
    }
    private Ticket recuperationTicket(Session session, int IdTicket) {
        Ticket ticket = null;
        Transaction tx = session.beginTransaction();
        List result = session.createQuery("FROM TicketEntity t WHERE t.id = " + IdTicket).list();
        TicketEntity ticketEntity;
        if(result.size() == 1) {
            ticketEntity = (TicketEntity) result.get(0);

            //Recuperation du nom et prenom du demandeur et du technicien
            PersonneEntity demandeurEntity = (PersonneEntity) session.createQuery("FROM PersonneEntity p WHERE p.idPersonne = " + ticketEntity.getDemandeur()).getSingleResult();
            StaffEntity technicienEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + ticketEntity.getTechnicien()).getSingleResult();

            Personne demandeur, technicien;
            demandeur = new Personne(demandeurEntity.getNom(), demandeurEntity.getPrenom(), demandeurEntity.getIdPersonne());
            technicien = new Personne(technicienEntity.getNom(), technicienEntity.getPrenom(), technicienEntity.getId());

            //Recuperation du nom de l'entreprise du client
            int siren = Integer.parseInt(((Long)ticketEntity.getAdresse()).toString().substring(0, 9));
            ClientEntity client = (ClientEntity) session.createQuery("FROM ClientEntity c WHERE c.siren = " + siren).getSingleResult();

            //Recuperation de l'adresse
            AdresseEntity adresse = (AdresseEntity) session.createQuery("FROM AdresseEntity a WHERE a.id = " + client.getAdresse()).getSingleResult();

            //Recuperation des competences
            ArrayList<String> competences = new ArrayList<>();
            String request = "SELECT c.competence FROM CompetencesEntity c, JonctionTicketCompetenceEntity jtc WHERE jtc.idTicket = " + IdTicket + " and jtc.competence = c.idCompetences";
            result = session.createQuery(request).list();
            for(Object o : result) {
                String competence = (String) o;
                competences.add(competence);
            }

            Adresse adresseClient = new Adresse(adresse.getNumero(), adresse.getCodePostal(), adresse.getRue(), adresse.getVille());
            ticket = new Ticket(ticketEntity.getType(), ticketEntity.getObjet(), ticketEntity.getDescription(), ticketEntity.getCategorie(),
                    ticketEntity.getStatut(), technicien, demandeur, client.getNom(), competences, adresseClient, ticketEntity.getId());
        }
        tx.commit();
        session.clear();
        return ticket;
    }

    private Ticket createObjectFromJson(String jsonStr) {
        Personne technicien = null;
        String description;
        JSONObject json = null;
        try {
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(jsonStr);
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du parsing de l'objet");
        }
        ArrayList<String> competences = (ArrayList<String>) json.get("competences");
        String categorie = (String) json.get("categorie");
        JSONObject demandeurJson = (JSONObject) json.get("demandeur");
        Personne demandeur = new Personne((String)demandeurJson.get("nom"), (String)demandeurJson.get("prenom"), Integer.parseInt(((Long)demandeurJson.get("id")).toString()));
        String objet = (String) json.get("objet");
        if(json.get("description") != null)
            description = (String) json.get("description");
        else
            description = "";

        String type = (String) json.get("type");
        String nomClient = (String) json.get("nomClient");
        String statut = (String) json.get("statut");
        if (json.get("technicien") != null) {
            JSONObject technicienJSON = (JSONObject) json.get("technicien");
            technicien = new Personne((String) technicienJSON.get("nom"), (String) technicienJSON.get("prenom"), Integer.parseInt(((Long)technicienJSON.get("id")).toString()));
        }

        JSONObject adresseJSON = (JSONObject) json.get("adresse");
        Adresse adresse = new Adresse(Integer.parseInt(((Long)adresseJSON.get("numero")).toString()), (String)adresseJSON.get("codePostal"), (String)adresseJSON.get("rue"), (String)adresseJSON.get("ville"));
        int id = -1;
        //Test avec null pointeur exception pour verifier que id existe dans si non nous sommes en creation
        try{id = Integer.parseInt(((Long)json.get("id")).toString());} catch(NullPointerException ignored){}

        Ticket ticket = new Ticket(type, objet, description, categorie, statut, technicien, demandeur, nomClient, competences, adresse, id);
        return ticket;
    }

    @POST
    @Path("/test")
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getOptions(String str){
        String msg = "";
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(str);
            msg = "ok, user = " + obj.get("username") + " pwd = " + obj.get("passworde");
        }catch(ParseException e){
            e.printStackTrace();
            System.err.println("Parse impossible sur l'objet envoye");
        }
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity(msg)
                .build();
    }
}