package API_REST;

import DataBase.ClientEntity;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

public class RessourceClient {
    @Path("/test")
    @POST
    @Produces("application/json")
    public ArrayList<ClientEntity> getListClient(@QueryParam("nom") String nom) {
        System.out.println("testHeloo" +nom);
        ArrayList<ClientEntity> listClients = new ArrayList<>();
        Transaction tx = null;

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            List clients = session.createQuery("FROM ClientEntity ").list();
            for (Object o : clients) {
                ClientEntity client = (ClientEntity) o;
                if(client.getActif() == 1) {
                    listClients.add(client);
                    System.out.print("First Name: " + client.getNom());
                    System.out.print("  Siren: " + client.getSiren());
                    System.out.println("  Actif: " + client.getActif());
                }
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return listClients;
    }
}