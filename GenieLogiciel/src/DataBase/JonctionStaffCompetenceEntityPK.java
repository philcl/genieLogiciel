package DataBase;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class JonctionStaffCompetenceEntityPK implements Serializable {
    private int staffId;
    private int competenceId;

    @Column(name = "staffID", nullable = false)
    @Id
    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    @Column(name = "competenceID", nullable = false)
    @Id
    public int getCompetenceId() {
        return competenceId;
    }

    public void setCompetenceId(int competenceId) {
        this.competenceId = competenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JonctionStaffCompetenceEntityPK that = (JonctionStaffCompetenceEntityPK) o;
        return staffId == that.staffId &&
                competenceId == that.competenceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffId, competenceId);
    }
}
