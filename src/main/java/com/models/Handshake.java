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
		this.essid = "";
		this.bssid = "";
		this.station = "";
		this.snonce = "";
		this.anonce = "";
		this.eapol = "";
		this.keyVersion = "";
		this.keyMic = "";
	}

	public Handshake(String bssid) {
		this.essid = "";
		this.bssid = bssid;
		this.station = "";
		this.snonce = "";
		this.anonce = "";
		this.eapol = "";
		this.keyVersion = "";
		this.keyMic = "";
	}

	public Handshake(Handshake hs) {
		this.essid = hs.essid;
		this.bssid = hs.bssid;
		this.station = hs.station;
		this.snonce = hs.snonce;
		this.anonce = hs.anonce;
		this.eapol = hs.eapol;
		this.keyVersion = hs.keyVersion;
		this.keyMic = hs.keyMic;
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
