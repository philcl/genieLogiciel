package DataBase;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "Staff", schema = "GenieLog", catalog = "")
public class StaffEntity {
    private int id;
    private String login;
    private byte[] mdp;
    private int adresse;
    private String telephone;
    private String mail;
    private String prenom;
    private String nom;
    private int actif;

    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "login")
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Basic
    @Column(name = "mdp")
    public byte[] getMdp() {
        return mdp;
    }

    public void setMdp(byte[] mdp) {
        this.mdp = mdp;
    }

    @Basic
    @Column(name = "adresse")
    public int getAdresse() {
        return adresse;
    }

    public void setAdresse(int adresse) {
        this.adresse = adresse;
    }

    @Basic
    @Column(name = "telephone")
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Basic
    @Column(name = "mail")
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Basic
    @Column(name = "prenom")
    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    @Basic
    @Column(name = "nom")
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Basic
    @Column(name = "actif")
    public int getActif() {
        return actif;
    }

    public void setActif(int actif) {
        this.actif = actif;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffEntity that = (StaffEntity) o;
        return id == that.id &&
                adresse == that.adresse &&
                actif == that.actif &&
                Objects.equals(login, that.login) &&
                Arrays.equals(mdp, that.mdp) &&
                Objects.equals(telephone, that.telephone) &&
                Objects.equals(mail, that.mail) &&
                Objects.equals(prenom, that.prenom) &&
                Objects.equals(nom, that.nom);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, login, adresse, telephone, mail, prenom, nom, actif);
        result = 31 * result + Arrays.hashCode(mdp);
        return result;
    }
}
