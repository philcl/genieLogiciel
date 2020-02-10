package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Adresse", schema = "GenieLog", catalog = "")
public class AdresseEntity {
    private int idAdresse;
    private Integer numero;
    private String rue;
    private String codePostal;
    private String ville;

    @Id
    @Column(name = "idAdresse", nullable = false)
    public int getIdAdresse() {
        return idAdresse;
    }

    public void setIdAdresse(int idAdresse) {
        this.idAdresse = idAdresse;
    }

    @Basic
    @Column(name = "numero", nullable = true)
    public Integer getNumero() {
        return numero;
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
    @Column(name = "codePostal", nullable = false, length = 50)
    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    @Basic
    @Column(name = "ville", nullable = false, length = 100)
    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdresseEntity that = (AdresseEntity) o;
        return idAdresse == that.idAdresse &&
                Objects.equals(numero, that.numero) &&
                Objects.equals(rue, that.rue) &&
                Objects.equals(codePostal, that.codePostal) &&
                Objects.equals(ville, that.ville);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAdresse, numero, rue, codePostal, ville);
    }
}
