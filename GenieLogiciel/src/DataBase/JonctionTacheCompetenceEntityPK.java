package DataBase;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class JonctionTacheCompetenceEntityPK implements Serializable {
    private int tache;
    private int competence;

    @Column(name = "tache", nullable = false)
    @Id
    public int getTache() {
        return tache;
    }

    public void setTache(int tache) {
        this.tache = tache;
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
        JonctionTacheCompetenceEntityPK that = (JonctionTacheCompetenceEntityPK) o;
        return tache == that.tache &&
                competence == that.competence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tache, competence);
    }
}
