package org.zfin.jbrowse.presentation;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.api.RestErrorException;
import org.zfin.framework.api.RestErrorMessage;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.genomebrowser.presentation.GenomeBrowserFactory;
import org.zfin.genomebrowser.presentation.GenomeBrowserImage;
import org.zfin.mapping.GenomeLocation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.util.ZfinCollectionUtils.firstInEachGrouping;


@Controller
@Log4j
public class JBrowseController {

    private @Autowired HttpServletRequest request;

    @RequestMapping("/jbrowse")
    public String jbrowse(Model model) {
        model.addAttribute("requestParams", request.getQueryString());
        model.addAttribute("urlPrefix",JBrowseImage.calculateBaseUrl());
        return "jbrowse/jbrowse-view";
    }

    /**
     * This method is used to redirect to a JBrowse image page for a given accession.
     * It can accept a ZFIN gene or feature ID, or an ENSDARG ID.
     * Example request:
     *  https://zfin.org/action/jbrowse/byName?name=ENSDARG00000104430
     *  which will redirect to:
     *  https://zfin.org/jbrowse/?data=data%2FGRCz11&name=18%3A33009828..33013057
     *
     * @param model
     * @return
     */
    @RequestMapping("/jbrowse/byName")
    public String jbrowseByName(Model model) {
        String name = request.getParameter("name");
        if (name == null) {
            throw new RestErrorException(new RestErrorMessage(404));
        }

        List<GenomeBrowserTrack> tracks = new ArrayList<>();
        tracks.add(GenomeBrowserTrack.TRANSCRIPTS);

        List<? extends GenomeLocation> locations = (name.startsWith("ZDB-")) ?
                getLocationsByZDB(name, tracks) : //ZDB ID
                getLocationsByAccession(name);    //ENSDARG ID

        GenomeLocation location = validateLocations(locations);

        GenomeBrowserImage image = GenomeBrowserFactory.getStaticImageBuilder()
                .setLandmarkByGenomeLocation(location)
                .tracks(tracks.toArray(new GenomeBrowserTrack[tracks.size()]))
                .build();

        String url = image.getLinkUrl();

        //redirect to url
        return "redirect:" + url;
    }

    private List<? extends GenomeLocation> getLocationsByZDB(String zdbID, List<GenomeBrowserTrack> tracks) {
        if (zdbID.startsWith("ZDB-ALT")) {
            //feature
            tracks.add(GenomeBrowserTrack.ZFIN_FEATURES);
            return getLocationsByFeatureID(zdbID);
        } else {
            //other
            tracks.add(GenomeBrowserTrack.GENES);
            return getLocationsByMarkerID(zdbID);
        }
    }

    private List<? extends GenomeLocation> getLocationsByMarkerID(String id) {
        return getLinkageRepository()
                .getGenomeLocation(
                        getMarkerRepository().getMarkerByID(id), GenomeLocation.Source.ZFIN);
    }

    private List<? extends GenomeLocation> getLocationsByFeatureID(String id) {
        return getLinkageRepository()
                .getGenomeLocation(
                        getFeatureRepository().getFeatureByID(id), GenomeLocation.Source.ZFIN);
    }

    private List<? extends GenomeLocation> getLocationsByAccession(String name) {
        List<DBLink> dblinks = RepositoryFactory.getSequenceRepository().getDBLinksForAccession(name);
        validateDBLinks(dblinks, name);
        return getLocationsByMarkerID(dblinks.get(0).getDataZdbID());
    }

    private void validateDBLinks(List<DBLink> dblinks, String name) {
        if (dblinks.size() == 0) {
            throwRest404Error();
        } else if (dblinks.size() > 1) {
            log.debug("More than one dblink found for ENSDARG: " + name);
            List<DBLink> distinctZdbIDs = firstInEachGrouping(dblinks, DBLink::getDataZdbID);

            if (distinctZdbIDs.size() > 1) {
                log.debug("More than one gene found for ENSDARG: " + name);
            }
        }
    }

    private GenomeLocation validateLocations(List<? extends GenomeLocation> locations) {
        if (locations.size() == 0) {
            throwRest404Error();
        } else if (locations.size() > 1) {
            log.debug("More than one genome location found. Using first one.");
        }
        return locations.get(0);
    }

    private void throwRest404Error() {
        throw new RestErrorException(new RestErrorMessage(404));
    }

}
