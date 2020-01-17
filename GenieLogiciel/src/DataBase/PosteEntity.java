package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Poste", schema = "GenieLog")
public class PosteEntity {
    private int idPersonne;
    private byte admin;
    private byte respTech;
    private byte technicien;
    private byte operateur;

    @Id
    @Column(name = "idPersonne", nullable = false)
    public int getIdPersonne() {
        return idPersonne;
    }

    public void setIdPersonne(int idPersonne) {
        this.idPersonne = idPersonne;
    }

    @Basic
    @Column(name = "Admin", nullable = false)
    public byte getAdmin() {
        return admin;
    }

    public void setAdmin(byte admin) {
        this.admin = admin;
    }

    @Basic
    @Column(name = "RespTech", nullable = false)
    public byte getRespTech() {
        return respTech;
    }

    public void setRespTech(byte respTech) {
        this.respTech = respTech;
    }

    @Basic
    @Column(name = "Technicien", nullable = false)
    public byte getTechnicien() {
        return technicien;
    }

    public void setTechnicien(byte technicien) {
        this.technicien = technicien;
    }

    @Basic
    @Column(name = "Operateur", nullable = false)
    public byte getOperateur() {
        return operateur;
    }

    public void setOperateur(byte operateur) {
        this.operateur = operateur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PosteEntity that = (PosteEntity) o;
        return idPersonne == that.idPersonne &&
                admin == that.admin &&
                respTech == that.respTech &&
                technicien == that.technicien &&
                operateur == that.operateur;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPersonne, admin, respTech, technicien, operateur);
    }
}
