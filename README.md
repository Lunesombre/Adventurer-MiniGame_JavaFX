# Lunesombre's Adventurer MiniGame

Adventurer MiniGame © 2024 by Romain Touchet is licensed under CC BY-NC-SA 4.0

[Version Française](#Adventurer-MiniGame-de-Lunesombre)

## About the Game

The game is a simple 2D game where you play as an Adventurer who has to find a treasure in a wooden area.
The bigger the map, the higher the difficulty chosen, and the shortest path taken to get to the Treasure, higher the final score you'll get.

### The Idea

I got the idea for this game from a recruitment test for an internship.

I was about to start a work-study program to retrain for a career as a software developer when given this test.
With no knowledge in Java (and only a few months in OOP), I managed to produce something good enough to get the internship.

In short, I was given a map (in form a .txt file) and a set of move instructions, and a set of start and end coordinates; and had to develop a short
soft to ensure the "adventurer" would end up in the right coordinates.
There were impassable cells called "impenetrable woods."

I gamified it a bit to make it more fun: when an instruction would cause the adventurer to cross an impenetrable wood, a random message would show in
the console to tell why the adventurer could cross this cell : he had heard the grim howling of wolves, or torn his pourpoint on sharp brambles...

A friend of mine in the same work-study program had also had this test, and later that year showed me a newer version of his work where the map was
drawn within the console, but his version now had a controllable adventurer, monsters and even bonus items.

It inspired me to do a new version too, but why not have a real user interface?

Some time later I heard about JavaFX, and decided the Adventurer's mini-game would be a good fun side-project to try JavaFX.

What you see now is the result. 😊

## Technologies Used

This mini-game is built using:

- Java 21, Spring Boot, Lombok
- JavaFX

## Version 1.0 Features

### Game Purpose

The main goal of the game is to move the Adventurer to the Treasure so he gets it.

The Adventurer always starts on a "path" tile on a random side of the map. Treasure's location is random but not too close, and always reachable.

There are "impenetrable wood" tiles that you can't cross, and trying will hurt you.

You can't leave the map... Not until you've found the treasure!

### Controls

#### Moves

- UP arrow: move up
- DOWN arrow: move down
- LEFT arrow: move left
- RIGHT arrow: move right

Please note that you can modify these movement controls in the options.

#### Pause

- Pause: you can pause and unpause the game by hitting the SPACE-bar.
    - Nb: opening the option menu will also pause the game. Closing the option menu will resume the game.

### Difficulty Modes

1. Easy: Treasure is always visible on the map as an X-mark on the map.
2. Normal: A vague direction to the treasure is given on start. The treasure location is revealed when you get nearby.
3. Hard: A vague direction to the treasure is given on start. You got to almost step on the treasure for its location to be shown.

The harder the chosen mode, the more points you might score.

### Options

- Language: the game is currently providing a French and an English version. You can dynamically switch to the desired language while in game.
- Key binding: the player can change the keys moving the Adventurer. Default move keys are arrows.
- High scores: one can display high scores and even reset them to the default placeholder high scores. Warning: deleted high scores might be lost
  forever.

## How to Run

_Please come back later for instructions._ 😉

## Future Plans

For upcoming versions, I'm planning to add:

- Monsters! New ways to die on your greed-fueled adventure!
- A visual rework (I intend to make sprites, but I'm a total novice and more generally artistically mediocre).

## Feedback

I would love to hear your thoughts! If you have any feedback, suggestions, or bug reports, please open an issue in this repository or reach out to me
directly. Your input could help make the game better!

## License

[![CC BY-NC-SA 4.0][cc-by-nc-sa-shield]][cc-by-nc-sa]

### This work is licensed under a [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License][cc-by-nc-sa].

[cc-by-nc-sa]: http://creativecommons.org/licenses/by-nc-sa/4.0/

[cc-by-nc-sa-shield]: https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey.svg

### Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International Public License (CC BY-NC-SA 4.0)

By exercising the Licensed Rights, you accept and agree to be bound by the terms and conditions of this Creative Commons
Attribution-NonCommercial-ShareAlike 4.0 International Public License ("Public License").

No warranties are given. The license may not give you all of the permissions necessary for your intended use. For example, other rights such as
publicity, privacy, or moral rights may limit how you use the material.

For more information on this license, visit: https://creativecommons.org/licenses/by-nc-sa/4.0/ or see the LICENSE file in this repo.


---
Version Française
---

# Adventurer MiniGame de Lunesombre

Mini-jeu de l'Aventurier © 2024 par Romain Touchet est sous licence CC BY-NC-SA 4.0

## À propos du jeu

Le jeu est un simple jeu 2D où vous incarnez un Aventurier qui doit trouver un trésor dans une zone boisée.
Plus la carte est grande, plus la difficulté choisie est élevée, et plus le chemin emprunté pour atteindre le Trésor est court, plus le score final
sera élevé.

### L'idée

J'ai eu l'idée de ce jeu à partir d'un test de recrutement pour un stage.

J'étais sur le point de commencer une formation en alternance pour me reconvertir en tant que développeur lorsque ce test m'a été donné.
Sans aucune connaissance en Java (et seulement quelques mois en POO), j'ai réussi à en faire quelque chose d'assez correct pour obtenir le stage.

En bref, on m'avait donné une carte (sous forme d'un fichier .txt), un ensemble d'instructions de déplacement, et des coordonnées de départ et
d'arrivée ; et je devais développer un petit logiciel pour s'assurer que l'"aventurier" finirait aux bonnes coordonnées.
Il y avait des cellules infranchissables appelées "bois impénétrables".

Je l'ai un peu gamifié pour le rendre plus amusant : lorsqu'une instruction amenait l'aventurier à traverser un bois impénétrable, un message
aléatoire s'affichait dans la console pour expliquer pourquoi l'aventurier ne pouvait pas traverser cette cellule : il avait entendu le hurlement
sinistre des loups, ou déchiré son pourpoint sur des ronces acérées...

Un ami dans la même formation en alternance avait également eu ce test, et plus tard dans l'année, il m'a montré une nouvelle version de son travail
où la carte était dessinée dans la console, mais sa version avait maintenant un aventurier contrôlable, des monstres et même des objets bonus.

Cela m'a inspiré pour faire aussi une nouvelle version, mais pourquoi ne pas avoir une vraie interface utilisateur ?

Quelque temps plus tard, j'ai entendu parler de JavaFX, et j'ai décidé que le mini-jeu de l'Aventurier serait un bon petit side project pour essayer
JavaFX.

Ce que vous voyez maintenant est le résultat. 😊

## Technologies utilisées

Ce mini-jeu est développé en utilisant :

- Java 21, Spring Boot, Lombok
- JavaFX

## Dans la version 1.0

### But du jeu

L'objectif principal du jeu est de déplacer l'Aventurier jusqu'au Trésor pour le récupérer.

L'Aventurier commence toujours sur une case "chemin" d'un côté aléatoire de la carte. L'emplacement du Trésor est aléatoire, mais pas trop proche, et
toujours accessible.

Il y a des cases "bois impénétrables" que vous ne pouvez pas traverser, et essayer vous blessera.

Vous ne pouvez pas sortir de la carte... Pas avant d'avoir trouvé le trésor voyons !

### Contrôles

#### Déplacements

- Flèche HAUT : se déplacer vers le haut
- Flèche BAS : se déplacer vers le bas
- Flèche GAUCHE : se déplacer vers la gauche
- Flèche DROITE : se déplacer vers la droite

Veuillez noter que vous pouvez modifier ces contrôles de mouvement dans les options.

#### Pause

- Pause : vous pouvez mettre en pause et reprendre le jeu en appuyant sur la barre ESPACE.
    - NB : ouvrir le menu des options mettra également le jeu en pause. Fermer le menu des options reprendra le jeu.

### Modes de difficulté

1. Facile : Le Trésor est toujours visible sur la carte sous forme d'un X rouge.
2. Normal : Une direction vague vers le trésor est donnée au début. L'emplacement du trésor est révélé lorsque vous vous en approchez.
3. Difficile : Une direction vague vers le trésor est donnée au début. Vous devez presque marcher sur le trésor pour que son emplacement soit révélé.

Plus le mode choisi est difficile, plus vous pourrez marquer de points.

### Options

- Langue : le jeu propose actuellement une version française et une version anglaise. Vous pouvez passer dynamiquement à la langue souhaitée en cours
  de jeu.
- Attribution des touches : le joueur peut changer les touches déplaçant l'Aventurier. Les touches de déplacement par défaut sont les flèches.
- Meilleurs scores : on peut afficher les meilleurs scores et même les réinitialiser aux meilleurs scores par défaut. Attention : les meilleurs scores
  supprimés pourraient être perdus à jamais.

## Comment lancer le jeu

_Veuillez revenir plus tard pour les instructions._ 😉

## Evolutions envisagées

Pour les versions à venir, je prévois d'ajouter :

- Des monstres ! De nouvelles façons de mourir dans votre aventure motivée par la cupidité !
- Une refonte visuelle (j'ai l'intention de faire des sprites, mais je suis totalement novice et plus globalement artistiquement mauvais).

## Feedback

J'aimerais beaucoup connaître votre avis ! Si vous avez des commentaires, des suggestions ou des rapports de bugs, n'hésitez pas à ouvrir une issue
dans ce repo ou à me contacter directement. Votre contribution pourrait aider à améliorer le jeu !

## Licence

[cc-by-nc-sa]: http://creativecommons.org/licenses/by-nc-sa/4.0/

[cc-by-nc-sa-shield]: https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey.svg
[![CC BY-NC-SA 4.0][cc-by-nc-sa-shield]][cc-by-nc-sa]

### Ce jeu est distribué sous la licence [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License][cc-by-nc-sa].

Aucune garantie n'est donnée. La licence peut ne pas vous donner toutes les autorisations nécessaires à l'utilisation que vous souhaitez en faire.
Par exemple, d'autres droits tels que la publicité, le respect de la vie privée ou les droits moraux peuvent limiter l'utilisation que vous faites du
matériel.

Pour plus d'informations sur cette licence, visitez : https://creativecommons.org/licenses/by-nc-sa/4.0/

[Back to top](#Lunesombres-Adventurer-MiniGame)
