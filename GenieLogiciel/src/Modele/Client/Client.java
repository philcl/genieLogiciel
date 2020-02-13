package Modele.Client;

import API_REST.CreateSession;
import API_REST.ReponseType;
import DataBase.AdresseEntity;
import DataBase.ClientEntity;
import Modele.Adresse;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.NoResultException;

public class Client {
    public int SIREN;
    public Adresse adresse;
    public String nom;

    public Client() {
        adresse = new Adresse();
    }

    public boolean getClient(int SIREN)
    {
        Transaction tx = null;

        try (Session session = CreateSession.getSession()) {

            tx = session.beginTransaction();

            try {
                ClientEntity clientEntity = (ClientEntity) session.createQuery("SELECT c FROM ClientEntity c WHERE c.siren = " + SIREN).getSingleResult();

                this.SIREN = clientEntity.getSiren();
                this.nom = clientEntity.getNom();

                AdresseEntity adresseEntity = (AdresseEntity) session.createQuery("SELECT a FROM AdresseEntity a WHERE a.idAdresse = " + clientEntity.getAdresse());

                this.adresse.ville = adresseEntity.getVille();
                this.adresse.rue = adresseEntity.getRue();
                this.adresse.codePostal = adresseEntity.getCodePostal();
                this.adresse.numero = adresseEntity.getNumero();

            } catch (NoResultException e) {
                e.printStackTrace();
                return false;
                //return ReponseType.getNOTOK("Il n'y a pas de client correspondant à cet ID", true, tx, session);
            }

            tx.commit();
            session.clear();

            return true;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return false;
    }

    public boolean verifSIREN(int SIREN) {
        Transaction tx = null;

        try (Session session = CreateSession.getSession()) {

            tx = session.beginTransaction();

            ClientEntity clientEntity;

            try {
                clientEntity = (ClientEntity) session.createQuery("SELECT c FROM ClientEntity c WHERE c.siren = " + SIREN).getSingleResult();
            } catch (NoResultException e) {
                e.printStackTrace();
                return false;
                //return ReponseType.getNOTOK("Il n'y a pas de client correspondant à cet ID", true, tx, session);
            }

            tx.commit();
            session.clear();

            return true;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return false;
    }
}
