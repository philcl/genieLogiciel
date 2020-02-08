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
import java.util.HashMap;
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
        int IdTicket = 0;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            IdClient = Long.parseLong((String) json.get("clientId"));
            IdTicket = Integer.parseInt((String) json.get("ticketId"));
            token = (String) json.get("token");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.err.print("received data : " + IdClient  + " " +  IdTicket + "  token " + token);
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
            result = session.createQuery("FROM PersonneEntity p WHERE p.employe = 1").list();
            for(Object o : result) {
                PersonneEntity technicienEntity = (PersonneEntity) o;
                answer.technicienList .add(new Personne(technicienEntity.getNom(), technicienEntity.getPrenom()));
            }
            tx.commit();
            session.clear();

            //Recuperation de la liste des demandeurs
            tx = session.beginTransaction();
            result = session.createQuery("FROM PersonneEntity p WHERE p.employe = 0 and p.siret LIKE '" + IdClient + "%'").list();
            for(Object o : result) {
                PersonneEntity demandeurEntity = (PersonneEntity) o;
                answer.demandeurList .add(new Personne(demandeurEntity.getNom(), demandeurEntity.getPrenom()));
            }
            tx.commit();
            session.clear();

            //Recuperation de la liste des sites du client
            tx = session.beginTransaction();
            String request = "FROM AdresseClientEntity A WHERE A.siret LIKE '" + IdClient + "%'";
            result = session.createQuery(request).list();
            if(result == null) {
                listInfos.add("L'id du client n'existe pas");
                return Response.status(406)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                        .allow("OPTIONS")
                        .entity(listInfos)
                        .build();
            }

            for(Object o : result) {
                AdresseClientEntity adresse = (AdresseClientEntity) o;
                if (adresse.getActif() == 1) {
                    AdresseClient adresseClient = new AdresseClient(adresse.getSiret(), adresse.getNumero(), adresse.getCodePostal(), adresse.getRue(), adresse.getVille());
                    answer.clientSiteList .add(adresseClient);
                }
            }
            tx.commit();
            session.clear();

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
            if(IdTicket != 0) {
                //todo : verifier que le clientId et le client du ticket sont bien les mêmes
                answer.ticket = recuperationTicket(session, IdTicket);
                if(answer.ticket == null)
                    return Response.status(406)
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                            .allow("OPTIONS")
                            .entity("Le ticket avec l'id " + IdTicket + " n'exsite pas")
                            .build();
            }
            session.close();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity(answer)
                .build();
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
            List result = session.createQuery("FROM PersonneEntity p WHERE p.prenom = '" + ticket.demandeur.prenom + "' and p.nom = '" + ticket.demandeur.nom + "'").list();
            //todo verifier sur la base de donnee que les champs nom et prenom sont en index unique
            PersonneEntity demandeur = (PersonneEntity)result.get(0);
            ticketEntity.setAdresse(demandeur.getSiret());
            ticketEntity.setDemandeur(demandeur.getIdPersonne());
            tx.commit();
            session.clear();

            tx = session.beginTransaction();
            result = session.createQuery("FROM PersonneEntity p WHERE p.prenom = '" + ticket.technicien.prenom + "' and p.nom = '" + ticket.technicien.nom + "'").list();
            PersonneEntity tech = (PersonneEntity)result.get(0);
            ticketEntity.setTechnicien(tech.getIdPersonne());
            tx.commit();
            session.clear();

            tx = session.beginTransaction();
            result = session.createQuery("SELECT MAX(t.id) FROM TicketEntity t").list();
            int maxID = (int) result.get(0);
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
            IdClient = Long.parseLong((String)json.get("clientId"));
            ticketId = Integer.parseInt((String)json.get("ticketId"));
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
            PersonneEntity tech = (PersonneEntity) session.createQuery("FROM PersonneEntity p WHERE p.prenom = '" + ticket.technicien.prenom + "' and p.nom = '" + ticket.technicien.nom + "'").getSingleResult();
            tx.commit();
            session.clear();

            if(tech == null)
                return ReponseType.getNOTOK("Le technicien n'existe pas");

            //Recuperation du demandeur
            tx = session.beginTransaction();
            PersonneEntity demandeur = (PersonneEntity) session.createQuery("FROM PersonneEntity p WHERE p.prenom = '" + ticket.demandeur.prenom + "' and p.nom = '" + ticket.demandeur.nom + "'").getSingleResult();
            tx.commit();
            session.clear();

            if(demandeur == null)
                return ReponseType.getNOTOK("Le demandeur n'existe pas");

            tx = session.beginTransaction();
            AdresseClientEntity client = (AdresseClientEntity) session.createQuery("FROM AdresseClientEntity ac WHERE ac.siret = " + IdClient).getSingleResult();
            tx.commit();
            session.clear();

            if(client == null)
                return ReponseType.getNOTOK("Le client n'existe pas");

            //Execution de la commande
            tx = session.beginTransaction();
            request = "UPDATE TicketEntity t SET t.adresse = " + client.getSiret() + ", t.demandeur = " + demandeur.getIdPersonne() + ", t.technicien = " + tech.getIdPersonne() + " WHERE t.id = " + ticketId;
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
            Query update = session.createQuery("UPDATE TicketEntity T set T.statut = '" + statut + "' WHERE t.id = " + ticketId);
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

            //Recuperation du nom et prenom du technicien et du demandeur
            tx = session.beginTransaction();
            result = session.createQuery("FROM PersonneEntity p WHERE p.idPersonne = " + ticketEntity.getTechnicien() + "or p.idPersonne = " + ticketEntity.getDemandeur()).list();

            Personne technicien, demandeur;

            PersonneEntity p = (PersonneEntity) result.get(0);
            if(p.getIdPersonne() == ticketEntity.getTechnicien()) {
                technicien = new Personne(p.getNom(), p.getPrenom());
                p = (PersonneEntity) result.get(1);
                demandeur = new Personne(p.getNom(), p.getPrenom());
            }
            else {
                demandeur = new Personne(p.getNom(), p.getPrenom());
                p = (PersonneEntity) result.get(1);
                technicien = new Personne(p.getNom(), p.getPrenom());
            }
            tx.commit();
            session.clear();

            //Recuperation du nom de l'entreprise du client
            tx = session.beginTransaction();
            int siren = Integer.parseInt(((Long)ticketEntity.getAdresse()).toString().substring(0, 9));
            String client = (String) session.createQuery("SELECT c.nom FROM ClientEntity c WHERE c.siren = " + siren).getSingleResult();

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

            ticket = new Ticket(ticketEntity.getType(), ticketEntity.getObjet(), ticketEntity.getDescription(), ticketEntity.getCategorie(),
                    ticketEntity.getStatut(), technicien, demandeur, client, competences);
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

            ArrayList<String> competences = (ArrayList<String>) json.get("competences");
            String categorie = (String) json.get("categorie");
            HashMap<String, String> demandeurMap = (HashMap<String, String>) json.get("demandeur");
            Personne demandeur = new Personne(demandeurMap.get("nom"), demandeurMap.get("prenom"));
            String objet = (String) json.get("objet");
            if(json.get("description") != null)
                description = (String) json.get("description");
            else
                description = "";

            String type = (String) json.get("type");
            String nomClient = (String) json.get("nomClient");
            String statut = (String) json.get("statut");
            if (json.get("technicien") != null) {
                HashMap<String, String> technicienMap = (HashMap<String, String>) json.get("technicien");
                technicien = new Personne(technicienMap.get("nom"), technicienMap.get("prenom"));
            }
            ticket = new Ticket(type, objet, description, categorie, statut, technicien, demandeur, nomClient, competences);
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