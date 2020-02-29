package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "Tache", schema = "GenieLog", catalog = "")
public class TacheEntity {
    private int id;
    private Timestamp debut;
    private Timestamp fin;
    private String statut;
    private int technicien;
    private int ticket;
    private Integer dureeEstimee;
    private Integer dureeReelle;
    private String objet;
    private String description;

    @Id
    @Column(name = "ID", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "debut", nullable = false)
    public Timestamp getDebut() {
        return debut;
    }

    public void setDebut(Timestamp debut) {
        this.debut = debut;
    }

    @Basic
    @Column(name = "fin", nullable = false)
    public Timestamp getFin() {
        return fin;
    }

    public void setFin(Timestamp fin) {
        this.fin = fin;
    }

    @Basic
    @Column(name = "statut", nullable = false, length = 50)
    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Basic
    @Column(name = "technicien", nullable = false)
    public int getTechnicien() {
        return technicien;
    }

    public void setTechnicien(int technicien) {
        this.technicien = technicien;
    }

    @Basic
    @Column(name = "ticket", nullable = false)
    public int getTicket() {
        return ticket;
    }

    public void setTicket(int ticket) {
        this.ticket = ticket;
    }

    @Basic
    @Column(name = "dureeEstimee", nullable = true)
    public Integer getDureeEstimee() {
        return dureeEstimee;
    }

    public void setDureeEstimee(Integer dureeEstimee) {
        this.dureeEstimee = dureeEstimee;
    }

    @Basic
    @Column(name = "dureeReelle", nullable = true)
    public Integer getDureeReelle() {
        return dureeReelle;
    }

    public void setDureeReelle(Integer dureeReelle) {
        this.dureeReelle = dureeReelle;
    }

    @Basic
    @Column(name = "objet", nullable = false, length = 255)
    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    @Basic
    @Column(name = "description", nullable = false, length = 2000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TacheEntity that = (TacheEntity) o;
        return id == that.id &&
                technicien == that.technicien &&
                ticket == that.ticket &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin) &&
                Objects.equals(statut, that.statut) &&
                Objects.equals(dureeEstimee, that.dureeEstimee) &&
                Objects.equals(dureeReelle, that.dureeReelle) &&
                Objects.equals(objet, that.objet) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, debut, fin, statut, technicien, ticket, dureeEstimee, dureeReelle, objet, description);
    }
}
