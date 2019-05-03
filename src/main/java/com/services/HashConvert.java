package com.services;

import com.exceptions.UnsupportedDataTypeException;
import com.models.Handshake;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HashConvert {

    Handshake convertTest(String pmkid) throws UnsupportedDataTypeException {
        //TODO ADD it
        if (pmkid.isEmpty())
            throw new UnsupportedDataTypeException("PMKID is missing");
        throw new UnsupportedDataTypeException("PMKID do not support yet");
    }

    Handshake convertFile(byte[] cap, String bssid, String essid) throws UnsupportedDataTypeException {
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
        int[] intCap = convertToIntArray(cap);
        List<Handshake> handshakes = new ArrayList<>();
        Handshake recordHandshake;
        int packetLength;
        int currentByte;
        int nonQosOffset;
        int totalBytes = intCap.length;
        if (totalBytes < 40)
            throw new UnsupportedDataTypeException("Invalid File");
        // GLOBAL HEADER (24 bytes)
        boolean useLittleEndian = intCap[0] == 212 && intCap[1] == 195 && intCap[2] == 178
                && intCap[3] == 161
                || intCap[0] == 77 && intCap[1] == 60 && intCap[2] == 178 && intCap[3] == 161;

        if (!(useLittleEndian
                || intCap[0] == 161 && intCap[1] == 178 && intCap[2] == 195 && intCap[3] == 212
                || intCap[0] == 161 && intCap[1] == 178 && intCap[2] == 60 && intCap[3] == 77
                || intCap[0] == 52 && intCap[1] == 205 && intCap[2] == 178 && intCap[3] == 161
                || intCap[0] == 161 && intCap[1] == 178 && intCap[2] == 205 && intCap[3] == 52))
            throw new UnsupportedDataTypeException("Invalid file signature");

        if (!(useLittleEndian && intCap[20] == 105 || !useLittleEndian && intCap[23] == 105
                || useLittleEndian && intCap[20] == 119 || !useLittleEndian && intCap[23] == 119
                || useLittleEndian && intCap[20] == 127 || !useLittleEndian && intCap[23] == 127
                || useLittleEndian && intCap[20] == 163 || !useLittleEndian && intCap[23] == 163))
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
                if (intCap[currentByte] == 128 || intCap[currentByte] == 80)
                    addBssid(handshakes, readBssid(cap, currentByte, 16));
                // MESSAGE 1 of 4
                if (isMessage(intCap, currentByte, 1))
                    addBssid(handshakes, readBssid(cap, currentByte, 10));
                    // Message 2 of 4
                else if (isMessage(intCap, currentByte, 2))
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
                if (intCap[currentByte] == 128) {
                    // grab BSSID and find this BSSIDs index in the list
                    recordHandshake = foundHsInList(handshakes, readBssid(cap, currentByte, 16));
                    // grab SSID
                    if (intCap[currentByte + 37] > 0 && intCap[currentByte + 37] <= 36) {
                        boolean ssidIsBlank = true;
                        for (int i = 1; i <= intCap[currentByte + 37]; i++)
                            if (intCap[currentByte + 37 + i] != 0) {
                                ssidIsBlank = false;
                                break;
                            }
                        if (!ssidIsBlank)
                            addEssid(recordHandshake, cap, currentByte);
                    }
                }
            // PROBE RESPONSE
            if (intCap[currentByte] == 80) {
                // grab BSSID find this BSSIDs index in the array
                recordHandshake = foundHsInList(handshakes, readBssid(cap, currentByte, 16));
                // grab SSID
                if (intCap[currentByte + 37] > 0 && intCap[currentByte + 37] <= 36)
                    addEssid(recordHandshake, cap, currentByte);
            }
            // Message 1 of 4
            if (isMessage(intCap, currentByte, 1)) {
                nonQosOffset = intCap[currentByte] == 8 ? 2 : 0;
                // BSSID (bytes 11 to 16) and find this BSSIDs index in the array
                recordHandshake = foundHsInList(handshakes, readBssid(cap, currentByte, 10));
                // Station Address
                // Receiver Address (bytes 5 to 10)
                if (recordHandshake.getSnonce().equals("")) {
                    recordHandshake.setStation("");
                    recordHandshake.setAnonce("");
                    if (Integer.valueOf(dec2hex(intCap[currentByte + 36 - nonQosOffset]) +
                            dec2hex(intCap[currentByte + 37 - nonQosOffset]), 16) < 118) {
                        recordHandshake.setStation(dec2hex(intCap[currentByte + 3 + 1]) + ":"
                                + dec2hex(intCap[currentByte + 5]) + ":" + dec2hex(intCap[currentByte + 6])
                                + ":" + dec2hex(intCap[currentByte + 7]) + ":"
                                + dec2hex(intCap[currentByte + 8]) + ":" + dec2hex(intCap[currentByte + 9]));
                        // ANONCE
                        if (!(intCap[currentByte + 51] == 0 && intCap[currentByte + 52] == 0
                                && intCap[currentByte + 53] == 0 && intCap[currentByte + 54] == 0
                                && intCap[currentByte + 55] == 0 && intCap[currentByte + 56] == 0
                                && intCap[currentByte + 57] == 0 && intCap[currentByte + 58] == 0
                                && intCap[currentByte + 59] == 0 && intCap[currentByte + 60] == 0
                                && intCap[currentByte + 61] == 0 && intCap[currentByte + 62] == 0
                                && intCap[currentByte + 63] == 0 && intCap[currentByte + 64] == 0
                                && intCap[currentByte + 65] == 0 && intCap[currentByte + 66] == 0
                                && intCap[currentByte + 67] == 0 && intCap[currentByte + 68] == 0
                                && intCap[currentByte + 69] == 0 && intCap[currentByte + 70] == 0
                                && intCap[currentByte + 71] == 0 && intCap[currentByte + 72] == 0
                                && intCap[currentByte + 73] == 0 && intCap[currentByte + 74] == 0
                                && intCap[currentByte + 75] == 0 && intCap[currentByte + 76] == 0
                                && intCap[currentByte + 77] == 0 && intCap[currentByte + 78] == 0
                                && intCap[currentByte + 79] == 0 && intCap[currentByte + 80] == 0
                                && intCap[currentByte + 81] == 0 && intCap[currentByte + 82] == 0))
                            // ANONCE (bytes 52 to 83)
                            for (int i = 1; i < 33; i++)
                                recordHandshake.setAnonce(recordHandshake.getAnonce()
                                        + dec2hex(intCap[currentByte + 50 - nonQosOffset + i]));
                    }
                }
                // Message 2 of 4
            } else if (isMessage(intCap, currentByte, 2)) {
                nonQosOffset = intCap[currentByte] == 8 ? 2 : 0;
                // BSSID (bytes 5 to 10) and find this BSSIDs index in the array
                recordHandshake = foundHsInList(handshakes, readBssid(cap, currentByte, 4));
                if (!(intCap[currentByte + 51 - nonQosOffset] == 0
                        && intCap[currentByte + 52 - nonQosOffset] == 0
                        && intCap[currentByte + 53 - nonQosOffset] == 0
                        && intCap[currentByte + 54 - nonQosOffset] == 0
                        && intCap[currentByte + 55 - nonQosOffset] == 0
                        && intCap[currentByte + 56 - nonQosOffset] == 0
                        && intCap[currentByte + 57 - nonQosOffset] == 0
                        && intCap[currentByte + 58 - nonQosOffset] == 0
                        && intCap[currentByte + 59 - nonQosOffset] == 0
                        && intCap[currentByte + 60 - nonQosOffset] == 0
                        && intCap[currentByte + 61 - nonQosOffset] == 0
                        && intCap[currentByte + 62 - nonQosOffset] == 0
                        && intCap[currentByte + 63 - nonQosOffset] == 0
                        && intCap[currentByte + 64 - nonQosOffset] == 0
                        && intCap[currentByte + 65 - nonQosOffset] == 0
                        && intCap[currentByte + 66 - nonQosOffset] == 0
                        && intCap[currentByte + 67 - nonQosOffset] == 0
                        && intCap[currentByte + 68 - nonQosOffset] == 0
                        && intCap[currentByte + 69 - nonQosOffset] == 0
                        && intCap[currentByte + 70 - nonQosOffset] == 0
                        && intCap[currentByte + 71 - nonQosOffset] == 0
                        && intCap[currentByte + 72 - nonQosOffset] == 0
                        && intCap[currentByte + 73 - nonQosOffset] == 0
                        && intCap[currentByte + 74 - nonQosOffset] == 0
                        && intCap[currentByte + 75 - nonQosOffset] == 0
                        && intCap[currentByte + 76 - nonQosOffset] == 0
                        && intCap[currentByte + 77 - nonQosOffset] == 0
                        && intCap[currentByte + 78 - nonQosOffset] == 0
                        && intCap[currentByte + 79 - nonQosOffset] == 0
                        && intCap[currentByte + 80 - nonQosOffset] == 0
                        && intCap[currentByte + 81 - nonQosOffset] == 0
                        && intCap[currentByte + 82 - nonQosOffset] == 0))
                    // EAPOL
                    if (packetLength > 34 - nonQosOffset)
                        // SNONCE
                        if (!recordHandshake.getAnonce().equals("") && !recordHandshake.getStation().equals("")
                                && recordHandshake.getSnonce().equals("")) {
                            // SNONCE (bytes 52 to 83)
                            for (int i = 1; i <= 32; i++)
                                recordHandshake.setSnonce(recordHandshake.getSnonce()
                                        + dec2hex(intCap[currentByte + 50 - nonQosOffset + i]));
                            int eapolSize = Integer.valueOf(dec2hex(intCap[currentByte + 36 - nonQosOffset])
                                    + dec2hex(intCap[currentByte + 37 - nonQosOffset]), 16) + 4;
                            if (eapolSize <= 0)
                                eapolSize = packetLength - (34 - nonQosOffset);
                            for (int i = 1; i <= eapolSize; i++) {
                                // Key Version
                                if (i == 7)
                                    recordHandshake.setKeyVersion("0" + (Integer.valueOf(
                                            dec2hex(intCap[currentByte + 33 - nonQosOffset + i - 1]) + ""
                                                    + dec2hex(intCap[currentByte + 33 - nonQosOffset + i]), 16) & 7));
                                // Key MIC
                                if (i > 81 && i < 98) {
                                    recordHandshake.setEapol(recordHandshake.getEapol() + "00");
                                    recordHandshake.setKeyMic(recordHandshake.getKeyMic()
                                            + dec2hex(intCap[currentByte + 33 - nonQosOffset + i]));
                                } else
                                    recordHandshake.setEapol(recordHandshake.getEapol()
                                            + dec2hex(intCap[currentByte + 33 - nonQosOffset + i]));
                            }
                        }
            }
            currentByte += packetLength;
            // move to next packet
        }
        List<Handshake> returnValue = new ArrayList<>();
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

    private void addBssid(List<Handshake> handshakes, String bssid) {
        for (Handshake handshake : handshakes)
            if (handshake.getBssid().equals(bssid))
                return;
        handshakes.add(new Handshake(bssid));
    }

    private void addEssid(Handshake handshake, byte[] cap, int currentByte) {
        if (handshake.getEssid().equals("")) {
            StringBuilder essid = new StringBuilder();
            for (int i = 1; i <= (cap[currentByte + 37] & 0xff); i++)
                essid.append((char) (cap[currentByte + 37 + i] & 0xff));
            handshake.setEssid(essid.toString());
        }
    }

    private String readBssid(byte[] cap, int currentByte, int offset) {
        StringBuilder bssid = new StringBuilder();
        for (int i = offset; i < offset + 5; i++) {
            bssid.append(dec2hex(cap[currentByte + i] & 0xff));
            bssid.append(":");
        }
        bssid.append(dec2hex(cap[currentByte + offset + 5] & 0xff));
        return bssid.toString();
    }

    private int[] convertToIntArray(byte[] input) {
        int[] ret = new int[input.length];
        for (int i = 0; i < input.length; i++)
            ret[i] = input[i] & 0xff;
        return ret;
    }

    private boolean isMessage(int[] intCap, int currentByte, int message) {
        return intCap[currentByte] == 136
                && (intCap[currentByte + 1] == 3 - message || intCap[currentByte + 1] == 11 - message)
                && intCap[currentByte + 32] == 136 && intCap[currentByte + 33] == 142
                || intCap[currentByte] == 8
                && (intCap[currentByte + 1] == 3 - message || intCap[currentByte + 1] == 11 - message)
                && intCap[currentByte + 30] == 136 && intCap[currentByte + 31] == 142;
    }
}