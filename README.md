
# 🖥️ Kids Desktop (Java)

## Description du Projet

**Kids Desktop** est une application JavaFX éducative destinée à offrir un environnement d'apprentissage complet, accessible hors ligne. Elle englobe diverses fonctionnalités comme la gestion des packs éducatifs, la gestion des utilisateurs, les modules de cours, les consultations médicales, ainsi que des jeux éducatifs basés sur plusieurs APIs externes.

L'application permet une utilisation totalement autonome grâce à un chatbot IA local entraîné avec plus de 1,5 milliard de paramètres (DeepSeek), pouvant être utilisé sans connexion internet.

---

## Table des matières

- [Description du Projet](#description-du-projet)
- [Installation](#installation)
- [Utilisation](#utilisation)
- [Fonctionnalités](#fonctionnalités)
  - [Gestion des Packs](#gestion-des-packs)
  - [Gestion des Utilisateurs](#gestion-des-utilisateurs)
  - [Cours & Modules](#cours--modules)
  - [Consultations Médicales](#consultations-médicales)
  - [Récompenses Éducatives](#récompenses-éducatives)
  - [Gestion des Évaluations](#gestion-des-évaluations)
- [Contributions](#contributions)
- [Contributeurs](#contributeurs)
- [Licence](#licence)

---

## Installation

1. Clonez le dépôt :  
   ```bash
   git clone https://github.com/Ham-Zeuss/projet_java3A39.git
   ```

---

## Utilisation

- Téléchargez le projet via GIT ou ZIP.  
- Importez le projet dans votre IDE (**IntelliJ**).  
- Configurez **JavaFX** si nécessaire.  
- Ajoutez les bibliothèques externes requises.  
- Exécutez l’application.  
- Connectez-vous ou créez un compte pour accéder aux fonctionnalités.  

---

## Fonctionnalités

### Gestion des Packs

- Commande et mise à niveau des packs  
- Gestion du solde utilisateur  
- Contrôle d’accès selon le type de pack  
- Historique des achats par utilisateur  
- Logique de paiement multi-canal : Stripe ou solde interne  
- Génération automatique de reçus PDF  
- Stockage et affichage de l’historique des conversations avec le chatbot IA local (DeepSeek 1.5B paramètres)

### Gestion des Utilisateurs

- Activation / Désactivation de comptes  
- Réinitialisation de mot de passe  
- Notifications personnalisées  
- Statistiques d’utilisation  
- Authentification sécurisée via Google Authenticator  

### Cours & Modules

- Upload et affichage de fichiers PDF via **Dropbox API**  
- Système de notation des cours (stocké en JSON)  
- Filtrage flexible : tous / récents / par créateur / mes cours  
- Affichage dynamique : les cours de l'utilisateur connecté apparaissent en premier  
- Incrémentation/décrémentation automatique du nombre total de cours  
- Recherche de modules depuis le Dashboard  

### Consultations Médicales

- Localisation des cabinets médicaux via **GluonGeo** (basé sur Gluon Maps)  
- Réservation sans conflit grâce à **SafeSlot**  
- Signalement de commentaires inappropriés via **CommentShield**  

### Récompenses Éducatives

- Jeux éducatifs basés sur mots/images  
- Intégration des APIs suivantes :  
  - **Pexels API** : images liées à un mot  
  - **Rapid API** : définitions de mots  
  - **PokeAPI** : Pokémons aléatoires  
  - **ScrambledWord API** : générateur de mots mélangés  
- Fonctionnalités vocales intégrées (**Text-to-Speech / Speech-to-Text**)  

### Gestion des Évaluations

- Envoi d’e-mails via serveur SMTP  
- Calcul de la note et affichage en temps réel pendant le quiz  
- Suivi personnalisé des statistiques de progression  
- Envoi de notifications par e-mail après chaque évaluation  

---

## Contributions

Nous tenons à remercier chaleureusement toute l’équipe pour leur implication, leur sérieux et leur collaboration fructueuse tout au long du développement de ce projet.  
Un remerciement tout particulier à nos formateurs Java, Mme Wiem Hjiri et Mme Ons Fadhel, pour leur accompagnement précieux et leur dévouement.  
Nous exprimons également notre gratitude à la faculté **ESPRIT Engineering** pour le cadre d’apprentissage stimulant et les ressources mises à disposition tout au long de cette expérience.

---

## Contributeurs

- **Boubaker Hachicha** – Gestion des packs, logique de paiement, historique, génération de reçus  
- **Hedyene Ben Haj Yahia** – Gestion des cours, filtrage, recherche, upload PDF via Dropbox  
- **Mohamed Ali Hamroun** – Gestion des utilisateurs, notifications, authentification  
- **Maryem Heni** – Consultations médicales, cartes, réservations, modération  
- **Hamza Hwaneb** – Récompenses éducatives, intégration API externe, interface vocale  
- **Oumaima Boulila** – Gestion des Évaluations  

---

## Licence

Ce projet est sous licence **MIT**.  
Vous êtes libre de l'utiliser, le modifier, le partager ou l'intégrer à d'autres projets, à condition de mentionner l'auteur d'origine.  
Pour plus d'informations, consultez le fichier [LICENSE](LICENSE).
