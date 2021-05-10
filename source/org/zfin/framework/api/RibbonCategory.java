package org.zfin.framework.api;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RibbonCategory {

    private String id;
    private String description;
    private String label;
    private List<RibbonGroup> groups;

}
