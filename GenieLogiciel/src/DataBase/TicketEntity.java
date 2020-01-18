package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "Ticket", schema = "GenieLog", catalog = "")
public class TicketEntity {
    private int id;
    private long adresse;
    private Integer technicien;
    private String categorie;
    private String statut;
    private String objet;
    private String description;
    private Timestamp date;
    private int demandeur;
    private String type;

    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "adresse")
    public long getAdresse() {
        return adresse;
    }

    public void setAdresse(long adresse) {
        this.adresse = adresse;
    }

    @Basic
    @Column(name = "technicien")
    public Integer getTechnicien() {
        return technicien;
    }

    public void setTechnicien(Integer technicien) {
        this.technicien = technicien;
    }

    @Basic
    @Column(name = "categorie")
    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    @Basic
    @Column(name = "statut")
    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Basic
    @Column(name = "objet")
    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "date")
    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Basic
    @Column(name = "demandeur")
    public int getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(int demandeur) {
        this.demandeur = demandeur;
    }

    @Basic
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketEntity that = (TicketEntity) o;
        return id == that.id &&
                adresse == that.adresse &&
                demandeur == that.demandeur &&
                Objects.equals(technicien, that.technicien) &&
                Objects.equals(categorie, that.categorie) &&
                Objects.equals(statut, that.statut) &&
                Objects.equals(objet, that.objet) &&
                Objects.equals(description, that.description) &&
                Objects.equals(date, that.date) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, adresse, technicien, categorie, statut, objet, description, date, demandeur, type);
    }
}
