package Modele.Client;

import java.util.ArrayList;

public class ClientInit {
    public Client client;
    public ArrayList<Demandeur> demandeurList;

    public ClientInit(){
        client = new Client();
        demandeurList = new ArrayList<>();
    }
}
