import io
import matplotlib.pyplot as plt
import numpy as np
from scipy.signal import find_peaks
from decimal import Decimal, ROUND_05UP

ts=200000*10**(-6)
fs=1/ts

def normalize(data):
    data=data-np.mean(data)
    data=data/np.max(abs(data))
    return data

def plot_autocorrelation(data):
    data=normalize(data)
    rxx=np.correlate(data,data,"full")
    ls, _ = find_peaks(rxx)
    period = np.mean(np.diff(ls))/fs

    fig, ax = plt.subplots()
    tau=np.arange(-len(data)+1,len(data),1)
    ax.plot(tau,rxx)
    ax.plot(tau[ls],rxx[ls],"v", label="Periodo: {}s".format(period))
    ax.plot(0,max(rxx[ls]),"X", label="Energía: {}".format(max(rxx[ls])))
    plt.xlabel('Muestras')
    plt.ylabel('Amplitud')
    ax.plot()

    ax.legend()

    f = io.BytesIO()
    plt.savefig(f, format="png")
    return f.getvalue()

def distance(datax):
    paso=(0.67+0.762)/2.0
    pos_diff=np.where(np.array(datax)>0.5)
    peaks=[]
    i=1
    peaks.append(pos_diff[0][0])

    while (i<len(pos_diff[0])-1):
        if pos_diff[0][i+1]-pos_diff[0][i]<=2:
            if datax[pos_diff[0][i+1]]>datax[pos_diff[0][i]]:
                peaks.append(pos_diff[0][i+1])
                i=i+2
            else:
                peaks.append(pos_diff[0][i])
                i=i+2
        else:
            peaks.append(pos_diff[0][i])
            i=i+1

    distance=len(peaks)*paso
    return distance

def getDistance(datax):
    dx = distance(normalize(datax))
    return round(dx,2)

def velocity(data):
    diff_data=np.diff(data)
    pos_diff=np.where(diff_data>0.1)
    t_start=pos_diff[0][0]
    diff_data=np.diff(data[::-1])
    pos_diff=np.where(diff_data>0.1)
    t_end=len(data)-pos_diff[0][0]

    t=(t_end-t_start)/fs
    d=distance(data)
    return d/t

def getVelocity(datax):
    v = velocity(normalize(datax))
    return round(v,2)

def getJumps(datay):
    datay = normalize(datay)
    minimos = find_peaks(datay*(-1))[0]
    loc_min = datay[minimos]<-0.6
    return minimos[loc_min].size

