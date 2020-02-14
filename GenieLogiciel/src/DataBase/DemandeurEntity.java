package DataBase;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Demandeur", schema = "GenieLog", catalog = "")
public class DemandeurEntity {
    private int idPersonne;
    private String prenom;
    private String nom;
    private String mail;
    private byte actif;
    private long siret;
    private String telephone;
    private String sexe;

    @Id
    @Column(name = "idPersonne", nullable = false)
    public int getIdPersonne() {
        return idPersonne;
    }

    public void setIdPersonne(int idPersonne) {
        this.idPersonne = idPersonne;
    }

    @Basic
    @Column(name = "Prenom", nullable = false, length = 45)
    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    @Basic
    @Column(name = "Nom", nullable = false, length = 45)
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Basic
    @Column(name = "mail", nullable = true, length = 100)
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Basic
    @Column(name = "actif", nullable = false)
    public byte getActif() {
        return actif;
    }

    public void setActif(byte actif) {
        this.actif = actif;
    }

    @Basic
    @Column(name = "SIRET", nullable = false)
    public long getSiret() {
        return siret;
    }

    public void setSiret(long siret) {
        this.siret = siret;
    }

    @Basic
    @Column(name = "telephone", nullable = false, length = 20)
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Basic
    @Column(name = "sexe", nullable = false, length = 10)
    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DemandeurEntity that = (DemandeurEntity) o;
        return idPersonne == that.idPersonne &&
                actif == that.actif &&
                siret == that.siret &&
                Objects.equals(prenom, that.prenom) &&
                Objects.equals(nom, that.nom) &&
                Objects.equals(mail, that.mail) &&
                Objects.equals(telephone, that.telephone) &&
                Objects.equals(sexe, that.sexe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPersonne, prenom, nom, mail, actif, siret, telephone, sexe);
    }
}
