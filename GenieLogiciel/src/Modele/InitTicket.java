package Modele;

import java.util.ArrayList;

public class InitTicket {
    public ArrayList<String> skillsList ;
    public ArrayList<String> statusList ;
    public ArrayList<String> demandeTypeList ;
    public ArrayList<Demandeur> clientSiteList ;
    public ArrayList<String> categorieList ;
    public ArrayList<Personne> technicienList ;
    public ArrayList<Personne> demandeurList ;
    public Ticket ticket;

    public InitTicket() {
        this.skillsList  = new ArrayList<>();
        this.statusList  = new ArrayList<>();
        this.demandeTypeList  = new ArrayList<>();
        this.clientSiteList  = new ArrayList<>();
        this.categorieList  = new ArrayList<>();
        this.technicienList  = new ArrayList<>();
        this.demandeurList  = new ArrayList<>();
    }
}
