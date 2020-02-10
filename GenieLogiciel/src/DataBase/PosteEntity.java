package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Poste", schema = "GenieLog", catalog = "")
public class PosteEntity {
    private int idPoste;
    private String poste;

    @Id
    @Column(name = "idPoste", nullable = false)
    public int getIdPoste() {
        return idPoste;
    }

    public void setIdPoste(int idPoste) {
        this.idPoste = idPoste;
    }

    @Basic
    @Column(name = "Poste", nullable = false, length = 100)
    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PosteEntity that = (PosteEntity) o;
        return idPoste == that.idPoste &&
                Objects.equals(poste, that.poste);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPoste, poste);
    }
}
