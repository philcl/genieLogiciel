package Modele.Client;

public class SendDeleteDemandeur {
    public int actif, idDemandeur;
    public String token;

    public SendDeleteDemandeur(int actif, int idDemandeur, String token) {
        this.actif = actif;
        this.idDemandeur = idDemandeur;
        this.token = token;
    }
}
