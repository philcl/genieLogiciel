package Modele.Client;

import DataBase.ClientEntity;
import Modele.Adresse;
import org.hibernate.Session;

import javax.persistence.NoResultException;

public class ClientSite {
    public long SIRET;
    public Adresse adresse;
    public int idAdresse;

    public ClientSite() {
        SIRET = -1;
        adresse = new Adresse();
        idAdresse = -1;
    }

    public ClientSite(long SIRET, Adresse adresse, int idAdresse) {
        this.SIRET = SIRET;
        this.adresse = adresse;
        this.idAdresse = idAdresse;
    }

    public boolean recupererClientSite(long SIRET, Session session) {
        ClientEntity clientEntity;

        try{clientEntity = (ClientEntity) session.createQuery("FROM ClientEntity c WHERE c.siren = JonctionSirensiretEntity.siren and JonctionSirensiretEntity.siret = " + SIRET).getSingleResult();}
        catch(NoResultException e) {return false;}
        this.SIRET = SIRET;
        if(!this.adresse.recupererAdresse(clientEntity.getAdresse()))
            return false;
        this.idAdresse = this.adresse.getId();
        if(this.idAdresse == -1)
            return false;
        return true;
    }
}
