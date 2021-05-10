package org.zfin.anatomy.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.repository.RepositoryFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This Bean can be extended to make use of basic anatomical form entries, such as
 * development stages...
 */
public class BasicAnatomyFormBean extends PaginationBean {

    private List stages;
    private Map<String, String> stageEntries;
    private AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();

    public List getStages() {
        return stages;
    }

    public void setStages(List stages) {
        this.stages = stages;
    }

    public Map<String, String> getDisplayStages() {
        if (stageEntries != null)
            return stageEntries;

        if (stages == null) {
            stages = anatomyRepository.getAllStagesWithoutUnknown();
        }

        stageEntries = new LinkedHashMap<String, String>();
        for (Object stage1 : stages) {
            DevelopmentStage stage = (DevelopmentStage) stage1;
            String labelString = StagePresentation.createDisplayEntryShort(stage);
            stageEntries.put(stage.getZdbID(), labelString);
        }
        return stageEntries;
    }

}