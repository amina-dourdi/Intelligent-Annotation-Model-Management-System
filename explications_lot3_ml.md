# Implémentation du Module d'Entraînement Machine Learning (Lot 3 - Section 5)

Ce document explique en détail les développements réalisés pour répondre aux exigences de la **Section 5** du cahier des charges : *Lancement de l'entraînement et du test de modèles NLP*.

## 1. Objectif

L'objectif était de permettre à un Administrateur de :
1. Configurer les hyperparamètres (Epochs, Learning Rate, Batch Size) depuis l'interface web.
2. Lancer un script Python d'entraînement en arrière-plan.
3. Récupérer les métriques d'évaluation (Accuracy, F1-Score).
4. Garder un historique complet de tous les entraînements lancés.

## 2. Architecture & Composants Développés

### A. La Base de Données (JPA / Hibernate)
Afin de conserver l'historique des modèles entraînés, nous avons créé une nouvelle entité :
- **Fichier :** `EntrainementModele.java`
- **Rôle :** Stocker la date de lancement, l'administrateur ayant lancé l'entraînement, les paramètres choisis, le statut de l'exécution (`EN_COURS`, `TERMINE`, `ERREUR`) ainsi que les métriques finales (Accuracy, F1-Score) et les logs brut de la console Python.
- **DAO :** `IEntrainementRepository.java` a été créé pour permettre la récupération de l'historique trié par date.

### B. Le Script Python (Le "Moteur" ML)
Comme demandé, nous devions intégrer un script Python. 
- **Fichier :** `train.py`
- **Rôle :** Simuler un véritable entraînement de Deep Learning. Le script utilise `argparse` pour lire les hyperparamètres passés par Java. Il effectue une boucle temporelle (`time.sleep`) pour simuler le temps d'apprentissage tout en générant des "logs" de progression (Loss, Accuracy par Epoch).
- **Sortie :** À la fin de son exécution, il imprime dans la console un objet JSON standardisé contenant les scores finaux.

### C. La Couche Service (L'intégration Java-Python)
C'est le cœur technique de cette tâche.
- **Fichier :** `MlTrainingServiceImpl.java`
- **Rôle :** Fait le pont entre l'application web Spring Boot et l'environnement système.
- **Technique :** Nous utilisons la classe native Java `ProcessBuilder` pour ouvrir une invite de commande système et exécuter la commande :
  `python train.py --epochs X --lr Y --batch_size Z`
- Le service "écoute" la console Python (`InputStreamReader`), enregistre chaque ligne de log, et détecte la ligne contenant le JSON final pour extraire proprement l'Accuracy et le F1-Score.

### D. L'Interface Web (Thymeleaf & Controllers)
- **Fichier :** `DatasetController.java`
- **Rôle :** Gère deux nouvelles routes `/admin/datasets/{id}/train`. La méthode `GET` affiche la page, et la méthode `POST` réceptionne les hyperparamètres du formulaire pour déclencher l'entraînement.
- **Fichier :** `dataset-train.html`
- **Rôle :** L'interface utilisateur. Elle est divisée en deux parties : 
  1. À gauche, le formulaire des hyperparamètres.
  2. À droite, le tableau d'historique des entraînements.
- **Fonctionnalité Bonus :** L'ajout d'une modale (fenêtre popup) qui s'ouvre lorsqu'on clique sur le bouton "Logs" du tableau. Elle permet de voir le texte exact (formaté en vert façon terminal) que Python a affiché, ce qui est extrêmement utile pour le débogage.

## 3. Pourquoi cette approche ?

1. **La Sécurité :** L'utilisation de `ProcessBuilder` est isolée et asynchrone, évitant que l'application Spring Boot ne plante si le script Python échoue.
2. **L'Évolutivité :** En utilisant un flux JSON standardisé entre Python et Java, vous pourrez très facilement remplacer le script `train.py` de "simulation" par un **véritable** script d'apprentissage (avec `TensorFlow` ou `HuggingFace Transformers`) dans le futur, sans avoir à changer une seule ligne du code Java !
3. **L'Expérience Utilisateur :** L'administrateur n'a plus besoin d'ouvrir un terminal. Tout se fait en quelques clics depuis le navigateur, respectant l'immersion totale exigée par le cahier des charges.
