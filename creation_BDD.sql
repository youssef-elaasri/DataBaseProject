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
                             idUsr INTEGER NOT NULL Constraint unicite UNIQUE REFERENCES CompteUtilisateur(idUsr),
                             SommeDue INTEGER NOT NULL CONSTRAINT Due CHECK (sommeDue >= 0),
                             SommeRemboursee INTEGER CONSTRAINT Remboursee CHECK (sommeRemboursee >= 0),
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
                        typePaiement varchar(30) NOT NULL CONSTRAINT Paiement CHECK(typePaiement in('espece', 'cheque', 'carte-bleue')),
                        prixNuitee integer NOT NULL CHECK(prixNuitee >= 0),
                        PRIMARY KEY(email)
);

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

CREATE TABLE Utilise (
                         typeActivite varchar(30) NOT NULL REFERENCES Activite(typeActivite),
                         marque varchar(30) NOT NULL,
                         modele varchar(30) NOT NULL,
                         annee integer NOT NULL,
                         PRIMARY KEY(marque,modele,annee,typeActivite),
                         FOREIGN KEY (marque,modele,annee) REFERENCES LotMateriel(marque,modele,annee)
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
                                  idUsr INTEGER NOT NULL REFERENCES Adherent(idUsr),
                                  PRIMARY KEY (idLocationMateriel)
);

CREATE TABLE ReservationPieces (
                                   nbPiecesReservees INTEGER NOT NULL CONSTRAINT nbPiecesRes CHECK (nbPiecesReservees >= 0),
                                   nbPiecesCasseesPerdues INTEGER NOT NULL CONSTRAINT Cassees CHECK (nbPiecesCasseesPerdues >= 0),
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




DROP TABLE DatePeremption;
DROP TABLE A_pour_datePeremption;

DROP TABLE ReservationPieces;
DROP TABLE LocationMateriel;




DROP TABLE Activite;
DROP TABLE A_pour_activite;


DROP TABLE Texte;
DROP TABLE Utilise;
DROP TABLE LotMateriel;
DROP TABLE ReservationFormation;
DROP Sequence idResFormationSeq;
DROP TABLE Formation;
DROP TABLE Propose;
DROP TABLE Repas;
DROP TABLE ReservationRefuge;
DROP TABLE Ref_NumTel;
DROP TABLE Adherent;
DROP TABLE Utilisateur;
DROP TABLE CompteUtilisateur;
DROP TABLE Refuge;
DROP Sequence idUsrSeq;
DROP Sequence idResRefSeq;
DROP SEQUENCE idLocationMatSeq;
DROP TABLE LocationMateriel;
DROP TABLE Categorie;
DROP TABLE A_comme_sous_categorie;


commit;