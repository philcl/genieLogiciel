package API_REST;

import DataBase.ClientEntity;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/client")
public class RessourceClient {
    @Path("/list")
    @GET
    @Produces("application/json")
    public ArrayList<ClientEntity> getListDemandeur(@QueryParam("clientId") int IdClient, @QueryParam("ticketId") int IdTicket) {
        ArrayList<ClientEntity> listClients = new ArrayList<>();
        Transaction tx = null;

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            List clients = session.createQuery("FROM ClientEntity ").list();
            for (Object o : clients) {
                ClientEntity client = (ClientEntity) o;
                if(client.getActif() == 1)
                    listClients.add(client);
            }
            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return listClients;
    }
    @Path("try")
    @GET
    // The Java method will produce content identified by the MIME Media type "text/plain"
    @Produces({MediaType.APPLICATION_JSON})
    public Response getClichedMessage() {
        // Return some cliched textual content
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity("Hello World")
                .build();
    }
}
