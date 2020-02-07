package Modele;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class Token extends  HashMap<String, Timestamp>{
    private static Token me = null;

    private Token() {me = this;}
    public static Token getInstance() {
        if(me == null)
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

    public String addUID() {
        String token = UUID.randomUUID().toString();
        this.put(token, Timestamp.valueOf(LocalDateTime.now()));
        return token;
    }
}
