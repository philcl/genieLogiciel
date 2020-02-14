package Modele.Client;

import DataBase.ClientEntity;
import Modele.Adresse;
import org.hibernate.Session;

import javax.persistence.NoResultException;

public class ClientSite {
    public long SIRET;
    public Adresse adresse;

    public ClientSite() {
        SIRET = -1;
        adresse = new Adresse();
    }

    public boolean recupererClientSite(long SIRET, Session session) {
        ClientEntity clientEntity;

        try{clientEntity = (ClientEntity) session.createQuery("FROM ClientEntity c WHERE c.siren = JonctionSirensiretEntity.siren and JonctionSirensiretEntity.siret = " + SIRET).getSingleResult();}
        catch(NoResultException e) {return false;}
        this.SIRET = SIRET;
        return this.adresse.recupererAdresse(clientEntity.getAdresse());
    }
}
