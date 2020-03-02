package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "TypeDemandes", schema = "GenieLog", catalog = "")
public class TypeDemandesEntity {
    private String idTypeDemandes;
    private Integer actif;
    private Timestamp debut;
    private Timestamp fin;

    @Id
    @Column(name = "idTypeDemandes", nullable = false, length = 100)
    public String getIdTypeDemandes() {
        return idTypeDemandes;
    }

    public void setIdTypeDemandes(String idTypeDemandes) {
        this.idTypeDemandes = idTypeDemandes;
    }

    @Basic
    @Column(name = "actif", nullable = true)
    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
        this.actif = actif;
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
        TypeDemandesEntity that = (TypeDemandesEntity) o;
        return Objects.equals(idTypeDemandes, that.idTypeDemandes) &&
                Objects.equals(actif, that.actif) &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTypeDemandes, actif, debut, fin);
    }
}
