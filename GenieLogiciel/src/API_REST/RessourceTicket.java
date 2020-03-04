package API_REST;

import DataBase.*;
import Modele.*;
import Modele.Client.ClientSite;
import Modele.Staff.Token;
import Modele.Ticket.InitTicket;
import Modele.Ticket.SendTache;
import Modele.Ticket.Tache;
import Modele.Ticket.Ticket;
import com.google.gson.Gson;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("JpaQlInspection") //Enleve les erreurs pour les requetes SQL elles peuvent etre juste
@Path("/ticket")
public class RessourceTicket {

    public Gson gson = new Gson();

    @Path("/init")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getInit(String jsonStr) {
        String token = "";
        long IdClient = 0;
        int IdTicket = -1;
        try {
            //Recuperation du JSON et parsing
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            IdClient = Long.parseLong((String) json.get("clientId"));
            try{IdTicket = Integer.parseInt (((Long) json.get("ticketId")).toString());} catch(NullPointerException ignored){}
            token = (String) json.get("token");
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (clientId, token, 'optionnel ticketId')", false, null, null);
        }

        //Verification du token
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        //Init des objets
        ArrayList<Object> listInfos = new ArrayList<>();
        InitTicket answer = new InitTicket();
        Transaction tx = null;

        try(Session session = CreateSession.getSession()) {
            //Recuperation des types des demandes
            tx = session.beginTransaction();
            List result = session.createQuery("FROM TypeDemandesEntity td WHERE td.actif = 1").list();
            for(Object o : result) {
                TypeDemandesEntity typeDemande = (TypeDemandesEntity) o;
                answer.demandeTypeList.add(typeDemande.getIdTypeDemandes());
            }

            //Recuperation de la liste des techniciens
            result = session.createQuery("FROM StaffEntity s WHERE s.actif = 1").list();
            for(Object o : result) {
                StaffEntity technicienEntity = (StaffEntity) o;
                answer.technicienList.add(new Personne(technicienEntity.getNom(), technicienEntity.getPrenom(), technicienEntity.getId()));
            }

            List siretList = null;
            try{
                siretList = session.createQuery("SELECT ss.siret FROM JonctionSirensiretEntity ss WHERE ss.siren = " + IdClient + " and ss.actif = 1").list();
                //if(siretList == null || siretList.isEmpty())
                //    return ReponseType.getNOTOK("L'id du client n'existe pas", true, tx, session);
            } catch (NoResultException ignored) {}//return ReponseType.getNOTOK("L'id du client n'existe pas", true, tx, session);}

            ClientEntity client;
            try{client = (ClientEntity) session.createQuery("FROM ClientEntity c WHERE c.siren = " + IdClient + " and c.actif = 1").getSingleResult();}
            catch(NoResultException e) {return ReponseType.getNOTOK("Le client avec le siren " + IdClient + " n'existe pas", true, tx, session);}
            answer.clientName = client.getNom();

            for(Object obj : siretList) {
                long siret = (long) obj;

                //Recuperation de la liste des demandeurs
                result = session.createQuery("FROM DemandeurEntity d WHERE d.siret = " + siret + " and d.actif = 1" ).list();
                for(Object o : result) {
                    DemandeurEntity demandeurEntity = (DemandeurEntity) o;
                    answer.demandeurList.add(new Personne(demandeurEntity.getNom(), demandeurEntity.getPrenom(), demandeurEntity.getIdPersonne()));

                    //Recuperation de la liste des sites du client
                    AdresseEntity adr = (AdresseEntity) session.createQuery("FROM AdresseEntity a WHERE a.idAdresse = " + demandeurEntity.getAdresse() + " and a.actif = 1").getSingleResult();
                    ClientSite adresseClient = new ClientSite(demandeurEntity.getSiret(), new Adresse(adr.getNumero(), adr.getCodePostal(), adr.getRue(), adr.getVille()), adr.getIdAdresse());
                    answer.clientSiteList.add(adresseClient);
                }
            }

            Adresse adresse = new Adresse();
            if(adresse.recupererAdresse(client.getAdresse()))
                answer.clientSiteList.add(new ClientSite(-1, adresse, client.getAdresse()));

            //Recuperation de la liste des categories
            result = session.createQuery("FROM CategorieEntity c WHERE c.actif = 1").list();
            for(Object o : result) {
                CategorieEntity categorie = (CategorieEntity) o;
                answer.categorieList.add(categorie.getCategorie());
            }

            //Recuperation de la liste des statut
            result = session.createQuery("FROM StatutTicketEntity s WHERE s.actif = 1").list();
            for (Object o : result) {
                StatutTicketEntity statut = (StatutTicketEntity) o;
                answer.statusList.add(statut.getIdStatusTicket());
            }

            // Recuperation de la liste des competences
            result = session.createQuery("FROM CompetencesEntity c WHERE c.actif = 1").list();
            for (Object o : result) {
                CompetencesEntity competence = (CompetencesEntity) o;
                answer.skillsList.add(competence.getCompetence());
            }

            //Recuperation de la liste des priorites
            result = session.createQuery("SELECT DISTINCT t.priorite FROM TicketEntity t").list();
            for(Object o : result) {
                Byte priority = (Byte) o;
                answer.priorityList.add(priority.intValue());
            }

            tx.commit();
            session.clear();

            //Ajout du ticket si son id est present
            if(IdTicket != -1) {
                answer.ticket = recuperationTicket(session, IdTicket);
                if(answer.ticket == null)
                    return ReponseType.getNOTOK("Le ticket avec l'id " + IdTicket + " n'existe pas", true, null, session);
                int pourcentage = 0;

                List res = session.createQuery("SELECT t FROM TacheEntity t, TicketJonctionEntity j WHERE j.idParent = " + answer.ticket.id + " and t.statut != 'Non resolu'").list();
                    for (Object o : res) {
                        TacheEntity tache = (TacheEntity) o;
                        //Ajout du pourcentage
                        pourcentage += (tache.getStatut().equals("Resolu")) ? 100 : 0;
                        if(res.size() != 0)
                            pourcentage /= res.size();
                        answer.ticket.pourcentage = pourcentage;
                    }
                }
            session.close();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK(gson.toJson(answer));
    }

    @Path("/create")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response postCreation(String jsonStr) {
        String token = "", ticketJson = "";
        JSONObject ticketJsonObject = null;
        HashMap<String, Integer> competencesTicket = new HashMap<>();
        int ticketParent = 1, maxID = -1;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String)json.get("token");
            ticketJsonObject = (JSONObject) json.get("ticket");
            ticketJson = ticketJsonObject.toJSONString();
            try{ticketParent = Integer.parseInt(((Long)json.get("ticketParent")).toString());} catch(NullPointerException ignored){}
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, ticket, 'optionnel ticketParent')", false,null, null);
        }
        //Verification du token
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        ArrayList<Object> list = new ArrayList<>();
        Transaction tx = null;
        TicketEntity ticketEntity = new TicketEntity();
        Ticket ticket = createTicketFromJson(ticketJson);

        if(ticket == null)
            return ReponseType.getNOTOK("Le ticket n'est pas present dans la requete ou est mal rempli", false, null, null);

        ticketEntity.setCategorie(ticket.categorie.replace("'", "''"));
        ticketEntity.setDate(Timestamp.from(Instant.now()));
        ticketEntity.setDescription(ticket.description.replace("'", "''"));
        ticketEntity.setObjet(ticket.objet.replace("'", "''"));
        ticketEntity.setStatut(ticket.statut.replace("'", "''"));
        ticketEntity.setType(ticket.type.replace("'", "''"));
        ticketEntity.setTicket(ticketParent);

        try(Session session = CreateSession.getSession()) {
            //Ajout de l'adresse (SIRET) des ID du demandeur et du technicien
            tx = session.beginTransaction();
            DemandeurEntity demandeur;
            try{demandeur = (DemandeurEntity) session.createQuery("FROM DemandeurEntity p WHERE p.id = " + ticket.demandeur.id + " and p.actif = 1").getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("Le demandeur avec l'id " + ticket.demandeur.id + " n'existe pas", true, tx, session);}

            ClientEntity clientEntity;
            try{clientEntity = (ClientEntity) session.createQuery("SELECT c FROM ClientEntity c, JonctionSirensiretEntity ss WHERE c.siren = ss.siren and ss.siret = " + demandeur.getSiret() + " and c.actif = 1 and ss.actif = 1").getSingleResult();}
            catch(NoResultException e) {return ReponseType.getNOTOK("Le client lie au demandeur avec l'id " + ticket.demandeur.id + " n'existe pas", true, tx, session);}

            ticketEntity.setSiren(clientEntity.getSiren());
            ticketEntity.setAdresse(clientEntity.getAdresse());
            ticketEntity.setDemandeur(demandeur.getIdPersonne());

            StaffEntity tech;
            try{
                tech = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + ticket.technicien.id + " and s.actif = 1").getSingleResult();
                ticketEntity.setTechnicien(tech.getId());
            }
            catch(NoResultException | NullPointerException e) {
                ticketEntity.setTechnicien(null);
            }


            maxID = (int) session.createQuery("SELECT MAX(t.id) FROM TicketEntity t").getSingleResult()+1;
            ticketEntity.setId(maxID); //Rajout de l'increment

            //Ajout en base de donnee du ticket
            session.save(ticketEntity);

            //Rajout des competences
            if(!ticket.competences.isEmpty()) {
                for (String competence : ticket.competences) {
                    int idCompetence;
                    try {
                        idCompetence = (int) session.createQuery("SELECT c.idCompetences FROM CompetencesEntity c WHERE c.competence = '" + competence + "' and c.actif = 1").getSingleResult();
                    } catch (NoResultException e) {
                        return ReponseType.getNOTOK("La competence " + competence + " n'existe pas", true, tx, session);
                    }
                    JonctionTicketCompetenceEntity jct = new JonctionTicketCompetenceEntity();
                    jct.setIdTicket(maxID);
                    jct.setCompetence(idCompetence);
                    jct.setActif(1);
                    jct.setDebut(Timestamp.from(Instant.now()));
                    session.save(jct);
                    tx.commit();
                    session.clear();
                    tx = session.beginTransaction();
                }
            }

            List result = session.createQuery("SELECT c FROM CompetencesEntity c, JonctionTicketCompetenceEntity j WHERE j.actif = 1 and c.actif = 1 and j.competence = c.idCompetences and j.idTicket = " + maxID).list();

            for(Object o : result) {
                CompetencesEntity competencesEntity = (CompetencesEntity) o;
                competencesTicket.put(competencesEntity.getCompetence(), competencesEntity.getIdCompetences());
            }

            ArrayList<Tache> taches;

            if(ticketJsonObject.get("taches") != null) {
                taches = Tache.RecupererListeTacheDepuisJson((ArrayList<JSONObject>) ticketJsonObject.get("taches"), maxID);
                if (taches == null)
                    return ReponseType.getNOTOK("Les taches sont mal formees", true, tx, session);
                ticket.taches = taches;
                ticket.id = maxID;
            }

            tx.commit();
            session.clear();
            session.close();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return ReponseType.getNOTOK("Erreur lors de la sauvegarde du ticket", false, null, null);
        }

            if(!ticket.taches.isEmpty()) {
                for(Tache tache : ticket.taches) {
                    Response resp = CreateTaskWithTicket(token, tache);
                    if (resp != null) return resp;
                    chekCompetenceForTicket(competencesTicket, tache.competences, maxID);
                }
            }

        list.add(ticket);
        System.err.println("ticket rajouté sur la liste");
        list.add(null);
        list.add(null);
        list.add(ticketEntity);
        System.err.println("tout est bon");

        System.err.println("ticket = " + list.get(0).toString() + " entity = " + list.get(3).toString());

        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .allow("OPTIONS")
                .entity(gson.toJson(list))
                .build();
    }


    @Path("/modify")
    @POST
    @Consumes("text/plain")
    public Response postModify(String jsonStr) {
        String token = "", ticketJson = "";
        long IdClient = 0;
        HashMap<String, Integer> competences = new HashMap<>();
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            IdClient = (Long) json.get("clientId");
            token = (String)json.get("token");
            ticketJson = ((JSONObject)json.get("ticket")).toJSONString();
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            System.err.println("impossible de parse");
            return ReponseType.getNOTOK("L'un des parametres n'est pas present (clientId, token, ticket)", false, null, null);
        }

        //Verification du token
        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        Transaction tx = null;
        Ticket ticket = createTicketFromJson(ticketJson);
        if(ticket == null)
            return ReponseType.getNOTOK("Le ticket n'est pas present dans la requete ou est mal rempli", false, null, null);

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            //Recuperation du tech
            StaffEntity tech;
            try{
                tech = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + ticket.technicien.id + " and s.actif = 1").getSingleResult();
            }
            catch(NoResultException | NullPointerException e) {
                tech = null;
            }

            //Recuperation du demandeur
            DemandeurEntity demandeur;
            try{demandeur = (DemandeurEntity) session.createQuery("FROM DemandeurEntity p WHERE p.id = " + ticket.demandeur.id + " and p.actif = 1").getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("Le demandeur n'existe pas", true, tx, session);}

            //Recuperation du client
            ClientEntity client = null;
            String SIRET = Long.toString(demandeur.getSiret());
            //todo verifier si le demandeur fait parti du client
            //if(!SIRET.substring(0,9).equals(Long.toString(IdClient)))
            //    return ReponseType.getNOTOK("Le client : " + IdClient + " n'a pas le demandeur : " + SIRET + " veuillez verifier", true, tx, session);

            try{client = (ClientEntity) session.createQuery("FROM ClientEntity c WHERE c.siren = " + IdClient + "and c.actif = 1").getSingleResult();}
            catch(NoResultException e) {return ReponseType.getNOTOK("Le client n'existe pas", true, tx, session);}

            try{session.createQuery("FROM TicketEntity t WHERE t.id = " + ticket.id + " and t.statut != 'Non resolu' and t.statut != 'Resolu'").getSingleResult();}
            catch (NoResultException e) {return ReponseType.getNOTOK("Le ticket n'existe pas ou est déja fermé", true, tx, session);}

            tx.commit();
            session.clear();

            //Execution de la commande update
            tx =  session.beginTransaction();

            TicketEntity ticketEntity;
            try{ticketEntity = (TicketEntity) session.createQuery("FROM TicketEntity t WHERE t.id = " + ticket.id + " and t.statut != 'Resolu' and t.statut != 'Non resolu'").getSingleResult();}
            catch (NoResultException e) {return  ReponseType.getNOTOK("Le ticket n'existe pas", true, tx, session);}

            if(tech == null)
                ticketEntity.setTechnicien(null);
            else
                ticketEntity.setTechnicien(tech.getId());

            ticketEntity.setObjet(ticket.objet.replace("'", "''"));
            ticketEntity.setCategorie(ticket.categorie.replace("'", "''"));
            ticketEntity.setDescription(ticket.description.replace("'", "''"));
            ticketEntity.setStatut(ticket.statut.replace("'", "''"));
            ticketEntity.setType(ticket.type.replace("'", "''"));
            ticketEntity.setDemandeur(demandeur.getIdPersonne());
            ticketEntity.setPriorite((byte) ticket.priorite);
            ticketEntity.setAdresse(client.getAdresse());
            ticketEntity.setSiren(client.getSiren());

            session.save(ticketEntity);
            tx.commit();
            session.clear();
            tx = session.beginTransaction();
            //todo faire en sorte de pouvoir supprimer des competences
            //todo accorder les competences des taches avec le ticket

            if(!ticket.competences.isEmpty() && (ticket.taches == null || ticket.taches.isEmpty())) {
                for (String competence : ticket.competences) {
                    int idCompetence;
                    try {
                        idCompetence = (int) session.createQuery("SELECT c.idCompetences FROM CompetencesEntity c WHERE c.competence = '" + competence + "' and c.actif = 1").getSingleResult();
                    } catch (NoResultException e) {
                        return ReponseType.getNOTOK("La competence " + competence + " n'existe pas", true, tx, session);
                    }
                    try{session.createQuery("FROM JonctionTicketCompetenceEntity j WHERE j.actif = 1 and j.idTicket = " + ticket.id + " and j.competence = " + idCompetence).getSingleResult();}
                    catch (NoResultException e) {
                        JonctionTicketCompetenceEntity jct = new JonctionTicketCompetenceEntity();
                        jct.setIdTicket(ticket.id);
                        jct.setCompetence(idCompetence);
                        jct.setActif(1);
                        jct.setDebut(Timestamp.from(Instant.now()));
                        session.saveOrUpdate(jct);
                    }
                }
            }
            tx.commit();
            session.clear();
            tx = session.beginTransaction();
            String request = "SELECT c FROM JonctionTicketCompetenceEntity j, CompetencesEntity c WHERE c.idCompetences = j.competence and j.idTicket = " + ticket.id + " and j.actif = 1 and c.actif = 1";
            List result = session.createQuery(request).list();

            for(Object o : result) {
                CompetencesEntity competencesEntity = (CompetencesEntity)o;
                competences.put(competencesEntity.getCompetence(), competencesEntity.getIdCompetences());
            }
            tx.commit();
            session.clear();
            session.close();
        }catch (HibernateException e){
            //if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        ArrayList<String> competencesTaches = new ArrayList<>();
        HashMap<Integer, Tache> map = new HashMap<>();
        int pourcentage = 0;

        if(ticket.taches != null && !ticket.taches.isEmpty()) {

            ArrayList<Tache> taches = Tache.getListTaskFromTicket(ticket.id);
            if(taches != null) {
                for(Tache tache : taches) {
                    map.put(tache.id, tache);
                    System.err.println("id in map = " + tache.id);
                    //Ajout du pourcentage
                    System.err.println("statut tache = " + tache.statut + " id = " + tache.id);
                    pourcentage += (tache.statut == "Resolu") ? 100 : 0;
                    pourcentage /= taches.size();
                    ticket.pourcentage = pourcentage;
                }

                for(Tache tache : ticket.taches) {
                    //Si la tache existe deja on la met a jour soit pour modifier soit pour supprimer via la mise a jour du statut
                    System.err.println("id de la tache = " + tache.id);
                    if(map.containsKey(tache.id)) {
                        SendTache myTask = new SendTache(token, tache);
                        String str = gson.toJson(myTask);
                        System.err.println("json final = " + str + "------------------------------------------------------");
                        Response resp = RessourceTache.modifyTask(str);
                        if(resp.getStatus() != 200)
                            return resp;
                        map.remove(tache.id);
                    }
                    //Sinon on la creer
                    else {
                        if(!tache.statut.equals("Resolu") || !tache.equals("Non resolu")) {
                            Response resp = CreateTaskWithTicket(token, tache);
                            if (resp != null) return resp;
                        }
                    }
                    competencesTaches.addAll(tache.competences);
                }
                //suppression des taches qui etaitent la auparavant
                Response resp = deleteTaskForTicket(token, ticket, map, competences, competencesTaches);
                if (resp != null) return resp;
            }
            //Mise a jour des competences du ticket en fonction de celles des taches
            chekCompetenceForTicket(competences, competencesTaches, ticket.id);
        }
        //suppression des taches associees
        else {
            ArrayList<Tache> taches = Tache.getListTaskFromTicket(ticket.id);
            if (taches != null) {
                for (Tache tache : taches) {
                    map.put(tache.id, tache);
                    System.err.println("id in map = " + tache.id);
                }

                Response resp = deleteTaskForTicket(token, ticket, map, competences, competencesTaches);
                if (resp != null) return resp;
            }
        }
        return ReponseType.getOK(pourcentage);
    }

    //todo possible seulemnet si aucune tache
    @Path("/state")
    @POST
    @Consumes("text/plain")
    public Response changeState(String jsonStr) {
        Transaction tx = null;
        int ticketId = -1;
        String token = "", statut = "";
        try (Session session = CreateSession.getSession()){
            try {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(jsonStr);
                ticketId = Integer.parseInt((String) json.get("ticketId"));
                token = (String) json.get("token");
                statut = (String) json.get("statut");
                if(Security.test(statut) == null)
                    return ReponseType.getNOTOK("Le statut contient des requetes SQL veuillez corriger", false, null, null);
            } catch (ParseException | NullPointerException e) {
                e.printStackTrace();
                return  ReponseType.getNOTOK("Il manque des parametres (ticketId, token, statut)", false, null, null);
            }

            if(!Token.tryToken(token))
                return Token.tokenNonValide();

            tx = session.beginTransaction();
            System.err.println("ticket id = " + ticketId);
            int id = -1;
            id = (int) session.createQuery("SELECT t.id FROM TicketEntity t WHERE t.id = " + ticketId).getSingleResult();
            tx.commit();
            session.clear();
            if(id == -1)
                return ReponseType.getNOTOK("Le ticketId n'existe pas", true, null, session);

            try {
                tx = session.beginTransaction();
                Query update = session.createQuery("UPDATE TicketEntity T set T.statut = '" + statut.replace("'", "''") + "' WHERE T.id = " + ticketId);
                int nbLignes = update.executeUpdate();
                if (nbLignes != 1)
                    return ReponseType.getNOTOK("Erreur lors de la requete en base de donnees", true, tx, session);
                tx.commit();
                session.clear();
                session.close();
            } catch(Exception e) { if(tx != null) tx.rollback(); return ReponseType.getNOTOK("Le statut " + statut + " n'existe pas", true, tx, session);}
        } catch (HibernateException e){
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK("");
    }

    @Path("/list")
    @POST
    @Consumes("text/plain")
    @Produces("application/json")
    public Response getList(String jsonStr) {
        Transaction tx = null;
        ArrayList<Ticket> tickets = new ArrayList<>();
        String token = "";
        int techId = -1;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonStr);
            token = (String)json.get("token");
            try {techId = Integer.parseInt(((Long)json.get("userId")).toString());} catch(Exception ignored){}
        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
            return ReponseType.getNOTOK("Il manque des parametres (token, 'optionnel userId')", false, null, null);
        }

        if(!Token.tryToken(token))
            return Token.tokenNonValide();

        //S'il n'y a pas de technicien en parametre
        try (Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();
            if(techId == -1) {
                List result = session.createQuery("SELECT t.id FROM TicketEntity t WHERE t.statut != 'Non resolu' and t.statut != 'Resolu'").list();
                tx.commit();
                session.clear();
                for (Object o : result) {
                    Ticket ticket = recuperationTicket(session, (int) o);
                    if(ticket != null)
                        tickets.add(ticket);
                }
            }
            else {
                List result = session.createQuery("SELECT t.id FROM TicketEntity t WHERE t.technicien = " + techId + "and t.statut != 'Non resolu' and t.statut != 'Resolu'").list();
                tx.commit();
                session.clear();
                for(Object o : result) {
                    Ticket ticket = recuperationTicket(session, (int) o);
                    if (ticket != null)
                        tickets.add(ticket);
                }
            }
            session.close();
        } catch (HibernateException e) {
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
        }
        return ReponseType.getOK(gson.toJson(tickets));
    }

    private Ticket recuperationTicket(Session session, int IdTicket) {
        Ticket ticket = null;
        Transaction tx = session.beginTransaction();
        List result = session.createQuery("FROM TicketEntity t WHERE t.id = " + IdTicket + " and t.statut != 'Non resolu' and t.statut != 'Resolu'").list();
        TicketEntity ticketEntity;
        if(result.size() == 1) {
            ticketEntity = (TicketEntity) result.get(0);

            //Recuperation du nom et prenom du demandeur et du technicien
            DemandeurEntity demandeurEntity;
            StaffEntity technicienEntity;

            try {
                demandeurEntity = (DemandeurEntity) session.createQuery("FROM DemandeurEntity p WHERE p.idPersonne = " + ticketEntity.getDemandeur() + " and p.actif = 1").getSingleResult();
                try{technicienEntity = (StaffEntity) session.createQuery("FROM StaffEntity s WHERE s.id = " + ticketEntity.getTechnicien() + " and s.actif = 1").getSingleResult();}
                catch (NoResultException e) {
                    technicienEntity = null;
                }
            }
            catch (NoResultException e) {
                tx.commit();
                session.clear();
                return null;
            }

            Personne demandeur, technicien = null;
            demandeur = new Personne(demandeurEntity.getNom(), demandeurEntity.getPrenom(), demandeurEntity.getIdPersonne());
            try{technicien = new Personne(technicienEntity.getNom(), technicienEntity.getPrenom(), technicienEntity.getId());}
            catch (NullPointerException ignored) {}

            //Recuperation du nom de l'entreprise du client
            int siren = ticketEntity.getSiren();
            ClientEntity client = (ClientEntity) session.createQuery("FROM ClientEntity c WHERE c.siren = " + siren + " and c.actif = 1").getSingleResult();

            //Recuperation de l'adresse
            AdresseEntity adresse = (AdresseEntity) session.createQuery("FROM AdresseEntity a WHERE a.id = " + client.getAdresse() + " and a.actif = 1").getSingleResult();

            //Recuperation des taches associees au tickets s'il y en a

            tx.commit();
            session.clear();
            tx = session.beginTransaction();

            //Recuperation des competences
            ArrayList<String> competences = new ArrayList<>();
            String request = "SELECT c.competence FROM CompetencesEntity c, JonctionTicketCompetenceEntity jtc WHERE jtc.idTicket = " + IdTicket + " and jtc.competence = c.idCompetences and jtc.actif = 1 and c.actif = 1";
            result = session.createQuery(request).list();
            for(Object o : result) {
                String competence = (String) o;
                competences.add(competence);
            }

            Adresse adresseClient = new Adresse(adresse.getNumero(), adresse.getCodePostal(), adresse.getRue(), adresse.getVille());
            ticket = new Ticket(ticketEntity.getType(), ticketEntity.getObjet(), ticketEntity.getDescription(), ticketEntity.getCategorie(),
                    ticketEntity.getStatut(), technicien, demandeur, competences, adresseClient, ticketEntity.getId(), ticketEntity.getPriorite(), client.getNom(), Tache.getListTaskFromTicket(IdTicket));
        }
        tx.commit();
        session.clear();
        return ticket;
    }

    //todo Gerer la securite des string que je reçoit avec le test d'une fonction sur select, from, where, delete, update, insert
    private Ticket createTicketFromJson(String jsonStr) {
        Personne technicien = null;
        String description;
        JSONObject json = null;
        try {
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(jsonStr);
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du parsing de l'objet");
        }
        try {
            //todo mettre des tests sur null pour etre sur que les chanps soient presents de plus verifier qu'ils sont bien remplis avec == ""
            int priorite  = -1;
            priorite = Integer.parseInt(((Long) json.get("priorite")).toString());
            if(priorite == -1)
                return null;
            JSONObject demandeurJson = (JSONObject) json.get("demandeur");
            Personne demandeur = new Personne();
            demandeur = demandeur.RecupererPersonDepuisJson(demandeurJson);
            if(demandeur == null)
                return null;

            String objet = Security.test((String) json.get("objet"));
            if (json.get("description") != null)
                description = Security.test((String) json.get("description"));
            else
                description = "";

            ArrayList<String> competences = Security.testArray((ArrayList<String>) json.get("competences"));
            String categorie = Security.test((String) json.get("categorie"));
            String type = Security.test((String) json.get("type"));
            String statut = Security.test((String) json.get("statut"));
            //todo continuer
            if (json.get("technicien") != null) {
                JSONObject technicienJSON = (JSONObject) json.get("technicien");
                technicien = new Personne((String) technicienJSON.get("nom"), (String) technicienJSON.get("prenom"), Integer.parseInt(((Long) technicienJSON.get("id")).toString()));
            }

            JSONObject adresseJSON = (JSONObject) json.get("adresse");
            Adresse adresse = new Adresse(Integer.parseInt(((Long) adresseJSON.get("numero")).toString()), (String) adresseJSON.get("codePostal"), (String) adresseJSON.get("rue"), (String) adresseJSON.get("ville"));
            int id = -1;
            //Test avec null pointeur exception pour verifier que id existe dans si non nous sommes en creation
            try {id = Integer.parseInt(((Long) json.get("id")).toString());}
            catch (NullPointerException ignored) {
                System.err.println("pas d'id trouve sur le ticket");
            }

            ArrayList<Tache> taches = null;
            if(id != -1) {
                System.err.println("id ticket != -1 --------------------- id = " + id);
                if(json.get("taches") != null) {
                    taches = Tache.RecupererListeTacheDepuisJson((ArrayList<JSONObject>) json.get("taches"), id);
                    if (taches == null)
                        return null;
                }
            }
            else {
                taches = new ArrayList<>();
            }

            Ticket ticket = new Ticket(type, objet, description, categorie, statut, technicien, demandeur, competences, adresse, id, priorite, taches);
            return ticket;
        } catch (NullPointerException e) {
            return null;
        }
    }

    private Response CreateTaskWithTicket(String token, Tache tache) {
        SendTache myTask = new SendTache(token, tache);
        String str = gson.toJson(myTask);
        System.err.println("json final pour creation = " + str + "------------------------------------------------------");
        Response resp = RessourceTache.createTache(str);
        if(resp.getStatus() != 200) {
            return resp;
        }
        return null;
    }

    private boolean chekCompetenceForTicket(HashMap<String, Integer> competencesTicket, ArrayList<String> competencesTaches, int idTicket) {
        Transaction tx = null;

        try(Session session = CreateSession.getSession()) {
            tx = session.beginTransaction();

            //update des competences vers le ticket
            //Ajout
            for(String competence : competencesTaches) {
                if(!competencesTicket.containsKey(competence)) {
                    JonctionTicketCompetenceEntity j = new JonctionTicketCompetenceEntity();
                    int idCompetence = (int) session.createQuery("SELECT c.idCompetences FROM CompetencesEntity c WHERE c.competence = '" + competence + "' and c.actif = 1").getSingleResult();
                    try{session.createQuery("FROM JonctionTicketCompetenceEntity j WHERE j.actif = 1 and j.idTicket = " + idTicket + " and j.competence = " + idCompetence).getSingleResult();}
                    catch (NoResultException e) {
                        j.setCompetence(idCompetence);
                        j.setIdTicket(idTicket);
                        j.setActif(1);
                        j.setDebut(Timestamp.from(Instant.now()));

                        session.save(j);
                        tx.commit();
                        session.clear();
                        tx = session.beginTransaction();
                    }
                    competencesTicket.remove(competence);
                }
            }
            tx.commit();
            session.clear();
            tx = session.beginTransaction();
            //Suppression
            for(String competence : competencesTicket.keySet()) {
                if(!competencesTaches.contains(competence)) {
                    System.err.println("competence a supprimer = " + competence + " pour le ticket = " +idTicket);
                    String request = "SELECT j FROM JonctionTicketCompetenceEntity j, CompetencesEntity c WHERE j.competence = c.idCompetences and c.competence = '" + competence + "' and j.idTicket = " + idTicket + " and j.actif = 1 and c.actif = 1";
                    System.err.println("competence delete ");
                    JonctionTicketCompetenceEntity j;
                    try{
                        j = (JonctionTicketCompetenceEntity) session.createQuery(request).getSingleResult();
                        j.setActif(0);
                        j.setFin(Timestamp.from(Instant.now()));
                        session.update(j);
                        competencesTaches.remove(competence);
                    }
                    catch (NoResultException ignored) {}
                }
            }
            tx.commit();
            session.clear();
            session.close();
        }
        catch (HibernateException e){
            if(tx != null)
                tx.rollback();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Response deleteTaskForTicket(String token, Ticket ticket, HashMap<Integer, Tache> map, HashMap<String, Integer> competences, ArrayList<String> competencesTaches) {
        Transaction tx;
        System.err.println("in delete task --------------");
        if(!map.isEmpty()) {
            System.err.println("begin");
            for(int id : map.keySet()) {
                System.err.println("la tache supprimer de la liste est " + id);
                SendTache myTask = new SendTache(token, map.get(id));
                Response resp = RessourceTache.deleteTask(gson.toJson(myTask));
                if(resp.getStatus() != 200)
                    return resp;

                try(Session session = CreateSession.getSession()) {
                    tx = session.beginTransaction();
                    TicketJonctionEntity j = (TicketJonctionEntity) session.createQuery("FROM TicketJonctionEntity t WHERE t.idParent = " + ticket.id + " and t.actif = 1 and t.idEnfant = " + id).getSingleResult();
                    j.setActif(0);
                    j.setFin(Timestamp.from(Instant.now()));
                    tx.commit();
                    session.clear();
                    session.close();
                    System.err.println("task " + id+ " delete");
                }
                chekCompetenceForTicket(competences, competencesTaches, ticket.id);
            }
        }
        return null;
    }
}