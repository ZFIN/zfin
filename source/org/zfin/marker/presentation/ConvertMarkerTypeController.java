package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/marker")
public class ConvertMarkerTypeController {

    private Logger logger = LogManager.getLogger(ConvertMarkerTypeController.class);

    @RequestMapping(value = "/convert-type", method = RequestMethod.GET)
    protected String getView(
            Model model,
            @RequestParam("zdbID") String zdbID,
            @ModelAttribute("formBean") ConvertMarkerTypeBean formBean,
            BindingResult result
    ) throws Exception {
        MarkerRepository markerRepo = RepositoryFactory.getMarkerRepository();
        Marker marker = markerRepo.getMarkerByID(zdbID);
        formBean.setZdbIDToConvert(zdbID);
        formBean.setMarker(marker);

        List<MarkerType> allTypes = markerRepo.getAllMarkerTypes();
        allTypes.removeIf(t -> t.getName().equals(marker.getMarkerType().getName()));
        Collections.sort(allTypes);
        formBean.setAvailableTypes(allTypes);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, marker.getAbbreviation());
        return "marker/convert-marker-type";
    }

    @RequestMapping(value = "/convert-type", method = RequestMethod.POST)
    protected String convertType(
            Model model,
            @ModelAttribute("formBean") ConvertMarkerTypeBean formBean,
            BindingResult result
    ) throws Exception {
        MarkerRepository markerRepo = RepositoryFactory.getMarkerRepository();
        Marker marker = markerRepo.getMarkerByID(formBean.getZdbIDToConvert());
        formBean.setMarker(marker);

        if (formBean.getNewMarkerTypeName() == null || formBean.getNewMarkerTypeName().isEmpty()) {
            result.rejectValue("newMarkerTypeName", "required", "Please select a target type");
            return getView(model, formBean.getZdbIDToConvert(), formBean, result);
        }

        try {
            HibernateUtil.createTransaction();
            String newZdbId = markerRepo.convertMarkerType(
                    formBean.getZdbIDToConvert(), formBean.getNewMarkerTypeName());
            formBean.setNewZdbId(newZdbId);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error("Error converting marker [" + formBean.getZdbIDToConvert() + "] to type [" + formBean.getNewMarkerTypeName() + "]", e);
            HibernateUtil.rollbackTransaction();
            result.reject("error", "Error converting marker: " + e.getMessage());
            return getView(model, formBean.getZdbIDToConvert(), formBean, result);
        }

        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, marker.getAbbreviation());
        return "marker/convert-marker-type-finish";
    }
}
