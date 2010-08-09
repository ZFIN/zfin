package org.zfin.framework.presentation;

import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileWrapper;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 */
public class FileContentBean {

    private String fileName;
    private FileWrapper wrapper;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileWrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(FileWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public File getFile() {
        String webRootDir = ZfinPropertiesEnum.WEBROOT_DIRECTORY.value();
        File webFile = new File(webRootDir, fileName);
        return webFile;
    }
}
