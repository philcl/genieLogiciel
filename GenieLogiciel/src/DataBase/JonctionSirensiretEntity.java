package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "JonctionSIRENSIRET", schema = "GenieLog", catalog = "")
@IdClass(JonctionSirensiretEntityPK.class)
public class JonctionSirensiretEntity {
    private long siret;
    private int siren;
    private Integer actif;
    private Timestamp debut;
    private Timestamp fin;

    @Id
    @Column(name = "SIRET", nullable = false)
    public long getSiret() {
        return siret;
    }

    public void setSiret(long siret) {
        this.siret = siret;
    }

    @Id
    @Column(name = "SIREN", nullable = false)
    public int getSiren() {
        return siren;
    }

    public void setSiren(int siren) {
        this.siren = siren;
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
        JonctionSirensiretEntity that = (JonctionSirensiretEntity) o;
        return siret == that.siret &&
                siren == that.siren &&
                Objects.equals(actif, that.actif) &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(siret, siren, actif, debut, fin);
    }
}
