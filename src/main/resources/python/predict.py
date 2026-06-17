import sys
import json
import re

def predict(text1, text2, classes_str):
    classes = [c.strip() for c in classes_str.split(',') if c.strip()]
    
    if not classes:
        return "UNKNOWN"
        
    # Basic normalization
    t1 = text1.lower()
    t2 = text2.lower()
    
    words1 = set(re.findall(r'\w+', t1))
    words2 = set(re.findall(r'\w+', t2))
    
    # Calculate overlap
    if len(words1) == 0 or len(words2) == 0:
        overlap_ratio = 0
    else:
        overlap = words1.intersection(words2)
        overlap_ratio = len(overlap) / min(len(words1), len(words2))
    
    # Simple heuristic logic to simulate ML
    # If the classes are for NLI (Entailment, Neutral, Contradiction)
    lower_classes = [c.lower() for c in classes]
    
    if "entailment" in lower_classes and "contradiction" in lower_classes:
        if overlap_ratio > 0.6:
            idx = lower_classes.index("entailment")
        elif overlap_ratio < 0.2:
            idx = lower_classes.index("contradiction")
        else:
            if "neutral" in lower_classes:
                idx = lower_classes.index("neutral")
            else:
                idx = 0
        return classes[idx]
        
    # If it's something else, return a class based on string length to simulate determinism
    # so the same text pairs get the same class
    deterministic_idx = (len(t1) + len(t2)) % len(classes)
    return classes[deterministic_idx]

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Usage: python predict.py <text1> <text2> <classes_comma_separated>")
        sys.exit(1)
        
    t1 = sys.argv[1]
    t2 = sys.argv[2]
    cls_str = sys.argv[3]
    
    try:
        prediction = predict(t1, t2, cls_str)
        # Print ONLY the prediction to stdout so Java can read it easily
        print(prediction)
    except Exception as e:
        # Print error to stderr
        print(f"Error during prediction: {e}", file=sys.stderr)
        sys.exit(1)
