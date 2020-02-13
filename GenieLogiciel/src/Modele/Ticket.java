package Modele;

import java.util.ArrayList;

public class Ticket {
    //todo ajouter l'id tu ticket
    public String type, objet, description, categorie, statut, clientName;
    public Personne technicien, demandeur;
    public ArrayList<String> competences;
    public Adresse adresse;
    public int id, priorite;

    public Ticket(){}

    public Ticket(String type, String objet, String description, String categorie, String statut, Personne technicien, Personne demandeur, ArrayList<String> competences, Adresse adresse, int id, int priorite, String clientName) {
        this.type = type;
        this.objet = objet;
        this.description = description;
        this.categorie = categorie;
        this.statut = statut;
        this.technicien = technicien;
        this.demandeur = demandeur;
        this.competences = competences;
        this.adresse = adresse;
        this.id = id;
        this.priorite = priorite;
        this.clientName = clientName;
    }

    public Ticket(String type, String objet, String description, String categorie, String statut, Personne technicien, Personne demandeur, ArrayList<String> competences, Adresse adresse, int id, int priorite) {
        this.type = type;
        this.objet = objet;
        this.description = description;
        this.categorie = categorie;
        this.statut = statut;
        this.technicien = technicien;
        this.demandeur = demandeur;
        this.competences = competences;
        this.adresse = adresse;
        this.id = id;
        this.priorite = priorite;
    }
}
