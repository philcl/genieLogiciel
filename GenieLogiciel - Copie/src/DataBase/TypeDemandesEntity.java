package DataBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "TypeDemandes", schema = "GenieLog", catalog = "")
public class TypeDemandesEntity {
    private String idTypeDemandes;

    @Id
    @Column(name = "idTypeDemandes", nullable = false, length = 100)
    public String getIdTypeDemandes() {
        return idTypeDemandes;
    }

    public void setIdTypeDemandes(String idTypeDemandes) {
        this.idTypeDemandes = idTypeDemandes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeDemandesEntity that = (TypeDemandesEntity) o;
        return Objects.equals(idTypeDemandes, that.idTypeDemandes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTypeDemandes);
    }
}
