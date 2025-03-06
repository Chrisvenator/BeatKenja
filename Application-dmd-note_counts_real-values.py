import json

import numpy as np
from matplotlib import pyplot as plt
from scipy.stats import dirichlet

# Define alpha parameters
D = 0
alpha = [371, 2366, 2569, 178, 140, 822, 774, 394, 64, 77, 19, 45, 124, 130, 7, 24, 19, 5, 2, 290, 71, 298, 34, 132,
         122, 108, 57, 34, 4, 35, 14, 27, 9, 17, 3, 65, 80, 27, 7, 10, 27, 9, 7, 21, 50, 26, 7, 12, 2, 10, 31, 3, 24,
         23, 7, 3, 3, 8, 9, 3, 2, 19, 5, 1, 1, 2, 2, 2, 2, 6, 2, 9, 3, 2, 3, 1, 1, 1, 3, 1, 4, 1]
alpha = np.sort(alpha)
alpha = alpha[::-1]

categories = [f"Note{i + 1}" for i in range(len(alpha))]

# Total notes in the section


# Create a 3x3 grid for pie charts
fig, axes = plt.subplots(3, 3, figsize=(12, 12))

# Load colors from the JSON file
with open("colors.json", "r") as f:
    colors_rgb = json.load(f)

# Convert RGB (0–255) values to Matplotlib's expected range (0–1)
colors = [(r / 255, g / 255, b / 255) for r, g, b in colors_rgb]

# Ensure that the number of colors matches the number of categories. If not, Generate colors dynamically based on the
# number of alpha values
if len(colors) < len(categories):
    num_colors = len(alpha)
    cmap = plt.colormaps["viridis"]  # Choose a colormap
    colors = [cmap(i / (num_colors - 1)) for i in range(num_colors)]  # Generate interpolated colors
    np.random.shuffle(colors)  # Shuffle the colors randomly


# Function to display percentages only if > 2%
def autopct_threshold(pct):
    if pct > 2:
        return f'{pct:.1f}%'  # Show percentage with one decimal
    return ''  # Leave blank for < 2%


# explicit function to normalize array
# https://www.geeksforgeeks.org/how-to-normalize-an-array-in-numpy-in-python/
def normalize(arr, t_min, t_max):
    norm_arr = []
    diff = t_max - t_min
    diff_arr = max(arr) - min(arr)
    for i in arr:
        temp = (((i - min(arr)) * diff) / diff_arr) + t_min
        norm_arr.append(temp)
    return norm_arr


# assign array and range
print(alpha)

# Simulate 9 times
for i in range(9):
    if D > 0:
        # We could not take 0 because of ther error: "ValueError: All parameters must be greater than 0".
        # So we just used something that is closed to 0
        new_alpha = normalize(alpha, 0.0001, D)
        proportions = dirichlet.rvs(new_alpha, size=1)[0]  # Sample proportions from Dirichlet
    else:
        proportions = alpha

    # Select subplot
    ax = axes[i // 3, i % 3]

    # Plot pie chart
    ax.pie(proportions, labels=categories, autopct=autopct_threshold, colors=colors)
    # ax.set_title(f"Simulation {i + 1}")
    ax.set_title(f"Visualization of c_i with D = {D}")
    D = D + 4


# Adjust layout
plt.tight_layout()
plt.suptitle("Pie Charts from 9 Simulations", fontsize=16, y=1.02)
plt.show()
