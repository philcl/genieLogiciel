package API_REST;

import DataBase.AdresseEntity;
import DataBase.ClientEntity;
import DataBase.DemandeurEntity;
import DataBase.JonctionSirensiretEntity;
import Modele.Client.*;
import Modele.Personne;
import Modele.Staff.Token;
import Modele.Ticket.SendTache;
import Modele.Ticket.Tache;
import com.google.gson.Gson;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("JpaQlInspection") //Enleve les erreurs pour les requetes SQL elles peuvent etre juste
@Path("/client")
public class RessourceClient {

    public Gson gson = new Gson();

    @Path("/getId")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getClientId(String jsonStr) {
        String token = "", clientName = "";
        int clientId = -1;
        Transaction tx = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            clientName = (String) json.get("clientName");
            if(Security.test(clientName) == null)
                return ReponseType.getNOTOK("Le clientName contient des commandes SQL veuillez corriger", false, null, null);
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, clientName)", false, null, null);
        }

        if (!Token.tryToken(token))
            return Token.tokenNonValide();

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            clientId = (int) session.createQuery("SELECT c.siren FROM ClientEntity c WHERE c.nom = '" + clientName.replace("'", "''") + "'").getSingleResult();
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
        } catch (NoResultException e) {
            return ReponseType.getNOTOK("Le client " + clientName + " n'existe pas", false, null, null);
        }

        return ReponseType.getOK(clientId);
    }

    @Path("/list")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getListDemandeur(String jsonStr) {
        ArrayList<ClientList> clientList = new ArrayList<>();
        Transaction tx = null;
        String token = "";
        long IdClient = 0;
        int IdTicket = 0;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (!Token.tryToken(token))
            return Token.tokenNonValide();

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            List clients = session.createQuery("FROM ClientEntity c WHERE c.actif = 1").list();
            for (Object o : clients) {
                ClientEntity client = (ClientEntity) o;
                ClientList myClient = new ClientList();
                myClient.name = client.getNom();
                myClient.SIREN = client.getSiren();

                AdresseEntity adresseEntity = (AdresseEntity) session.createQuery("SELECT t FROM AdresseEntity t WHERE t.idAdresse = " + client.getAdresse()).getSingleResult();

                myClient.adresse.numero = adresseEntity.getNumero();
                myClient.adresse.codePostal = adresseEntity.getCodePostal();
                myClient.adresse.rue = adresseEntity.getRue();
                myClient.adresse.ville = adresseEntity.getVille();

                Long nbTicketActif = 0L;
                try{nbTicketActif = (Long) session.createQuery("SELECT COUNT(t.id) FROM TicketEntity t WHERE  (t.statut = 'Resolu' or t.statut = 'Non Resolu')").getSingleResult();}
                catch(NoResultException ignored){}
                myClient.nbTicket = Integer.parseInt(nbTicketActif.toString());

                List demandeurs = session.createQuery("SELECT d FROM DemandeurEntity d, JonctionSirensiretEntity ss WHERE d.siret = ss.siret and ss.siren = " + myClient.SIREN + " and d.actif = 1").list();
                for (Object p : demandeurs)
                {
                    DemandeurEntity personneEntity = (DemandeurEntity) p;
                    Demandeur demandeur = new Demandeur();
                    demandeur.recupererDemandeur(personneEntity.getIdPersonne());

                    myClient.demandeurs.add(demandeur);
                }

                clientList.add(myClient);
            }
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK(clientList);
    }

    @Path("/create")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response createClient(String jsonStr) {
        String token = "";
        JSONObject clientJSON = null;
        Transaction tx = null;

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            clientJSON = (JSONObject) json.get("client");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, client)", false, null, null);
        }
        if (!Token.tryToken(token))
            return Token.tokenNonValide();

        Client client = getClientFromJSON(clientJSON);
        if (client == null)
            return ReponseType.getNOTOK("Le JSON du client est mal forme ou contient des requetes SQL veuillez verifier", false, null, null);

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setActif((byte) 1);
        clientEntity.setNom(client.nom);
        clientEntity.setSiren(client.SIREN);

        int adresseId = client.adresse.getId();
        if (adresseId == -1)
            if (client.adresse.addAdresse() == -1)
                return ReponseType.getNOTOK("Impossible de rajouter l'adresse sur la base", false, null, null);
        clientEntity.setAdresse(client.adresse.getId());

        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            try {
                session.createQuery("FROM ClientEntity c WHERE c.siren = " + client.SIREN).getSingleResult();
                return ReponseType.getNOTOK("Le SIREN " + client.SIREN + " existe deja veuillez le changer", true, tx, session);
            } catch (NoResultException ignored) {
            }

            try {
                session.createQuery("FROM ClientEntity c WHERE c.nom = '" + client.nom.replace("'", "''") + "'").getSingleResult();
                return ReponseType.getNOTOK("Le client " + client.nom + " existe deja veuillez le changer", true, tx, session);
            } catch (NoResultException ignored) {
            }

            try{
                session.createQuery("FROM ClientEntity c WHERE c.adresse = " + adresseId).getSingleResult();
                return ReponseType.getNOTOK("Impossible de créer le client avec l'adresse id = " + adresseId + " un client possede deja cette adresse", true, tx, session);
            }
            catch (NoResultException ignored) {}

            session.save(clientEntity);
            tx.commit();
            session.clear();

            ArrayList<JSONObject> demandeursJSON;
            try{
                demandeursJSON = (ArrayList<JSONObject>) clientJSON.get("demandeurs");
                ArrayList<Demandeur> demandeurs = Demandeur.recupererListDemandeurDepuisJSON(demandeursJSON, client.SIREN);
                if(demandeurs == null) {
                    tx = session.beginTransaction();
                    session.delete(clientEntity);
                    tx.commit();
                    session.clear();
                    session.close();
                    return ReponseType.getNOTOK("Les Demandeurs sont mal former", false, null, null);
                }
                client.demandeurs = demandeurs;
            }
            catch (NullPointerException ignored) {
                System.err.println("dans le null pointeur except");
            }

            System.err.println("client demandeurs = " + client.demandeurs.isEmpty());

            if(client.demandeurs != null && !client.demandeurs.isEmpty()) {
                for(Demandeur demandeur : client.demandeurs) {
                    System.err.println("--------------demandeur = " + demandeur.toString());
                    Response response = CreateDemandeurForClient(token, demandeur, client.SIREN);
                    if(response != null) {

                        return response;
                    }
                }
            }
            session.close();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
            return ReponseType.getNOTOK("Impossible de sauvegarder le client sur la base", false, null, null);
        }
        return ReponseType.getOK("");
    }

    @Path("/modify")
    @POST
    @Consumes("text/plain")
    public Response modifyClient(String jsonStr) {
        String token = "";
        JSONObject json, clientJson;
        Transaction tx = null;

        try{
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            clientJson = (JSONObject) json.get("client");
        } catch (ParseException |NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, client)", false, null, null);
        }

        Client client = getClientFromJSON(clientJson);
        if (client == null)
            return ReponseType.getNOTOK("Le JSON du client est mal forme ou contient des requetes SQL veuillez verifier", false, null, null);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            ClientEntity clientEntity;

            try{clientEntity = (ClientEntity) session.createQuery("FROM ClientEntity c WHERE c.siren = " + client.SIREN).getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("Le SIREN du client n'existe pas ", true, tx, session);}

            //Recherche puis ajout si elle n'existe pas
            int adrId = client.adresse.getId();
            if(adrId == -1)
                if(client.adresse.addAdresse() == -1)
                    return ReponseType.getNOTOK("Impossible d'ajouter l'adresse", true, tx, session);

            //Modification du client
            clientEntity.setAdresse(adrId);
            clientEntity.setNom(client.nom);

            session.update(clientEntity);

            if(client.demandeurs != null) {
                HashMap<Integer, Demandeur> map = new HashMap<>();
                ArrayList<Demandeur> demandeurs = Demandeur.getDemandeurFromClient(client.SIREN, session);
                if( demandeurs != null) {
                    for(Demandeur demandeur : demandeurs) {
                        map.put(demandeur.demandeur.id, demandeur);
                        System.err.println("id in map = " + demandeur.demandeur.id);
                    }

                    for(Demandeur demandeur : client.demandeurs) {
                        //Si le demandeur existe deja on le met a jour soit pour modifier soit pour supprimer via la mise a jour du statut
                        System.err.println("id du demandeur = " + demandeur.demandeur.id);
                        if(map.containsKey(demandeur.demandeur.id)) {
                            SendDemandeur myDemandeur = new SendDemandeur(token, demandeur, client.SIREN);
                            String str = gson.toJson(myDemandeur);
                            System.err.println("json final = " + str + "------------------------------------------------------");
                            Response resp = RessourceDemandeur.modifyDemandeurs(str);
                            if(resp.getStatus() != 200)
                                return resp;
                            map.remove(demandeur.demandeur.id);
                        }
                        //Sinon on le creer
                        else {
                            Response resp = CreateDemandeurForClient(token, demandeur, client.SIREN);
                            if (resp != null) return resp;
                        }
                    }
                    //suppression des demandeurs qui etaitent la auparavant
                    if(!map.isEmpty()) {
                        for(int id : map.keySet()) {
                            System.err.println("le demandeur supprimer de la liste est " + id);
                            SendDeleteDemandeur myDemandeur = new SendDeleteDemandeur(0, id, token);
                            Response resp = RessourceDemandeur.deleteDemandeurAPI(gson.toJson(myDemandeur));
                            if(resp.getStatus() != 200)
                                return resp;
                        }
                    }
                }
                else {
                    for(Demandeur demandeur : client.demandeurs) {
                        Response resp = CreateDemandeurForClient(token, demandeur, client.SIREN);
                        if (resp != null) return resp;
                    }
                }

            }

            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }

        return ReponseType.getOK("");
    }

    @Path("/init")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getInit(String jsonStr) {
        String token = "";
        int SIREN = -1;
        JSONObject json;
        Transaction tx = null;

        try{
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            SIREN = Integer.parseInt (((Long) json.get("SIREN")).toString());

        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des paramètres (token, SIREN)", false, null, null);
        }
        ClientInit clientInit = new ClientInit();

        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        if (SIREN == -1)
            return ReponseType.getNOTOK("Le SIREN ne convient pas", false, null, null);
        if(!clientInit.client.recupererClient(SIREN))
            return ReponseType.getNOTOK("Immposible de recuperer le client avec le SIREN " + SIREN, false, null, null);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            List result = session.createQuery("SELECT d FROM DemandeurEntity d, JonctionSirensiretEntity ss WHERE d.siret = ss.siret and ss.siren = " + SIREN +  " and d.actif = 1").list();

            HashMap<Integer, Long> map = new HashMap<>();
            for(Object o : result) {
                DemandeurEntity p = (DemandeurEntity) o;
                Demandeur demandeur = new Demandeur();
                if(!demandeur.recupererDemandeur(p.getIdPersonne()))
                    return ReponseType.getNOTOK("Impossible de lister les demandeurs du SIREN " + SIREN, true, tx, session);

                clientInit.demandeurList.add(demandeur);

                ClientSite clientSite = new ClientSite();
                clientSite.adresse.recupererAdresse(demandeur.idAdresse);
                clientSite.SIRET = demandeur.SIRET;
                clientSite.idAdresse = demandeur.idAdresse;

                //La map permet d'eviter les doublons
                if(!map.containsKey(demandeur.idAdresse))
                    clientInit.clientSiteList.add(clientSite);
                map.put(demandeur.idAdresse,demandeur.SIRET);
            }

            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK(clientInit);
    }

    @Path("/delete")
    @POST
    @Consumes("text/plain")
    public Response deleteClient(String jsonStr) {
        String token = "";
        int SIREN = -1;
        JSONObject json;
        Transaction tx = null;

        try{
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(jsonStr);
            token = (String) json.get("token");
            SIREN = Integer.parseInt(((Long) json.get("SIREN")).toString());
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, SIREN)", false, null, null);
        }
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        ClientEntity p;

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            try{p = (ClientEntity) session.createQuery("FROM ClientEntity c WHERE c.siren = " + SIREN).getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("Le client avec le siren : " + SIREN + " n'existe pas", true, tx, session);}

            List result = session.createQuery("SELECT j.siret FROM JonctionSirensiretEntity j WHERE j.siren = " + SIREN).list();

            for(Object o : result) {
                long SIRET = (long) o;

                List res = session.createQuery("FROM DemandeurEntity d WHERE d.siret = " + SIRET).list();
                for(Object obj : res) {
                    DemandeurEntity demandeurEntity = (DemandeurEntity) obj;
                    SendDeleteDemandeur delete = new SendDeleteDemandeur(0, demandeurEntity.getIdPersonne(), token);
                    Response resp = RessourceDemandeur.deleteDemandeurAPI(gson.toJson(delete));
                    if (resp.getStatus() != 200)
                        return resp;
                }
            }
            p.setActif((byte) 0);
            session.update(p);
            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }

        return ReponseType.getOK("");
    }

    private Client getClientFromJSON(JSONObject json) {
        Client client = new Client();

        try {
            client.nom = Security.test((String) json.get("nom"));
            client.SIREN = -1;
            client.SIREN = Integer.parseInt(((Long) json.get("SIREN")).toString());

            if (client.nom == null || client.SIREN == -1)
                return null;

            //Ajout de l'adresse
            JSONObject adresse = (JSONObject) json.get("adresse");
            if (adresse == null) {
                System.err.println("L'adresse est mal formee ou contient des requetes SQL");
                return null;
            } else if (!client.adresse.RecupererAdresseDepuisJson(adresse)) {
                System.err.println("impossible de recupérer l'adresse depuis le json " + adresse);
                return null;
            }

            ArrayList<JSONObject> demandeursJSON;
            try{
                demandeursJSON = (ArrayList<JSONObject>) json.get("demandeurs");
                System.err.println("-------------------------ok------------------- size" + demandeursJSON.size() + "--------");
                ArrayList<Demandeur> demandeurs = Demandeur.recupererListDemandeurDepuisJSON(demandeursJSON, client.SIREN);
                if(demandeurs != null)
                    client.demandeurs = demandeurs;
            }
            catch (NullPointerException ignored) {}

        } catch (NullPointerException e) {
            System.err.println("Erreur lors de la recuperation du client depuis un json parse impossible");
            return null;
        }
        return client;
    }

    private Response CreateDemandeurForClient(String token, Demandeur demandeur, int SIREN) {
        SendDemandeur myDemandeur = new SendDemandeur(token, demandeur, SIREN);
        String str = gson.toJson(myDemandeur);
        System.err.println("json final = " + str + "------------------------------------------------------");
        Response resp = RessourceDemandeur.createDemandeur(str);
        if(resp.getStatus() != 200) {
            return resp;
        }
        return null;
    }
}
