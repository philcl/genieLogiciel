package Modele;

import java.rmi.server.UID;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;

public class Token extends  HashMap<UID, Timestamp>{
    private static Token me = null;

    private Token() {me = this;}
    public static Token getInstance() {
        if(me != null)
            new Token();
        return me;
    }
    public boolean tryToken(int token) {
        if(this.containsKey(token)) {
            Timestamp hour = Timestamp.valueOf(LocalDateTime.now());
            return (this.get(token).getTime() + (1 * 1000 * 60 * 60)) - hour.getTime() > 0;
        }
        return false;
    }

    public UID add() {
        UID token = new UID();
        this.put(token, Timestamp.valueOf(LocalDateTime.now()));
        return token;
    }
}
