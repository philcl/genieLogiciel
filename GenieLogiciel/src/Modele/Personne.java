package Modele;

import API_REST.CreateSession;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

import javax.persistence.NoResultException;

public class Personne {
    public String nom, prenom, sexe;
    public int id = -1;

    public Personne(){}

    public Personne(String nom, String prenom, int id) {
        this.nom = nom;
        this.prenom = prenom;
        this.id = id;
        this.sexe = sexe;
    }

    public Personne getFromJSON(JSONObject personneJSON) {
        try {
            nom = (String) personneJSON.get("nom");
            prenom = (String) personneJSON.get("prenom");
            id = -1;
            id = Integer.parseInt(((Long) personneJSON.get("id")).toString());
        } catch (NullPointerException e) {
            System.err.println("La personne est mal formee");
            return null;
        }
        if(nom.isEmpty() || prenom.isEmpty() || id == -1)
            return null;
        else
            return this;
    }

    public boolean verifyIdExistance() {
        try(Session session = CreateSession.getSession()) {
            Transaction tx = session.beginTransaction();
            try{session.createQuery("FROM PersonneEntity p WHERE p.id = " + id).getSingleResult();}
            catch (NoResultException e) {return false;}
            tx.commit();
            session.clear();
            session.close();
            } catch (HibernateException e) {e.printStackTrace();}
        return true;
        }

    public boolean isEmpty() {
        return id == -1 || nom.isEmpty() || prenom.isEmpty();
    }
}
