package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "JonctionAdresseSIRET", schema = "GenieLog")
public class JonctionAdresseSiretEntity {
    private long siret;
    private int idAdresse;
    private byte actif;

    @Id
    @Column(name = "SIRET")
    public long getSiret() {
        return siret;
    }

    public void setSiret(long siret) {
        this.siret = siret;
    }

    @Basic
    @Column(name = "idAdresse")
    public int getIdAdresse() {
        return idAdresse;
    }

    public void setIdAdresse(int idAdresse) {
        this.idAdresse = idAdresse;
    }

    @Basic
    @Column(name = "actif")
    public byte getActif() {
        return actif;
    }

    public void setActif(byte actif) {
        this.actif = actif;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JonctionAdresseSiretEntity that = (JonctionAdresseSiretEntity) o;
        return siret == that.siret &&
                idAdresse == that.idAdresse &&
                actif == that.actif;
    }

    @Override
    public int hashCode() {
        return Objects.hash(siret, idAdresse, actif);
    }
}
