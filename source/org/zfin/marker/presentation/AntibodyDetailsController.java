package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.profile.service.BeanCompareService;
import org.zfin.profile.service.BeanFieldUpdate;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@Log4j2
@RestController
@RequestMapping("/api")
public class AntibodyDetailsController {

    @Autowired
    private AntibodyRepository antibodyRepository;

    @Autowired
    private BeanCompareService beanCompareService;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @JsonView(View.AntibodyDetailsAPI.class)
    @RequestMapping(value = "/antibody/{antibodyZdbId}/details", method = RequestMethod.GET)
    public Antibody getAntibodyDetails(@PathVariable String antibodyZdbId) {
        Antibody antibody = antibodyRepository.getAntibodyByID(antibodyZdbId);
        antibody.setABRegistryID(getMarkerRepository().getABRegID(antibodyZdbId)); //hydrate ABRegistryID
        return antibody;
    }

    @SneakyThrows
    @JsonView(View.AntibodyDetailsAPI.class)
    @RequestMapping(value = "/antibody/{antibodyZdbId}/details", method = RequestMethod.POST)
    public Antibody updateAntibodyDetails(@PathVariable String antibodyZdbId,
                                          @RequestBody Antibody formData,
                                          @RequestParam(value = "publicationID", required = false) String publicationID) {
        //compare logic of this method to old GWT way of calling 2 RPC methods: updateAntibodyHeaders(AntibodyDTO)
        //                                              and for name change: addDataAliasRelatedEntity(RelatedEntityDTO)

        HibernateUtil.createTransaction();

        Antibody antibody = antibodyRepository.getAntibodyByID(antibodyZdbId);
        antibody.setABRegistryID(getMarkerRepository().getABRegID(antibodyZdbId)); //hydrate ABRegistryID

        List<BeanFieldUpdate> updates = new ArrayList<>();

        //special logic for name change:
        //  antibody rules require the name and abbreviation be the same
        //  need a publication reference
        BeanFieldUpdate nameUpdate = beanCompareService.compareBeanField("name", antibody, formData);
        if (nameUpdate != null) {
            BeanFieldUpdate abbreviationUpdate = nameUpdate.clone();
            abbreviationUpdate.setField("abbreviation");
            updates.add(nameUpdate);
            updates.add(abbreviationUpdate);

            AntibodyService.addDataAliasRelatedEntity(antibodyZdbId, formData.getName(), publicationID);
        }

        //special handling for ABRegistryID
        if (null != beanCompareService.compareBeanField("ABRegistryID", antibody, formData)) {
            AntibodyService.setABRegistryID(antibody, formData.getABRegistryID());
            antibody.setABRegistryID(formData.getABRegistryID());
        }

        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("hostSpecies", antibody, formData));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("immunogenSpecies", antibody, formData));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("heavyChainIsotype", antibody, formData));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("lightChainIsotype", antibody, formData));
        CollectionUtils.addIgnoreNull(updates, beanCompareService.compareBeanField("clonalType", antibody, formData));

        beanCompareService.applyUpdates(antibody, updates);
        HibernateUtil.currentSession().save(antibody);
        infrastructureRepository.insertUpdatesTable(antibodyZdbId, updates);
        HibernateUtil.flushAndCommitCurrentSession();

        return antibody;
    }


}
