package org.zfin.database;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 */
public class UpgradeFile {

    private File file;
    private int majorVersion;
    private int minorVersion;

    public UpgradeFile(File file, int majorVersion, int minorVersion) {
        this.file = file;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public File getFile() {
        return file;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

}
