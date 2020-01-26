package API_REST;

import DataBase.*;
import Modele.AdresseClient;
import Modele.InitTicket;
import Modele.Personne;
import Modele.Ticket;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.ws.rs.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("JpaQlInspection") //Enleve les erreurs pour les requetes SQL elles peuvent etre juste
@Path("/ticket")
public class RessourceTicket {
    //todo : Ajouter priorityList
    @Path("/init")
    @POST
    @Produces("application/json")
    public Object getInit(@QueryParam("clientId") int IdClient, @QueryParam("ticketId") int IdTicket) {
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
            //todo : Ajout de l'exception si le client n'existe pas a tester
            tx = session.beginTransaction();
            String request = "FROM AdresseClientEntity A WHERE A.siret LIKE '" + IdClient + "%'";
            result = session.createQuery(request).list();
            if(result == null) {
                listInfos.add("L'id du client n'existe pas");
                return listInfos;
            }

            for(Object o : result) {
                AdresseClientEntity adresse = (AdresseClientEntity) o;
                if (adresse.getActif() == 1) {
                    AdresseClient adresseClient = new AdresseClient((int) adresse.getSiret(), adresse.getNumero(), adresse.getCodePostal(), adresse.getRue(), adresse.getVille());
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
                answer.ticket = recuperationTicket(session, tx, result, IdTicket);
            }
            session.close();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return answer;
    }

    private Ticket recuperationTicket(Session session, Transaction tx, List result, int IdTicket) {
        Ticket ticket = null;
        tx = session.beginTransaction();
        result = session.createQuery("FROM TicketEntity t WHERE t.id = " + IdTicket).list();
        TicketEntity ticketEntity = null;
        if(result.size() == 1) {
            ticketEntity = (TicketEntity) result.get(0);
            tx.commit();
            session.clear();

            //Recuperation du nom et prenom du technicien et du demandeur
            tx = session.beginTransaction();
            result = session.createQuery("FROM PersonneEntity p WHERE p.idPersonne = " + ticketEntity.getTechnicien() + "or p.idPersonne = " + ticketEntity.getDemandeur()).list();

            Personne technicien = null, demandeur = null;

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
            Integer siret = Integer.parseInt(((Long)ticketEntity.getAdresse()).toString().substring(0, 9));
            result = session.createQuery("SELECT c.nom FROM ClientEntity c WHERE c.siren = " + siret).list();
            String client = (String) result.get(0);

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

    @Path("/create")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public ArrayList<Object> postCreation(HashMap<String, Object> json) {
        ArrayList<Object> list = new ArrayList<>();
        Transaction tx = null;
        TicketEntity ticketEntity = new TicketEntity();

        Ticket ticket = createObjectFromJson(json);
        if(ticket == null) {
            //todo renvoie d'une erreur pour mauvais format de ticket
            System.out.println("h");
        }

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
        return list;
    }

    private Ticket createObjectFromJson(HashMap<String, Object> json) {
        Personne technicien = null;
        String description = "";
        Ticket ticket = null;

        try {
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
        } catch(Exception e) {
            e.printStackTrace();
        }
        return ticket;
    }
}