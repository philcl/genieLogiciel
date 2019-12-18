package TemporaireJulesClasse;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="Client",schema = "GenieLog")
public class Client {
    @Id
    @Column(name = "SIREN", nullable = false)
    private int siren;

    @Basic
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Basic
    @Column(name = "actif", nullable = false)
    private boolean actif;

    @OneToMany(mappedBy = "client")
    private List<AdresseClient> adresseClients;


    public int getSiren() {
        return siren;
    }

    public void setSiren(int siren) {
        this.siren = siren;
    }


    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public boolean getActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }


    public List<AdresseClient> getAdresseClients() {
        return adresseClients;
    }

    public void setAdresseClients(List<AdresseClient> adresseClients) {
        this.adresseClients = adresseClients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client that = (Client) o;
        return siren == that.siren &&
                actif == that.actif &&
                Objects.equals(nom, that.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(siren, nom, actif);
    }
}
