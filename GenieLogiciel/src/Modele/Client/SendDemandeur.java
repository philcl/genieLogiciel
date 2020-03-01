package Modele.Client;

public class SendDemandeur {
    public String token;
    public Demandeur demandeur;
    public int clientSIREN;

    public SendDemandeur(String token, Demandeur demandeur, int clientSIREN) {
        this.token = token;
        this.demandeur = demandeur;
        this.clientSIREN = clientSIREN;
    }
}
