package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.profile.Person;
import org.zfin.profile.presentation.PersonLookupEntry;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/marker")
public class RegionAddController {

    private static Logger LOG = Logger.getLogger(RegionAddController.class);

    @Autowired
    MarkerRepository markerRepository;

    @Autowired
    PublicationRepository publicationRepository;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new RegionAddFormBeanValidator());
    }

    @ModelAttribute("formBean")
    public RegionAddFormBean getDefaultForm(@RequestParam(required = false) String type,
                                          @RequestParam(required = false) String source) {
        RegionAddFormBean form = new RegionAddFormBean();
        form.setPublicationId(source);
        form.setType(type);
        List<MarkerType> markerTypes = markerRepository.getMarkerTypesByGroup(Marker.TypeGroup.NONTSCRBD_REGION);
        Map<String, String> allTypes = new LinkedHashMap<>(markerTypes.size());
        for (MarkerType markerType : markerTypes) {
                            allTypes.put(markerType.getType().name(), markerType.getDisplayName());
                    }



        form.setAllTypes(allTypes);
        return form;
    }

    @RequestMapping(value = "/nonTranscribedRegion-add", method = RequestMethod.GET)
    public String showRegionAddForm(Model model) {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add New NTR");
        return "marker/nonTranscribedRegion-add.page";
    }

    @RequestMapping(value = "/nonTranscribedRegion-add", method = RequestMethod.POST)
    public String processRegionAddForm(Model model,
                                     @Valid @ModelAttribute("formBean") RegionAddFormBean formBean,
                                     BindingResult result) {
        if (result.hasErrors()) {
            return showRegionAddForm(model);
        }

        Marker newRegion = new Marker();
        newRegion.setMarkerType(markerRepository.getMarkerTypeByName(formBean.getType()));
        newRegion.setName(formBean.getName());
        newRegion.setAbbreviation(formBean.getAbbreviation());
        newRegion.setPublicComments(formBean.getPublicNote());

        Publication reference = publicationRepository.getPublication(formBean.getPublicationId());

        try {
            HibernateUtil.createTransaction();
            markerRepository.createMarker(newRegion, reference);

            if (StringUtils.isNotEmpty(formBean.getAlias())) {
                markerRepository.addMarkerAlias(newRegion, formBean.getAlias(), reference);
            }

            if (StringUtils.isNotEmpty(formBean.getCuratorNote())) {
                markerRepository.addMarkerDataNote(newRegion, formBean.getCuratorNote());
            }

            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return "redirect:/" + newRegion.getZdbID();
    }

}
