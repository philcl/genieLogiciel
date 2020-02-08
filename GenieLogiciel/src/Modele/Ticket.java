package Modele;

import java.util.ArrayList;

public class Ticket {
    public String type, objet, description, categorie, statut, nomClient;
    public Personne technicien, demandeur;
    public ArrayList<String> competences;
    public Adresse adresse;

    public Ticket(){}

    public Ticket(String type, String objet, String description, String categorie, String statut, Personne technicien, Personne demandeur, String nomClient, ArrayList<String> competences, Adresse adresse) {
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
    }
}
