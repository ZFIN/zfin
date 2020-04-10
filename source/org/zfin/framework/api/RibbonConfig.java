package org.zfin.framework.api;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RibbonConfig {

    private String solrRequestHandler;
    private List<RibbonConfigCategory> categories;

}
