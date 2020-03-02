package Modele.Ticket;

import API_REST.CreateSession;
import API_REST.Security;
import DataBase.*;
import Modele.Personne;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

//todo asscoié les compétences de la tâche au ticket parent/tache parente
//todo check lors d'un update d'une tache vers resolu si toutes les tache d'un ticket sont resolu alors le ticket est resolu

public class Tache {
    public String statut = "", objet = "", description = "";
    public int ticketParent = -1, tempsPasse = -1, id = -1, pourcentage = 0, tempsEstime = -1;
    public ArrayList<String> competences;
    public Personne technicien;

    public Tache() {
        technicien = new Personne();
        competences = new ArrayList<>();
    }

    public Tache(String statut, String objet, String description, int ticketParent, int tempsPasse, int tempsEstime, ArrayList<String> competences, Personne technicien) {
        this.statut = statut;
        this.objet = objet;
        this.description = description;
        this.ticketParent = ticketParent;
        this.tempsPasse = tempsPasse;
        this.tempsEstime = tempsEstime;
        this.competences = competences;
        this.technicien = technicien;
    }

    public boolean RecupererTacheDepuisJSON(JSONObject json) {
        statut = Security.test((String) json.get("statut"));
        objet = Security.test((String) json.get("objet"));
        if(json.get("description") != null)
            description = Security.test((String) json.get("description"));
        else
            description = "";
        try{ticketParent = Integer.parseInt(((Long) json.get("ticketParent")).toString());}
        catch(NullPointerException ignored) {}
        if(json.get("tempsPasse") != null)
            tempsPasse = Integer.parseInt(((Long) json.get("tempsPasse")).toString());
        else
            tempsPasse = -1;
        try{id = Integer.parseInt(((Long) json.get("id")).toString());}
        catch (NullPointerException ignored) {}

        pourcentage = statut.equals("Resolu") ? 100 : 0;

        if(json.get("tempsEstime") != null) {
           tempsEstime = Integer.parseInt(((Long) json.get("tempsEstime")).toString());
        }

        competences = (ArrayList<String>) json.get("competences");
        technicien.RecupererPersonDepuisJson((JSONObject) json.get("technicien"));

        return !statut.isEmpty() && !objet.isEmpty() && ticketParent != -1 && technicien != null;
    }

    public boolean RecupererTacheDepuisJSON(JSONObject json, int idTicket) {
        if(!RecupererTacheDepuisJSON(json))
            ticketParent = idTicket;
        return !statut.isEmpty() && !objet.isEmpty() && ticketParent != -1 && technicien != null;
    }

    public ArrayList<String> getCompetencesForTask(int idTask, Session session) {
        List result = session.createQuery("FROM JonctionTacheCompetenceEntity j WHERE j.tache = " + idTask + " and j.actif = 1").list();
        ArrayList<String> competences = new ArrayList<>();

        for(Object o : result) {
            JonctionTacheCompetenceEntity jct = (JonctionTacheCompetenceEntity)o;
            CompetencesEntity competencesEntity;
             try{competencesEntity = (CompetencesEntity) session.createQuery("FROM CompetencesEntity c WHERE c.idCompetences = " + jct.getCompetence() + " and c.actif = 1").getSingleResult();}
             catch (NoResultException e) {return null;}
             competences.add(competencesEntity.getCompetence());
        }
        return competences;
    }

    public boolean getTaskFromId(int idTask, Session session) {
        TacheEntity tacheEntity;

        try{tacheEntity = (TacheEntity) session.createQuery("FROM TacheEntity t WHERE t.id = " + idTask + " and t.statut != 'Resolu' and t.statut != 'Non Resolu'").getSingleResult();}
        catch (NoResultException e) {return false;}

        //Remplissage des champs
        try{this.tempsEstime = tacheEntity.getDureeEstimee();}
        catch (NullPointerException e) {this.tempsEstime = -1;}

        try{this.tempsPasse = tacheEntity.getDureeReelle();}
        catch(NullPointerException e) {this.tempsPasse = 0;}

        this.description = tacheEntity.getDescription();
        this.objet = tacheEntity.getObjet();
        this.statut = tacheEntity.getStatut();
        this.ticketParent = tacheEntity.getTicket();
        this.id = idTask;
        this.pourcentage = statut.equals("Resolu") ? 100 : 0;

        StaffEntity staff;
        try{staff = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + tacheEntity.getTechnicien() + " and s.actif = 1").getSingleResult();}
        catch (NoResultException e) {return false;}
        this.technicien = new Personne(staff.getNom(), staff.getPrenom(), staff.getId());
        this.competences = this.getCompetencesForTask(idTask, session);

        return this.competences != null;
    }

    public static ArrayList<Tache> RecupererListeTacheDepuisJson(ArrayList<JSONObject> json, int IdTicket) {
        ArrayList<Tache> taches = new ArrayList<>();
        for(JSONObject o : json) {
            Tache tache = new Tache();
            if(!tache.RecupererTacheDepuisJSON(o, IdTicket))
                return null;
            else
                taches.add(tache);
        }
        return taches;
    }

    public static ArrayList<Tache> getListTaskFromTicket(int idTicket) {
        Transaction tx = null;
        ArrayList<Tache> taches = new ArrayList<>();

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            try{session.createQuery("FROM TicketEntity t WHERE t.id = " + idTicket + " and t.statut != 'Resolu' and t.statut != 'Non Resolu'").getSingleResult();}
            catch (NoResultException e) { return null;}

            List result = session.createQuery(" SELECT j.idEnfant FROM TicketJonctionEntity j WHERE j.idParent = " + idTicket + " and j.actif = 1").list();

            for(Object o : result) {
                int idTask = (int)o;
                Tache t = new Tache();
                if(t.getTaskFromId(idTask, session))
                    taches.add(t);
            }

            tx.commit();
            session.clear();
            session.close();
        }
        catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return taches;
    }

    public boolean isEmpty() {
        return this.technicien.isEmpty() || this.id == -1 || this.objet.isEmpty() || this.ticketParent == -1 || this.tempsEstime == -1 || this.statut.isEmpty() || this.tempsPasse == -1;
    }
}
