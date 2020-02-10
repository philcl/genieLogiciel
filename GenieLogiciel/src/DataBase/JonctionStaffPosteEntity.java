package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "JonctionStaffPoste", schema = "GenieLog", catalog = "")
@IdClass(JonctionStaffPosteEntityPK.class)
public class JonctionStaffPosteEntity {
    private int idStaff;
    private int idPoste;

    @Id
    @Column(name = "idStaff", nullable = false)
    public int getIdStaff() {
        return idStaff;
    }

    public void setIdStaff(int idStaff) {
        this.idStaff = idStaff;
    }

    @Id
    @Column(name = "idPoste", nullable = false)
    public int getIdPoste() {
        return idPoste;
    }

    public void setIdPoste(int idPoste) {
        this.idPoste = idPoste;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JonctionStaffPosteEntity that = (JonctionStaffPosteEntity) o;
        return idStaff == that.idStaff &&
                idPoste == that.idPoste;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idStaff, idPoste);
    }
}
