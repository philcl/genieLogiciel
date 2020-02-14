package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "JonctionSIRENSIRET", schema = "GenieLog", catalog = "")
@IdClass(JonctionSirensiretEntityPK.class)
public class JonctionSirensiretEntity {
    private long siret;
    private int siren;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JonctionSirensiretEntity that = (JonctionSirensiretEntity) o;
        return siret == that.siret &&
                siren == that.siren;
    }

    @Override
    public int hashCode() {
        return Objects.hash(siret, siren);
    }
}
