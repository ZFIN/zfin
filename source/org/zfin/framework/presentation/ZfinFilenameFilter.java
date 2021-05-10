package org.zfin.framework.presentation;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by IntelliJ IDEA.
 */
public class ZfinFilenameFilter implements FilenameFilter {

    private String fileExtension;

    public ZfinFilenameFilter(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public boolean accept(File dir, String name) {
        if (dir == null || dir.getName().equals("CVS"))
            return false;
        File subFile = new File(dir, name);
        if (subFile.isDirectory()) {
            if (name.equals("CVS"))
                return false;
            else
                return true;
        }
        if (name != null && name.endsWith(fileExtension))
            return true;

        return false;
    }
}
