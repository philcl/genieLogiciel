package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Competences", schema = "GenieLog", catalog = "")
public class CompetencesEntity {
    private int idCompetences;
    private String competence;

    @Id
    @Column(name = "idCompetences")
    public int getIdCompetences() {
        return idCompetences;
    }

    public void setIdCompetences(int idCompetences) {
        this.idCompetences = idCompetences;
    }

    @Basic
    @Column(name = "competence")
    public String getCompetence() {
        return competence;
    }

    public void setCompetence(String competence) {
        this.competence = competence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencesEntity that = (CompetencesEntity) o;
        return idCompetences == that.idCompetences &&
                Objects.equals(competence, that.competence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCompetences, competence);
    }
}
