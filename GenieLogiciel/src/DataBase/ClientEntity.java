package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Client", schema = "GenieLog")
public class ClientEntity {
    private int siren;
    private String nom;
    private byte actif;
    private int adresse;

    @Id
    @Column(name = "SIREN")
    public int getSiren() {
        return siren;
    }

    public void setSiren(int siren) {
        this.siren = siren;
    }

    @Basic
    @Column(name = "nom")
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Basic
    @Column(name = "actif")
    public byte getActif() {
        return actif;
    }

    public void setActif(byte actif) {
        this.actif = actif;
    }

    @Basic
    @Column(name = "Adresse")
    public int getAdresse() {
        return adresse;
    }

    public void setAdresse(int adresse) {
        this.adresse = adresse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientEntity that = (ClientEntity) o;
        return siren == that.siren &&
                actif == that.actif &&
                adresse == that.adresse &&
                Objects.equals(nom, that.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(siren, nom, actif, adresse);
    }
}
