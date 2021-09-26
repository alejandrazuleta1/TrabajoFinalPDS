import io
import matplotlib.pyplot as plt
import numpy as np
from scipy.signal import find_peaks

def plot(datax):
    rxx = np.correlate(datax,datax,"same")
    fig, ax = plt.subplots()
    ax.plot(rxx)

    f = io.BytesIO()
    plt.savefig(f, format="png")
    return f.getvalue()
