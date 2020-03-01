package Modele;

import API_REST.CreateSession;
import API_REST.Security;
import DataBase.AdresseEntity;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;

import javax.persistence.NoResultException;

public class Adresse {
    public int numero = -1;
    public String rue, ville, codePostal;

    public Adresse(){}

    public Adresse(int numero, String codePostal, String rue, String ville) {
        this.numero = numero;
        this.codePostal = codePostal;
        this.rue = rue;
        this.ville = ville;
    }

    public boolean recupererAdresse(int id) {
        Transaction tx = null;
        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            AdresseEntity adr;
            try{ adr = (AdresseEntity) session.createQuery("FROM AdresseEntity a WHERE a.idAdresse = " + id).getSingleResult();}
            catch(NoResultException e) {return false;}

            this.numero = adr.getNumero();
            this.rue = adr.getRue();
            this.codePostal = adr.getCodePostal();
            this.ville = adr.getVille();

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

    public int getId() {
        Transaction tx = null;
        int id = -1;
        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            String request = "FROM AdresseEntity a WHERE a.numero = " + this.numero + " and a.rue = '" + this.rue + "' and a.ville = '" + this.ville + "' and a.codePostal = '" + this .codePostal + "'";
            AdresseEntity adresseEntity = null;
            try{
                adresseEntity = (AdresseEntity) session.createQuery(request).getSingleResult();
                id = adresseEntity.getIdAdresse();
            }
            catch (NoResultException e) { id = -1;}
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
            System.err.println("Erreur lors de la recuperation de l'id sur la table Adresse");
        }
        return  id;
    }

    public int addAdresse() {
        Transaction tx = null;
        int id;
        if(codePostal.isEmpty() || numero == -1 || ville.isEmpty() || rue.isEmpty()) {
            System.err.println("L'adresse est mal remplie");
            System.err.println("code postal = " + codePostal + " numero = " + numero + " ville = " + ville + " rue = " + rue);
            return -1;
        }
        if(this.verifyAdresseExistance() != -1)
            return this.getId();
        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            AdresseEntity adresseEntity = new AdresseEntity();
            adresseEntity.setCodePostal(codePostal);
            adresseEntity.setNumero(numero);
            adresseEntity.setRue(rue);
            adresseEntity.setVille(ville);

            id = (int) session.createQuery("SELECT MAX(a.idAdresse) FROM AdresseEntity a").getSingleResult();
            adresseEntity.setIdAdresse(id+1);
            session.save(adresseEntity);
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
            System.err.println("Erreur lors de l'ajout d'une adresse sur la base");
            return -1;
        }
        return id;
    }

    public int verifyAdresseExistance() {
        Transaction tx = null;
        int id = -1;

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            try{id = (int) session.createQuery("SELECT a.idAdresse FROM AdresseEntity a WHERE a.numero = " + numero + " and a.rue = '" + rue + "' and a.codePostal = '" + codePostal + "' and a.ville = '" + ville + "'").getSingleResult();}
            catch (NoResultException e) {return -1;}
            tx.commit();
            session.clear();
            session.close();
        }
        catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return id;
    }

    public boolean RecupererAdresseDepuisJson(JSONObject adr) {
        numero = -1;
        numero = Integer.parseInt(((Long) adr.get("numero")).toString());
        codePostal = Security.test((String) adr.get("codePostal"));
        rue = Security.test((String) adr.get("rue"));
        ville = Security.test((String) adr.get("ville"));

        if(numero == -1 || codePostal == null || rue == null || ville == null)
            return false;
        else
            return true;
    }

    public String toString() {
        return numero + " " + rue + " " + codePostal + " " + ville;
    }

    public boolean isEmpty() {
        return numero == -1 || rue.isEmpty() || codePostal.isEmpty() || ville.isEmpty();
    }
}
