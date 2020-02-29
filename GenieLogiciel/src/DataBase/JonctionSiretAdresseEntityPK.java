package DataBase;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class JonctionSiretAdresseEntityPK implements Serializable {
    private long siret;
    private int idAdresse;

    @Column(name = "SIRET", nullable = false)
    @Id
    public long getSiret() {
        return siret;
    }

    public void setSiret(long siret) {
        this.siret = siret;
    }

    @Column(name = "idAdresse", nullable = false)
    @Id
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
        JonctionSiretAdresseEntityPK that = (JonctionSiretAdresseEntityPK) o;
        return siret == that.siret &&
                idAdresse == that.idAdresse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(siret, idAdresse);
    }
}
