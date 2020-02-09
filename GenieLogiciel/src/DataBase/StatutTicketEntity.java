package DataBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "StatutTicket", schema = "GenieLog", catalog = "")
public class StatutTicketEntity {
    private String idStatusTicket;

    @Id
    @Column(name = "idStatusTicket")
    public String getIdStatusTicket() {
        return idStatusTicket;
    }

    public void setIdStatusTicket(String idStatusTicket) {
        this.idStatusTicket = idStatusTicket;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatutTicketEntity that = (StatutTicketEntity) o;
        return Objects.equals(idStatusTicket, that.idStatusTicket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idStatusTicket);
    }
}
