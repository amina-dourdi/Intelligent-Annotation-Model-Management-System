import sys
import time
import json
import random
import argparse

def main():
    parser = argparse.ArgumentParser(description="NLP Model Training Mock")
    parser.add_argument("--dataset_path", type=str, required=True, help="Path to the dataset CSV")
    parser.add_argument("--epochs", type=int, default=5, help="Number of epochs")
    parser.add_argument("--lr", type=float, default=0.001, help="Learning rate")
    parser.add_argument("--batch_size", type=int, default=32, help="Batch size")
    args = parser.parse_args()

    print(f"--- Démarrage de l'entraînement NLP ---")
    print(f"Dataset : {args.dataset_path}")
    print(f"Hyperparamètres : Epochs={args.epochs}, LR={args.lr}, BatchSize={args.batch_size}")
    
    # Simulation de l'entraînement
    for epoch in range(1, args.epochs + 1):
        loss = random.uniform(0.1, 1.0) / epoch
        acc = 0.5 + (0.4 * (epoch / args.epochs))
        print(f"Epoch {epoch}/{args.epochs} - loss: {loss:.4f} - accuracy: {acc:.4f}")
        time.sleep(1) # Simule le temps de calcul
        
    print("--- Entraînement terminé ---")
    print("--- Évaluation sur le set de test ---")
    
    time.sleep(1)
    
    # Génération de scores finaux réalistes
    final_acc = random.uniform(0.80, 0.95)
    final_f1 = final_acc - random.uniform(0.01, 0.05)
    
    # Output final en JSON pour que Java puisse le lire facilement
    result = {
        "accuracy": round(final_acc, 4),
        "f1_score": round(final_f1, 4),
        "confusion_matrix": "[[45, 5], [8, 42]]"
    }
    
    # La ligne commençant par RESULT_JSON sera parsée par Spring Boot
    print(f"RESULT_JSON={json.dumps(result)}")

if __name__ == "__main__":
    main()
