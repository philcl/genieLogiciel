#Import des librairies

import numpy
import sklearn
import glob
import os
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA
import matplotlib.pyplot as plt
import pandas
import fonctionConvertisseur as fctConv
import fonctionRetabli as fctRet
import fonctionFormatage as fctForm
import fonctionFinalize as fctFin
import nltk

source = open("Exemple.csv","r")

sortie = "Exemple.http"

i = 0

temp = []

for ligne in source:
	if i == 1:
		temp3 = [ligne.split()]
		if len(temp3)==15:
			temp += temp3
	i = 1

resultat = ""

for i in temp
	resultat += "### Send POST to create Ticket\n"
	resultat += "POST http://localhost:8080/genielog/ticket/create \n"
	resultat += "Content-Type: text/plain\n"
	resultat += "\n"
	resultat += "{\"token\":\"96b29b22-cefb-4699-93b9-9fcc97aa003e\",\"ticket\":{\"adresse\":{\"codePostal\":\"69000\",\"numero\":32,\"rue\":\"RUE ALAINE\",\"ville\":\"LYON\"},\"categorie\":\"Etude\",\"competences\":[],\"demandeur\":{\"id\":12,\"nom\":\"RENARD\",\"prenom\":\"Maxime\"},\"objet\":\"TESTEST\",\"statut\":\"Resolu\",\"technicien\":{\"id\":2,\"nom\":\"VITTONE\",\"prenom\":\"Jules\"},\"type\":\"Demande\",\"priorite\":1}} \n"
	resultat += "###"

numpy.savetxt(sortie,[resultat],fmt='%s')

source.close()