package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "JonctionInterventionCompetence", schema = "GenieLog", catalog = "")
@IdClass(JonctionInterventionCompetenceEntityPK.class)
public class JonctionInterventionCompetenceEntity {
    private int intervention;
    private int competence;

    @Id
    @Column(name = "intervention")
    public int getIntervention() {
        return intervention;
    }

    public void setIntervention(int intervention) {
        this.intervention = intervention;
    }

    @Id
    @Column(name = "competence")
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
        JonctionInterventionCompetenceEntity that = (JonctionInterventionCompetenceEntity) o;
        return intervention == that.intervention &&
                competence == that.competence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intervention, competence);
    }
}
