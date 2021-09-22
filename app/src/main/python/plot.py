import io
import matplotlib.pyplot as plt
import numpy as np

def plot(datax):
    x = [float(data) for data in datax.split()]
    rxx = np.correlate(x,x,"full")
    fig, ax = plt.subplots()
    ax.plot(rxx)

    f = io.BytesIO()
    plt.savefig(f, format="png")
    return f.getvalue()
