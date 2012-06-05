package org.zfin.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to work on streams.
 */
public class StreamUtil {

    public static InputStream replaceStringsInStream(ByteArrayInputStream bais, String toReplace, String replacementString){
        Map<byte[], byte[]> replacements = new HashMap<byte[], byte[]>(1);
        replacements.put(toReplace.getBytes(), replacementString.getBytes());
        ReplaceFilterInputStream in = new ReplaceFilterInputStream(bais, replacements);
        return in;
    }
}
