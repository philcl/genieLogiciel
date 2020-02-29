package Modele.Ticket;

public class SendTache {
    public String token;
    public Tache tache;

    public SendTache(String token, Tache tache) {
        this.token = token;
        this.tache = tache;
    }
}
