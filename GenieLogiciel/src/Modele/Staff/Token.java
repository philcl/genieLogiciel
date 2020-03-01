package Modele.Staff;

import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Token {
    private static HashMap<String, Token> me = new HashMap<>();
    public ArrayList<String> roles = new ArrayList<>();
    private Timestamp time;

    private Token() {}

    public static boolean tryToken(String tokenStr) {
        //todo pour le rendu a enlever
        if(!me.containsKey("96b29b22-cefb-4699-93b9-9fcc97aa003e")) {
            Timestamp t = Timestamp.valueOf(LocalDateTime.now());
            t.setTime(t.getTime() + (24 * 1000 * 60 * 60));
            Token token = new Token();
            token.roles.add("Admin");
            token.time = t;
            me.put("96b29b22-cefb-4699-93b9-9fcc97aa003e", token);
        }

        if(me.containsKey(tokenStr)) {
            Timestamp hour = Timestamp.valueOf(LocalDateTime.now());
            boolean res = (me.get(tokenStr).time.getTime()) - hour.getTime() > 0;

            if(!res)
                me.remove(tokenStr);
            return res;
        }
        return false;
    }

    public static String addUID(ArrayList<String> roles) {
        String tokenStr = UUID.randomUUID().toString();
        Timestamp t = Timestamp.valueOf(LocalDateTime.now());
        t.setTime(t.getTime() + (1000 * 60 * 60));
        Token token = new Token();
        token.time = t;
        token.roles = roles;
        me.put(tokenStr, token);
        return tokenStr;
    }

    public static ArrayList<String> getRolesFromToken(String tokenStr) {
        if(me.containsKey(tokenStr)) {
            return me.get(tokenStr).roles;
        }
        return null;
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
//todo controller le user pour redonner le meme token si deja login

//todo :
//Client avec les demandeurs (seulement le demandeur aussi)
//Ticket avec les taches (seulement la tache aussi)
//
