package Modele;

public class Adresse {
    public int numero;
    public String rue, ville, codePostal;

    public Adresse(int numero, String codePostal, String rue, String ville) {
        this.numero = numero;
        this.codePostal = codePostal;
        this.rue = rue;
        this.ville = ville;
    }
}
