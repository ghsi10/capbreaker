package com.configs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SecurityConfigTest {

    private PasswordEncoder passwordEncoder;
    private PasswordEncoder encoder;

    @Before
    public void init() {
        passwordEncoder = new SecurityConfig.NoOpPasswordEncoder();
        encoder = new BCryptPasswordEncoder();
    }

    @Test
    public void encodePassTest() {
        String pass = "pass";
        Assert.assertEquals("Password encoder fail", passwordEncoder.encode(pass), pass);
    }


    @Test
    public void encodeFailTest() {
        String pass = "pass";
        String otherPass = "pass1";
        Assert.assertNotEquals("Password encoder should fail", passwordEncoder.encode(pass), otherPass);
    }

    @Test
    public void matchesPassHardCoded() {
        String pass = "pass";
        Assert.assertTrue("Password hardcoded matcher fail", passwordEncoder.matches(pass, pass));
    }

    @Test
    public void matchesFailHardCoded() {
        String pass = "pass";
        String otherPass = "pass1";
        Assert.assertFalse("Password hardcoded matcher should fail", passwordEncoder.matches(pass, otherPass));
    }


    @Test
    public void matchesPassEncoded() {
        String pass = "pass";
        String otherPass = encoder.encode(pass);
        Assert.assertTrue("Password encoded matcher fail", passwordEncoder.matches(otherPass, pass));
    }

    @Test
    public void matchesFailEncoded() {
        String pass = "pass";
        String otherPass = "pass1";
        otherPass = encoder.encode(otherPass);
        Assert.assertFalse("Password encoded matcher should fail", passwordEncoder.matches(otherPass, pass));
    }
}
