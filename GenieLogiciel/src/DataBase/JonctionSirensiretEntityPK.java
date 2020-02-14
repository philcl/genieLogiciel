package DataBase;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class JonctionSirensiretEntityPK implements Serializable {
    private long siret;
    private int siren;

    @Column(name = "SIRET", nullable = false)
    @Id
    public long getSiret() {
        return siret;
    }

    public void setSiret(long siret) {
        this.siret = siret;
    }

    @Column(name = "SIREN", nullable = false)
    @Id
    public int getSiren() {
        return siren;
    }

    public void setSiren(int siren) {
        this.siren = siren;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JonctionSirensiretEntityPK that = (JonctionSirensiretEntityPK) o;
        return siret == that.siret &&
                siren == that.siren;
    }

    @Override
    public int hashCode() {
        return Objects.hash(siret, siren);
    }
}
