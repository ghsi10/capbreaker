package com.services;

import java.util.ArrayList;

import javax.activation.UnsupportedDataTypeException;

import org.springframework.stereotype.Component;

import com.models.Handshake;

@Component
public class HashConvert {

	public Handshake convert(byte[] cap, String bssid, String essid) throws UnsupportedDataTypeException {
		ArrayList<Handshake> handshakes = readCap(cap);
		if (handshakes.size() == 0)
			throw new UnsupportedDataTypeException("No handshake found!");
		if (!essid.equals("") || !bssid.equals(""))
			for (Handshake handshake : handshakes)
				if (handshake.getEssid().equals(essid) || handshake.getBssid().equals(bssid))
					return handshake;
		return handshakes.get(0);
	}

	private ArrayList<Handshake> readCap(byte[] cap) throws UnsupportedDataTypeException {
		ArrayList<Handshake> handshakes = new ArrayList<Handshake>();
		int recordIndex = 0;
		boolean useLittleEndian;
		int totalBytes;
		int packetLength;
		int currentByte;
		String tmpBssid;
		String tmpEssid;
		boolean ssidIsBlank;
		int nonQosOffset;
		int eapolLengthToUse;
		totalBytes = cap.length;
		if (totalBytes < 40)
			throw new UnsupportedDataTypeException("Invalid File");
		// GLOBAL HEADER (24 bytes)
		if (!((cap[0] & 0xff) == 212 && (cap[1] & 0xff) == 195 && (cap[2] & 0xff) == 178 && (cap[3] & 0xff) == 161)
				|| ((cap[0] & 0xff) == 77 && (cap[1] & 0xff) == 60 && (cap[2] & 0xff) == 178 && (cap[3] & 0xff) == 161)
				|| ((cap[0] & 0xff) == 161 && (cap[1] & 0xff) == 178 && (cap[2] & 0xff) == 195
						&& (cap[3] & 0xff) == 212)
				|| ((cap[0] & 0xff) == 161 && (cap[1] & 0xff) == 178 && (cap[2] & 0xff) == 60 && (cap[3] & 0xff) == 77)
				|| ((cap[0] & 0xff) == 52 && (cap[1] & 0xff) == 205 && (cap[2] & 0xff) == 178 && (cap[3] & 0xff) == 161)
				|| ((cap[0] & 0xff) == 161 && (cap[1] & 0xff) == 178 && (cap[2] & 0xff) == 205
						&& (cap[3] & 0xff) == 52))
			throw new UnsupportedDataTypeException("Invalid file signature");

		useLittleEndian = ((cap[0] & 0xff) == 212 && (cap[1] & 0xff) == 195 && (cap[2] & 0xff) == 178
				&& (cap[3] & 0xff) == 161)
				|| (cap[0] & 0xff) == 77 && (cap[1] & 0xff) == 60 && (cap[2] & 0xff) == 178 && (cap[3] & 0xff) == 161;

		if (!(((useLittleEndian && (cap[20] & 0xff) == 105) || (!useLittleEndian && (cap[23] & 0xff) == 105))
				|| (useLittleEndian && (cap[20] & 0xff) == 119) || (!useLittleEndian && (cap[23] & 0xff) == 119)
				|| (useLittleEndian && (cap[20] & 0xff) == 127) || (!useLittleEndian && (cap[23] & 0xff) == 127)
				|| (useLittleEndian && (cap[20] & 0xff) == 163) || (!useLittleEndian && (cap[23] & 0xff) == 163)))
			throw new UnsupportedDataTypeException("Invalid Link Layer");
		// COUNT UNIQUE BSSIDS
		currentByte = 24;
		while (currentByte < totalBytes) {
			if (useLittleEndian)
				packetLength = bytes2num(cap[(currentByte + 8)], cap[(currentByte + 9)]);
			else
				packetLength = bytes2num(cap[(currentByte + 10)], cap[(currentByte + 11)]);
			currentByte = currentByte + 16;
			if (packetLength > 0) {
				// BEACON FRAME
				if ((cap[currentByte] & 0xff) == 128) {
					tmpBssid = dec2hex((cap[(currentByte + 16)] & 0xff)) + ":"
							+ dec2hex((cap[(currentByte + 17)] & 0xff)) + ":"
							+ dec2hex((cap[(currentByte + 18)] & 0xff)) + ":"
							+ dec2hex((cap[(currentByte + 19)] & 0xff)) + ":"
							+ dec2hex((cap[(currentByte + 20)] & 0xff)) + ":"
							+ dec2hex((cap[(currentByte + 21)] & 0xff));
					if (!findBssid(handshakes, tmpBssid))
						handshakes.add(new Handshake(tmpBssid));
				}
				// PROBE RESPONSE
				if ((cap[currentByte] & 0xff) == 80) {
					tmpBssid = dec2hex((cap[(currentByte + 16)] & 0xff)) + ":"
							+ dec2hex((cap[(currentByte + 17)] & 0xff)) + ":"
							+ dec2hex((cap[(currentByte + 18)] & 0xff)) + ":"
							+ dec2hex((cap[(currentByte + 19)] & 0xff)) + ":"
							+ dec2hex((cap[(currentByte + 20)] & 0xff)) + ":"
							+ dec2hex((cap[(currentByte + 21)] & 0xff));
					if (!findBssid(handshakes, tmpBssid))
						handshakes.add(new Handshake(tmpBssid));
				}

				// MESSAGE 1 of 4
				if ((((cap[currentByte] & 0xff) == 136) && ((((cap[(currentByte + 1)] & 0xff) == 2)
						|| ((cap[(currentByte + 1)] & 0xff) == 10))
						&& (((cap[(currentByte + 32)] & 0xff) == 136) && ((cap[(currentByte + 33)] & 0xff) == 142))))
						|| (((cap[currentByte] & 0xff) == 8)
								&& ((((cap[(currentByte + 1)] & 0xff) == 2) || ((cap[(currentByte + 1)] & 0xff) == 10))
										&& (((cap[(currentByte + 30)] & 0xff) == 136)
												&& ((cap[(currentByte + 31)] & 0xff) == 142))))) {
					tmpBssid = dec2hex(cap[(currentByte + (9 + 1))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (9 + 2))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (9 + 3))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (9 + 4))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (9 + 5))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (9 + 6))] & 0xff);
					if (!findBssid(handshakes, tmpBssid))
						handshakes.add(new Handshake(tmpBssid));
					// Message 2 of 4
				} else if ((((cap[currentByte] & 0xff) == 136) && ((((cap[(currentByte + 1)] & 0xff) == 1)
						|| ((cap[(currentByte + 1)] & 0xff) == 9))
						&& (((cap[(currentByte + 32)] & 0xff) == 136) && ((cap[(currentByte + 33)] & 0xff) == 142))))
						|| (((cap[currentByte] & 0xff) == 8)
								&& ((((cap[(currentByte + 1)] & 0xff) == 1) || ((cap[(currentByte + 1)] & 0xff) == 9))
										&& (((cap[(currentByte + 30)] & 0xff) == 136)
												&& ((cap[(currentByte + 31)] & 0xff) == 142))))) {
					tmpBssid = dec2hex(cap[(currentByte + (3 + 1))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (3 + 2))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (3 + 3))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (3 + 4))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (3 + 5))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (3 + 6))] & 0xff);
					if (!findBssid(handshakes, tmpBssid))
						handshakes.add(new Handshake(tmpBssid));
				}
				// move to next packet
				currentByte += packetLength;
			}
		}
		if (handshakes.size() == 0)
			throw new UnsupportedDataTypeException("No BSSIDs found!");
		currentByte = 24;
		while ((currentByte < totalBytes)) {
			if (useLittleEndian)
				packetLength = bytes2num(cap[(currentByte + 8)], cap[(currentByte + 9)]);
			else
				packetLength = bytes2num(cap[(currentByte + 10)], cap[(currentByte + 11)]);
			currentByte += 16;
			// PACKET DATA (variable length)
			if (packetLength > 0) {
				// beacon frame
				if (((cap[currentByte] & 0xff) == 128)) {
					// grab BSSID
					tmpBssid = dec2hex(cap[(currentByte + (15 + 1))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (15 + 2))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (15 + 3))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (15 + 4))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (15 + 5))] & 0xff) + ":"
							+ dec2hex(cap[(currentByte + (15 + 6))] & 0xff);
					// find this BSSIDs index in the list
					for (int i = 0; i < handshakes.size(); i++) {
						if (handshakes.get(i).getBssid().equals(tmpBssid)) {
							recordIndex = i;
							break;
						}
					}
					// grab SSID
					if (((cap[(currentByte + 37)] & 0xff) > 0) && ((cap[(currentByte + 37)] & 0xff) <= 36)) {
						ssidIsBlank = true;
						for (int i = 1; i <= (cap[(currentByte + 37)] & 0xff); i++) {
							if ((cap[(currentByte + (37 + i))] & 0xff) != 0) {
								ssidIsBlank = false;
								break;
							}
						}
						if (!ssidIsBlank) {
							if (handshakes.get(recordIndex).getEssid().equals("")) {
								tmpEssid = "";
								for (int i = 1; i <= (cap[(currentByte + 37)] & 0xff); i++)
									tmpEssid += (char) (cap[currentByte + (37 + i)] & 0xff);
								handshakes.get(recordIndex).setEssid(tmpEssid);
							}
						}
					}
				}
			}
			// PROBE RESPONSE
			if ((cap[currentByte] & 0xff) == 80) {
				// grab BSSID
				tmpBssid = dec2hex(cap[(currentByte + (15 + 1))] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + (15 + 2))] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + (15 + 3))] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + (15 + 4))] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + (15 + 5))] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + (15 + 6))] & 0xff);
				// find this BSSIDs index in the array
				for (int i = 0; i < handshakes.size(); i++) {
					if (handshakes.get(i).getBssid().equals(tmpBssid)) {
						recordIndex = i;
						break;
					}
				}
				// grab SSID
				if (((cap[(currentByte + 37)] & 0xff) > 0) && ((cap[(currentByte + 37)] & 0xff) <= 36)) {
					if (handshakes.get(recordIndex).getEssid().equals("")) {
						tmpEssid = "";
						for (int i = 1; i <= (cap[(currentByte + 37)] & 0xff); i++)
							tmpEssid += (char) (cap[currentByte + (37 + i)] & 0xff);
						handshakes.get(recordIndex).setEssid(tmpEssid);
					}
				}
			}
			// Message 1 of 4
			if ((((cap[currentByte] & 0xff) == 136) && ((((cap[(currentByte + 1)] & 0xff) == 2)
					|| ((cap[(currentByte + 1)] & 0xff) == 10))
					&& (((cap[(currentByte + 32)] & 0xff) == 136) && ((cap[(currentByte + 33)] & 0xff) == 142))))
					|| (((cap[currentByte] & 0xff) == 8)
							&& ((((cap[(currentByte + 1)] & 0xff) == 2) || ((cap[(currentByte + 1)] & 0xff) == 10))
									&& (((cap[(currentByte + 30)] & 0xff) == 136)
											&& ((cap[(currentByte + 31)] & 0xff) == 142))))) {
				if ((cap[currentByte] & 0xff) == 8)
					nonQosOffset = 2;
				else
					nonQosOffset = 0;
				// BSSID (bytes 11 to 16)
				tmpBssid = dec2hex(cap[(currentByte + (9 + 1))] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + (9 + 2))] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + (9 + 3))] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + (9 + 4))] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + (9 + 5))] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + (9 + 6))] & 0xff);
				// find this BSSIDs index in the array
				for (int i = 0; i < handshakes.size(); i++)
					if (handshakes.get(i).getBssid().equals(tmpBssid)) {
						recordIndex = i;
						break;
					}
				// Station Address
				// Receiver Address (bytes 5 to 10)
				if (handshakes.get(recordIndex).getSnonce().equals("")) {
					handshakes.get(recordIndex).setStation("");
					handshakes.get(recordIndex).setAnonce("");
					if (Integer.valueOf(dec2hex(cap[currentByte + (36 - nonQosOffset)] & 0xff)
							+ dec2hex(cap[currentByte + 37 - nonQosOffset] & 0xff), 16) < 118) {
						handshakes.get(recordIndex)
								.setStation(dec2hex(cap[(currentByte + (3 + 1))] & 0xff) + ":"
										+ dec2hex(cap[(currentByte + (3 + 2))] & 0xff) + ":"
										+ dec2hex(cap[(currentByte + (3 + 3))] & 0xff) + ":"
										+ dec2hex(cap[(currentByte + (3 + 4))] & 0xff) + ":"
										+ dec2hex(cap[(currentByte + (3 + 5))] & 0xff) + ":"
										+ dec2hex(cap[(currentByte + (3 + 6))] & 0xff));

						// ANONCE
						if (!(((cap[(currentByte + 51)] & 0xff) == 0) && ((cap[(currentByte + 52)] & 0xff) == 0)
								&& ((cap[(currentByte + 53)] & 0xff) == 0) && ((cap[(currentByte + 54)] & 0xff) == 0)
								&& ((cap[(currentByte + 55)] & 0xff) == 0) && ((cap[(currentByte + 56)] & 0xff) == 0)
								&& ((cap[(currentByte + 57)] & 0xff) == 0) && ((cap[(currentByte + 58)] & 0xff) == 0)
								&& ((cap[(currentByte + 59)] & 0xff) == 0) && ((cap[(currentByte + 60)] & 0xff) == 0)
								&& ((cap[(currentByte + 61)] & 0xff) == 0) && ((cap[(currentByte + 62)] & 0xff) == 0)
								&& ((cap[(currentByte + 63)] & 0xff) == 0) && ((cap[(currentByte + 64)] & 0xff) == 0)
								&& ((cap[(currentByte + 65)] & 0xff) == 0) && ((cap[(currentByte + 66)] & 0xff) == 0)
								&& ((cap[(currentByte + 67)] & 0xff) == 0) && ((cap[(currentByte + 68)] & 0xff) == 0)
								&& ((cap[(currentByte + 69)] & 0xff) == 0) && ((cap[(currentByte + 70)] & 0xff) == 0)
								&& ((cap[(currentByte + 71)] & 0xff) == 0) && ((cap[(currentByte + 72)] & 0xff) == 0)
								&& ((cap[(currentByte + 73)] & 0xff) == 0) && ((cap[(currentByte + 74)] & 0xff) == 0)
								&& ((cap[(currentByte + 75)] & 0xff) == 0) && ((cap[(currentByte + 76)] & 0xff) == 0)
								&& ((cap[(currentByte + 77)] & 0xff) == 0) && ((cap[(currentByte + 78)] & 0xff) == 0)
								&& ((cap[(currentByte + 79)] & 0xff) == 0) && ((cap[(currentByte + 80)] & 0xff) == 0)
								&& ((cap[(currentByte + 81)] & 0xff) == 0) && ((cap[(currentByte + 82)] & 0xff) == 0)))

							for (int i = 1; i < 33; i++) {
								// ANONCE (bytes 52 to 83)
								handshakes.get(recordIndex).setAnonce(handshakes.get(recordIndex).getAnonce()
										+ dec2hex(cap[currentByte + ((50 - nonQosOffset) + i)] & 0xff));
								if (i < 32)
									handshakes.get(recordIndex)
											.setAnonce(handshakes.get(recordIndex).getAnonce() + " ");
							}
					}
				}
				// Message 2 of 4
			} else if ((((cap[currentByte] & 0xff) == 136) && ((((cap[(currentByte + 1)] & 0xff) == 1)
					|| ((cap[(currentByte + 1)] & 0xff) == 9))
					&& (((cap[(currentByte + 32)] & 0xff) == 136) && ((cap[(currentByte + 33)] & 0xff) == 142))))
					|| (((cap[currentByte] & 0xff) == 8)
							&& ((((cap[(currentByte + 1)] & 0xff) == 1) || ((cap[(currentByte + 1)] & 0xff) == 9))
									&& (((cap[(currentByte + 30)] & 0xff) == 136)
											&& ((cap[(currentByte + 31)] & 0xff) == 142))))) {
				if ((cap[currentByte] & 0xff) == 8)
					nonQosOffset = 2;
				else
					nonQosOffset = 0;
				// BSSID (bytes 5 to 10)
				tmpBssid = dec2hex(cap[(currentByte + 4)] & 0xff) + ":" + dec2hex(cap[(currentByte + 5)] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + 6)] & 0xff) + ":" + dec2hex(cap[(currentByte + 7)] & 0xff) + ":"
						+ dec2hex(cap[(currentByte + 8)] & 0xff) + ":" + dec2hex(cap[(currentByte + (3 + 6))] & 0xff);
				// find this BSSIDs index in the array
				for (int i = 0; i < handshakes.size(); i++) {
					if (handshakes.get(i).getBssid().equals(tmpBssid)) {
						recordIndex = i;
						break;
					}
				}
				if (!(((cap[(currentByte + (51 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (52 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (53 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (54 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (55 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (56 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (57 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (58 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (59 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (60 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (61 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (62 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (63 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (64 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (65 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (66 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (67 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (68 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (69 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (70 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (71 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (72 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (73 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (74 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (75 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (76 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (77 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (78 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (79 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (80 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (81 - nonQosOffset))] & 0xff) == 0)
						&& ((cap[(currentByte + (82 - nonQosOffset))] & 0xff) == 0))) {
					// EAPOL
					if (packetLength > (34 - nonQosOffset)) {
						// SNONCE
						if (!handshakes.get(recordIndex).getAnonce().equals("")
								&& !handshakes.get(recordIndex).getStation().equals("")
								&& handshakes.get(recordIndex).getSnonce().equals("")) {
							for (int i = 1; i <= 32; i++) {
								// SNONCE (bytes 52 to 83)
								handshakes.get(recordIndex).setSnonce(handshakes.get(recordIndex).getSnonce()
										+ dec2hex(cap[(currentByte + ((50 - nonQosOffset) + i))] & 0xff));
								if (i < 32)
									handshakes.get(recordIndex)
											.setSnonce(handshakes.get(recordIndex).getSnonce() + " ");
							}
							handshakes.get(recordIndex)
									.setEapolSize(Integer
											.valueOf(dec2hex(cap[currentByte + (36 - nonQosOffset)] & 0xff)
													+ dec2hex(cap[currentByte + 37 - nonQosOffset] & 0xff), 16)
											+ 4 + "");
							if (Integer.valueOf(handshakes.get(recordIndex).getEapolSize()) > 0)
								eapolLengthToUse = Integer.valueOf(handshakes.get(recordIndex).getEapolSize());
							else
								eapolLengthToUse = packetLength - (34 - nonQosOffset);
							for (int i = 1; i <= eapolLengthToUse; i++) {
								// Key Version
								if (i == 7)
									handshakes.get(recordIndex)
											.setKeyVersion(
													"" + (Integer
															.valueOf(dec2hex(
																	cap[(currentByte + ((33 - nonQosOffset) + (i - 1)))]
																			& 0xff)
																	+ ""
																	+ dec2hex(cap[(currentByte
																			+ ((33 - nonQosOffset) + i))] & 0xff),
																	16)
															& 7));
								// Key MIC
								if ((i > 81) && (i < 98)) {
									handshakes.get(recordIndex).setEapol(handshakes.get(recordIndex).getEapol() + "00");
									handshakes.get(recordIndex).setKeyMic(handshakes.get(recordIndex).getKeyMic()
											+ dec2hex(cap[(currentByte + ((33 - nonQosOffset) + i))] & 0xff));
									if ((i < 97))
										handshakes.get(recordIndex)
												.setKeyMic(handshakes.get(recordIndex).getKeyMic() + " ");
								} else
									handshakes.get(recordIndex).setEapol(handshakes.get(recordIndex).getEapol()
											+ dec2hex(cap[(currentByte + ((33 - nonQosOffset) + i))] & 0xff));
								if ((i < eapolLengthToUse))
									handshakes.get(recordIndex).setEapol(handshakes.get(recordIndex).getEapol() + " ");
							}
						}
					}
				}
			}
			currentByte += packetLength;
			// move to next packet
		}
		ArrayList<Handshake> returnValue = new ArrayList<Handshake>();
		for (Handshake handshake : handshakes) {
			if (!(handshake.getEssid().equals("") || handshake.getBssid().equals("")
					|| handshake.getStation().equals("") || handshake.getSnonce().equals("")
					|| handshake.getAnonce().equals("") || handshake.getEapol().equals("")
					|| handshake.getEapolSize().equals("") || handshake.getKeyVersion().equals("")
					|| handshake.getKeyMic().equals("")))
				returnValue.add(handshake);
		}
		return returnValue;

	}

	private int bytes2num(byte loByte, byte hiByte) {
		if ((((hiByte & 0xFF) & 0x80) != 0))
			return (((hiByte & 0xFF) * 0x100) | (loByte & 0xFF)) | 0xFFFF0000;
		return ((hiByte & 0xFF) * 0x100) | (loByte & 0xFF);
	}

	private String dec2hex(int num) {

		if (num / 16 == 0)
			return "0" + Integer.toHexString(num).toUpperCase();
		return Integer.toHexString(num).toUpperCase();
	}

	private boolean findBssid(ArrayList<Handshake> handshakes, String bssid) {
		for (Handshake handshake : handshakes)
			if (handshake.getBssid().equals(bssid))
				return true;
		return false;
	}
}