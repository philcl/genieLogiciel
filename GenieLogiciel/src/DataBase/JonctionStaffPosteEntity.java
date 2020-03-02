package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "JonctionStaffPoste", schema = "GenieLog", catalog = "")
@IdClass(JonctionStaffPosteEntityPK.class)
public class JonctionStaffPosteEntity {
    private int idStaff;
    private int idPoste;
    private Integer actif;
    private Timestamp debut;
    private Timestamp fin;

    @Id
    @Column(name = "idStaff", nullable = false)
    public int getIdStaff() {
        return idStaff;
    }

    public void setIdStaff(int idStaff) {
        this.idStaff = idStaff;
    }

    @Id
    @Column(name = "idPoste", nullable = false)
    public int getIdPoste() {
        return idPoste;
    }

    public void setIdPoste(int idPoste) {
        this.idPoste = idPoste;
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
        JonctionStaffPosteEntity that = (JonctionStaffPosteEntity) o;
        return idStaff == that.idStaff &&
                idPoste == that.idPoste &&
                Objects.equals(actif, that.actif) &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idStaff, idPoste, actif, debut, fin);
    }
}
