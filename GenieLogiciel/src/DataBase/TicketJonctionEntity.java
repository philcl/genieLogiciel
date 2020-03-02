package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "TicketJonction", schema = "GenieLog", catalog = "")
@IdClass(TicketJonctionEntityPK.class)
public class TicketJonctionEntity {
    private int idParent;
    private int idEnfant;
    private Integer actif;
    private Timestamp debut;
    private Timestamp fin;

    @Id
    @Column(name = "IDParent", nullable = false)
    public int getIdParent() {
        return idParent;
    }

    public void setIdParent(int idParent) {
        this.idParent = idParent;
    }

    @Id
    @Column(name = "IDEnfant", nullable = false)
    public int getIdEnfant() {
        return idEnfant;
    }

    public void setIdEnfant(int idEnfant) {
        this.idEnfant = idEnfant;
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
        TicketJonctionEntity that = (TicketJonctionEntity) o;
        return idParent == that.idParent &&
                idEnfant == that.idEnfant &&
                Objects.equals(actif, that.actif) &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idParent, idEnfant, actif, debut, fin);
    }
}
