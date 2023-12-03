## DataBaseProject

# Documentation du projet:
**1.1 Analyse du Problème**

Le projet débute par l’analyse du texte expliquant les besoins du client. Notre stratégie consistait à
identifier les objets nécessaires pour construire les tables de notre future base de données, en incluant
les dépendances fonctionnelles entre eux ainsi que les contraintes de valeurs et de multiplicité.
L’email du refuge a été utilisé pour déterminer tous les attributs de la table "refuge". De plus, en
connaissant l’objet "Repas", nous avons pu établir une relation permettant de déduire le prix associé à
ce repas. L’idée d’avoir une propriété propre a commencé à prendre forme.
Il a été décidé qu’un refuge peut disposer d’au plus un numéro. En ce qui concerne les "Formations",
nous avons observé qu’une formation peut proposer au moins une activité, introduisant ainsi une nou-
velle contrainte de multiplicité.
Pour assurer la conformité de notre base de données au Règlement Général sur la Protection des
Données (RGPD), nous avons créé un identifiant utilisateur ("IdUsr") pour préserver les traces de
réservations sans avoir besoin de stocker les données clients directement. Ainsi, les données clients
sont mieux protégées et peuvent être supprimées si le client en fait la demande.
Afin de bien identifier les réservations pour les refuges et les formations, des identifiants ont été
introduits pour jouer le rôle de clés primaires
The Refuge Database project is dedicated to creating a comprehensive database to manage information related to refugees. This initiative aims to streamline the storage, retrieval, and management of data concerning displaced individuals and populations.

**1.1.1 Conception du Diagramme Entités/Associations (UML)** 

La deuxième étape a été la conception du diagramme Entités/Associations (UML). La table \texttt{"Refuge"} regroupe toutes les informations relatives au refuge. Afin de respecter la contrainte de multiplicité stipulant qu'un refuge peut avoir au plus un numéro, nous avons établi une association sémantique entre la table \texttt{"Refuge"} et la table \texttt{"Ref\_NumTel"}. Une propriété propre relie également \texttt{"Refuge"} à \texttt{"Repas"}, permettant d'indiquer le prix d'un repas dans un refuge spécifique.

La table \texttt{"ReservationRefuge"} est naturellement liée à la table \texttt{"Refuge"} pour indiquer à quel refuge la réservation correspond. Elle est également liée à la table \texttt{"CompteUtilisateur"}, permettant d'identifier à qui appartient cette réservation.

Les données utilisateur sont stockées dans une table distincte du même nom, associée à la table \texttt{"CompteUtilisateur"}, qui ne contient que l'identifiant de l'utilisateur. Cela garantit la conformité au RGPD. De plus, étant donné qu'un utilisateur peut être adhérent ou non, la table \texttt{"Adherents"} est une sous-entité faible de la table \texttt{"Utilisateur"}.

Cette dernière est associée aux tables \texttt{"ReservationFormation"} et \texttt{"LocationMateriel"}. Ces deux tables sont respectivement liées à la table \texttt{"Formations"} par une association sémantique, et à la table \texttt{"LotMateriel"} par une propriété propre déterminant le nombre de pièces réservées et le nombre cassées/perdues.

La table \texttt{"LotMateriel"} est associée à une table texte pour indiquer si un lot peut ou non avoir une description. Elle est également associée à une seule et unique catégorie dans la table \texttt{"Categorie"}. Cette dernière possède une association réflexive permettant de représenter le fait qu'une catégorie mère peut avoir plusieurs sous-catégories ou aucune.

La table \texttt{"Activité"} est associée à \texttt{"Formation"} pour indiquer de quelle formation il s'agit. Elle est également associée à la table \texttt{"LotMateriel"}, indiquant le matériel utilisé pour une certaine activité.

**Location Tracking:**  Geographic information is incorporated to monitor and analyze the movement of refugees.

**Educational History:** Capture educational background information to facilitate educational support programs for refugees

**Security and Privacy:** users can at any time delete their personal information without infecting our database.
