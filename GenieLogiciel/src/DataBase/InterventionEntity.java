package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "Intervention", schema = "GenieLog", catalog = "")
public class InterventionEntity {
    private int id;
    private Timestamp debut;
    private Timestamp fin;
    private int etat;
    private int technicien;
    private int ticket;
    private Timestamp dureeEstimee;

    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "debut")
    public Timestamp getDebut() {
        return debut;
    }

    public void setDebut(Timestamp debut) {
        this.debut = debut;
    }

    @Basic
    @Column(name = "fin")
    public Timestamp getFin() {
        return fin;
    }

    public void setFin(Timestamp fin) {
        this.fin = fin;
    }

    @Basic
    @Column(name = "etat")
    public int getEtat() {
        return etat;
    }

    public void setEtat(int etat) {
        this.etat = etat;
    }

    @Basic
    @Column(name = "technicien")
    public int getTechnicien() {
        return technicien;
    }

    public void setTechnicien(int technicien) {
        this.technicien = technicien;
    }

    @Basic
    @Column(name = "ticket")
    public int getTicket() {
        return ticket;
    }

    public void setTicket(int ticket) {
        this.ticket = ticket;
    }

    @Basic
    @Column(name = "dureeEstimee")
    public Timestamp getDureeEstimee() {
        return dureeEstimee;
    }

    public void setDureeEstimee(Timestamp dureeEstimee) {
        this.dureeEstimee = dureeEstimee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterventionEntity that = (InterventionEntity) o;
        return id == that.id &&
                etat == that.etat &&
                technicien == that.technicien &&
                ticket == that.ticket &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin) &&
                Objects.equals(dureeEstimee, that.dureeEstimee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, debut, fin, etat, technicien, ticket, dureeEstimee);
    }
}
