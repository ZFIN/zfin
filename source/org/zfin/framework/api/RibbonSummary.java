package org.zfin.framework.api;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RibbonSummary {

    private List<RibbonCategory> categories;
    private List<RibbonSubject> subjects;

}
