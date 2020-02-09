package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "TicketJonction", schema = "GenieLog", catalog = "")
@IdClass(TicketJonctionEntityPK.class)
public class TicketJonctionEntity {
    private int idParent;
    private int idEnfant;

    @Id
    @Column(name = "IDParent")
    public int getIdParent() {
        return idParent;
    }

    public void setIdParent(int idParent) {
        this.idParent = idParent;
    }

    @Id
    @Column(name = "IDEnfant")
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
        TicketJonctionEntity that = (TicketJonctionEntity) o;
        return idParent == that.idParent &&
                idEnfant == that.idEnfant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idParent, idEnfant);
    }
}
