package org.zfin.genomebrowser.presentation;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;
import org.zfin.jbrowse.presentation.JBrowse2ImageBuilder;
import org.zfin.jbrowse.presentation.JBrowseImageBuilder;

@Setter
@Log4j2
@Component("genomeBrowserFactory")
public class GenomeBrowserFactory {

    public static GenomeBrowserImageBuilder getStaticImageBuilder() {
        GenomeBrowserFactory factory = new GenomeBrowserFactory();
        return factory.getImageBuilder();
    }

    public GenomeBrowserImageBuilder getImageBuilder() {
        if (FeatureFlags.isFlagEnabled(FeatureFlagEnum.JBROWSE2)) {
            return new JBrowse2ImageBuilder();
        } else {
            return new JBrowseImageBuilder();
        }
    }

}
