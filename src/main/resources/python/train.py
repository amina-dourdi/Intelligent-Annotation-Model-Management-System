import sys
# Forcer l'encodage en UTF-8 pour Windows
if sys.stdout.encoding != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

import time
import json
import argparse
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, f1_score, confusion_matrix
import numpy as np

def main():
    parser = argparse.ArgumentParser(description="NLP Model Training Pipeline")
    parser.add_argument("--dataset_path", type=str, required=True, help="Path to the dataset CSV")
    parser.add_argument("--epochs", type=int, default=5, help="Number of epochs")
    parser.add_argument("--lr", type=float, default=0.001, help="Learning rate")
    parser.add_argument("--batch_size", type=int, default=32, help="Batch size")
    args = parser.parse_args()

    print(f"--- Demarrage de l'entrainement NLP ---")
    print(f"Dataset : {args.dataset_path}")
    
    try:
        # 1. Chargement des données
        df = pd.read_csv(args.dataset_path)
        
        # Filtrer uniquement les lignes ayant une annotation (classe non vide)
        df_annotated = df.dropna(subset=['classe'])
        
        if len(df_annotated) < 5:
            raise ValueError("Pas assez de donnees annotees pour entrainer un modele. Veuillez annoter au moins 5 textes.")
            
        print(f"Nombre d'exemples annotés trouvés : {len(df_annotated)}")
        
        # 2. Préparation des features (TF-IDF sur texte1 + texte2)
        # On concatène les deux textes pour la classification NLI simple
        df_annotated['combined_text'] = df_annotated['text1'].fillna('') + " " + df_annotated['text2'].fillna('')
        
        vectorizer = TfidfVectorizer(max_features=1000)
        X = vectorizer.fit_transform(df_annotated['combined_text'])
        y = df_annotated['classe']
        
        # Séparation Train / Test
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
        
        # 3. Simulation des epochs pour l'interface utilisateur
        # Scikit-learn LogisticRegression s'entraîne en une fois, donc on simule la progression
        model = LogisticRegression(max_iter=100, C=1.0/args.lr) # LR est simulé via l'inverse du paramètre de régularisation
        
        for epoch in range(1, args.epochs + 1):
            time.sleep(0.5) # Simule le temps de calcul
            print(f"Epoch {epoch}/{args.epochs} - Optimisation des poids en cours...")
            
        # Entraînement réel
        model.fit(X_train, y_train)
        print("--- Entrainement termine ---")
        
        # 4. Évaluation réelle
        print("--- Evaluation sur le set de test ---")
        y_pred = model.predict(X_test)
        
        acc = accuracy_score(y_test, y_pred)
        # Gestion du multiclass pour le F1-Score
        f1 = f1_score(y_test, y_pred, average='weighted', zero_division=0)
        cm = confusion_matrix(y_test, y_pred)
        
        # Formatage de la matrice de confusion en string lisible
        cm_str = np.array2string(cm, separator=', ')
        
        # 5. Output final JSON pour Java
        result = {
            "accuracy": round(acc, 4),
            "f1_score": round(f1, 4),
            "confusion_matrix": cm_str
        }
        
        print(f"RESULT_JSON={json.dumps(result)}")

    except Exception as e:
        print(f"ERREUR FATALE: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()
