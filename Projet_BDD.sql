Create Sequence idUsrSeq;
CREATE TABLE CompteUtilisateur (
idUsr INTEGER Default idUsrSeq.nextval,
PRIMARY KEY(idUsr)
);

CREATE TABLE Utilisateur (
emailUsr varchar(30) NOT NULL,
pwdUsr varchar(40) NOT NULL,
nomUsr varchar(20) NOT NULL,
prenomUsr varchar(30) NOT NULL,
adresseUsr varchar(40) NOT NULL,
idUsr INTEGER NOT NULL REFERENCES CompteUtilisateur(idUsr),
PRIMARY KEY(emailUsr)
);

CREATE TABLE Adherent(
idUsr INTEGER NOT NULL REFERENCES CompteUtilisateur(idUsr),
PRIMARY KEY(idUsr)
);

CREATE TABLE Refuge (
email varchar(40) NOT NULL,
nomRefuge varchar(30) NOT NULL,
secteurGeo varchar(30) NOT NULL,
dateOuverture date NOT NULL,
dateFermeture date NOT NULL,
nbPlacesRepas integer NOT NULL CHECK(nbPlacesRepas >= 0),
nbPlacesDormir integer NOT NULL CHECK(nbPlacesDormir >= 0),
texteRepresentatif varchar(450) NOT NULL,
typePaiement varchar(30) NOT NULL CONSTRAINT Paiement CHECK (typePaiement in('espece', 'cheque', 'carte-bleue')),
prixNuitee integer NOT NULL CHECK(prixNuitee >= 0),
PRIMARY KEY(email)
);
DROP TABLE Ref_NumTel;
CREATE TABLE Ref_NumTel (
numTel varchar(20) NOT NULL,
email varchar(40) NOT NULL REFERENCES Refuge(email),
PRIMARY KEY(numTel)
);

Create Sequence idResRefSeq;
CREATE TABLE ReservationRefuge (
idResRefuge INTEGER Default idResRefSeq.nextval,
dateResRefuge DATE NOT NULL,
heureResRefuge INTEGER NOT NULL CONSTRAINT heure CHECK (heureResRefuge >= 0),
nbNuitResRefuge INTEGER NOT NULL CONSTRAINT nbNuit CHECK (nbNuitResRefuge >= 0),
nbRepasResRefuge INTEGER NOT NULL CONSTRAINT nbRepas CHECK (nbRepasResRefuge >= 0),
prixResRefuge INTEGER NOT NULL CONSTRAINT prixResRef CHECK (prixResRefuge >=0),
email varchar(40) NOT NULL REFERENCES Refuge(email),
idUsr INTEGER NOT NULL REFERENCES CompteUtilisateur(idUsr),
PRIMARY KEY(idResRefuge)
);


CREATE TABLE Repas (
repas varchar(30) NOT NULL CONSTRAINT Repas CHECK (repas in('dejeuner', 'diner', 'souper', 'casse-croute')),
PRIMARY KEY(repas)
);

CREATE TABLE Propose (
email varchar(40) NOT NULL REFERENCES Refuge(email),
repas varchar(30) NOT NULL REFERENCES Repas(repas),
prix INTEGER NOT NULL CONSTRAINT Prix_repas CHECK (prix >= 0),
PRIMARY KEY(email,repas)
);


CREATE TABLE Formation (
annee integer NOT NULL CHECK(annee > 0),
rang integer NOT NULL CHECK(rang >= 0),
nomFormation varchar(30) NOT NULL,
dateDemarrage date NOT NULL,
dureeFormation integer NOT NULL CHECK(dureeFormation > 0),
nbPlacesFormation integer NOT NULL CHECK(nbPlacesFormation >= 0),
descriptionFormation varchar(450) NOT NULL,
prixFormation integer NOT NULL CHECK(prixFormation >= 0),
PRIMARY KEY(annee,rang)
);

Create Sequence idResFormationSeq;
CREATE TABLE ReservationFormation (
idReservationFormation INTEGER Default idResFormationSeq.nextval,
rangAttente INTEGER NOT NULL CONSTRAINT rangAtt CHECK (rangAttente >= 0),
annee INTEGER NOT NULL,
rang INTEGER NOT NULL,
idUsr INTEGER NOT NULL REFERENCES Adherent(idUsr),
PRIMARY KEY(idReservationFormation),
FOREIGN KEY (annee, rang) REFERENCES Formation(annee, rang)
);
CREATE TABLE Activite (
typeActivite varchar(30) NOT NULL,
PRIMARY KEY(typeActivite)
);

CREATE TABLE A_pour_activite (
annee integer NOT NULL,
rang integer NOT NULL,
typeActivite varchar(30) NOT NULL REFERENCES Activite(typeActivite),
PRIMARY KEY(annee,rang,typeActivite),
FOREIGN KEY (annee, rang) REFERENCES Formation(annee, rang)
);

CREATE TABLE LotMateriel (
marque varchar(30) NOT NULL,
modele varchar(30) NOT NULL,
annee integer NOT NULL CONSTRAINT anneeMat CHECK (annee > 0),
nbPieces integer NOT NULL CONSTRAINT nbPiecesMat CHECK (nbPieces >= 0),
prix integer NOT NULL CONSTRAINT prixMat CHECK (prix >= 0),
categorie varchar(30) NOT NULL REFERENCES Categorie(categorie),
PRIMARY KEY(marque,modele,annee)
);

CREATE TABLE Texte (
texte varchar(200) NOT NULL,
marque varchar(30) NOT NULL,
modele varchar(30) NOT NULL,
annee integer NOT NULL, 
PRIMARY KEY(texte),
FOREIGN KEY (marque,modele,annee) REFERENCES LotMateriel(marque,modele,annee)
);

CREATE TABLE Categorie (
categorie varchar(30) NOT NULL,
PRIMARY KEY(categorie)
);

CREATE TABLE A_comme_sous_categorie (
categorie varchar(30) NOT NULL REFERENCES Categorie(categorie),
sousCategorie varchar(30) NOT NULL REFERENCES Categorie(categorie),
PRIMARY KEY(sousCategorie)
);

CREATE Sequence idLocationMatSeq;
CREATE TABLE LocationMateriel (
idLocationMateriel INTEGER Default idLocationMatSeq.nextval,
dateRecup DATE NOT NULL,
dateRetour DATE NOT NULL,
nbPiecesCasseesPerdues INTEGER NOT NULL CONSTRAINT Cassees CHECK (nbPiecesCasseesPerdues >= 0),
sommeDue INTEGER NOT NULL CONSTRAINT Due CHECK (sommeDue >= 0),
sommeRemboursee INTEGER NOT NULL CONSTRAINT Remboursee CHECK (sommeRemboursee >= 0),
idUsr INTEGER NOT NULL REFERENCES Adherent(idUsr),
PRIMARY KEY (idLocationMateriel)
);

CREATE TABLE ReservationPieces (
nbPiecesReservees INTEGER NOT NULL CONSTRAINT nbPiecesRes CHECK (nbPiecesReservees >= 0),
marque varchar(30) NOT NULL,
modele varchar(30) NOT NULL,
annee integer NOT NULL,
idLocationMateriel INTEGER NOT NULL REFERENCES LocationMateriel(idLocationMateriel),
PRIMARY KEY(marque, modele, annee, idLocationMateriel),
FOREIGN KEY (marque,modele,annee) REFERENCES LotMateriel(marque,modele,annee)
);

CREATE TABLE DatePeremption (
datePeremption DATE NOT NULL,
PRIMARY KEY(datePeremption)
);

CREATE TABLE A_pour_datePeremption (
datePeremption DATE NOT NULL REFERENCES DatePeremption(datePeremption),
marque varchar(30) NOT NULL,
modele varchar(30) NOT NULL,
annee integer NOT NULL,
PRIMARY KEY(marque, modele, annee),
FOREIGN KEY (marque,modele,annee) REFERENCES LotMateriel(marque,modele,annee)
);



DROP TABLE Utilisateur;


INSERT INTO CompteUtilisateur VALUES(DEFAULT);
INSERT INTO Utilisateur VALUES ('aymanlmimouni@gmail.com', 'ProjetBDD123', 'LMIMOUNI', 'Ayman', 'ENSIMAG', 1);
INSERT INTO Utilisateur VALUES ('sarabenomar@gmail.com', 'ProjetBDD123', 'BENOMAR', 'Sara', 'ENSIMAG', 2);
INSERT INTO Utilisateur VALUES ('meryemarroussi@gmail.com', 'ProjetBDD123', 'ARROUSSI', 'Meryem', 'ENSIMAG', 3);
INSERT INTO Utilisateur VALUES ('youssefelasri@gmail.com', 'ProjetBDD123', 'ELASRI', 'Youssef', 'ENSIMAG', 4);
INSERT INTO Utilisateur VALUES ('yakoubdehbi@gmail.com', 'ProjetBDD123', 'DEHBI', 'Yakoub', 'ENSIMAG', 5);

INSERT INTO Adherent VALUES (1);
INSERT INTO Adherent VALUES (3);
INSERT INTO Adherent VALUES (4);

INSERT INTO Refuge VALUES ('refuge1@gmail.com', 'Refuge1', 'Rabat', TO_DATE('11-24', 'MM-DD'), TO_DATE('02-24', 'MM-DD'), 20, 30, 'Magnifique vue avec une terrasse splendide', 'carte-bleue', 3);
INSERT INTO Refuge VALUES ('refuge2@gmail.com', 'Refuge2', 'Casablanca', TO_DATE('11-24', 'MM-DD'), TO_DATE('02-24', 'MM-DD'), 5, 5, ' ', 'cheque', 5);
INSERT INTO Refuge VALUES ('refuge3@gmail.com', 'Refuge3', 'Temara', TO_DATE('01-04', 'MM-DD'), TO_DATE('04-03', 'MM-DD'), 2, 3, 'Meilleur refuge au Maroc', 'espece', 15);

INSERT INTO Ref_NumTel VALUES ('0605040203','refuge2@gmail.com');
INSERT INTO Ref_NumTel VALUES ('0621472593','refuge3@gmail.com');

/*INSERT INTO ReservationRefuge(dateResRefuge, heureResRefuge, nbNuitResRefuge, nbRepasResRefuge, prixResRefuge, email, idUsr) VALUES ( TO_DATE('2019-11-24', 'YYYY-MM-DD')*/

INSERT INTO Repas VALUES ('dejeuner');
INSERT INTO Repas VALUES ('diner');
INSERT INTO Repas VALUES ('casse-croute');
INSERT INTO Repas VALUES ('souper');

INSERT INTO Propose VALUES ('refuge1@gmail.com', 'dejeuner', 4);
INSERT INTO Propose VALUES ('refuge1@gmail.com', 'diner', 15);
INSERT INTO Propose VALUES ('refuge1@gmail.com', 'souper', 7);
INSERT INTO Propose VALUES ('refuge3@gmail.com', 'casse-croute', 1);
INSERT INTO Propose VALUES ('refuge2@gmail.com', 'dejeuner', 4);

INSERT INTO Formation VALUES (2023, 1, 'ski', TO_DATE('2023-11-26', 'YYYY-MM-DD'), 15, 30, 'Formation intensive.', 100);  
INSERT INTO Formation VALUES (2023, 2, 'randonnee', TO_DATE('2023-11-30', 'YYYY-MM-DD'), 5, 30, 'En pleine foret.', 25);  
INSERT INTO Formation VALUES (2023, 3, 'football', TO_DATE('2023-01-26', 'YYYY-MM-DD'), 30, 11, 'Formation mixte.', 5);  

select * from Formation;





































