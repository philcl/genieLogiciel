package DataBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "Sexe", schema = "GenieLog", catalog = "")
public class SexeEntity {
    private String sexe;

    @Id
    @Column(name = "sexe", nullable = false, length = 5)
    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SexeEntity that = (SexeEntity) o;
        return Objects.equals(sexe, that.sexe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sexe);
    }
}
