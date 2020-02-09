package Modele.Staff;

import Modele.Adresse;

import java.util.ArrayList;

public class Staff {
    public int staffId;
    public String staffSurname, staffName, staffTel, staffUserName, staffPassword, staffMail;
    public ArrayList<String> staffCompetency, staffRole;
    public Adresse staffAdress;

    public Staff() {
        staffCompetency = new ArrayList<>();
        staffRole = new ArrayList<>();
        staffAdress = new Adresse();
    }
}
