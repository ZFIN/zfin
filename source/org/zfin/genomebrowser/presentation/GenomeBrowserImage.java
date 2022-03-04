package org.zfin.genomebrowser.presentation;

import org.zfin.genomebrowser.GenomeBrowserType;

public interface GenomeBrowserImage {

    String getImageUrl();

    String getLinkUrl();

    String getLandmark();

    String getBuild();

    GenomeBrowserType getType();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

}
