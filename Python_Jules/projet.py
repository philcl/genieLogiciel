#Import des librairies

print("Import")

import numpy

source = open("Exemple.csv","r")

sortie1 = "CreaDemandeur.http"
sortie2 = "CreaStaff.http"
sortie3 = "CreaTicket.http"

i = 0

print("Debut")

temp = []

for ligne in source:
	if i == 1:
		temp3 = ligne.split(";")
		#print(temp3)
		#print(len(temp3))
		if len(temp3)==15:
			temp += [temp3]
	i = 1

print("Etape 1")

resultat = ""

for i in temp:
	resultat += "### Send POST to change clientName to clientId\n"
	resultat += "POST http://localhost:8080/genielog/demandeur/create \n"
	resultat += "Content-Type: text/plain\n"
	resultat += "\n"
	resultat += "{\"token\":\"96b29b22-cefb-4699-93b9-9fcc97aa003e\", \"clientID\":12, \"demandeur\":{\"SIRET\":12345678912349, \"idAdresse\":12, \"demandeur\":{\"id\":" + i[9] + ",\"nom\":\"" + i[10] + "\",\"prenom\":\"" + i[11] + "\",\"sexe\":\"M\"}, \"telephone\":\"0645789878\"}} \n"
	resultat += "###\n\n"

numpy.savetxt(sortie1,[resultat],fmt='%s')

print("Etape 2")

resultat = ""

for i in temp:
	resultat += "### create User\n"
	resultat += "POST http://localhost:8080/genielog/user/create \n"
	resultat += "Content-Type: text/plain\n"
	resultat += "\n"
	resultat += "{\"token\":\"96b29b22-cefb-4699-93b9-9fcc97aa003e\",\"staff\":{\"staffId\":" + i[12] + ",\"staffUserName\":\"" + i[12] + "\",\"staffPassword\":\"1234\",\"staffMail\":\"test@test.fr\",\"staffAdress\":{\"numero\":12,\"rue\":\"avenue des champs\",\"ville\":\"PARIS\",\"codePostal\":\"75000\"},\"staffSurname\":\"" + i[13] + "\",\"staffName\":\"" + i[14] + "\",\"staffSexe\":\"M\",\"staffTel\":\"0504070105\",\"staffCompetency\":[\"\"],\"staffRole\":[\"Tech\"]}} \n"
	resultat += "###\n\n"

numpy.savetxt(sortie2,[resultat],fmt='%s')

print("Etape 3")

resultat = ""

for i in temp:
	resultat += "### Send POST to create Ticket\n"
	resultat += "POST http://localhost:8080/genielog/ticket/create \n"
	resultat += "Content-Type: text/plain\n"
	resultat += "\n"
	resultat += "{\"token\":\"96b29b22-cefb-4699-93b9-9fcc97aa003e\",\"ticket\":{\"adresse\":{\"codePostal\":\"" + i[0] + "\",\"numero\":" + i[1] + ",\"rue\":\"" + i[2] + "\",\"ville\":\"" + i[3] + "\"},\"categorie\":\"" + i[4] + "\",\"competences\":[],\"demandeur\":{\"id\":" + i[9] + ",\"nom\":\"" + i[10] + "\",\"prenom\":\"" + i[11] + "\"},\"objet\":\"" + i[5] + "\",\"statut\":\"" + i[6] + "\",\"technicien\":{\"id\":" + i[12] + ",\"nom\":\"" + i[13] + "\",\"prenom\":\"" + i[14] + "\"},\"type\":\"" + i[7] + "\",\"priorite\":" + i[8] + "}} \n"
	resultat += "###\n\n"

numpy.savetxt(sortie3,[resultat],fmt='%s')

source.close()

print("Fin")