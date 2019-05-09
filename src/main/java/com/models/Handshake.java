package com.models;

import java.io.Serializable;

public class Handshake implements Serializable {

    private static final long serialVersionUID = 2845682546058658729L;

    private String essid;
    private String bssid;
    private String station;
    private String snonce;
    private String anonce;
    private String eapol;
    private String keyVersion;
    private String keyMic;

    public Handshake() {
        essid = "";
        bssid = "";
        station = "";
        snonce = "";
        anonce = "";
        eapol = "";
        keyVersion = "";
        keyMic = "";
    }

    public Handshake(String bssid) {
        this.bssid = bssid;
        essid = "";
        station = "";
        snonce = "";
        anonce = "";
        eapol = "";
        keyVersion = "";
        keyMic = "";
    }

    public String getEssid() {
        return essid;
    }

    public void setEssid(String essid) {
        this.essid = essid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getSnonce() {
        return snonce;
    }

    public void setSnonce(String snonce) {
        this.snonce = snonce;
    }

    public String getAnonce() {
        return anonce;
    }

    public void setAnonce(String anonce) {
        this.anonce = anonce;
    }

    public String getEapol() {
        return eapol;
    }

    public void setEapol(String eapol) {
        this.eapol = eapol;
    }

    public String getKeyVersion() {
        return keyVersion;
    }

    public void setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
    }

    public String getKeyMic() {
        return keyMic;
    }

    public void setKeyMic(String keyMic) {
        this.keyMic = keyMic;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Handshake) {
            Handshake handshake = (Handshake) obj;
            return handshake.anonce.equals(anonce) && handshake.bssid.equals(bssid) && handshake.eapol.equals(eapol)
                    && handshake.essid.equals(essid) && handshake.keyMic.equals(keyMic)
                    && handshake.keyVersion.equals(keyVersion) && handshake.snonce.equals(snonce)
                    && handshake.station.equals(station);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Handshake:\n essid=" + essid + "\n bssid=" + bssid + "\n station=" + station + "\n snonce=" + snonce
                + "\n anonce=" + anonce + "\n eapol=" + eapol + "\n keyVersion=" + keyVersion + "\n keyMic=" + keyMic;
    }
}
