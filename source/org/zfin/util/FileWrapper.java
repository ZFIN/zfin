package org.zfin.util;
//Should we add package foo?

import java.io.File;

/**
 * wrapper class that contains a file and meta data avout it.
 */
public class FileWrapper {

    private File file;
    private String title;

    public FileWrapper(File file) {
        this.file = file;
    }

    public FileWrapper(File file, String title) {
        this.file = file;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName(){
        return file.getName();
    }

    public String getAbsolutePath(){
        return file.getAbsolutePath();
    }

    public File getFile(){
        return file;
    }

    public String getContents(){
        return FileUtil.readFile(file);
    }

}
