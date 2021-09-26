import io
import matplotlib.pyplot as plt
import numpy as np
from scipy.signal import find_peaks

ts=200000*10**(-6)
fs=1/ts

def getPeriod(data):
    rxx = np.correlate(data, data, "same")
    ls, _ = find_peaks(rxx)

    period = np.mean(np.diff(ls))/fs

    fig, ax = plt.subplots()
    ax.plot(rxx)
    ax.plot(ls,rxx[ls],"x", label="Periodo: {}s".format(period))
    ax.legend()

    f = io.BytesIO()
    plt.savefig(f, format="png")
    return f.getvalue()

def getDistance(data):
    rxx = np.correlate(data, data, "same")
    ls, _ = find_peaks(rxx)

    xf = ls.size*0.3
    ## TODO: eliminar ceros al ppio y al final para hallar tiempo total y velocidad

    # time = len(data)/fs
    # vf = np.sum(data)
    # xf = vf * time
    return xf