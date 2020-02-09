package Modele.Staff;

import java.util.ArrayList;

public class UserConnection {
    public int id;
    public String firstName, lastName, token;
    public ArrayList<String> role = new ArrayList<>();

    public UserConnection(){}

    public UserConnection(int id, String token, String firstName, String lastName) {
        this.id = id;
        this.token = token;
        this.lastName = lastName;
        this.firstName = firstName;
    }
}
