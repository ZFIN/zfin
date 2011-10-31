package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

/**
 */
@Controller
public class MarkerViewController {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private Logger logger = Logger.getLogger(MarkerViewController.class);

    private final Pattern typePattern = Pattern.compile("ZDB-([\\p{Alpha}_]+)-.*");


    @Autowired
    private CloneViewController cloneViewController;
    @Autowired
    private AntibodyViewController antibodyViewController;
    @Autowired
    private GeneViewController geneViewController;
    @Autowired
    private PseudoGeneViewController pseudoGeneViewController;
    @Autowired
    private EfgViewController efgViewController;
    @Autowired
    private ConstructViewController constructViewController;
    @Autowired
    private MorpholinoViewController morpholinoViewController;
    @Autowired
    private SnpViewController snpViewController;
    @Autowired
    private TranscriptViewController transcriptViewController;
    @Autowired
    private GenericMarkerViewController genericMarkerViewController;


    @RequestMapping("/view/{key}")
    public String getAnyMarker(Model model
            , @PathVariable String key
            , @RequestHeader("User-Agent") String userAgent) {

        // first we set the key properly
        if (key.startsWith("ZDB-")) {
            if (false == markerRepository.markerExistsForZdbID(key)) {
                String replacedZdbID = infrastructureRepository.getReplacedZdbID(key);
                logger.debug("trying to find a replaced zdbID for: " + key);
                if (replacedZdbID != null) {
                    if (markerRepository.markerExistsForZdbID(replacedZdbID)) {
                        logger.debug("found a replaced zdbID for: " + key + "->" + replacedZdbID);
                        key = replacedZdbID;
                    }
                }
            }
        } else {
            Marker marker = markerRepository.getMarkerByAbbreviationIgnoreCase(key);
            if (marker == null) {
                marker = markerRepository.getMarkerByName(key);
            }
            if (marker == null) {
                List<Marker> markers = markerRepository.getMarkersByAlias(key);
                if (markers != null && markers.size() == 1) {
                    marker = markers.get(0);
                } else if (markers.size() != 1) {
                    //http://quark.zfin.org/quark/webdriver
                    return "redirect:/" + ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value() +
//                                "?MIval=aa-newmrkrselect.apg&marker_type=all&query_results=t&input_name="+
                            "?MIval=aa-newmrkrselect.apg&compare=starts&marker_type=all&query_results=exist&action=SEARCH&input_name=" +
                            key +
                            "&compare=starts&WINSIZE=200";
                }
            }

            if (marker != null) {
                key = marker.getZdbID();
            }
        }

        if (key == null || key.isEmpty() || false == markerRepository.markerExistsForZdbID(key)) {
            model.addAttribute(LookupStrings.ZDB_ID, key);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        } else {
            return getViewForMarkerZdbID(model, key, userAgent);
        }
    }

    public String getTypeForZdbID(String zdbID) {
        Matcher matcher = typePattern.matcher(zdbID);
        if (matcher.matches()) {
            int numGroups = matcher.groupCount();
            assert (numGroups == 1);
            return matcher.group(1);
        }
        return null;
    }

    private String getViewForMarkerZdbID(Model model, String zdbID, String userAgent) {

        String type = getTypeForZdbID(zdbID);

        try {
            if (type.equals(Marker.Type.ATB.name())) {
                return antibodyViewController.getAntibodyView(model, zdbID);
            } else if (type.equals(Marker.Type.BAC.name())
                    || type.equals(Marker.Type.CDNA.name())
                    || type.equals(Marker.Type.EST.name())
                    || type.equals(Marker.Type.FOSMID.name())
                    || type.equals(Marker.Type.PAC.name())
                    ) {
                return cloneViewController.getCloneView(model, zdbID);
            } else if (type.equals(Marker.Type.GENE.name())) {
                return geneViewController.getGeneView(model, zdbID);
            } else if (type.equals(Marker.Type.GENEP.name())) {
                return pseudoGeneViewController.getGeneView(model, zdbID);
            } else if (type.equals(Marker.Type.EFG.name())) {
                return efgViewController.getView(model, zdbID);
            } else if (type.equals(Marker.Type.ETCONSTRCT.name())
                    || type.equals(Marker.Type.GTCONSTRCT.name())
                    || type.equals(Marker.Type.PTCONSTRCT.name())
                    || type.equals(Marker.Type.TGCONSTRCT.name())
                    ) {
                return constructViewController.getGeneView(model, zdbID);
            } else if (type.equals(Marker.Type.MRPHLNO.name())) {
                return morpholinoViewController.getGeneView(model, zdbID);
            } else if (type.equals(Marker.Type.SNP.name())) {
                return snpViewController.getView(model, zdbID);
            } else if (type.equals(Marker.Type.TSCRIPT.name())) {
                return transcriptViewController.getTranscriptView(model, zdbID, userAgent);
            } else if (type.equals(Marker.Type.RAPD.name())
                    || type.equals(Marker.Type.STS.name())
                    || type.equals(Marker.Type.SSLP.name())
                    || type.equals(Marker.Type.BAC_END.name())
                    || type.equals(Marker.Type.PAC_END.name())
                    || type.equals(Marker.Type.REGION.name())
                    ) {
                return genericMarkerViewController.getGenericMarkerView(model, zdbID);
            }
            // includes GENEFAMILY and INDEL!
            else {
                logger.error("Should not display marker of type " + type + " for ID " + zdbID);
                return genericMarkerViewController.getGenericMarkerView(model, zdbID);
            }

        } catch (Exception e) {
            logger.error("Problem loading marker page: " + zdbID, e);
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
    }

    @RequestMapping("/view-all-engineered-regions/")
    public String viewAllEngineeredRegions(Model model) {

        List<Marker> engineeredRegions = getMarkerRepository().getAllEngineeredRegions();
        model.addAttribute("engineeredRegions",engineeredRegions);
        return "marker/view-all-engineered-regions.page";
    }

}

