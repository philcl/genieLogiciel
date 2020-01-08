import API_REST.CreateSession;
import API_REST.RessourceClient;
import org.hibernate.*;
import org.hibernate.query.Query;
import javax.persistence.metamodel.EntityType;
import java.util.Scanner;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

public class Main {
    private static final Session ourSessionFactory = CreateSession.getSession();
    /* Method to  READ all the employees */

    public static void main(final String[] args) {
        //Part about REST
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(RessourceClient.class);
        sf.setProvider(new JacksonJaxbJsonProvider());
        sf.setResourceProvider(
                RessourceClient.class,
                new SingletonResourceProvider(new RessourceClient())
        );
        sf.setAddress("http://localhost:8161/");
        sf.create();

        System.out.println("Saisir car+return pour stopper le serveur");
        new Scanner(System.in).next();

        System.out.println("Fin");

        //Part about database
        try (Session session = CreateSession.getSession()) {
            System.out.println("querying all the managed entities...");
            final Metamodel metamodel = session.getSessionFactory().getMetamodel();

            for (EntityType<?> entityType : metamodel.getEntities()) {
                final String entityName = entityType.getName();
                final Query query = session.createQuery("from " + entityName);
                System.out.println("executing: " + query.getQueryString());
                for (Object o : query.list()) {
                    System.out.println("  " + o);
                }
            }
        }
    }
}