package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "AdresseClient", schema = "GenieLog", catalog = "")
public class AdresseClientEntity {
    private long siret;
    private Integer numero;
    private String rue;
    private int codePostal;
    private String ville;
    private byte actif;

    @Id
    @Column(name = "SIRET", nullable = false)
    public long getSiret() {
        return siret;
    }

    public void setSiret(long siret) {
        this.siret = siret;
    }

    @Basic
    @Column(name = "numero", nullable = true)
    public Integer getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    @Basic
    @Column(name = "rue", nullable = false, length = 100)
    public String getRue() {
        return rue;
    }

    public void setRue(String rue) {
        this.rue = rue;
    }

    @Basic
    @Column(name = "codePostal", nullable = false)
    public int getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(int codePostal) {
        this.codePostal = codePostal;
    }

    @Basic
    @Column(name = "ville", nullable = false, length = 50)
    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    @Basic
    @Column(name = "actif", nullable = false)
    public byte getActif() {
        return actif;
    }

    public void setActif(byte actif) {
        this.actif = actif;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdresseClientEntity that = (AdresseClientEntity) o;
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
