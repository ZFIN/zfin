package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.Database;

import java.util.ArrayList;
import java.util.List;

/**
 */
@Controller
public class SnpViewController {

    private Logger logger = Logger.getLogger(SnpViewController.class);
    private String snpBlastUrl ;
    private String ncbiBlastUrl ;

    @Autowired
    private MarkerRepository markerRepository ;

    public SnpViewController(){
        snpBlastUrl = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.SNPBLAST).getLocation();
        ncbiBlastUrl = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.BLAST).getLocation();
    }

    @RequestMapping(value ="/snp/view/{zdbID}")
    public String getView(
            Model model
            , @PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean

        logger.debug("zdbID: " + zdbID);
        Marker marker = markerRepository.getMarkerByID(zdbID);
        logger.debug("snp marker: " + marker);

        SnpMarkerBean snpMarkerBean = new SnpMarkerBean();
        snpMarkerBean.setMarker(marker);

        MarkerService.createDefaultViewForMarker(snpMarkerBean);
        snpMarkerBean.setSnpBlastUrl(snpBlastUrl);
        snpMarkerBean.setNcbiBlastUrl(ncbiBlastUrl);

        // add variant
        snpMarkerBean.setVariant(markerRepository.getVariantForSnp(zdbID));

        // add sequence
        snpMarkerBean.setSequences(markerRepository.getMarkerSequences(marker));

        // snp marker relationships (is only secondary)
        List<MarkerRelationshipPresentation> cloneRelationships  = new ArrayList<MarkerRelationshipPresentation>();
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, false));
        snpMarkerBean.setMarkerRelationshipPresentationList(cloneRelationships);


        // add snp mapping
        MappedMarkerBean mappedMarkerBean = MarkerService.getSnpMappedMarkers(marker);
        mappedMarkerBean.setMarker(marker);
        snpMarkerBean.setMappedMarkerBean(mappedMarkerBean);


        model.addAttribute(LookupStrings.FORM_BEAN, snpMarkerBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, marker.getType().toString() + marker.getAbbreviation());

        return "marker/snp-view.page";
    }
}