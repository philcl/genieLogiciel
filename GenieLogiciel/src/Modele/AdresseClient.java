package Modele;

public class AdresseClient {
    public int SIRET, numero, codePostal;
    public String rue, ville;

    public AdresseClient(int SIRET, int numero, int codePostal, String rue, String ville) {
        this.SIRET = SIRET;
        this.numero = numero;
        this.codePostal = codePostal;
        this.rue = rue;
        this.ville = ville;
    }
}
