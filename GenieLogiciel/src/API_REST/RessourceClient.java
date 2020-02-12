package API_REST;

import DataBase.AdresseEntity;
import DataBase.ClientEntity;
import Modele.Adresse;
import Modele.Client.Client;
import Modele.Client.ClientList;
import Modele.Staff.Token;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

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
            token = (String)json.get("token");
            clientName = (String)json.get("clientName");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, clientName)", false, null, null);
        }

        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            clientId = (int) session.createQuery("SELECT c.siren FROM ClientEntity c WHERE c.nom = '" + clientName + "'").getSingleResult();
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        } catch (NoResultException e) {return ReponseType.getNOTOK("Le client " + clientName + " n'existe pas", false, null, null);}

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
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            List clients = session.createQuery("FROM ClientEntity ").list();
            for (Object o : clients) {
                ClientEntity client = (ClientEntity) o;
                if(client.getActif() == 1) {
                    ClientList myClient = new ClientList();
                    myClient.name = client.getNom();
                    myClient.SIREN = client.getSiren();

                    List temp = session.createQuery("SELECT t FROM AdresseEntity t WHERE t.idAdresse = " + client.getAdresse()).list();

                    if(temp.isEmpty())
                    {
                        //throw new Exception("Pas d'adresse pour ce client");
                        System.out.println("Pas d'adresse pour ce client");
                    }
                    else if(temp.size()!=1)
                    {
                        System.out.println("ID adresse dupliquée");
                    }
                    else
                    {
                        for (Object p : temp)
                        {
                            AdresseEntity adresseEntity = (AdresseEntity) p;
                            myClient.adresse.numero = adresseEntity.getNumero();
                            myClient.adresse.codePostal = adresseEntity.getCodePostal();
                            myClient.adresse.rue = adresseEntity.getRue();
                            myClient.adresse.ville = adresseEntity.getVille();
                        }
                    }

                    clients.add(myClient);
                }
            }
            tx.commit();
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

        try{
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            clientJSON = (JSONObject) json.get("client");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, client)", false, null, null);
        }
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        Client client = getClientFromJSON(clientJSON);
        if(client == null)
            return ReponseType.getNOTOK("Le JSON du client est mal forme veuillez verifier", false, null, null);

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setActif((byte) 1);
        clientEntity.setNom(client.nom);
        clientEntity.setSiren(client.SIREN);

        int adresseId = client.adresse.getId();
        if(adresseId == -1)
            if(!client.adresse.addAdresse())
                return ReponseType.getNOTOK("Impossible de rajouter l'adresse sur la base", false, null, null);
        clientEntity.setAdresse(adresseId);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            try{
                session.createQuery("FROM ClientEntity c WHERE c.siren = " + client.SIREN).getSingleResult();
                return ReponseType.getNOTOK("Le SIREN " + client.SIREN + " existe deja veuillez le changer", true, tx, session);
            } catch (NoResultException ignored) {}

            try {
                session.createQuery("FROM ClientEntity c WHERE c.nom = '" + client.nom + "'").getSingleResult();
                return ReponseType.getNOTOK("Le client " + client.nom + " existe deja veuillez le changer", true, tx, session);
            } catch (NoResultException ignored) {}

            session.save(clientEntity);
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
            return ReponseType.getNOTOK("Impossible de sauvegarder le client sur la base", false, null, null);
        }
        return ReponseType.getOK("");
    }

    private Client getClientFromJSON(JSONObject json) {
        Client client = new Client();

        try{
            client.nom = (String) json.get("nom");
            client.SIREN = -1;
            client.SIREN = Integer.parseInt(((Long) json.get("SIREN")).toString());

            System.err.println("nom = " + client.nom + " siren = " + client.SIREN);

            if(client.nom == null || client.SIREN == -1)
                return null;

            //Ajout de l'adresse
            JSONObject adresse = (JSONObject) json.get("adresse");
            if(adresse == null) {
                System.err.println("L'adresse est mal formee");
                return null;
            }
            else
                if(!client.adresse.getFromJSON(adresse))
                    return null;

        } catch (NullPointerException e) {
            System.err.println("Erreur lors de la recuperation du client depuis un json parse impossible");
            return null;
        }
        return client;
    }


}
