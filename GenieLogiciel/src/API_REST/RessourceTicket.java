package API_REST;

import DataBase.*;
import Modele.AdresseClient;
import Modele.Personne;
import Modele.Ticket;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JpaQlInspection") //Enleve les erreurs pour les requetes SQL elles peuvent etre juste
@Path("/ticket")
public class RessourceTicket {
    //todo : Ajouter Liste des tech & Liste des demandeurs & priorityList
    @Path("/init")
    @POST
    @Produces("application/json")
    public ArrayList<Object> getInit(@QueryParam("clientId") int IdClient, @QueryParam("ticketId") int IdTicket) {
        //Init des objets
        ArrayList<Object> listInfos = new ArrayList<>();
        ArrayList<String> competences = new ArrayList<>();
        ArrayList<String> statuts = new ArrayList<>();
        ArrayList<String> typeDemandes = new ArrayList<>();
        ArrayList<AdresseClient> adresses = new ArrayList<>();
        ArrayList<String> categories = new ArrayList<>();
        ArrayList<Personne> techniciens = new ArrayList<>();
        ArrayList<Personne> demandeurs = new ArrayList<>();
        Transaction tx = null;

        try(Session session = CreateSession.getSession()) {
            //Recuperation des types des demandes
            tx = session.beginTransaction();
            List result = session.createQuery("FROM TypeDemandesEntity ").list();
            for(Object o : result) {
                TypeDemandesEntity typeDemande = (TypeDemandesEntity) o;
                typeDemandes.add(typeDemande.getIdTypeDemandes());
            }
            tx.commit();
            session.clear();

            //Recuperation de la liste des techniciens
            tx = session.beginTransaction();
            result = session.createQuery("FROM PersonneEntity p WHERE p.employe = 1").list();
            for(Object o : result) {
                PersonneEntity technicienEntity = (PersonneEntity) o;
                techniciens.add(new Personne(technicienEntity.getNom(), technicienEntity.getPrenom()));
            }
            tx.commit();
            session.clear();

            //Recuperation de la liste des demandeurs
            tx = session.beginTransaction();
            result = session.createQuery("FROM PersonneEntity p WHERE p.employe = 0 and p.siret LIKE '" + IdClient + "%'").list();
            for(Object o : result) {
                PersonneEntity demandeurEntity = (PersonneEntity) o;
                demandeurs.add(new Personne(demandeurEntity.getNom(), demandeurEntity.getPrenom()));
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
                    adresses.add(adresseClient);
                }
            }
            tx.commit();
            session.clear();

            //Recuperation de la liste des categories
            tx = session.beginTransaction();
            result = session.createQuery("FROM CategorieEntity ").list();
            for(Object o : result) {
                CategorieEntity categorie = (CategorieEntity) o;
                categories.add(categorie.getCategorie());
            }
            tx.commit();
            session.clear();

            //Recuperation de la liste des statut
            tx = session.beginTransaction();
             result = session.createQuery("FROM StatutTicketEntity ").list();
            for (Object o : result) {
                StatutTicketEntity statut = (StatutTicketEntity) o;
                statuts.add(statut.getIdStatusTicket());
            }
            tx.commit();
            session.clear();

            // Recuperation de la liste des competences
            tx = session.beginTransaction();
            result = session.createQuery("FROM CompetencesEntity ").list();
            for (Object o : result) {
                CompetencesEntity competence = (CompetencesEntity) o;
                competences.add(competence.getCompetence());
            }
            tx.commit();
            session.clear();

            //ajout des elements sur l'objet de retour
            listInfos.add(typeDemandes);
            listInfos.add(techniciens);
            listInfos.add(demandeurs);
            listInfos.add(adresses);
            listInfos.add(categories);
            listInfos.add(statuts);
            listInfos.add(competences);

            //Ajout du ticket si son id est present
            if(IdTicket != 0) {
                //todo : verifier que le clientId et le client du ticket sont bien les mêmes
                listInfos.add(recuperationTicket(session, tx, result, IdTicket));
            }
            session.close();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return listInfos;
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
}