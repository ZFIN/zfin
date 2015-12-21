package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;

@Controller
@RequestMapping("/marker")
public class MarkerAliasController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @InitBinder("markerAliasBean")
    public void initAliasBinder(WebDataBinder binder) {
        binder.setValidator(new MarkerAliasBeanValidator());
    }

    @InitBinder("markerReferenceBean")
    public void initReferenceBinder(WebDataBinder binder) {
        binder.setValidator(new MarkerReferenceBeanValidator());
    }


    @ResponseBody
    @RequestMapping(value = "/{markerID}/aliases", method = RequestMethod.GET)
    public Collection<MarkerAliasBean> getMarkerAliases(@PathVariable String markerID) {
        Marker marker = markerRepository.getMarkerByID(markerID);
        Collection<MarkerAliasBean> beans = new ArrayList<>();
        Collection<MarkerAlias> aliases = marker.getAliases();
        if (CollectionUtils.isNotEmpty(aliases)) {
            for (MarkerAlias markerAlias : marker.getAliases()) {
                beans.add(MarkerAliasBean.convert(markerAlias));
            }
        }
        return beans;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerID}/aliases", method = RequestMethod.POST)
    public MarkerAliasBean addMarkerAlias(@PathVariable String markerID,
                                          @Valid @RequestBody MarkerAliasBean newAlias,
                                          BindingResult errors) {
        Marker marker = markerRepository.getMarkerByID(markerID);

        Collection<MarkerAlias> aliases = marker.getAliases();
        if (CollectionUtils.isNotEmpty(aliases)) {
            for (MarkerAlias alias : aliases) {
                if (alias.getAlias().equals(newAlias.getAlias())) {
                    errors.rejectValue("alias", "marker.alias.inuse");
                }
            }
        }

        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid alias", errors);
        }

        // when creating a new alias, the assumption is that there is only one reference
        String pubID = newAlias.getReferences().iterator().next().getZdbID();
        Publication publication = publicationRepository.getPublication(pubID);

        HibernateUtil.createTransaction();
        MarkerAlias alias = markerRepository.addMarkerAlias(marker, newAlias.getAlias(), publication);
        HibernateUtil.flushAndCommitCurrentSession();

        return MarkerAliasBean.convert(alias);
    }

    @ResponseBody
    @RequestMapping(value = "/alias/{aliasID}", method = RequestMethod.DELETE)
    public String removeMarkerAlias(@PathVariable String aliasID) {
        MarkerAlias alias = markerRepository.getMarkerAlias(aliasID);

        HibernateUtil.createTransaction();
        markerRepository.deleteMarkerAlias(alias.getMarker(), alias);
        HibernateUtil.flushAndCommitCurrentSession();

        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/alias/{aliasID}/references", method = RequestMethod.POST)
    public MarkerAliasBean addAliasReference(@PathVariable String aliasID,
                                             @Valid @RequestBody MarkerReferenceBean newReference,
                                             BindingResult errors) {
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid reference", errors);
        }

        MarkerAlias alias = markerRepository.getMarkerAlias(aliasID);
        Publication publication = publicationRepository.getPublication(newReference.getZdbID());

        for (PublicationAttribution reference : alias.getPublications()) {
            if (reference.getPublication().equals(publication)) {
                errors.rejectValue("zdbID", "marker.reference.inuse");
                throw new InvalidWebRequestException("Invalid reference", errors);
            }
        }

        HibernateUtil.createTransaction();
        markerRepository.addDataAliasAttribution(alias, publication, alias.getMarker());
        HibernateUtil.flushAndCommitCurrentSession();

        return MarkerAliasBean.convert(alias);
    }

    @ResponseBody
    @RequestMapping(value = "/alias/{aliasID}/references/{pubID}", method = RequestMethod.DELETE)
    public String removeAliasReference(@PathVariable String aliasID,
                                       @PathVariable String pubID) {
        HibernateUtil.createTransaction();
        infrastructureRepository.deleteRecordAttribution(aliasID, pubID);
        HibernateUtil.flushAndCommitCurrentSession();

        return "OK";
    }

}
