package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "JonctionStaffCompetence", schema = "GenieLog", catalog = "")
@IdClass(JonctionStaffCompetenceEntityPK.class)
public class JonctionStaffCompetenceEntity {
    private int staffId;
    private int competenceId;
    private Integer actif;
    private Timestamp debut;
    private Timestamp fin;

    @Id
    @Column(name = "staffID", nullable = false)
    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    @Id
    @Column(name = "competenceID", nullable = false)
    public int getCompetenceId() {
        return competenceId;
    }

    public void setCompetenceId(int competenceId) {
        this.competenceId = competenceId;
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
        JonctionStaffCompetenceEntity that = (JonctionStaffCompetenceEntity) o;
        return staffId == that.staffId &&
                competenceId == that.competenceId &&
                Objects.equals(actif, that.actif) &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffId, competenceId, actif, debut, fin);
    }
}
