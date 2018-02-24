package com.services;

import java.util.ArrayList;
import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import org.springframework.stereotype.Component;

import com.models.Handshake;

@Component
public class HashConvert {

	public Handshake convert(byte[] cap, String bssid, String essid) throws UnsupportedDataTypeException {
		List<Handshake> handshakes = readCap(cap);
		if (handshakes.size() == 0)
			throw new UnsupportedDataTypeException("No handshake found!");
		if (!essid.equals("") || !bssid.equals(""))
			for (Handshake handshake : handshakes)
				if (handshake.getEssid().equals(essid) || handshake.getBssid().equals(bssid))
					return handshake;
		return handshakes.get(0);
	}

	private List<Handshake> readCap(byte[] cap) throws UnsupportedDataTypeException {
		ArrayList<Handshake> handshakes = new ArrayList<Handshake>();
		Handshake recordHandshake;
		int packetLength;
		int currentByte;
		int nonQosOffset;
		int totalBytes = cap.length;
		if (totalBytes < 40)
			throw new UnsupportedDataTypeException("Invalid File");
		// GLOBAL HEADER (24 bytes)

		boolean useLittleEndian = (cap[0] & 0xff) == 212 && (cap[1] & 0xff) == 195 && (cap[2] & 0xff) == 178
				&& (cap[3] & 0xff) == 161
				|| (cap[0] & 0xff) == 77 && (cap[1] & 0xff) == 60 && (cap[2] & 0xff) == 178 && (cap[3] & 0xff) == 161;

		if (!(useLittleEndian
				|| (cap[0] & 0xff) == 161 && (cap[1] & 0xff) == 178 && (cap[2] & 0xff) == 195 && (cap[3] & 0xff) == 212
				|| (cap[0] & 0xff) == 161 && (cap[1] & 0xff) == 178 && (cap[2] & 0xff) == 60 && (cap[3] & 0xff) == 77
				|| (cap[0] & 0xff) == 52 && (cap[1] & 0xff) == 205 && (cap[2] & 0xff) == 178 && (cap[3] & 0xff) == 161
				|| (cap[0] & 0xff) == 161 && (cap[1] & 0xff) == 178 && (cap[2] & 0xff) == 205 && (cap[3] & 0xff) == 52))
			throw new UnsupportedDataTypeException("Invalid file signature");

		if (!(useLittleEndian && (cap[20] & 0xff) == 105 || !useLittleEndian && (cap[23] & 0xff) == 105
				|| useLittleEndian && (cap[20] & 0xff) == 119 || !useLittleEndian && (cap[23] & 0xff) == 119
				|| useLittleEndian && (cap[20] & 0xff) == 127 || !useLittleEndian && (cap[23] & 0xff) == 127
				|| useLittleEndian && (cap[20] & 0xff) == 163 || !useLittleEndian && (cap[23] & 0xff) == 163))
			throw new UnsupportedDataTypeException("Invalid Link Layer");
		// COUNT UNIQUE BSSIDS
		currentByte = 24;
		int offset = 10;
		if (useLittleEndian)
			offset = 8;
		while (currentByte < totalBytes) {
			packetLength = bytes2num(cap[currentByte + offset], cap[currentByte + offset + 1]);
			currentByte += 16;
			if (packetLength > 0) {
				// BEACON FRAME + PROBE RESPONSE
				if ((cap[currentByte] & 0xff) == 128 || (cap[currentByte] & 0xff) == 80)
					addBssid(handshakes, readBssid(cap, currentByte, 16));
				// MESSAGE 1 of 4
				if ((cap[currentByte] & 0xff) == 136
						&& ((cap[currentByte + 1] & 0xff) == 2 || (cap[currentByte + 1] & 0xff) == 10)
						&& (cap[currentByte + 32] & 0xff) == 136 && (cap[currentByte + 33] & 0xff) == 142
						|| (cap[currentByte] & 0xff) == 8
								&& ((cap[currentByte + 1] & 0xff) == 2 || (cap[currentByte + 1] & 0xff) == 10)
								&& (cap[currentByte + 30] & 0xff) == 136 && (cap[currentByte + 31] & 0xff) == 142)
					addBssid(handshakes, readBssid(cap, currentByte, 10));
				// Message 2 of 4
				else if ((cap[currentByte] & 0xff) == 136
						&& ((cap[currentByte + 1] & 0xff) == 1 || (cap[currentByte + 1] & 0xff) == 9)
						&& (cap[currentByte + 32] & 0xff) == 136 && (cap[currentByte + 33] & 0xff) == 142
						|| (cap[currentByte] & 0xff) == 8
								&& ((cap[currentByte + 1] & 0xff) == 1 || (cap[currentByte + 1] & 0xff) == 9)
								&& (cap[currentByte + 30] & 0xff) == 136 && (cap[currentByte + 31] & 0xff) == 142)
					addBssid(handshakes, readBssid(cap, currentByte, 4));
				// move to next packet
				currentByte += packetLength;
			}
		}
		if (handshakes.size() == 0)
			throw new UnsupportedDataTypeException("No BSSIDs found!");
		currentByte = 24;
		while (currentByte < totalBytes) {
			packetLength = bytes2num(cap[currentByte + offset], cap[currentByte + offset + 1]);
			currentByte += 16;
			// PACKET DATA (variable length)
			if (packetLength > 0)
				// beacon frame
				if ((cap[currentByte] & 0xff) == 128) {
					// grab BSSID and find this BSSIDs index in the list
					recordHandshake = foundHsInList(handshakes, readBssid(cap, currentByte, 16));
					// grab SSID
					if ((cap[currentByte + 37] & 0xff) > 0 && (cap[currentByte + 37] & 0xff) <= 36) {
						boolean ssidIsBlank = true;
						for (int i = 1; i <= (cap[currentByte + 37] & 0xff); i++)
							if ((cap[currentByte + 37 + i] & 0xff) != 0) {
								ssidIsBlank = false;
								break;
							}
						if (!ssidIsBlank)
							if (recordHandshake.getEssid().equals("")) {
								String tmpEssid = "";
								for (int i = 1; i <= (cap[currentByte + 37] & 0xff); i++)
									tmpEssid += (char) (cap[currentByte + 37 + i] & 0xff);
								recordHandshake.setEssid(tmpEssid);
							}
					}
				}
			// PROBE RESPONSE
			if ((cap[currentByte] & 0xff) == 80) {
				// grab BSSID find this BSSIDs index in the array
				recordHandshake = foundHsInList(handshakes, readBssid(cap, currentByte, 16));
				// grab SSID
				if ((cap[currentByte + 37] & 0xff) > 0 && (cap[currentByte + 37] & 0xff) <= 36)
					if (recordHandshake.getEssid().equals("")) {
						String tmpEssid = "";
						for (int i = 1; i <= (cap[currentByte + 37] & 0xff); i++)
							tmpEssid += (char) (cap[currentByte + 37 + i] & 0xff);
						recordHandshake.setEssid(tmpEssid);
					}
			}
			// Message 1 of 4
			if ((cap[currentByte] & 0xff) == 136
					&& ((cap[currentByte + 1] & 0xff) == 2 || (cap[currentByte + 1] & 0xff) == 10)
					&& (cap[currentByte + 32] & 0xff) == 136 && (cap[currentByte + 33] & 0xff) == 142
					|| (cap[currentByte] & 0xff) == 8
							&& ((cap[currentByte + 1] & 0xff) == 2 || (cap[currentByte + 1] & 0xff) == 10)
							&& (cap[currentByte + 30] & 0xff) == 136 && (cap[currentByte + 31] & 0xff) == 142) {
				nonQosOffset = (cap[currentByte] & 0xff) == 8 ? 2 : 0;
				// BSSID (bytes 11 to 16) and find this BSSIDs index in the array
				recordHandshake = foundHsInList(handshakes, readBssid(cap, currentByte, 10));
				// Station Address
				// Receiver Address (bytes 5 to 10)
				if (recordHandshake.getSnonce().equals("")) {
					recordHandshake.setStation("");
					recordHandshake.setAnonce("");
					if (Integer.valueOf(dec2hex(cap[currentByte + 36 - nonQosOffset] & 0xff)
							+ dec2hex(cap[currentByte + 37 - nonQosOffset] & 0xff), 16) < 118) {
						recordHandshake.setStation(dec2hex(cap[currentByte + 3 + 1] & 0xff) + ":"
								+ dec2hex(cap[currentByte + 5] & 0xff) + ":" + dec2hex(cap[currentByte + 6] & 0xff)
								+ ":" + dec2hex(cap[currentByte + 7] & 0xff) + ":"
								+ dec2hex(cap[currentByte + 8] & 0xff) + ":" + dec2hex(cap[currentByte + 9] & 0xff));
						// ANONCE
						if (!((cap[currentByte + 51] & 0xff) == 0 && (cap[currentByte + 52] & 0xff) == 0
								&& (cap[currentByte + 53] & 0xff) == 0 && (cap[currentByte + 54] & 0xff) == 0
								&& (cap[currentByte + 55] & 0xff) == 0 && (cap[currentByte + 56] & 0xff) == 0
								&& (cap[currentByte + 57] & 0xff) == 0 && (cap[currentByte + 58] & 0xff) == 0
								&& (cap[currentByte + 59] & 0xff) == 0 && (cap[currentByte + 60] & 0xff) == 0
								&& (cap[currentByte + 61] & 0xff) == 0 && (cap[currentByte + 62] & 0xff) == 0
								&& (cap[currentByte + 63] & 0xff) == 0 && (cap[currentByte + 64] & 0xff) == 0
								&& (cap[currentByte + 65] & 0xff) == 0 && (cap[currentByte + 66] & 0xff) == 0
								&& (cap[currentByte + 67] & 0xff) == 0 && (cap[currentByte + 68] & 0xff) == 0
								&& (cap[currentByte + 69] & 0xff) == 0 && (cap[currentByte + 70] & 0xff) == 0
								&& (cap[currentByte + 71] & 0xff) == 0 && (cap[currentByte + 72] & 0xff) == 0
								&& (cap[currentByte + 73] & 0xff) == 0 && (cap[currentByte + 74] & 0xff) == 0
								&& (cap[currentByte + 75] & 0xff) == 0 && (cap[currentByte + 76] & 0xff) == 0
								&& (cap[currentByte + 77] & 0xff) == 0 && (cap[currentByte + 78] & 0xff) == 0
								&& (cap[currentByte + 79] & 0xff) == 0 && (cap[currentByte + 80] & 0xff) == 0
								&& (cap[currentByte + 81] & 0xff) == 0 && (cap[currentByte + 82] & 0xff) == 0))
							// ANONCE (bytes 52 to 83)
							for (int i = 1; i < 33; i++)
								recordHandshake.setAnonce(recordHandshake.getAnonce()
										+ dec2hex(cap[currentByte + 50 - nonQosOffset + i] & 0xff));
					}
				}
				// Message 2 of 4
			} else if ((cap[currentByte] & 0xff) == 136
					&& ((cap[currentByte + 1] & 0xff) == 1 || (cap[currentByte + 1] & 0xff) == 9)
					&& (cap[currentByte + 32] & 0xff) == 136 && (cap[currentByte + 33] & 0xff) == 142
					|| (cap[currentByte] & 0xff) == 8
							&& ((cap[currentByte + 1] & 0xff) == 1 || (cap[currentByte + 1] & 0xff) == 9)
							&& (cap[currentByte + 30] & 0xff) == 136 && (cap[currentByte + 31] & 0xff) == 142) {
				nonQosOffset = (cap[currentByte] & 0xff) == 8 ? 2 : 0;
				// BSSID (bytes 5 to 10) and find this BSSIDs index in the array
				recordHandshake = foundHsInList(handshakes, readBssid(cap, currentByte, 4));
				if (!((cap[currentByte + 51 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 52 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 53 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 54 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 55 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 56 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 57 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 58 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 59 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 60 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 61 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 62 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 63 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 64 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 65 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 66 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 67 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 68 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 69 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 70 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 71 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 72 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 73 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 74 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 75 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 76 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 77 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 78 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 79 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 80 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 81 - nonQosOffset] & 0xff) == 0
						&& (cap[currentByte + 82 - nonQosOffset] & 0xff) == 0))
					// EAPOL
					if (packetLength > 34 - nonQosOffset)
						// SNONCE
						if (!recordHandshake.getAnonce().equals("") && !recordHandshake.getStation().equals("")
								&& recordHandshake.getSnonce().equals("")) {
							// SNONCE (bytes 52 to 83)
							for (int i = 1; i <= 32; i++)
								recordHandshake.setSnonce(recordHandshake.getSnonce()
										+ dec2hex(cap[currentByte + 50 - nonQosOffset + i] & 0xff));
							int eapolSize = Integer.valueOf(dec2hex(cap[currentByte + 36 - nonQosOffset] & 0xff)
									+ dec2hex(cap[currentByte + 37 - nonQosOffset] & 0xff), 16) + 4;
							if (eapolSize <= 0)
								eapolSize = packetLength - (34 - nonQosOffset);
							for (int i = 1; i <= eapolSize; i++) {
								// Key Version
								if (i == 7)
									recordHandshake
											.setKeyVersion("0" + (Integer.valueOf(
													dec2hex(cap[currentByte + 33 - nonQosOffset + i - 1] & 0xff) + ""
															+ dec2hex(cap[currentByte + 33 - nonQosOffset + i] & 0xff),
													16) & 7));
								// Key MIC
								if (i > 81 && i < 98) {
									recordHandshake.setEapol(recordHandshake.getEapol() + "00");
									recordHandshake.setKeyMic(recordHandshake.getKeyMic()
											+ dec2hex(cap[currentByte + 33 - nonQosOffset + i] & 0xff));
								} else
									recordHandshake.setEapol(recordHandshake.getEapol()
											+ dec2hex(cap[currentByte + 33 - nonQosOffset + i] & 0xff));
							}
						}
			}
			currentByte += packetLength;
			// move to next packet
		}
		ArrayList<Handshake> returnValue = new ArrayList<Handshake>();
		for (Handshake handshake : handshakes)
			if (!(handshake.getEssid().equals("") || handshake.getBssid().equals("")
					|| handshake.getStation().equals("") || handshake.getSnonce().equals("")
					|| handshake.getAnonce().equals("") || handshake.getEapol().equals("")
					|| handshake.getKeyVersion().equals("") || handshake.getKeyMic().equals("")))
				returnValue.add(handshake);
		return returnValue;
	}

	private Handshake foundHsInList(List<Handshake> handshakes, String bssid) throws UnsupportedDataTypeException {
		for (Handshake handshake : handshakes)
			if (handshake.getBssid().equals(bssid))
				return handshake;
		throw new UnsupportedDataTypeException("Unexpected error");
	}

	private int bytes2num(byte loByte, byte hiByte) {
		if ((hiByte & 0xFF & 0x80) != 0)
			return (hiByte & 0xFF) * 0x100 | loByte & 0xFF | 0xFFFF0000;
		return (hiByte & 0xFF) * 0x100 | loByte & 0xFF;
	}

	private String dec2hex(int num) {

		if (num / 16 == 0)
			return "0" + Integer.toHexString(num).toUpperCase();
		return Integer.toHexString(num).toUpperCase();
	}

	private void addBssid(ArrayList<Handshake> handshakes, String bssid) {
		for (Handshake handshake : handshakes)
			if (handshake.getBssid().equals(bssid))
				return;
		handshakes.add(new Handshake(bssid));
	}

	private String readBssid(byte[] cap, int currentByte, int offset) {
		String bssid = "";
		for (int i = offset; i < offset + 5; i++)
			bssid += dec2hex(cap[currentByte + i] & 0xff) + ":";
		bssid += dec2hex(cap[currentByte + offset + 5] & 0xff);
		return bssid;
	}
}