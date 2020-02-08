package DataBase;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class TicketJonctionEntityPK implements Serializable {
    private int idParent;
    private int idEnfant;

    @Column(name = "IDParent")
    @Id
    public int getIdParent() {
        return idParent;
    }

    public void setIdParent(int idParent) {
        this.idParent = idParent;
    }

    @Column(name = "IDEnfant")
    @Id
    public int getIdEnfant() {
        return idEnfant;
    }

    public void setIdEnfant(int idEnfant) {
        this.idEnfant = idEnfant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketJonctionEntityPK that = (TicketJonctionEntityPK) o;
        return idParent == that.idParent &&
                idEnfant == that.idEnfant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idParent, idEnfant);
    }
}
