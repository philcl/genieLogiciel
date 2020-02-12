package Modele;

import API_REST.CreateSession;
import DataBase.AdresseEntity;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.NoResultException;

public class Adresse {
    public int numero;
    public String rue, ville, codePostal;

    public Adresse(){}

    public Adresse(int numero, String codePostal, String rue, String ville) {
        this.numero = numero;
        this.codePostal = codePostal;
        this.rue = rue;
        this.ville = ville;
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
}
