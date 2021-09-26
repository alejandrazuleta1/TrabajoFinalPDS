import io
import matplotlib.pyplot as plt
import numpy as np
from scipy.signal import find_peaks

ts=200000*10**(-6)
fs=1/ts

def normalize(data):
    data=data-np.mean(data)
    data=data/np.max(abs(data))
    return data

def plot_autocorrelation(data):
    data=normalize(data)
    rxx=np.correlate(data,data,"same")
    ls, _ = find_peaks(rxx)
    period = np.mean(np.diff(ls))/fs

    fig, ax = plt.subplots()
    ax.plot(rxx)
    ax.plot(ls,rxx[ls],"v", label="Periodo: {}s".format(period))
    ax.plot(np.where(rxx == max(rxx[ls]))[0][0],max(rxx[ls]),"X", label="Energía: {}".format(max(rxx[ls])))
    ax.plot()

    ax.legend()

    f = io.BytesIO()
    plt.savefig(f, format="png")
    return f.getvalue()