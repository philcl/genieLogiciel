package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "JonctionClient", schema = "GenieLog")
@IdClass(JonctionClientEntityPK.class)
public class JonctionClientEntity {
    private int siren;
    private long siret;

    @Id
    @Column(name = "SIREN", nullable = false)
    public int getSiren() {
        return siren;
    }

    public void setSiren(int siren) {
        this.siren = siren;
    }

    @Id
    @Column(name = "SIRET", nullable = false)
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
        JonctionClientEntity that = (JonctionClientEntity) o;
        return siren == that.siren &&
                siret == that.siret;
    }

    @Override
    public int hashCode() {
        return Objects.hash(siren, siret);
    }
}
