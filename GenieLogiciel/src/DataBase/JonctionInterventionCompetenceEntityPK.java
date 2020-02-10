package DataBase;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class JonctionInterventionCompetenceEntityPK implements Serializable {
    private int intervention;
    private int competence;

    @Column(name = "intervention", nullable = false)
    @Id
    public int getIntervention() {
        return intervention;
    }

    public void setIntervention(int intervention) {
        this.intervention = intervention;
    }

    @Column(name = "competence", nullable = false)
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
        JonctionInterventionCompetenceEntityPK that = (JonctionInterventionCompetenceEntityPK) o;
        return intervention == that.intervention &&
                competence == that.competence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intervention, competence);
    }
}
