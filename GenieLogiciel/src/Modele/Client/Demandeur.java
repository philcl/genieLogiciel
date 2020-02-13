package Modele.Client;

import API_REST.CreateSession;
import Modele.Adresse;
import Modele.Personne;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

public class Demandeur {
    public long SIRET = -1;
    public Adresse adresse;
    public Personne demandeur;

    public Demandeur() {
        adresse = new Adresse();
        demandeur = new Personne();
    }

    public Demandeur(long siret, Adresse adresse) {
        SIRET = siret;
        this.adresse = adresse;
        demandeur = new Personne();
    }

    public Demandeur RecupererDemandandeurDepuisJson(JSONObject demandeurJSON) {
        try {
            SIRET = -1;
            SIRET = (Long) demandeurJSON.get("SIRET");
            JSONObject adresseJSON = (JSONObject) demandeurJSON.get("adresse");
            JSONObject demandeur = (JSONObject) demandeurJSON.get("demandeur");
            if (SIRET == -1 || adresseJSON == null ||demandeur == null)
                return null;

            this.adresse.RecupererAdresseDepuisJson(adresseJSON);
            this.demandeur.RecupererPersonDepuisJson(demandeur);

            if(this.adresse == null || this.demandeur == null)
                return null;

        } catch (NullPointerException e) {
            System.err.println("Impossible de parse le demandeur");
            return null;
        }
        return this;
    }

    public boolean addDemandeur() {
        Transaction tx = null;

        //todo faire l'ajout d'un demandeur (voir si je verifie l'adresse ici ou avant dans RessourceClient
        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();


            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return true;
    }

    public boolean isEmpty() {
        return SIRET == -1 || adresse.isEmpty() || demandeur.isEmpty();
    }
}
