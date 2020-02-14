package Modele.Client;

import API_REST.CreateSession;
import DataBase.DemandeurEntity ;
import Modele.Adresse;
import Modele.Personne;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

import javax.persistence.NoResultException;

public class Demandeur {
    public long SIRET = -1;
    public Adresse adresse;
    public Personne demandeur;
    public String telephone="1";

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

    public boolean verifyDemandeurExistance(long SIRET) {
        Transaction tx = null;
        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            try{session.createQuery("FROM DemandeurEntity p WHERE p.siret = " + SIRET).getSingleResult();}
            catch(NoResultException e) {
                tx.commit();
                session.clear();
                session.close();
                return false;
            }
        } catch (HibernateException e) {
            if(tx == null)
                tx.rollback();
            e.printStackTrace();
        }
        return true;
    }

    public boolean recupererDemandeur(long SIRET) {
        Transaction tx = null;
        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            if(verifyDemandeurExistance(SIRET)) {
                DemandeurEntity p = (DemandeurEntity) session.createQuery("FROM DemandeurEntity p WHERE p.siret = " + SIRET).getSingleResult();

                this.SIRET = SIRET;
                this.telephone = "1";
                //Init demandeur
                this.demandeur.prenom = p.getPrenom();
                this.demandeur.nom = p.getNom();
                this.demandeur.id = p.getIdPersonne();
                this.demandeur.sexe = p.getSexe();

                int idAdr = p.getAdresse();
                if(!this.adresse.recupererAdresse(idAdr))
                    return  false;
            }
            else
                return false;
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
