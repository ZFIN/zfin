package org.zfin.zebrashare.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
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
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;
import org.zfin.zebrashare.FeatureCommunityContribution;
import org.zfin.zebrashare.repository.ZebrashareRepository;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/zebrashare")
public class LineEditController {

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private ZebrashareRepository zebrashareRepository;

    private final static Logger LOG = LogManager.getLogger(LineEditController.class);

    @ModelAttribute("functionalConsequenceList")
    public List<FeatureCommunityContribution.FunctionalConsequence> getFunctionalConsequenceList() {
        return Arrays.asList(FeatureCommunityContribution.FunctionalConsequence.values());
    }
    @ModelAttribute("nmdApparentList")
    public List<FeatureCommunityContribution.NMDApparent> getNMDApparentList() {
        return Arrays.asList(FeatureCommunityContribution.NMDApparent.values());
    }
    @RequestMapping(value = "/line-edit/{id}", method = RequestMethod.GET)
    public String viewLineEditForm(@PathVariable String id,
                                   @ModelAttribute("formBean") LineEditBean bean,
                                   Model model,
                                   HttpServletResponse response) {
        Feature feature = featureRepository.getFeatureByID(id);

        if (!ProfileService.isRootUser() && !zebrashareRepository.isAuthorizedSubmitter(feature, ProfileService.getCurrentSecurityUser())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        if (feature == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        FeatureCommunityContribution current = zebrashareRepository.getLatestCommunityContribution(feature);
        if (current != null) {
            bean.setFunctionalConsequence(current.getFunctionalConsequence());
            bean.setNmdApparent(current.getNmdApparent());
            bean.setAdultViable(current.getAdultViable());
            bean.setMaternalZygosityExamined(current.getMaternalZygosityExamined());
            bean.setCurrentlyAvailable(current.getCurrentlyAvailable());
            bean.setOtherLineInformation(current.getOtherLineInformation());
        }

        model.addAttribute(LookupStrings.FORM_BEAN, bean);
        addModelAttributes(model, feature);

        return "zebrashare/line-edit";
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

        FeatureCommunityContribution prevContribution = zebrashareRepository.getLatestCommunityContribution(feature);
        if (prevContribution == null) {
            prevContribution = new FeatureCommunityContribution();
        }
        FeatureCommunityContribution newContribution = new FeatureCommunityContribution();
        Person user = ProfileService.getCurrentSecurityUser();
        newContribution.setFeature(feature);
        newContribution.setFunctionalConsequence(bean.getFunctionalConsequence());
        newContribution.setNmdApparent(bean.getNmdApparent());
        newContribution.setAdultViable(bean.getAdultViable());
        newContribution.setMaternalZygosityExamined(bean.getMaternalZygosityExamined());
        newContribution.setCurrentlyAvailable(bean.getCurrentlyAvailable());
        newContribution.setOtherLineInformation(bean.getOtherLineInformation());
        newContribution.setDate(new GregorianCalendar());
        newContribution.setSubmitter(user);

        Transaction tx = HibernateUtil.createTransaction();
        try {
            HibernateUtil.currentSession().save(newContribution);
            tx.commit();
        } catch (Exception e) {
            LOG.error(e);
            tx.rollback();
            model.addAttribute("error", "There was an error while saving your update.");
            return "zebrashare/line-edit";
        }

        redirectAttributes.addFlashAttribute("success", "Update saved");

        if (!ProfileService.isRootUser()) {
            String subject = String.format("ZebraShare: %s (%s) updated by %s (%s)", feature.getName(), feature.getZdbID(),
                    user.getDisplay(), user.getZdbID());
            StringBuilder message = new StringBuilder(String.format(
                    "<p>ZebraShare details for feature <a href='https://%1$s/%2$s'>%3$s</a> updated by <a href='https://%1$s/%4$s'>%5$s</a>.</p>",
                    ZfinPropertiesEnum.DOMAIN_NAME, feature.getZdbID(), feature.getName(), user.getZdbID(), user.getDisplay()
            ));
            message.append("<table>");
            appendDiff(message, "Functional Consequence", prevContribution.getFunctionalConsequence(), newContribution.getFunctionalConsequence());
            appendDiff(message, "NMD Apparent", prevContribution.getNmdApparent(), newContribution.getNmdApparent());
            appendDiff(message, "Adult Viable", prevContribution.getAdultViable(), newContribution.getAdultViable());
            appendDiff(message, "Maternal Zygocity Examined", prevContribution.getMaternalZygosityExamined(), newContribution.getMaternalZygosityExamined());
            appendDiff(message, "Currently Available", prevContribution.getCurrentlyAvailable(), newContribution.getCurrentlyAvailable());
            appendDiff(message, "Other Line Information", prevContribution.getOtherLineInformation(), newContribution.getOtherLineInformation());
            message.append("</table>");

            AbstractZfinMailSender.getInstance().sendHtmlMail(subject, message.toString(), new String[] {ZfinPropertiesEnum.CURATORS_AT_ZFIN.toString()}, null);
        }

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

    private void appendDiff(StringBuilder message, String label, Object previous, Object updated) {
        if (Objects.equals(previous, updated)) {
            return;
        }
        message.append("<tr>")
                .append("<td>").append(label).append(": ").append("</td>")
                .append("<td>");
        if (previous != null) {
            message.append("<span style='background-color:#ffe7e7;text-decoration:line-through;'>")
                    .append(valueToString(previous))
                    .append("</span> ");
        }
        if (updated != null) {
            message.append("<span style='background-color:#ddfade;'>")
                    .append(valueToString(updated))
                    .append("</span> ");
        }
        message.append("</td>")
                .append("</tr>");
    }

    private String valueToString(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? "Yes" : "No";
        }
        return value.toString();
    }

}
