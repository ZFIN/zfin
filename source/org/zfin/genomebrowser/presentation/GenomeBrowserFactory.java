package org.zfin.genomebrowser.presentation;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;
import org.zfin.gbrowse.presentation.GBrowseImageBuilder;
import org.zfin.jbrowse.presentation.JBrowseImageBuilder;

import javax.servlet.http.HttpServletRequest;

@Setter
@Log4j2
@Component("genomeBrowserFactory")
public class GenomeBrowserFactory {

    @Autowired
    private HttpServletRequest request;

    public static GenomeBrowserImageBuilder getStaticImageBuilder() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        GenomeBrowserFactory factory = new GenomeBrowserFactory();
        factory.setRequest(request);
        return factory.getImageBuilder();
    }

    public GenomeBrowserImageBuilder getImageBuilder() {
        if (FeatureFlags.isFlagEnabled(FeatureFlagEnum.JBROWSE)) {
            return new JBrowseImageBuilder();
        } else {
            return new GBrowseImageBuilder();
        }
    }

}
