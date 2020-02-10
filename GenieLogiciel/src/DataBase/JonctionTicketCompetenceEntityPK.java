package DataBase;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class JonctionTicketCompetenceEntityPK implements Serializable {
    private int idTicket;
    private int competence;

    @Column(name = "IdTicket", nullable = false)
    @Id
    public int getIdTicket() {
        return idTicket;
    }

    public void setIdTicket(int idTicket) {
        this.idTicket = idTicket;
    }

    @Column(name = "Competence", nullable = false)
    @Id
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
        JonctionTicketCompetenceEntityPK that = (JonctionTicketCompetenceEntityPK) o;
        return idTicket == that.idTicket &&
                competence == that.competence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTicket, competence);
    }
}
