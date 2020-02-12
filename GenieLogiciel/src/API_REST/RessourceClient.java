package API_REST;

import DataBase.ClientEntity;
import Modele.ClientList;
import Modele.Staff.Token;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
                    //todo completer avec l'adresse
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



}
