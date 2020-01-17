import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/toto")
public class toto {
    @GET
    @Produces("text/plain")
    public String getShow()
    {
        return "hello toto";
    }
}
