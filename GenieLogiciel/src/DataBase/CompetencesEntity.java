package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "Competences", schema = "GenieLog", catalog = "")
public class CompetencesEntity {
    private int idCompetences;
    private String competence;
    private Integer actif;
    private Timestamp debut;
    private Timestamp fin;

    @Id
    @Column(name = "idCompetences", nullable = false)
    public int getIdCompetences() {
        return idCompetences;
    }

    public void setIdCompetences(int idCompetences) {
        this.idCompetences = idCompetences;
    }

    @Basic
    @Column(name = "competence", nullable = false, length = 100)
    public String getCompetence() {
        return competence;
    }

    public void setCompetence(String competence) {
        this.competence = competence;
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
        CompetencesEntity that = (CompetencesEntity) o;
        return idCompetences == that.idCompetences &&
                Objects.equals(competence, that.competence) &&
                Objects.equals(actif, that.actif) &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCompetences, competence, actif, debut, fin);
    }
}
