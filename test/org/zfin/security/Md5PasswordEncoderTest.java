package org.zfin.security;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;


/**
 * Test that passwords are encoded correctly using a salt or not.
 */
public class Md5PasswordEncoderTest {

    @Test
    public void testWithoutSalt() {
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        String pw = "henriette";
        String password = encoder.encodePassword(pw, null);
        Assert.assertTrue(password != null);
        Assert.assertEquals("35112f892ca1d11a427bbb756e1effda", password);

    }

    @Test
    public void testWithSalt() {

        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        String pw = "henriette";
        String salt = "dedicated to George Streisinger";
        String saltedPassword = encoder.encodePassword(pw, salt);
        Assert.assertTrue(saltedPassword != null);
        Assert.assertEquals("1bb59c58eae951e08f352d87e26f9483", saltedPassword);

    }
}
