package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "JonctionSIRETAdresse", schema = "GenieLog", catalog = "")
@IdClass(JonctionSiretAdresseEntityPK.class)
public class JonctionSiretAdresseEntity {
    private long siret;
    private int idAdresse;

    @Id
    @Column(name = "SIRET", nullable = false)
    public long getSiret() {
        return siret;
    }

    public void setSiret(long siret) {
        this.siret = siret;
    }

    @Id
    @Column(name = "idAdresse", nullable = false)
    public int getIdAdresse() {
        return idAdresse;
    }

    public void setIdAdresse(int idAdresse) {
        this.idAdresse = idAdresse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JonctionSiretAdresseEntity that = (JonctionSiretAdresseEntity) o;
        return siret == that.siret &&
                idAdresse == that.idAdresse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(siret, idAdresse);
    }
}
