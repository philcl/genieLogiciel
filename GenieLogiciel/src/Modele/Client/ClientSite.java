package Modele.Client;

import API_REST.CreateSession;
import DataBase.JonctionAdresseSiretEntity;
import Modele.Adresse;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.NoResultException;

public class ClientSite {
    public long SIRET;
    public Adresse adresse;

    public ClientSite() {
        SIRET = -1;
        adresse = new Adresse();
    }

    public boolean recupererClientSite(long SIRET) {
        Transaction tx = null;

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            JonctionAdresseSiretEntity jct;

            try{jct = (JonctionAdresseSiretEntity) session.createQuery("FROM JonctionAdresseSiretEntity  j WHERE j.siret = " + SIRET).getSingleResult();}
            catch(NoResultException e) {return false;}
            this.SIRET = SIRET;
            if(!this.adresse.recupererAdresse(jct.getIdAdresse()))
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
}
