import tkinter as tk
from tkinter import messagebox

def calculate_bmi():
    try:
        weight = float(weight_entry.get())
        height = float(height_entry.get()) / 100  # convert cm to meters
        bmi = weight / (height ** 2)
        result = f"Your BMI is: {bmi:.2f}"
        
        if bmi < 18.5:
            result += " (Underweight)"
        elif 18.5 <= bmi < 24.9:
            result += " (Normal weight)"
        elif 25 <= bmi < 29.9:
            result += " (Overweight)"
        else:
            result += " (Obese)"
        
        messagebox.showinfo("BMI Result", result)
    except ValueError:
        messagebox.showerror("Input Error", "Please enter valid numbers.")

# Set up the main window
window = tk.Tk()
window.title("BMI Calculator")
window.geometry("300x200")

# Label and Entry for weight
weight_label = tk.Label(window, text="Weight (kg):")
weight_label.pack()
weight_entry = tk.Entry(window)
weight_entry.pack()

# Label and Entry for height
height_label = tk.Label(window, text="Height (cm):")
height_label.pack()
height_entry = tk.Entry(window)
height_entry.pack()

# Calculate Button
calculate_button = tk.Button(window, text="Calculate BMI", command=calculate_bmi)
calculate_button.pack()

# Run the application
window.mainloop()
