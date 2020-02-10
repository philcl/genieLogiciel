package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "JonctionStaffCompetence", schema = "GenieLog", catalog = "")
@IdClass(JonctionStaffCompetenceEntityPK.class)
public class JonctionStaffCompetenceEntity {
    private int staffId;
    private int competenceId;

    @Id
    @Column(name = "staffID", nullable = false)
    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    @Id
    @Column(name = "competenceID", nullable = false)
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
        JonctionStaffCompetenceEntity that = (JonctionStaffCompetenceEntity) o;
        return staffId == that.staffId &&
                competenceId == that.competenceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffId, competenceId);
    }
}
