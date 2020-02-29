package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "JonctionTacheCompetence", schema = "GenieLog", catalog = "")
@IdClass(JonctionTacheCompetenceEntityPK.class)
public class JonctionTacheCompetenceEntity {
    private int tache;
    private int competence;

    @Id
    @Column(name = "tache", nullable = false)
    public int getTache() {
        return tache;
    }

    public void setTache(int tache) {
        this.tache = tache;
    }

    @Id
    @Column(name = "competence", nullable = false)
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
        JonctionTacheCompetenceEntity that = (JonctionTacheCompetenceEntity) o;
        return tache == that.tache &&
                competence == that.competence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tache, competence);
    }
}
