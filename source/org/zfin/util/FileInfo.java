package org.zfin.util;

import java.io.File;

/**
 * File meta data object.
 */
public class FileInfo implements Comparable<FileInfo> {

    private File file;
    private long size;
    private int numberOfLines;

    public FileInfo(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getByteCountDisplay() {
        return ByteUtil.getBytesWithUnit(size);
    }

    public int getNumberOfLines() {
        return numberOfLines;
    }

    public void setNumberOfLines(int numberOfLines) {
        this.numberOfLines = numberOfLines;
    }

    @Override
    public int compareTo(FileInfo o) {
        return file.getName().compareToIgnoreCase(o.getName());
    }

}

