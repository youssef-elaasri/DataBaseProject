INSERT INTO CompteUtilisateur VALUES(DEFAULT);

INSERT INTO Utilisateur VALUES ('aymanlmimouni@gmail.com', 'ProjetBDD123', 'LMIMOUNI', 'Ayman', 'ENSIMAG', 1,0,0);
INSERT INTO Utilisateur VALUES ('sarabenomar@gmail.com', 'ProjetBDD123', 'BENOMAR', 'Sara', 'ENSIMAG', 2,0,0);
INSERT INTO Utilisateur VALUES ('meryemarroussi@gmail.com', 'ProjetBDD123', 'ARROUSSI', 'Meryem', 'ENSIMAG', 3,0,0);
INSERT INTO Utilisateur VALUES ('youssefelasri@gmail.com', 'ProjetBDD123', 'ELASRI', 'Youssef', 'ENSIMAG', 4,0,0);
INSERT INTO Utilisateur VALUES ('yakoubdehbi@gmail.com', 'ProjetBDD123', 'DEHBI', 'Yakoub', 'ENSIMAG', 5,0,0);
INSERT INTO Utilisateur VALUES ('admin', 'admin', 'admin', 'admin', 'ENSIMAG', 6,0,0);

INSERT INTO Adherent VALUES (1);
INSERT INTO Adherent VALUES (3);
INSERT INTO Adherent VALUES (4);
INSERT INTO Adherent VALUES (6);

INSERT INTO Refuge VALUES ('refuge1@gmail.com', 'Refuge1', 'secteur1', TO_DATE('11-24', 'MM-DD'), TO_DATE('02-24', 'MM-DD'), 20, 30, 'Magnifique vue avec une terrasse splendide', 'carte-bleue', 3);
INSERT INTO Refuge VALUES ('refuge2@gmail.com', 'Refuge2', 'secteur2', TO_DATE('11-24', 'MM-DD'), TO_DATE('02-24', 'MM-DD'), 5, 5, ' ', 'cheque', 5);
INSERT INTO Refuge VALUES ('refuge3@gmail.com', 'Refuge3', 'secteur3', TO_DATE('01-04', 'MM-DD'), TO_DATE('04-03', 'MM-DD'), 2, 3, 'Meilleur refuge au Maroc', 'espece', 15);

INSERT INTO Ref_NumTel VALUES ('0605040203','refuge2@gmail.com');
INSERT INTO Ref_NumTel VALUES ('0621472593','refuge3@gmail.com');

INSERT INTO Repas VALUES ('dejeuner');
INSERT INTO Repas VALUES ('diner');
INSERT INTO Repas VALUES ('casse-croute');
INSERT INTO Repas VALUES ('souper');

INSERT INTO Propose VALUES ('refuge1@gmail.com', 'dejeuner', 4);
INSERT INTO Propose VALUES ('refuge1@gmail.com', 'diner', 15);
INSERT INTO Propose VALUES ('refuge1@gmail.com', 'souper', 20);
INSERT INTO Propose VALUES ('refuge1@gmail.com', 'casse-croute', 25);
INSERT INTO Propose VALUES ('refuge2@gmail.com', 'casse-croute', 1);
INSERT INTO Propose VALUES ('refuge2@gmail.com', 'dejeuner', 4);
INSERT INTO Formation VALUES (2023, 1, 'formation1', TO_DATE('2023-11-26', 'YYYY-MM-DD'), 15, 30, 'Formation intensive.', 100);
INSERT INTO Formation VALUES (2023, 2, 'formation2', TO_DATE('2023-11-30', 'YYYY-MM-DD'), 5, 30, 'En pleine foret.', 25);
INSERT INTO Formation VALUES (2023, 3, 'formation3', TO_DATE('2023-01-26', 'YYYY-MM-DD'), 30, 11, 'Formation mixte.', 15);
INSERT INTO Formation VALUES (2023, 4, 'formation4', TO_DATE('2023-01-26', 'YYYY-MM-DD'), 30, 11, 'Formation amusante.', 35);
INSERT INTO Formation VALUES (2023, 5, 'formation5', TO_DATE('2023-01-26', 'YYYY-MM-DD'), 30, 11, 'Formation de relaxation.', 20);




INSERT INTO Activite VALUES ('activite1');
INSERT INTO Activite VALUES ('activite2');
INSERT INTO Activite VALUES ('activite3');
INSERT INTO Activite VALUES ('activite4');

INSERT INTO A_pour_activite VALUES (2023, 1, 'activite1');
INSERT INTO A_pour_activite VALUES (2023, 1, 'activite2');
INSERT INTO A_pour_activite VALUES (2023, 2, 'activite1');
INSERT INTO A_pour_activite VALUES (2023, 2, 'activite2');
INSERT INTO A_pour_activite VALUES (2023, 2, 'activite3');
INSERT INTO A_pour_activite VALUES (2023, 3, 'activite4');

INSERT INTO Categorie VALUES ('categorie1');
INSERT INTO Categorie VALUES ('categorie2');
INSERT INTO Categorie VALUES ('categorie3');
INSERT INTO Categorie VALUES ('categorie4');
INSERT INTO Categorie VALUES ('categorie5');
INSERT INTO Categorie VALUES ('categorie6');


INSERT INTO LotMateriel VALUES ('marque1', 'modele1', 2020, 15, 20,'categorie1');
INSERT INTO LotMateriel VALUES ('marque2', 'modele2', 2020, 18, 15,'categorie2');
INSERT INTO LotMateriel VALUES ('marque3', 'modele2', 2019, 20, 25,'categorie3');
INSERT INTO LotMateriel VALUES ('marque4', 'modele1', 2021, 25, 30,'categorie4');
INSERT INTO LotMateriel VALUES ('marque4', 'modele2', 2022, 30, 10,'categorie5');

INSERT INTO Utilise VALUES ('activite1', 'marque1', 'modele1', 2020);
INSERT INTO Utilise VALUES ('activite1', 'marque2', 'modele2', 2020);
INSERT INTO Utilise VALUES ('activite2', 'marque3', 'modele2', 2019);
INSERT INTO Utilise VALUES ('activite3', 'marque4', 'modele1', 2021);
INSERT INTO Utilise VALUES ('activite4', 'marque4', 'modele2', 2022);


INSERT INTO Texte VALUES ('Materiel dedie pour activite1','marque1','modele1', 2020);
INSERT INTO Texte VALUES ('Materiel dedie pour activite2', 'marque2', 'modele2', 2020);


INSERT INTO A_comme_sous_categorie VALUES ('categorie1','categorie2');
INSERT INTO A_comme_sous_categorie VALUES ('categorie1','categorie3');
INSERT INTO A_comme_sous_categorie VALUES ('categorie2','categorie4');
INSERT INTO A_comme_sous_categorie VALUES ('categorie1','categorie5');

INSERT INTO DatePeremption Values (TO_DATE('2024-11-30', 'YYYY-MM-DD'));
INSERT INTO DatePeremption Values (TO_DATE('2024-12-31', 'YYYY-MM-DD'));
select * from LotMateriel;
INSERT INTO A_pour_datePeremption Values (TO_DATE('2024-11-30', 'YYYY-MM-DD'), 'marque1','modele1', 2020);
INSERT INTO A_pour_datePeremption Values (TO_DATE('2024-12-31', 'YYYY-MM-DD'), 'marque4','modele1', 2021);
commit;
