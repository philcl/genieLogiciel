package Modele.Ticket;

import Modele.Adresse;
import Modele.Personne;

import java.util.ArrayList;

public class Ticket {
    public String type, objet, description, categorie, statut, clientName;
    public Personne technicien, demandeur;
    public ArrayList<String> competences;
    public Adresse adresse;
    public int id, priorite;
    public int pourcentage = 0;
    public ArrayList<Tache> taches;

    public Ticket(){}

    public Ticket(String type, String objet, String description, String categorie, String statut, Personne technicien, Personne demandeur, ArrayList<String> competences, Adresse adresse, int id, int priorite, String clientName, ArrayList<Tache> taches) {
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
        this.taches = taches;
    }

    public Ticket(String type, String objet, String description, String categorie, String statut, Personne technicien, Personne demandeur, ArrayList<String> competences, Adresse adresse, int id, int priorite, ArrayList<Tache> taches) {
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
        this.taches = taches;
    }
}
