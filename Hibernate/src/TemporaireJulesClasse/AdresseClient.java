package TemporaireJulesClasse;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "AdresseClient", schema = "GenieLog", catalog = "")
public class AdresseClient {
    @Id
    @Column(name = "SIRET", nullable = false)
    private long siret;

    @Basic
    @Column(name = "numero", nullable = true)
    private Integer numero;

    @Basic
    @Column(name = "rue", nullable = false, length = 100)
    private String rue;

    @Basic
    @Column(name = "codePostal", nullable = false)
    private int codePostal;

    @Basic
    @Column(name = "ville", nullable = false, length = 50)
    private String ville;

    @Basic
    @Column(name = "actif", nullable = false)
    private boolean actif;

    @ManyToOne
    @JoinColumn(name = "client")
    private Client client;


    public long getSiret() {
        return siret;
    }

    public void setSiret(long siret) {
        this.siret = siret;
    }


    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }


    public String getRue() {
        return rue;
    }

    public void setRue(String rue) {
        this.rue = rue;
    }


    public int getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(int codePostal) {
        this.codePostal = codePostal;
    }


    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }


    public boolean getActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }


    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdresseClient that = (AdresseClient) o;
        return siret == that.siret &&
                codePostal == that.codePostal &&
                actif == that.actif &&
                Objects.equals(numero, that.numero) &&
                Objects.equals(rue, that.rue) &&
                Objects.equals(ville, that.ville);
    }

    @Override
    public int hashCode() {
        return Objects.hash(siret, numero, rue, codePostal, ville, actif);
    }
}
