package Modele;

import java.util.ArrayList;

public class Staff {
    int staffId;
    String staffSurname, staffName, staffTel, staffUserName, staffPassword;
    ArrayList<String> staffCompetency, staffRole;
    Adresse staffAdress;

    public Staff() {
        staffCompetency = new ArrayList<>();
        staffRole = new ArrayList<>();
        staffAdress = new Adresse();
    }
}
