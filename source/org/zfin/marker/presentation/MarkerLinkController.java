package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.DisplayGroupRepository;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.*;

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
                                     @RequestBody LinkDisplay newLink) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        String accessionNo = newLink.getAccession();
        ReferenceDatabase refDB = sequenceRepository.getReferenceDatabaseByID(newLink.getReferenceDatabaseZdbID());

        // assume there's only one pub coming in on a new db link
        String pubId = newLink.getAttributionZdbIDs().iterator().next();

        HibernateUtil.createTransaction();
        DBLink link = markerRepository.addDBLink(marker, accessionNo, refDB, pubId);
        HibernateUtil.flushAndCommitCurrentSession();

        // this is kinda weird because LinkDisplay is only produced by a query transformer
        List<LinkDisplay> linkDisplays = markerRepository.getMarkerDBLink(link.getZdbID());
        if (linkDisplays.size() > 1) {
            LOG.error("too many LinkDisplays returned for " + link.getZdbID());
        }
        return linkDisplays.get(0);
    }

    @ResponseBody
    @RequestMapping(value = "/link/{linkId}", method = RequestMethod.DELETE)
    public String deleteMarkerLink(@PathVariable String linkId) {
        DBLink link = sequenceRepository.getDBLinkByID(linkId);
        sequenceRepository.removeDBLinks(Collections.singletonList(link));
        return "OK";
    }

}
