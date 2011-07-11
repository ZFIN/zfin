package org.zfin.marker.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
@Controller
public class MarkerNotesController {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    @RequestMapping("/note/expression")
    public String getXpatSelectNote() {
        return "marker/expression-note.insert";
    }


    @RequestMapping("/note/phenotype")
    public String getPhenotypeSelectNote() {
        return "marker/phenotype-note.insert";
    }

    @RequestMapping("/note/external/{zdbID}")
    public String getExternalNote(
            @PathVariable String zdbID
            , Model model) {

        Marker marker = markerRepository.getMarkerByID(zdbID);
        List<String> notes = RepositoryFactory.getInfrastructureRepository().getExternalOrthologyNoteStrings(marker.getZdbID());
        model.addAttribute("marker", marker);
        model.addAttribute("notes", notes);

        return "marker/external-note.insert";
    }


    @RequestMapping("/gene-product-description/{zdbID}")
    public String getGeneProducts(
            @PathVariable String zdbID
            , Model model
    ) {
        List<GeneProductsBean> geneProductsBeans = markerRepository.getGeneProducts(zdbID);
        Marker marker = markerRepository.getMarkerByID(zdbID);

        if (geneProductsBeans == null) {
            geneProductsBeans = new ArrayList<GeneProductsBean>();
        } else {
            for (GeneProductsBean geneProductsBean : geneProductsBeans) {
                geneProductsBean.setMarker(marker);
            }
        }
        model.addAttribute(LookupStrings.FORM_BEAN, geneProductsBeans);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Gene Products: " + marker.getAbbreviation());
        return "marker/gene-product-description.insert";
    }

    @RequestMapping("/snp-publication-list")
    public String snpPublicationListHandler(
            @RequestParam("markerID") String zdbID
            , @RequestParam String orderBy
            , Model model
    ) {
        SNPBean bean = new SNPBean();
        Marker marker = markerRepository.getMarkerByID(zdbID);
        bean.setMarker(marker);

        // TODO: make this method suck less
        // TODO: implement order by
        List<String> pubIDs = RepositoryFactory.getPublicationRepository().getSNPPublicationIDs(bean.getMarker());
        Set<Publication> pubs = new HashSet<Publication>();
        for (String id : pubIDs) {
            pubs.add(RepositoryFactory.getPublicationRepository().getPublication(id));
        }

        bean.setPublications(pubs);
        model.addAttribute(LookupStrings.FORM_BEAN, bean);

        return "marker/snp-publication-list.page";
    }

}

