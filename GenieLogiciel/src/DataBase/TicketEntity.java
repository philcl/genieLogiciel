package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "Ticket", schema = "GenieLog", catalog = "")
public class TicketEntity {
    private int id;
    private int siren;
    private int adresse;
    private Integer technicien;
    private String categorie;
    private String statut;
    private String objet;
    private String description;
    private Timestamp date;
    private int demandeur;
    private String type;
    private int ticket;
    private byte priorite;

    @Id
    @Column(name = "ID", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "SIREN", nullable = false)
    public int getSiren() {
        return siren;
    }

    public void setSiren(int siren) {
        this.siren = siren;
    }

    @Basic
    @Column(name = "adresse", nullable = false)
    public int getAdresse() {
        return adresse;
    }

    public void setAdresse(int adresse) {
        this.adresse = adresse;
    }

    @Basic
    @Column(name = "technicien", nullable = true)
    public Integer getTechnicien() {
        return technicien;
    }

    public void setTechnicien(Integer technicien) {
        this.technicien = technicien;
    }

    @Basic
    @Column(name = "categorie", nullable = false, length = 50)
    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
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

    @Basic
    @Column(name = "date", nullable = false)
    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Basic
    @Column(name = "demandeur", nullable = false)
    public int getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(int demandeur) {
        this.demandeur = demandeur;
    }

    @Basic
    @Column(name = "type", nullable = false, length = 100)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "Ticket", nullable = false)
    public int getTicket() {
        return ticket;
    }

    public void setTicket(int ticket) {
        this.ticket = ticket;
    }

    @Basic
    @Column(name = "priorite", nullable = false)
    public byte getPriorite() {
        return priorite;
    }

    public void setPriorite(byte priorite) {
        this.priorite = priorite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketEntity that = (TicketEntity) o;
        return id == that.id &&
                siren == that.siren &&
                adresse == that.adresse &&
                demandeur == that.demandeur &&
                ticket == that.ticket &&
                priorite == that.priorite &&
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
        return Objects.hash(id, siren, adresse, technicien, categorie, statut, objet, description, date, demandeur, type, ticket, priorite);
    }
}
