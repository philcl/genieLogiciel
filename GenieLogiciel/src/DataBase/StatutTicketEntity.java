package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "StatutTicket", schema = "GenieLog", catalog = "")
public class StatutTicketEntity {
    private String idStatusTicket;
    private Integer actif;
    private Timestamp debut;
    private Timestamp fin;

    @Id
    @Column(name = "idStatusTicket", nullable = false, length = 50)
    public String getIdStatusTicket() {
        return idStatusTicket;
    }

    public void setIdStatusTicket(String idStatusTicket) {
        this.idStatusTicket = idStatusTicket;
    }

    @Basic
    @Column(name = "actif", nullable = true)
    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
        this.actif = actif;
    }

    @Basic
    @Column(name = "debut", nullable = true)
    public Timestamp getDebut() {
        return debut;
    }

    public void setDebut(Timestamp debut) {
        this.debut = debut;
    }

    @Basic
    @Column(name = "fin", nullable = true)
    public Timestamp getFin() {
        return fin;
    }

    public void setFin(Timestamp fin) {
        this.fin = fin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatutTicketEntity that = (StatutTicketEntity) o;
        return Objects.equals(idStatusTicket, that.idStatusTicket) &&
                Objects.equals(actif, that.actif) &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idStatusTicket, actif, debut, fin);
    }
}
