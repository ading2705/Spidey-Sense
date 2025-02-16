import tensorflow_hub as hub
import librosa
import numpy as np
import pandas as pd

# Load YAMNet and class labels
yamnet = hub.load('https://tfhub.dev/google/yamnet/1')
class_map = pd.read_csv("https://raw.githubusercontent.com/tensorflow/models/master/research/audioset/yamnet/yamnet_class_map.csv")
class_names = class_map["display_name"].tolist()

# Load audio
audio, sr = librosa.load("audio.wav", sr=16000, mono=True)

# Run inference
scores, embeddings, spectrogram = yamnet(audio)

# Print top 2 predictions per window
for i, window_scores in enumerate(scores):
    # Convert TensorFlow tensor to numpy and get top 2 indices
    scores_np = window_scores.numpy()
    top_indices = np.argsort(scores_np)[-2:][::-1]  # Indices sorted descendingly
    top_classes = [class_names[idx] for idx in top_indices]
    top_confidences = scores_np[top_indices]

    print(f"Window {i} ({i*0.975:.2f}s - {(i+1)*0.975:.2f}s):")
    print(f"  1st: {top_classes[0]:<20} Confidence: {top_confidences[0]:.4f}")
    print(f"  2nd: {top_classes[1]:<20} Confidence: {top_confidences[1]:.4f}\n")