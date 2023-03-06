package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;

import java.util.*;

@RestController
@RequestMapping("/api/correspondence")
public class CorrespondenceAPIController {

    @JsonView(View.API.class)
    @RequestMapping(value = "/need/{pubID}", method = RequestMethod.GET)
    public List<CorrespondenceNeedDTO> getPublicationCorrespondenceNeed(@PathVariable String pubID) {
        return CorrespondenceService.getCorrespondenceNeedDTOsGridByPublicationID(pubID);
    }


    @JsonView(View.API.class)
    @RequestMapping(value = "/need/{pubID}", method = RequestMethod.POST)
    public List<CorrespondenceNeedDTO> setPublicationCorrespondenceNeed(@PathVariable String pubID,
                                                                          @RequestBody List<CorrespondenceNeedDTO> correspondenceNeedDTOS) {
        HibernateUtil.createTransaction();
        CorrespondenceService.setCorrespondenceNeedByPublicationID(pubID, correspondenceNeedDTOS);
        HibernateUtil.flushAndCommitCurrentSession();
        return correspondenceNeedDTOS;
    }


    @JsonView(View.API.class)
    @RequestMapping(value = "/resolution/{pubID}", method = RequestMethod.GET)
    public List<CorrespondenceResolutionDTO> getPublicationCorrespondenceResolution(@PathVariable String pubID) {
        return CorrespondenceService.getCorrespondenceResolutionDTOsGridByPublicationID(pubID);
    }


    @JsonView(View.API.class)
    @RequestMapping(value = "/resolution/{pubID}", method = RequestMethod.POST)
    public List<CorrespondenceResolutionDTO> setPublicationCorrespondenceResolution(@PathVariable String pubID,
                                                                          @RequestBody List<CorrespondenceResolutionDTO> correspondenceResolutionDTOS) {
        HibernateUtil.createTransaction();
        CorrespondenceService.setCorrespondenceResolutionByPublicationID(pubID, correspondenceResolutionDTOS);
        HibernateUtil.flushAndCommitCurrentSession();
        return correspondenceResolutionDTOS;
    }


}
