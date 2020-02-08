package Modele;

public class Demandeur {
    public long SIRET;
    public Adresse adresse;

    public Demandeur(){}
    public Demandeur(long SIRET, Adresse adresse) {
        this.SIRET = SIRET;
        this.adresse = adresse;
    }
}
