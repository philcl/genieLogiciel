package API_REST;

import javax.ws.rs.core.Response;

public class ReponseType {
    private ReponseType me = new ReponseType();

    public static Response getNOTOK(String message) {
        return Response.status(406)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity(message)
                .build();
    }

    public static Response getUnanthorized(String message) {
        return Response.status(401)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity(message)
                .build();
    }

    public static Response getOK(Object entity) {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity(entity)
                .build();
    }
}
