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
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
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
            if(Security.test(telephone) == null)
                return false;

            this.idAdresse = Integer.parseInt(((Long) demandeurJSON.get("idAdresse")).toString());
            if(this.demandeur.RecupererPersonDepuisJson(demandeur) == null)
                return false;
            System.err.println("demandeur personne = " + demandeur);
            System.err.println("demandeur apres construct = " + this.demandeur.toString());

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
                try {session.createQuery("FROM ClientEntity c WHERE c.siren = " + SIREN + " and c.actif = 1").getSingleResult();}
                catch (NoResultException e) {return false;}

                try {session.createQuery("FROM JonctionSirensiretEntity j WHERE j.siret = " + SIRET + " and j.siren = " + SIREN + " and j.actif = 1").getSingleResult();}
                catch (NoResultException e) {
                    JonctionSirensiretEntity j = new JonctionSirensiretEntity();
                    j.setSiren(SIREN);
                    j.setSiret(SIRET);
                    j.setActif(1);
                    j.setDebut(Timestamp.from(Instant.now()));
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
        return demandeur == null && !telephone.isEmpty() && idAdresse != -1 && SIRET != -1;
    }

    public static ArrayList<Demandeur> recupererListDemandeurDepuisJSON(ArrayList<JSONObject> demandeursJSON, int SIREN) {
        ArrayList<Demandeur> demandeurs = new ArrayList<>();
        System.err.println("list demandeurs = " + demandeursJSON);

        for(JSONObject jsonObject : demandeursJSON) {
            Demandeur demandeur = new Demandeur();
            if(!demandeur.RecupererDemandandeurDepuisJson(jsonObject, SIREN) && demandeur.demandeur.id != -1 && demandeur.SIRET == -1) {
                System.err.println("n'a pas trouve de demandeurs");
                return null;
            }
            else {
                demandeurs.add(demandeur);
            }
        }
        return demandeurs;
    }

    public boolean verifyDemandeurExistance(int idDemandeur) {
        Transaction tx = null;
        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            try{session.createQuery("FROM DemandeurEntity p WHERE p.idPersonne = " + idDemandeur + " and p.actif = 1").getSingleResult();}
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

    public boolean recupererDemandeur(int idDemandeur) {
        Transaction tx = null;
        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            if(verifyDemandeurExistance(idDemandeur)) {
                DemandeurEntity p = (DemandeurEntity) session.createQuery("FROM DemandeurEntity p WHERE p.idPersonne = " + idDemandeur).getSingleResult();

                this.SIRET = p.getSiret();
                this.telephone = p.getTelephone();
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
            try{demandeurEntity = (DemandeurEntity) session.createQuery("FROM DemandeurEntity d WHERE d.idPersonne = " + idDemandeur + " and d.actif = 1").getSingleResult();}
            catch (NoResultException e) {return false;}

            demandeurEntity.setActif((byte) 0);
            demandeurEntity.setFin(Timestamp.from(Instant.now()));
            session.update(demandeurEntity);
            tx.commit();
            session.clear();

            tx = session.beginTransaction();

            JonctionSirensiretEntity jct;
            try{jct = (JonctionSirensiretEntity) session.createQuery("FROM JonctionSirensiretEntity j WHERE j.actif = 1 and j.siret = " + demandeurEntity.getSiret()).getSingleResult();}
            catch (NoResultException e) {return false;}

            jct.setActif(0);
            jct.setFin(Timestamp.from(Instant.now()));
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

    public static ArrayList<Demandeur> getDemandeurFromClient(int SIREN, Session session) {
        ArrayList<Demandeur> demandeurs = new ArrayList<>();

        List result = session.createQuery("SELECT j.siret FROM JonctionSirensiretEntity j WHERE j.siren = " + SIREN + " and j.actif = 1").list();
        for(Object o : result) {
            long SIRET = (long) o;
            List res = session.createQuery("FROM DemandeurEntity d WHERE d.siret = " + SIRET + " and d.actif = 1").list();

            for(Object obj : res) {
                DemandeurEntity demandeurEntity = (DemandeurEntity) obj;
                Demandeur demandeur = new Demandeur();
                if(!demandeur.recupererDemandeur(demandeurEntity.getIdPersonne()))
                    return null;
                demandeurs.add(demandeur);
            }
        }
        return demandeurs;
    }

    public boolean isEmpty() {
        return SIRET == -1 || idAdresse == -1 || demandeur.isEmpty();
    }

    public String toString() {
        String str = "";
        str += "SIRET = " + SIRET;
        str += " adresse : " + idAdresse;
        str += " telephone = " + telephone;
        str+= " demandeur = " + demandeur.toString();
        return str;
    }
}
