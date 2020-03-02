package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "JonctionTacheCompetence", schema = "GenieLog", catalog = "")
@IdClass(JonctionTacheCompetenceEntityPK.class)
public class JonctionTacheCompetenceEntity {
    private int tache;
    private int competence;
    private Integer actif;
    private Timestamp debut;
    private Timestamp fin;

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
        JonctionTacheCompetenceEntity that = (JonctionTacheCompetenceEntity) o;
        return tache == that.tache &&
                competence == that.competence &&
                Objects.equals(actif, that.actif) &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tache, competence, actif, debut, fin);
    }
}
