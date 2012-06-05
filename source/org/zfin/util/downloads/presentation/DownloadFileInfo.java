package org.zfin.util.downloads.presentation;

import org.zfin.util.FileInfo;
import org.zfin.util.FileUtil;
import org.zfin.util.downloads.jaxb.DownloadFileEntry;

import java.io.File;
import java.io.IOException;

/**
 * File meta data object.
 */
public class DownloadFileInfo extends FileInfo {

    private DownloadFileEntry downloadFile;

    public DownloadFileInfo(DownloadFileEntry downloadFile, File file) {
        super(file);
        this.downloadFile = downloadFile;
    }

    public DownloadFileEntry getDownloadFile() {
        return downloadFile;
    }

    public int compareTo(DownloadFileInfo o) {
        return o.getDownloadFile().getName().compareToIgnoreCase(o.getDownloadFile().getName());
    }

    public static DownloadFileInfo getFileInfo(File file, DownloadFileEntry type) throws IOException {
        DownloadFileInfo info = new DownloadFileInfo(type, file);
        info.setNumberOfLines(FileUtil.countLines(file));
        info.setSize(file.length());
        return info;

    }

}

