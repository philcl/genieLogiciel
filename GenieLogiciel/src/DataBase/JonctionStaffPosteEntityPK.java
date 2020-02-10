package DataBase;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class JonctionStaffPosteEntityPK implements Serializable {
    private int idStaff;
    private int idPoste;

    @Column(name = "idStaff", nullable = false)
    @Id
    public int getIdStaff() {
        return idStaff;
    }

    public void setIdStaff(int idStaff) {
        this.idStaff = idStaff;
    }

    @Column(name = "idPoste", nullable = false)
    @Id
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
        JonctionStaffPosteEntityPK that = (JonctionStaffPosteEntityPK) o;
        return idStaff == that.idStaff &&
                idPoste == that.idPoste;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idStaff, idPoste);
    }
}
