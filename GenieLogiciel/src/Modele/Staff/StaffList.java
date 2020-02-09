package Modele.Staff;

import Modele.Adresse;

import java.util.ArrayList;

public class StaffList {
    public int staffId;
    public String staffSurname, staffName, staffTel, staffMail;
    public ArrayList<String> staffRole;
    public Adresse staffAdress;

    public StaffList() {
        staffRole = new ArrayList<>();
        staffAdress = new Adresse();
    }
}
