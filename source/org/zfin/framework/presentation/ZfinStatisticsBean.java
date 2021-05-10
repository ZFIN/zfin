package org.zfin.framework.presentation;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 */
public class ZfinStatisticsBean {

    public static final long TOTAL_NUMBER_OF_FILES = 260;
    public static final long TOTAL_SIZE = 2171474;

    private List<File> apgFiles;
    private List<File> jspFiles;
    private List<File> classFiles;


    public List<File> getApgFiles() {
        return apgFiles;
    }

    public void setApgFiles(List<File> apgFiles) {
        this.apgFiles = apgFiles;
    }

    public long getNumberOfApgFiles() {
        if (apgFiles == null)
            return 0;
        return apgFiles.size();
    }

    public long getNumberOfJspFiles() {
        if (jspFiles == null)
            return 0;
        return jspFiles.size();
    }

    public long getNumberOfClassFiles() {
        if (classFiles == null)
            return 0;
        return classFiles.size();
    }

    public double getRelativeNumberOfApgFiles() {
        return (double) getNumberOfApgFiles() / (double) TOTAL_NUMBER_OF_FILES;
    }

    public double getRelativeApgFileSize() {
        return (double) getTotalApgFileSize() / (double) TOTAL_SIZE;
    }

    public long getTotalApgFileSize() {
        if (apgFiles == null)
            return 0;
        long size = 0;
        for (File file : apgFiles) {
            size += file.length();
        }
        return size;
    }

    public long getTotalJspFileSize() {
        if (jspFiles == null)
            return 0;
        long size = 0;
        for (File file : jspFiles) {
            size += file.length();
        }
        return size;
    }

    public long getTotalClassesFileSize() {
        if (classFiles == null)
            return 0;
        long size = 0;
        for (File file : classFiles) {
            size += file.length();
        }
        return size;
    }

    public List<File> getJspFiles() {
        return jspFiles;
    }

    public void setJspFiles(List<File> jspFiles) {
        this.jspFiles = jspFiles;
    }

    public void setClassesFiles(List<File> classesFiles) {
        classFiles = classesFiles;
    }
}
