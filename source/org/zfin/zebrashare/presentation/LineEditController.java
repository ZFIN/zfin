package org.zfin.zebrashare.presentation;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.zebrashare.FeatureCommunityContribution;
import org.zfin.zebrashare.repository.ZebrashareRepository;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

@Controller
@RequestMapping("/zebrashare")
public class LineEditController {

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private ZebrashareRepository zebrashareRepository;

    private final static Logger LOG = Logger.getLogger(LineEditController.class);

    @ModelAttribute("functionalConsequenceList")
    public List<FeatureCommunityContribution.FunctionalConsequence> getFunctionalConsequenceList() {
        return Arrays.asList(FeatureCommunityContribution.FunctionalConsequence.values());
    }

    @RequestMapping(value = "/line-edit/{id}", method = RequestMethod.GET)
    public String viewLineEditForm(@PathVariable String id,
                                   @ModelAttribute("formBean") LineEditBean bean,
                                   Model model,
                                   HttpServletResponse response) {
        Feature feature = featureRepository.getFeatureByID(id);

        if (feature == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        FeatureCommunityContribution current = zebrashareRepository.getLatestCommunityContribution(feature);
        if (current != null) {
            bean.setFunctionalConsequence(current.getFunctionalConsequence());
            bean.setAdultViable(current.getAdultViable());
            bean.setMaternalZygosityExamined(current.getMaternalZygosityExamined());
            bean.setCurrentlyAvailable(current.getCurrentlyAvailable());
            bean.setOtherLineInformation(current.getOtherLineInformation());
        }

        model.addAttribute(LookupStrings.FORM_BEAN, bean);
        addModelAttributes(model, feature);

        return "zebrashare/line-edit.page";
    }

    @RequestMapping(value = "/line-edit/{id}", method = RequestMethod.POST)
    public String processLineEditForm(@PathVariable String id,
                                      @ModelAttribute("formBean") LineEditBean bean,
                                      Model model,
                                      HttpServletResponse response,
                                      RedirectAttributes redirectAttributes) {
        Feature feature = featureRepository.getFeatureByID(id);

        if (feature == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        addModelAttributes(model, feature);

        FeatureCommunityContribution newContribution = new FeatureCommunityContribution();
        newContribution.setFeature(feature);
        newContribution.setFunctionalConsequence(bean.getFunctionalConsequence());
        newContribution.setAdultViable(bean.getAdultViable());
        newContribution.setMaternalZygosityExamined(bean.getMaternalZygosityExamined());
        newContribution.setCurrentlyAvailable(bean.getCurrentlyAvailable());
        newContribution.setOtherLineInformation(bean.getOtherLineInformation());
        newContribution.setDate(new GregorianCalendar());
        newContribution.setSubmitter(ProfileService.getCurrentSecurityUser());

        Transaction tx = HibernateUtil.createTransaction();
        try {
            HibernateUtil.currentSession().save(newContribution);
            tx.commit();
        } catch (Exception e) {
            LOG.error(e);
            tx.rollback();
            model.addAttribute("error", "There was an error while saving your update.");
            return "zebrashare/line-edit.page";
        }

        redirectAttributes.addFlashAttribute("success", "Update saved");
        return "redirect:" + feature.getZdbID();
    }

    private void addModelAttributes(Model model, Feature feature) {
        model.addAttribute("feature", feature);
        Publication publication = zebrashareRepository.getZebraSharePublicationForFeature(feature);
        if (publication != null) {
            model.addAttribute("publication", publication);
            model.addAttribute("otherFeatures", featureRepository.getFeaturesByPublication(publication.getZdbID()));
        }
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit Line: " + feature.getName());
    }

}
