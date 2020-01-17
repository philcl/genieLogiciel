package DataBase;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class JonctionClientEntityPK implements Serializable {
    private int siren;
    private long siret;

    @Column(name = "SIREN", nullable = false)
    @Id
    public int getSiren() {
        return siren;
    }

    public void setSiren(int siren) {
        this.siren = siren;
    }

    @Column(name = "SIRET", nullable = false)
    @Id
    public long getSiret() {
        return siret;
    }

    public void setSiret(long siret) {
        this.siret = siret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JonctionClientEntityPK that = (JonctionClientEntityPK) o;
        return siren == that.siren &&
                siret == that.siret;
    }

    @Override
    public int hashCode() {
        return Objects.hash(siren, siret);
    }
}
