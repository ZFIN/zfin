package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.DisplayGroupRepository;
import org.zfin.sequence.repository.SequenceRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/marker")
public class MarkerLinkController {

    private static Logger LOG = Logger.getLogger(MarkerLinkController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private DisplayGroupRepository displayGroupRepository;

    @Autowired
    private SequenceRepository sequenceRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @InitBinder("linkData")
    public void initLinkBinder(WebDataBinder binder) {
        binder.setValidator(new LinkDisplayValidator());
    }

    @InitBinder("markerReferenceBean")
    public void initReferenceBinder(WebDataBinder binder) {
        binder.setValidator(new MarkerReferenceBeanValidator());
    }


    @ResponseBody
    @RequestMapping("/link/databases")
    public Collection<ReferenceDatabaseDTO> getLinkDatabases(@RequestParam(name = "group", required = true) String groupName) {
        DisplayGroup.GroupName group = DisplayGroup.GroupName.getGroup(groupName);
        List<ReferenceDatabase> databases = displayGroupRepository.getReferenceDatabasesForDisplayGroup(group);
        List<ReferenceDatabaseDTO> databaseNames = new ArrayList<>(databases.size());
        for (ReferenceDatabase database : databases) {
            databaseNames.add(DTOConversionService.convertToReferenceDatabaseDTO(database));
        }
        return databaseNames;
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/links", method = RequestMethod.GET)
    public List<LinkDisplay> getMarkerLinks(@PathVariable String markerId,
                                            @RequestParam(name = "group", required = true) String groupName) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        DisplayGroup.GroupName group = DisplayGroup.GroupName.getGroup(groupName);

        return markerRepository.getMarkerDBLinksFast(marker, group);
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/links", method = RequestMethod.POST)
    public LinkDisplay addMarkerLink(@PathVariable String markerId,
                                     @Valid @RequestBody LinkDisplay newLink,
                                     BindingResult errors) {
        Marker marker = null;
        String accessionNo = null;
        ReferenceDatabase refDB = null;

        if (!errors.hasErrors()) {
            marker = markerRepository.getMarkerByID(markerId);
            accessionNo = newLink.getAccession();
            refDB = sequenceRepository.getReferenceDatabaseByID(newLink.getReferenceDatabaseZdbID());

            Collection<? extends DBLink> links = marker.getDbLinks();
            if (CollectionUtils.isNotEmpty(links)) {
                for (DBLink link : marker.getDbLinks()) {
                    if (link.getReferenceDatabase().equals(refDB) && link.getAccessionNumber().equals(accessionNo)) {
                        errors.reject("marker.link.duplicate");
                    }
                }
            }
        }

        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid marker DBLink", errors);
        }


        // assume there's only one pub coming in on a new db link
        String pubId = newLink.getReferences().iterator().next().getZdbID();

        HibernateUtil.createTransaction();
        DBLink link = markerRepository.addDBLink(marker, accessionNo, refDB, pubId);
        HibernateUtil.flushAndCommitCurrentSession();

        return getLinkDisplayById(link.getZdbID());
    }

    @ResponseBody
    @RequestMapping(value = "/link/{linkId}", method = RequestMethod.DELETE)
    public String deleteMarkerLink(@PathVariable String linkId) {
        DBLink link = sequenceRepository.getDBLinkByID(linkId);
        sequenceRepository.removeDBLinks(Collections.singletonList(link));
        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/link/{linkId}/references", method = RequestMethod.POST)
    public LinkDisplay addLinkReference(@PathVariable String linkId,
                                        @Valid @RequestBody MarkerReferenceBean newReference,
                                        BindingResult errors) {
        if (infrastructureRepository.getRecordAttribution(linkId, newReference.getZdbID(), RecordAttribution.SourceType.STANDARD) != null) {
            errors.rejectValue("zdbID", "marker.reference.inuse");
        }

        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid reference", errors);
        }

        HibernateUtil.createTransaction();
        infrastructureRepository.insertPublicAttribution(linkId, newReference.getZdbID());
        HibernateUtil.flushAndCommitCurrentSession();

        return getLinkDisplayById(linkId);
    }

    @ResponseBody
    @RequestMapping(value = "/link/{linkId}/references/{pubID}", method = RequestMethod.DELETE)
    public String removeLinkReference(@PathVariable String linkId,
                                      @PathVariable String pubID) {
        HibernateUtil.createTransaction();
        infrastructureRepository.deleteRecordAttribution(linkId, pubID);
        HibernateUtil.flushAndCommitCurrentSession();

        return "OK";
    }

    private LinkDisplay getLinkDisplayById(String linkId) {
        // this is kinda weird because LinkDisplay is only produced by a query transformer
        List<LinkDisplay> linkDisplays = markerRepository.getMarkerDBLink(linkId);
        if (linkDisplays.size() > 1) {
            LOG.error("too many LinkDisplays returned for " + linkId);
        }
        return linkDisplays.get(0);
    }

}
