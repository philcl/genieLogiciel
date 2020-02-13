package API_REST;

import DataBase.AdresseEntity;
import DataBase.ClientEntity;
import DataBase.PersonneEntity;
import Modele.Adresse;
import Modele.Client.Client;
import Modele.Client.ClientInit;
import Modele.Client.ClientList;
import Modele.Client.Demandeur;
import Modele.Personne;
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
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JpaQlInspection") //Enleve les erreurs pour les requetes SQL elles peuvent etre juste
@Path("/client")
public class RessourceClient {
    @Path("/getId")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getClientId(String jsonStr) {
        String token = "", clientName = "";
        int clientId = -1;
        Transaction tx = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            clientName = (String) json.get("clientName");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, clientName)", false, null, null);
        }

        if (!Token.tryToken(token))
            return Token.tokenNonValide();

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            clientId = (int) session.createQuery("SELECT c.siren FROM ClientEntity c WHERE c.nom = '" + clientName + "'").getSingleResult();
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
        } catch (NoResultException e) {
            return ReponseType.getNOTOK("Le client " + clientName + " n'existe pas", false, null, null);
        }

        return ReponseType.getOK(clientId);
    }

    @Path("/list")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getListDemandeur(String jsonStr) {
        ArrayList<ClientList> clientList = new ArrayList<>();
        Transaction tx = null;
        String token = "";
        long IdClient = 0;
        int IdTicket = 0;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (!Token.tryToken(token))
            return Token.tokenNonValide();

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            List clients = session.createQuery("FROM ClientEntity ").list();
            for (Object o : clients) {
                ClientEntity client = (ClientEntity) o;
                if (client.getActif() == 1) {
                    ClientList myClient = new ClientList();
                    myClient.name = client.getNom();
                    myClient.SIREN = client.getSiren();

                    AdresseEntity adresseEntity = (AdresseEntity) session.createQuery("SELECT t FROM AdresseEntity t WHERE t.idAdresse = " + client.getAdresse()).getSingleResult();

                    myClient.adresse.numero = adresseEntity.getNumero();
                    myClient.adresse.codePostal = adresseEntity.getCodePostal();
                    myClient.adresse.rue = adresseEntity.getRue();
                    myClient.adresse.ville = adresseEntity.getVille();

                    List demandeurs = session.createQuery("FROM PersonneEntity p WHERE p.siret LIKE '" + myClient.SIREN + "%'").list();

                    for (Object p : demandeurs)
                    {
                        PersonneEntity personneEntity = (PersonneEntity) p;

                        int idAdresse = -1;

                        AdresseEntity adresseEntity1;

                        try{
                            idAdresse = (int) session.createQuery("SELECT j.idAdresse FROM JonctionAdresseSiretEntity j WHERE j.siret = " + personneEntity.getSiret()).getSingleResult();
                            adresseEntity1 = (AdresseEntity) session.createQuery("SELECT a FROM AdresseEntity a WHERE a.id = " + idAdresse).getSingleResult();
                        }
                        catch (NoResultException e)
                        {
                            e.printStackTrace();
                            return ReponseType.getNOTOK("Wesh y a pas de siret bro", true, tx, session);
                        }

                        myClient.demandeurs.add(new Adresse(adresseEntity1.getNumero(),adresseEntity1.getCodePostal(),adresseEntity1.getRue(),adresseEntity1.getVille()));
                    }

                    clientList.add(myClient);
                }
            }
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK(clientList);
    }

    @Path("/create")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response createClient(String jsonStr) {
        String token = "";
        JSONObject clientJSON = null;
        Transaction tx = null;

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            clientJSON = (JSONObject) json.get("client");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, client)", false, null, null);
        }
        if (!Token.tryToken(token))
            return Token.tokenNonValide();

        Client client = getClientFromJSON(clientJSON);
        if (client == null)
            return ReponseType.getNOTOK("Le JSON du client est mal forme veuillez verifier", false, null, null);

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setActif((byte) 1);
        clientEntity.setNom(client.nom);
        clientEntity.setSiren(client.SIREN);

        int adresseId = client.adresse.getId();
        if (adresseId == -1)
            if (!client.adresse.addAdresse())
                return ReponseType.getNOTOK("Impossible de rajouter l'adresse sur la base", false, null, null);
        clientEntity.setAdresse(adresseId);

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            try {
                session.createQuery("FROM ClientEntity c WHERE c.siren = " + client.SIREN).getSingleResult();
                return ReponseType.getNOTOK("Le SIREN " + client.SIREN + " existe deja veuillez le changer", true, tx, session);
            } catch (NoResultException ignored) {
            }

            try {
                session.createQuery("FROM ClientEntity c WHERE c.nom = '" + client.nom + "'").getSingleResult();
                return ReponseType.getNOTOK("Le client " + client.nom + " existe deja veuillez le changer", true, tx, session);
            } catch (NoResultException ignored) {
            }

            session.save(clientEntity);
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
            return ReponseType.getNOTOK("Impossible de sauvegarder le client sur la base", false, null, null);
        }
        return ReponseType.getOK("");
    }

    @Path("/init")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getInit(String jsonStr) {
        String token = "";
        int SIREN = -1;
        JSONObject json;
        Transaction tx = null;

        try{
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            SIREN = Integer.parseInt (((Long) json.get("SIREN")).toString());

        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des paramètres (token, clientId)", false, null, null);
        }
        ClientInit clientInit = new ClientInit();

        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        if (SIREN == -1)
            return ReponseType.getNOTOK("Le clientId ne convient pas", false, null, null);
        if(!clientInit.client.recupererClient(SIREN))
            return ReponseType.getNOTOK("Immposible de creer le client avec le SIREN " + SIREN, false, null, null);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            List result = session.createQuery("FROM PersonneEntity p WHERE p.siret LIKE '" + SIREN + "%' and p.actif = 1").list();

            for(Object o : result) {
                PersonneEntity p = (PersonneEntity) o;
                Demandeur demandeur = new Demandeur();
                if(!demandeur.recupererDemandeur(p.getSiret()))
                    return ReponseType.getNOTOK("Impossible de lister les demandeurs du SIREN " + SIREN, true, tx, session);

                clientInit.demandeurList.add(demandeur);
            }

            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }


        return ReponseType.getOK(clientInit);
    }

    private Client getClientFromJSON(JSONObject json) {
        Client client = new Client();

        try {
            client.nom = (String) json.get("nom");
            client.SIREN = -1;
            client.SIREN = Integer.parseInt(((Long) json.get("SIREN")).toString());

            System.err.println("nom = " + client.nom + " siren = " + client.SIREN);

            if (client.nom == null || client.SIREN == -1)
                return null;

            //Ajout de l'adresse
            JSONObject adresse = (JSONObject) json.get("adresse");
            if (adresse == null) {
                System.err.println("L'adresse est mal formee");
                return null;
            } else if (!client.adresse.RecupererAdresseDepuisJson(adresse))
                return null;


        } catch (NullPointerException e) {
            System.err.println("Erreur lors de la recuperation du client depuis un json parse impossible");
            return null;
        }
        return client;
    }


}
