package org.zfin.genomebrowser;

import org.zfin.properties.ZfinPropertiesEnum;

public enum GenomeBrowserBuild {
    ZV9("Zv9", ZfinPropertiesEnum.GBROWSE_ZV9_PATH_FROM_ROOT.toString(), ZfinPropertiesEnum.GBROWSE_ZV9_IMG_PATH_FROM_ROOT.toString()),
    GRCZ10("GRCz10", ZfinPropertiesEnum.GBROWSE_GRCZ10_PATH_FROM_ROOT.toString(), ZfinPropertiesEnum.GBROWSE_GRCZ10_IMG_PATH_FROM_ROOT.toString()),
    CURRENT("GRCz11", ZfinPropertiesEnum.GBROWSE_PATH_FROM_ROOT.toString(), ZfinPropertiesEnum.GBROWSE_IMG_PATH_FROM_ROOT.toString());

    private final String value;
    private final String path;
    private final String imagePath;

    GenomeBrowserBuild(String value, String path, String imagePath) {
        this.value = value;
        this.path = path;
        this.imagePath = imagePath;
    }

    public String getValue() {
        return value;
    }

    public String getPath() {
        return path;
    }

    public String getImagePath() {
        return imagePath;
    }
}
