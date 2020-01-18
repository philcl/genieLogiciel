package API_REST;

import DataBase.AdresseClientEntity;
import DataBase.CompetencesEntity;
import DataBase.StatutTicketEntity;
import DataBase.TypeDemandesEntity;
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
        ArrayList<String> demandes = new ArrayList<>();
        ArrayList<AdresseClientEntity> adresses = new ArrayList<>();
        Transaction tx = null;

        try(Session session = CreateSession.getSession()) {
            //Recuperation des types de demandes
            tx = session.beginTransaction();
            List result = session.createQuery("FROM TypeDemandesEntity ").list();
            for(Object o : result) {
                TypeDemandesEntity demande = (TypeDemandesEntity) o;
                demandes.add(demande.getIdTypeDemandes());
            }
            tx.commit();
            session.clear();

            //Recuperation de la liste des sites du client
            //todo : Ajout de l'exception si le client n'existe pas
            tx = session.beginTransaction();
            String request = "FROM AdresseClientEntity A WHERE A.siret LIKE '" + IdClient + "%'";
            result = session.createQuery(request).list();
            for(Object o : result) {
                AdresseClientEntity adresse = (AdresseClientEntity) o;
                if(adresse.getActif() == 1)
                    adresses.add(adresse);
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

            //Recuperation de la liste des statut
            tx = session.beginTransaction();
             result = session.createQuery("FROM StatutTicketEntity ").list();
            for (Object o : result) {
                StatutTicketEntity statut = (StatutTicketEntity) o;
                statuts.add(statut.getIdStatusTicket());
            }
            tx.commit();
            session.clear();
            session.close();

            //ajout des elements sur l'objet de retour
            listInfos.add(demandes);
            listInfos.add(adresses);
            listInfos.add(statuts);
            listInfos.add(competences);
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return listInfos;
    }
}
