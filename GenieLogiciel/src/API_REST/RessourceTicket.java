package API_REST;

import DataBase.*;
import Modele.*;
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
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            IdClient = Long.parseLong((String) json.get("clientId"));
            IdTicket = Integer.parseInt (((Long) json.get("ticketId")).toString());
            token = (String) json.get("token");
        } catch (ParseException e) {
            e.printStackTrace();
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
            tx.commit();
            session.clear();

            //Recuperation de la liste des techniciens
            tx = session.beginTransaction();
            result = session.createQuery("FROM StaffEntity").list();
            for(Object o : result) {
                StaffEntity technicienEntity = (StaffEntity) o;
                answer.technicienList .add(new Personne(technicienEntity.getNom(), technicienEntity.getPrenom(), technicienEntity.getId()));
            }
            tx.commit();
            session.clear();

            //Recuperation de la liste des demandeurs
            tx = session.beginTransaction();
            result = session.createQuery("FROM PersonneEntity p WHERE p.siret LIKE '" + IdClient + "%'").list();
            for(Object o : result) {
                PersonneEntity demandeurEntity = (PersonneEntity) o;
                answer.demandeurList .add(new Personne(demandeurEntity.getNom(), demandeurEntity.getPrenom(), demandeurEntity.getIdPersonne()));
            }
            tx.commit();
            session.clear();

            //Recuperation de la liste des sites du client
            tx = session.beginTransaction();
            String request = "FROM JonctionAdresseSiretEntity A WHERE A.siret LIKE '" + IdClient + "%'";
            result = session.createQuery(request).list();
            if(result == null)
                return ReponseType.getNOTOK("L'id du client n'existe pas");
            tx.commit();
            session.clear();

            for(Object o : result) {
                JonctionAdresseSiretEntity adresse = (JonctionAdresseSiretEntity) o;
                if (adresse.getActif() == 1) {
                    tx = session.beginTransaction();
                    AdresseEntity adr = (AdresseEntity) session.createQuery("FROM AdresseEntity a WHERE a.id = " + adresse.getIdAdresse()).getSingleResult();
                    Demandeur adresseClient = new Demandeur(adresse.getSiret(), new Adresse(adr.getNumero(), adr.getCodePostal(), adr.getRue(), adr.getVille()));
                    answer.clientSiteList.add(adresseClient);
                    tx.commit();
                    session.clear();
                }
            }

            //Recuperation de la liste des categories
            tx = session.beginTransaction();
            result = session.createQuery("FROM CategorieEntity ").list();
            for(Object o : result) {
                CategorieEntity categorie = (CategorieEntity) o;
                answer.categorieList .add(categorie.getCategorie());
            }
            tx.commit();
            session.clear();

            //Recuperation de la liste des statut
            tx = session.beginTransaction();
            result = session.createQuery("FROM StatutTicketEntity ").list();
            for (Object o : result) {
                StatutTicketEntity statut = (StatutTicketEntity) o;
                answer.statusList .add(statut.getIdStatusTicket());
            }
            tx.commit();
            session.clear();

            // Recuperation de la liste des competences
            tx = session.beginTransaction();
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
                    return ReponseType.getNOTOK("Le ticket avec l'id " + IdTicket + " n'exsite pas");
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
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String)json.get("token");
            ticketJson = ((JSONObject)json.get("ticket")).toJSONString();
        } catch (ParseException e) {
            e.printStackTrace();
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
                    .entity("Le ticket n'est pas présent dans la requete")
                    .build();

        ticketEntity.setCategorie(ticket.categorie);
        ticketEntity.setDate(Timestamp.from(Instant.now()));
        ticketEntity.setDescription(ticket.description);
        ticketEntity.setObjet(ticket.objet);
        ticketEntity.setStatut(ticket.statut);
        ticketEntity.setType(ticket.type);

        try(Session session = CreateSession.getSession()) {
            //Ajout de l'adresse (SIRET) des ID du demandeur et du technicien
            tx = session.beginTransaction();
            PersonneEntity demandeur= (PersonneEntity) session.createQuery("FROM PersonneEntity p WHERE p.id = " + ticket.demandeur.id).getSingleResult();
            ticketEntity.setAdresse(demandeur.getSiret());
            ticketEntity.setDemandeur(demandeur.getIdPersonne());
            tx.commit();
            session.clear();

            tx = session.beginTransaction();
            StaffEntity tech = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + ticket.technicien.id).getSingleResult();
            ticketEntity.setTechnicien(tech.getId());
            tx.commit();
            session.clear();

            tx = session.beginTransaction();
            int maxID = (int) session.createQuery("SELECT MAX(t.id) FROM TicketEntity t").getSingleResult();
            ticketEntity.setId(maxID+1); //Rajout de l'increment
            tx.commit();
            session.clear();

            //Ajout en base de donnee du ticket
            tx = session.beginTransaction();
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
        int ticketId = 0;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            IdClient = (Long) json.get("clientId");
            ticketId = Integer.parseInt (((Long) json.get("ticketId")).toString());
            token = (String)json.get("token");
            ticketJson = ((JSONObject)json.get("ticket")).toJSONString();

        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("impossible de parse ///");
        }

        //Verification du token
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        Transaction tx = null;
        Ticket ticket = createObjectFromJson(ticketJson);
        if(ticket == null)
            return ReponseType.getNOTOK("Le ticket n'est pas présent dans la requete");

        try(Session session = CreateSession.getSession()) {
            String request = "UPDATE TicketEntity t SET t.objet = '" + ticket.objet + "', t.categorie = '" + ticket.categorie + "', t.description ='" + ticket.description + "', t.statut ='" + ticket.statut + "', t.type = '" + ticket.type + "' WHERE t.id = " + ticketId;
            //Creation de la requete pour l'update
            tx =  session.beginTransaction();
            Query update = session.createQuery(request);
            int nbLignes = update.executeUpdate();
            tx.commit();
            session.clear();
            System.err.println("nb lignes = " + nbLignes + " deuxieme request = " + request);

            //Recuperation du tech
            tx = session.beginTransaction();
            StaffEntity tech = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + ticket.technicien.id).getSingleResult();
            tx.commit();
            session.clear();

            if(tech == null)
                return ReponseType.getNOTOK("Le technicien n'existe pas");

            //Recuperation du demandeur
            tx = session.beginTransaction();
            PersonneEntity demandeur = (PersonneEntity) session.createQuery("FROM PersonneEntity p WHERE p.id = " + ticket.demandeur.id).getSingleResult();
            tx.commit();
            session.clear();

            if(demandeur == null)
                return ReponseType.getNOTOK("Le demandeur n'existe pas");

            tx = session.beginTransaction();
            JonctionAdresseSiretEntity client = (JonctionAdresseSiretEntity) session.createQuery("FROM JonctionAdresseSiretEntity ac WHERE ac.siret = " + IdClient).getSingleResult();
            tx.commit();
            session.clear();

            if(client == null)
                return ReponseType.getNOTOK("Le client n'existe pas");

            //Execution de la commande
            tx = session.beginTransaction();
            request = "UPDATE TicketEntity t SET t.adresse = " + client.getSiret() + ", t.demandeur = " + demandeur.getIdPersonne() + ", t.technicien = " + tech.getId() + " WHERE t.id = " + ticketId;
            update = session.createQuery(request);
            nbLignes += update.executeUpdate();
            tx.commit();
            session.clear();
            System.err.println("nb lignes = " + nbLignes + " deuxieme request = " + request);

            if(nbLignes != 2)
                return ReponseType.getNOTOK("Erreur lors de l'execution de la requete");
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
        try (Session session = CreateSession.getSession()){
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            int ticketId = Integer.parseInt((String)json.get("ticketId"));
            String token = (String)json.get("token");
            String statut = (String)json.get("statut");

            if(!Token.tryToken(token))
                return Token.tokenNonValide();

            tx = session.beginTransaction();
            System.err.println("ticket id = " + ticketId);
            int id = -1;
            id = (int) session.createQuery("SELECT t.id FROM TicketEntity t WHERE t.id = " + ticketId).getSingleResult();
            tx.commit();
            session.clear();
            if(id == -1)
                return ReponseType.getNOTOK("Le ticketId n'existe pas");

            tx = session.beginTransaction();
            Query update = session.createQuery("UPDATE TicketEntity T set T.statut = '" + statut + "' WHERE T.id = " + ticketId);
            int nbLignes = update.executeUpdate();
            if(nbLignes != 1)
                return  ReponseType.getNOTOK("Erreur lors de la requete en base de donnees");

        } catch (ParseException e) {
            e.printStackTrace();
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
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String)json.get("token");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            List result = session.createQuery("SELECT t.id FROM TicketEntity t").list();
            tx.commit();
            session.clear();
            for(Object o : result)
                tickets.add(recuperationTicket(session, (int)o));
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
            tx.commit();
            session.clear();

            //Recuperation du nom et prenom du demandeur et du technicien
            tx = session.beginTransaction();
            PersonneEntity demandeurEntity = (PersonneEntity) session.createQuery("FROM PersonneEntity p WHERE p.idPersonne = " + ticketEntity.getDemandeur()).getSingleResult();
            StaffEntity technicienEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + ticketEntity.getTechnicien()).getSingleResult();
            tx.commit();
            session.clear();

            Personne demandeur, technicien;
            demandeur = new Personne(demandeurEntity.getNom(), demandeurEntity.getPrenom(), demandeurEntity.getIdPersonne());
            technicien = new Personne(technicienEntity.getNom(), technicienEntity.getPrenom(), technicienEntity.getId());

            //Recuperation du nom de l'entreprise du client
            tx = session.beginTransaction();
            int siren = Integer.parseInt(((Long)ticketEntity.getAdresse()).toString().substring(0, 9));
            ClientEntity client = (ClientEntity) session.createQuery("FROM ClientEntity c WHERE c.siren = " + siren).getSingleResult();
            tx.commit();
            session.clear();

            //Recuperation de l'adresse
            tx = session.beginTransaction();
            AdresseEntity adresse = (AdresseEntity) session.createQuery("FROM AdresseEntity a WHERE a.id = " + client.getAdresse()).getSingleResult();
            tx.commit();
            session.clear();

            //Recuperation des competences
            tx = session.beginTransaction();
            ArrayList<String> competences = new ArrayList<>();
            String request = "SELECT c.competence FROM CompetencesEntity c, JonctionTicketCompetenceEntity jtc WHERE jtc.idTicket = " + IdTicket + " and jtc.competence = c.idCompetences";
            result = session.createQuery(request).list();
            for(Object o : result) {
                String competence = (String) o;
                competences.add(competence);
            }

            Adresse adresseClient = new Adresse(adresse.getNumero(), adresse.getCodePostal(), adresse.getRue(), adresse.getVille());
            ticket = new Ticket(ticketEntity.getType(), ticketEntity.getObjet(), ticketEntity.getDescription(), ticketEntity.getCategorie(),
                    ticketEntity.getStatut(), technicien, demandeur, client.getNom(), competences, adresseClient);
        }
        tx.commit();
        session.clear();
        return ticket;
    }

    private Ticket createObjectFromJson(String jsonStr) {
        Personne technicien = null;
        String description;
        Ticket ticket = null;

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            JSONObject myTemp;

            ArrayList<String> competences = (ArrayList<String>) json.get("competences");
            String categorie = (String) json.get("categorie");
            myTemp = (JSONObject) json.get("demandeur");
            Personne demandeur = new Personne((String)myTemp.get("nom"), (String)myTemp.get("prenom"), Integer.parseInt(((Long)myTemp.get("id")).toString()));
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
            System.err.println("adresse : " + adresseJSON.toJSONString());
            Adresse adresse = new Adresse(Integer.parseInt(((Long)adresseJSON.get("numero")).toString()), (String)adresseJSON.get("codePostal"), (String)adresseJSON.get("rue"), (String)adresseJSON.get("ville"));
            ticket = new Ticket(type, objet, description, categorie, statut, technicien, demandeur, nomClient, competences, adresse);
        } catch(HibernateException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du parsing de l'objet");
        }
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