package Modele;

import java.util.ArrayList;

public class Ticket {
    //todo ajouter l'id tu ticket
    public String type, objet, description, categorie, statut, nomClient;
    public Personne technicien, demandeur;
    public ArrayList<String> competences;
    public Adresse adresse;
    public int id;

    public Ticket(){}

    public Ticket(String type, String objet, String description, String categorie, String statut, Personne technicien, Personne demandeur, String nomClient, ArrayList<String> competences, Adresse adresse, int id) {
        this.type = type;
        this.objet = objet;
        this.description = description;
        this.categorie = categorie;
        this.statut = statut;
        this.technicien = technicien;
        this.nomClient = nomClient;
        this.demandeur = demandeur;
        this.competences = competences;
        this.adresse = adresse;
        this.id = id;
    }
}
