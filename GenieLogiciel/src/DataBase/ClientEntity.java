package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "Client", schema = "GenieLog", catalog = "")
public class ClientEntity {
    private int idClient;
    private int siren;
    private String nom;
    private byte actif;
    private int adresse;
    private Timestamp debut;
    private Timestamp fin;

    @Id
    @Column(name = "idClient", nullable = false)
    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    @Basic
    @Column(name = "SIREN", nullable = false)
    public int getSiren() {
        return siren;
    }

    public void setSiren(int siren) {
        this.siren = siren;
    }

    @Basic
    @Column(name = "nom", nullable = false, length = 100)
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Basic
    @Column(name = "actif", nullable = false)
    public byte getActif() {
        return actif;
    }

    public void setActif(byte actif) {
        this.actif = actif;
    }

    @Basic
    @Column(name = "Adresse", nullable = false)
    public int getAdresse() {
        return adresse;
    }

    public void setAdresse(int adresse) {
        this.adresse = adresse;
    }

    @Basic
    @Column(name = "debut", nullable = true)
    public Timestamp getDebut() {
        return debut;
    }

    public void setDebut(Timestamp debut) {
        this.debut = debut;
    }

    @Basic
    @Column(name = "fin", nullable = true)
    public Timestamp getFin() {
        return fin;
    }

    public void setFin(Timestamp fin) {
        this.fin = fin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientEntity that = (ClientEntity) o;
        return idClient == that.idClient &&
                siren == that.siren &&
                actif == that.actif &&
                adresse == that.adresse &&
                Objects.equals(nom, that.nom) &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idClient, siren, nom, actif, adresse, debut, fin);
    }
}
