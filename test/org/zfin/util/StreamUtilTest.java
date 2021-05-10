package org.zfin.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StreamUtilTest {

    @Test
    public void replaceBytesInInputStream() throws IOException {
        Map<byte[], byte[]> replacements = new HashMap<byte[], byte[]>();
        replacements.put(new byte[]{1, 2}, new byte[]{7, 8});
        replacements.put(new byte[]{1}, new byte[]{9});
        replacements.put(new byte[]{3, 2}, new byte[0]);
        byte[] input = {4, 3, 2, 1, 2, 1, 3};
        ReplaceFilterInputStream in = new ReplaceFilterInputStream(new ByteArrayInputStream(input), replacements);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while ((read = in.read()) >= 0) {
            out.write(read);
        }
        assertEquals("[4, 7, 8, 9, 3]", Arrays.toString(out.toByteArray()));
    }

    @Test
    public void replaceStringsInInputStream() throws IOException {
        String originalString = "e";
        String replacementString = "a";
        String inputString = "Was ist denn heute los";
        ByteArrayInputStream bais = new ByteArrayInputStream(inputString.getBytes());
        InputStream in = StreamUtil.replaceStringsInStream(bais, originalString, replacementString);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while ((read = in.read()) >= 0) {
            out.write(read);
        }
        String replacedString = new String(out.toByteArray());
        assertEquals("Was ist dann hauta los", replacedString);
        assertEquals(inputString.length(), replacedString.length());
    }

    @Test
    public void replaceTabsToCommaInInputStream() throws IOException {
        String originalString = "\t";
        String replacementString = ",";
        String inputString = "Was\tist\tdenn\theute\tlos";
        ByteArrayInputStream bais = new ByteArrayInputStream(inputString.getBytes());
        InputStream in = StreamUtil.replaceStringsInStream(bais, originalString, replacementString);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while ((read = in.read()) >= 0) {
            out.write(read);
        }
        String replacedString = new String(out.toByteArray());
        assertEquals("Was,ist,denn,heute,los", replacedString);
        assertEquals(inputString.length(), replacedString.length());
    }

}
