package Modele.Client;

import Modele.Adresse;

public class Client {
    public int SIREN;
    public Adresse adresse;
    public String nom;

    public Client() {
        adresse = new Adresse();
    }
}
