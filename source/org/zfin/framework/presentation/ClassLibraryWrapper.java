package org.zfin.framework.presentation;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

/**
 * Wrapper class that holds a class libary and if the file is found.
 */
public class ClassLibraryWrapper {

    private String libaryFileName;
    private File file;
    private String version;

    public String getLibaryFileName() {
        return libaryFileName;
    }

    public void setLibaryFileName(String libaryFileName) {
        this.libaryFileName = libaryFileName;
    }

    public boolean isLibraryFileExists() {
        File file = new File(libaryFileName);
        return file.exists();
    }

    public String getVersion() {
        String temp = "";
        if (libaryFileName == null)
            return temp;

        if (!libaryFileName.endsWith("jar"))
            return temp;

        JarFile jar = null;
        Manifest manifest = null;
        try {
            jar = new JarFile(file);
            manifest = jar.getManifest();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Attributes attribute = manifest.getMainAttributes();
        if(attribute == null)
            return temp;
        String value = attribute.getValue("Implementation-Version");
        if(value == null)
            return temp;
        return value;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
