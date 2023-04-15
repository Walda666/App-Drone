# App Drone

## Description
Application Kotlin permettant de contrôller un drone marin, relié à un simulateur (NMEA).

Crée dans le cadre d'un projet scolaire (ULR).

L'application se sépare en 4 vues :
- Vue 0 : Menu d'accueil permettant d'accéder aux autres.
- Vue 1 : Affichage du drone sur une carte et de sa trajectoire à partir des données récupérées dans le simulateur.
- Vue 2 : Contrôle du drone via le gyroscope du téléphone pour la position et une Seekbar pour la vitesse. La vue contient également un bouton 'Stop' qui arrête brutalement le moteur et un bouton 'Home' qui le fait retourner au point de départ.
- Vue 3 : Possibilité de créer des Waypoints en touchant la carte pour que le drone puisse suivre un itinéraire en autopilote.


## Installation & lancement

Pour installer le bot, procédez comme tel :
- Télécharger le logiciel [NMEA Simulator](https://github.com/panaaj/nmeasimulator) et le configurer en TCP server sur le port `1234`
- Ajouter une clé de l'API Google Maps valide
- Synchroniser et lancer un build avec Gradle

## Ajouts possibles
Nous n'avons pas eu assez de temps pour finir ce projet comme nous le souhaitions. Les modifications que nous aurions aimé effectuer sont les suivantes :
- Vue 2 : pour le bouton Home, le bateau revient au point de départ mais ne s'arrête pas précisement sur ce dernier
- Vue 2 : La Seekbar de vitesse aurait été mieux visuellement au dessus de la carte, au format paysage
- Vue 3 : Faire bien fonctionner le fichier de coordonnées généré dans l'Autopilot du simulateur NMEA.

## Ateurs & rôles
Ce projet à été réalisé par :
- **Chef de projet :** [Romain Piet](https://github.com/Jhyiin)
- **Scrum Master :** [Titouan Paris](https://github.com/TitouanPrsDev)
- **Lead Dev :** [Florian Parenté](https://github.com/FloFloZone)
- **Développeurs :**
    - [Léo Rassindrame](https://github.com/lrassindrame)
    - [Raphaël Victor](https://github.com/Sparteee)
    - [Moi même](https://github.com/Walda666)

[Accéder au repository original](https://github.com/TitouanPrsDev/projet-kotlin)