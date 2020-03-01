package Modele.Client;

import API_REST.CreateSession;
import API_REST.Security;
import DataBase.DemandeurEntity ;
import DataBase.JonctionSirensiretEntity;
import Modele.Personne;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

public class Demandeur {
    public long SIRET = -1;
    public int idAdresse = -1;
    public Personne demandeur;
    public String telephone="1";

    public Demandeur() {
        demandeur = new Personne();
    }

    public Demandeur(long siret, int idAdresse) {
        SIRET = siret;
        this.idAdresse = idAdresse;
        demandeur = new Personne();
    }

    public boolean RecupererDemandandeurDepuisJson(JSONObject demandeurJSON) {
        try {
            SIRET = -1;
            SIRET = (Long) demandeurJSON.get("SIRET");
            JSONObject demandeur = (JSONObject) demandeurJSON.get("demandeur");
            this.telephone = (String) demandeurJSON.get("telephone");
            if (SIRET == -1 || demandeur == null || telephone.isEmpty())
                return false;
            System.err.println("-----------------------great---------------------");
            if(Security.test(telephone) == null)
                return false;

            this.idAdresse = Integer.parseInt(((Long) demandeurJSON.get("idAdresse")).toString());
            this.demandeur.RecupererPersonDepuisJson(demandeur);

            if(this.idAdresse == -1 || this.demandeur == null)
                return false;

        } catch (NullPointerException e) {
            System.err.println("Impossible de parse le demandeur");
            return false;
        }
        return true;
    }

    public boolean RecupererDemandandeurDepuisJson(JSONObject demandeurJSON, int SIREN) {
        if(RecupererDemandandeurDepuisJson(demandeurJSON)) {
            Transaction tx = null;
            try(Session session = CreateSession.getSession()) {
                tx = session.beginTransaction();
                try {
                    session.createQuery("FROM ClientEntity c WHERE c.siren = " + SIREN).getSingleResult();
                } catch (NoResultException e) {
                    System.err.println("no client " + SIREN);
                    return false;
                }

                try {
                    session.createQuery("FROM JonctionSirensiretEntity j WHERE j.siret = " + SIRET + " and j.siren = " + SIREN).getSingleResult();
                    System.err.println("jct found with SIREN = " + SIREN + " SIRET = " + SIRET);
                } catch (NoResultException e) {
                    JonctionSirensiretEntity j = new JonctionSirensiretEntity();
                    j.setSiren(SIREN);
                    j.setSiret(SIRET);
                    System.err.println("SIREN = " + SIREN + " SIRET = " + SIRET);
                    session.save(j);
                }

                tx.commit();
                session.clear();
                session.close();
            }
            catch (HibernateException e) {
                if(tx != null)
                    tx.rollback();
                e.printStackTrace();
                return  false;
            }
        }
        else
            System.err.println("----------------------prob-----------------");
        return demandeur == null && !telephone.isEmpty() && idAdresse != -1 && SIRET != -1;
    }

    public static ArrayList<Demandeur> recupererListDemandeurDepuisJSON(ArrayList<JSONObject> demandeursJSON, int SIREN) {
        ArrayList<Demandeur> demandeurs = new ArrayList<>();

        for(JSONObject jsonObject : demandeursJSON) {
            Demandeur demandeur = new Demandeur();
            System.err.println("--------------------- json = " + jsonObject);
            if(!demandeur.RecupererDemandandeurDepuisJson(jsonObject, SIREN) && demandeur.demandeur.id != -1 && demandeur.SIRET == -1) {
                return null;
            }
            else {
                demandeurs.add(demandeur);
                System.err.println("demandeur ajouter");
            }
        }

        return demandeurs;
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
                this.idAdresse = p.getAdresse();
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

    public static boolean deleteDemandeur(int idDemandeur) {
        Transaction tx = null;
        DemandeurEntity demandeurEntity;

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            try{demandeurEntity = (DemandeurEntity) session.createQuery("FROM DemandeurEntity d WHERE d.idPersonne = " + idDemandeur).getSingleResult();}
            catch (NoResultException e) {return false;}

            demandeurEntity.setActif((byte) 0);
            session.update(demandeurEntity);
            tx.commit();
            session.clear();
            session.close();
        }
        catch (HibernateException e) {
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

    public static ArrayList<Demandeur> getDemandeurFromClient(int SIREN, Session session) {
        ArrayList<Demandeur> demandeurs = new ArrayList<>();

        List result = session.createQuery("SELECT j.siret FROM JonctionSirensiretEntity j WHERE j.siren = " + SIREN).list();
        for(Object o : result) {
            long SIRET = (long) o;
            Demandeur demandeur = new Demandeur();
            if(!demandeur.recupererDemandeur(SIRET))
                return null;
            demandeurs.add(demandeur);
        }
        return demandeurs;
    }

    public boolean isEmpty() {
        return SIRET == -1 || idAdresse == -1 || demandeur.isEmpty();
    }
}
