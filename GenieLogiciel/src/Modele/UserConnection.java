package Modele;

public class UserConnection {
    public int id;
    public String firstName, lastName, role, token;

    public UserConnection(int id, String token, String firstName, String lastName, String role) {
        this.id = id;
        this.role = role;
        this.lastName = lastName;
        this.firstName = firstName;
        this.token = token;
    }
}
