package org.zfin.util;

import org.apache.commons.io.FileUtils;

/**
 * Created with IntelliJ IDEA.
 * User: cmpich
 * Date: 4/4/12
 * Time: 9:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class ByteUtil {


    public static String getBytesWithUnit(long size) {
        return FileUtils.byteCountToDisplaySize(size);
    }
}
