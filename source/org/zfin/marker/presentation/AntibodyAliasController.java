package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.marker.MarkerAlias;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@Log4j2
@RestController
@RequestMapping("/api")
public class AntibodyAliasController {

    @Autowired
    private AntibodyRepository antibodyRepository;

    @JsonView(View.AntibodyAliasAPI.class)
    @RequestMapping(value = "/antibody/{antibodyZdbId}/aliases", method = RequestMethod.GET)
    public List<RelatedEntityDTO> getAliasesForAntibody(@PathVariable String antibodyZdbId) {
        Antibody antibody = antibodyRepository.getAntibodyByID(antibodyZdbId);

        return DTOMarkerService.getMarkerAliasDTOs(antibody)
            .stream()
            .peek(dto -> dto.setDataZdbID(antibodyZdbId)) //the DTOs dataZdbIDs are empty initially?
            .toList();
    }


    @JsonView(View.AntibodyAliasAPI.class)
    @RequestMapping(value = "/antibody/{antibodyZdbId}/aliases", method = RequestMethod.POST)
    public RelatedEntityDTO addAliasForAntibody(@PathVariable String antibodyZdbId,
                                                @RequestBody RelatedEntityDTO formData) {

        HibernateUtil.createTransaction();
        AntibodyService.addDataAliasRelatedEntity(antibodyZdbId, formData.getName(), formData.getPublicationZdbID());
        HibernateUtil.flushAndCommitCurrentSession();

        return formData;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/antibody/{publicationId}/{aliasID}", method = RequestMethod.DELETE)
    public String deleteAliasForAntibody(@PathVariable String aliasID,
                                         @PathVariable String publicationId) {


        HibernateUtil.createTransaction();
        MarkerAlias alias = getMarkerRepository().getMarkerAlias(aliasID);
        AntibodyService.removeDataAliasAttributionAndAliasIfUnique(alias, publicationId);
        HibernateUtil.flushAndCommitCurrentSession();

        return alias.getAlias();
    }

}
