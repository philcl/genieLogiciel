package DataBase;

import javax.persistence.*;
import java.sql.Timestamp;
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
    private String sexe;
    private Timestamp debut;
    private Timestamp fin;

    @Id
    @Column(name = "ID", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "login", nullable = false, length = 50)
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Basic
    @Column(name = "mdp", nullable = false)
    public byte[] getMdp() {
        return mdp;
    }

    public void setMdp(byte[] mdp) {
        this.mdp = mdp;
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
    @Column(name = "telephone", nullable = false, length = 10)
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Basic
    @Column(name = "mail", nullable = true, length = 50)
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Basic
    @Column(name = "prenom", nullable = false, length = 50)
    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    @Basic
    @Column(name = "nom", nullable = false, length = 100)
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Basic
    @Column(name = "actif", nullable = false)
    public int getActif() {
        return actif;
    }

    public void setActif(int actif) {
        this.actif = actif;
    }

    @Basic
    @Column(name = "sexe", nullable = false, length = 10)
    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    @Basic
    @Column(name = "debut", nullable = true)
    public Timestamp getDebut() {
        return debut;
    }

    public void setDebut(Timestamp debut) {
        this.debut = debut;
    }

    @Basic
    @Column(name = "fin", nullable = true)
    public Timestamp getFin() {
        return fin;
    }

    public void setFin(Timestamp fin) {
        this.fin = fin;
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
                Objects.equals(nom, that.nom) &&
                Objects.equals(sexe, that.sexe) &&
                Objects.equals(debut, that.debut) &&
                Objects.equals(fin, that.fin);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, login, adresse, telephone, mail, prenom, nom, actif, sexe, debut, fin);
        result = 31 * result + Arrays.hashCode(mdp);
        return result;
    }
}
