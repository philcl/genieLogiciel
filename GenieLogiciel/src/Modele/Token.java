package Modele;

import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class Token extends  HashMap<String, Timestamp>{
    private static Token me = new Token();

    private Token() {}

    public static boolean tryToken(String token) {
        if(me.containsKey(token)) {
            Timestamp hour = Timestamp.valueOf(LocalDateTime.now());
            boolean res = (me.get(token).getTime() + (1 * 1000 * 60 * 60)) - hour.getTime() > 0;

            if(!res)
                me.remove(token);
            return res;
        }
        return false;
    }

    public static String addUID() {
        String token = UUID.randomUUID().toString();
        me.put(token, Timestamp.valueOf(LocalDateTime.now()));
        return token;
    }

    public static Response tokenNonValide() {
        return Response.status(401)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity("Token non valide")
                .build();
    }
}
//todo :
//ticket Suppression
//
//Client Ajout
//Client Modification
//Client Suppression
//
//Demandeur Ajout
//Demandeur Modification
//Demandeur Suppression
//Demandeur Listing
//
//User account Ajout
//User account Modification
//User account Suppression
//User account Listing
//
//
// - Ajout
//- Modification
//- Suppression
//- Listing