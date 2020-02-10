package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "JonctionTicketCompetence", schema = "GenieLog", catalog = "")
@IdClass(JonctionTicketCompetenceEntityPK.class)
public class JonctionTicketCompetenceEntity {
    private int idTicket;
    private int competence;

    @Id
    @Column(name = "IdTicket", nullable = false)
    public int getIdTicket() {
        return idTicket;
    }

    public void setIdTicket(int idTicket) {
        this.idTicket = idTicket;
    }

    @Id
    @Column(name = "Competence", nullable = false)
    public int getCompetence() {
        return competence;
    }

    public void setCompetence(int competence) {
        this.competence = competence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JonctionTicketCompetenceEntity that = (JonctionTicketCompetenceEntity) o;
        return idTicket == that.idTicket &&
                competence == that.competence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTicket, competence);
    }
}
