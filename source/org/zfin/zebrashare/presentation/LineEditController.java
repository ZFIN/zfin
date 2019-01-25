package org.zfin.zebrashare.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.Publication;
import org.zfin.zebrashare.repository.ZebrashareRepository;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/zebrashare")
public class LineEditController {

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private ZebrashareRepository zebrashareRepository;

    private final static Logger LOG = Logger.getLogger(LineEditController.class);

    @RequestMapping(value = "/line-edit/{id}", method = RequestMethod.GET)
    public String viewLineEditForm(@PathVariable String id,
                                   Model model,
                                   HttpServletResponse response) {
        Feature feature = featureRepository.getFeatureByID(id);

        if (feature == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        LineEditBean bean = new LineEditBean();
        bean.setFeature(feature);

        model.addAttribute(LookupStrings.FORM_BEAN, bean);
        Publication publication = zebrashareRepository.getZebraSharePublicationForFeature(feature);
        if (publication != null) {
            model.addAttribute("publication", publication);
            model.addAttribute("otherFeatures", featureRepository.getFeaturesByPublication(publication.getZdbID()));
        }
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit Line: " + feature.getName());

        return "zebrashare/line-edit.page";
    }

}
