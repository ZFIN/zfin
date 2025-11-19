package org.zfin.genomebrowser.presentation;

import org.zfin.genomebrowser.GenomeBrowserType;

public interface GenomeBrowserImage {

    String getReactComponentId();

    String getImageUrl();

    String getLinkUrl();

    String getLandmark();

    String getChromosome();

    String getBuild();

    GenomeBrowserType getType();

    Integer getHeight();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    String calculateBaseUrl();

    String getFullLinkUrl(String assembly);

}
