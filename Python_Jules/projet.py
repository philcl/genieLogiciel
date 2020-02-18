#Import des librairies

import numpy
import sklearn
import glob
import os
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA
import matplotlib.pyplot as plt
import pandas
import nltk

source = open("Exemple.csv","r")

sortie = "Exemple.http"

i = 0

temp = []

for ligne in source:
	if i == 1:
		temp3 = ligne.split(";")
		#print(temp3)
		#print(len(temp3))
		if len(temp3)==15:
			temp += [temp3]
	i = 1

#print(temp)

resultat = ""

for i in temp:
	resultat += "### Send POST to create Ticket\n"
	resultat += "POST http://localhost:8080/genielog/ticket/create \n"
	resultat += "Content-Type: text/plain\n"
	resultat += "\n"
	resultat += "{\"token\":\"96b29b22-cefb-4699-93b9-9fcc97aa003e\",\"ticket\":{\"adresse\":{\"codePostal\":\"" + i[0] + "\",\"numero\":" + i[1] + ",\"rue\":\"" + i[2] + "\",\"ville\":\"" + i[3] + "\"},\"categorie\":\"" + i[4] + "\",\"competences\":[],\"demandeur\":{\"id\":" + i[9] + ",\"nom\":\"" + i[10] + "\",\"prenom\":\"" + i[11] + "\"},\"objet\":\"" + i[5] + "\",\"statut\":\"" + i[6] + "\",\"technicien\":{\"id\":" + i[12] + ",\"nom\":\"" + i[13] + "\",\"prenom\":\"" + i[14] + "\"},\"type\":\"" + i[7] + "\",\"priorite\":" + i[8] + "}} \n"
	resultat += "###\n\n"

numpy.savetxt(sortie,[resultat],fmt='%s')

source.close()