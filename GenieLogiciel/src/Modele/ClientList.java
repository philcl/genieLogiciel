package Modele;

import java.util.ArrayList;

public class ClientList {
    public int SIREN;
    public Adresse adresse;
    public String name;
    public int nbTicket;
    public ArrayList<Adresse> demandeurs;

    public ClientList() {
        adresse = new Adresse();
        demandeurs = new ArrayList<>();
        nbTicket = 0;
    }
}
