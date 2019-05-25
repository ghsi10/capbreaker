package com.services;

import com.exceptions.UnsupportedDataTypeException;
import com.models.Handshake;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@RunWith(SpringRunner.class)
public class HashConvertTest {

    private HashConvert hashConvert;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void init() {
        hashConvert = new HashConvert();
    }

    @Test
    public void convertFilePassTest() throws IOException, URISyntaxException {
        Handshake expectedHs = new Handshake("C0:FF:D4:82:F6:FD");
        expectedHs.setEssid("ehudaviran_2.4");
        expectedHs.setStation("D0:13:FD:36:7F:72");
        expectedHs.setSnonce("25BE4DEFAA20D58E0AF302E2144992CDE659A2621F0E431443A5D8DEBE456B4F");
        expectedHs.setAnonce("D0F085A84E0EAE263899BE0E86890B9D2B848450D276B03A4B5F585A51850135");
        expectedHs.setEapol("0103007502010A0000000000000000000125BE4DEFAA20D58E0AF302E2144992CDE659A2621F0E431443A5D8DEBE456B4F000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001630140100000FAC040100000FAC040100000FAC028000");
        expectedHs.setKeyVersion("02");
        expectedHs.setKeyMic("6185AD5EDD8FC3605724EF879E34A1CF");
        String file = "hs/hs1.cap";
        byte[] cap = Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(file)).toURI()));
        Handshake hs = hashConvert.convertFile(cap, "", "");
        Assert.assertEquals("Bad file handshake convert", hs, expectedHs);
    }

    @Test
    public void convertFileWithBssidPassTest() throws IOException, URISyntaxException {
        Handshake expectedHs = new Handshake("C0:FF:D4:82:F6:FD");
        expectedHs.setEssid("ehudaviran_2.4");
        expectedHs.setStation("D0:13:FD:36:7F:72");
        expectedHs.setSnonce("25BE4DEFAA20D58E0AF302E2144992CDE659A2621F0E431443A5D8DEBE456B4F");
        expectedHs.setAnonce("D0F085A84E0EAE263899BE0E86890B9D2B848450D276B03A4B5F585A51850135");
        expectedHs.setEapol("0103007502010A0000000000000000000125BE4DEFAA20D58E0AF302E2144992CDE659A2621F0E431443A5D8DEBE456B4F000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001630140100000FAC040100000FAC040100000FAC028000");
        expectedHs.setKeyVersion("02");
        expectedHs.setKeyMic("6185AD5EDD8FC3605724EF879E34A1CF");
        String file = "hs/hs1.cap";
        byte[] cap = Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(file)).toURI()));
        Handshake hs = hashConvert.convertFile(cap, "C0:FF:D4:82:F6:FD", "");
        Assert.assertEquals("Bad file handshake convert", hs, expectedHs);
    }


    @Test
    public void convertFileWithEssidPassTest() throws IOException, URISyntaxException {
        Handshake expectedHs = new Handshake("C0:FF:D4:82:F6:FD");
        expectedHs.setEssid("ehudaviran_2.4");
        expectedHs.setStation("D0:13:FD:36:7F:72");
        expectedHs.setSnonce("25BE4DEFAA20D58E0AF302E2144992CDE659A2621F0E431443A5D8DEBE456B4F");
        expectedHs.setAnonce("D0F085A84E0EAE263899BE0E86890B9D2B848450D276B03A4B5F585A51850135");
        expectedHs.setEapol("0103007502010A0000000000000000000125BE4DEFAA20D58E0AF302E2144992CDE659A2621F0E431443A5D8DEBE456B4F000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001630140100000FAC040100000FAC040100000FAC028000");
        expectedHs.setKeyVersion("02");
        expectedHs.setKeyMic("6185AD5EDD8FC3605724EF879E34A1CF");
        String file = "hs/hs1.cap";
        byte[] cap = Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(file)).toURI()));
        Handshake hs = hashConvert.convertFile(cap, "", "ehudaviran_2.4");
        Assert.assertEquals("Bad file handshake convert", hs, expectedHs);
    }


    @Test
    public void convertFileBadSignatureFailTest() throws IOException, URISyntaxException {
        expectedEx.expect(UnsupportedDataTypeException.class);
        expectedEx.expectMessage("Invalid file signature");
        String file = "hs/hs2.cap";
        byte[] cap = Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(file)).toURI()));
        hashConvert.convertFile(cap, "", "");
    }

    @Test
    public void convertEmptyFileFailTest() throws IOException, URISyntaxException {
        expectedEx.expect(UnsupportedDataTypeException.class);
        expectedEx.expectMessage("Invalid File");
        String file = "hs/hs3.cap";
        byte[] cap = Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(file)).toURI()));
        hashConvert.convertFile(cap, "", "");
    }

    @Test
    public void convertTextPassTest() throws UnsupportedDataTypeException {
        String pmid = "d0f86da0a2d167b8c7b81ed870748545*c0ffd482f6fd*b0ece1d8a476*6568756461766972616e";
        Handshake hs = hashConvert.convertTest(pmid);
        Assert.assertEquals("Bad text handshake convert", hs.getEssid(), "ehudaviran");
        Assert.assertEquals("Bad text handshake convert", hs.getBssid(), "C0:FF:D4:82:F6:FD");
        Assert.assertEquals("Bad text handshake convert", hs.getStation(), "B0:EC:E1:D8:A4:76");
        Assert.assertEquals("Bad text handshake convert", hs.getKeyMic(), "d0f86da0a2d167b8c7b81ed870748545*c0ffd482f6fd*b0ece1d8a476*6568756461766972616e");
    }

    @Test
    public void convertTextEmptyInputFailTest() throws UnsupportedDataTypeException {
        expectedEx.expect(UnsupportedDataTypeException.class);
        expectedEx.expectMessage("PMKID is missing");
        String pmid = "";
        hashConvert.convertTest(pmid);
    }

    @Test
    public void convertTextBadInputNotHexFailTest() throws UnsupportedDataTypeException {
        expectedEx.expect(UnsupportedDataTypeException.class);
        expectedEx.expectMessage("Invalid PMKID");
        String pmkid = "d0f86da0a2d167b8c7b81ed870748545*c0ffd482f6fd*b0ece1d8a476*6568756461766972616e1";
        hashConvert.convertTest(pmkid);
    }

    @Test
    public void convertTextBadInputFailTest() throws UnsupportedDataTypeException {
        expectedEx.expect(UnsupportedDataTypeException.class);
        expectedEx.expectMessage("Invalid PMKID");
        String pmkid = "jfi20fwf2wf:23r23f:3r223f:23";
        hashConvert.convertTest(pmkid);
    }
}
