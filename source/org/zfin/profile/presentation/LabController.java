package org.zfin.profile.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.feature.FeaturePrefix;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.profile.Lab;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

/**
 */
@Controller
@RequestMapping("/profile")
public class LabController {

    private Logger logger = LogManager.getLogger(LabController.class);

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileService profileService;

    public static enum TAB_INDEX {
        INFORMATION("information", 0),
        MEMBERS("members", 1),
        PICTURE("picture", 2);

        private String label;
        private int index;

        TAB_INDEX(String label, int index) {
            this.label = label;
            this.index = index;
        }

        public String getLabel() {
            return label;
        }

        public int getIndex() {
            return index;
        }

    }

    @RequestMapping(value = "/lab/edit/{zdbID}", method = RequestMethod.GET)
    public String editView(@PathVariable String zdbID, Model model) {
        Lab lab = profileRepository.getLabById(zdbID);

        boolean isOwner = profileService.isCurrentSecurityUserRoot();
        if (!isOwner && profileService.getCurrentSecurityUser() != null) {
            isOwner = profileService.getCurrentSecurityUser().getZdbID().equals(lab.getContactPerson().getZdbID());
        }
        model.addAttribute(LookupStrings.IS_OWNER, isOwner);

        model.addAttribute(LookupStrings.FORM_BEAN, lab);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.LAB.getTitleString() + lab.getName());
        model.addAttribute("members", profileRepository.getLabMembers(zdbID));
        model.addAttribute("positions", profileRepository.getLabPositions());
        model.addAttribute("countryList", profileService.getCountries());
        List<String> prefixes = featureRepository.getAllFeaturePrefixes();
        prefixes.add(0, "- None -");
        model.addAttribute("prefixes", prefixes);
        lab.setPrefix(featureRepository.getCurrentPrefixForLab(zdbID));

        // designations // if submit, only show if root

        return "profile/profile-edit";
    }


    @RequestMapping(value = "/lab/edit/{zdbID}", method = RequestMethod.POST)
    public String submitEdit(@PathVariable String zdbID, Model model, @ModelAttribute("formBean") Lab newLab, BindingResult errors) {
        profileService.validateLab(newLab, errors);

        if (errors.hasErrors()) {
            return "profile/profile-edit";
        }

        final Lab lab = profileRepository.getLabById(zdbID);

        final String securityPersonZdbId = profileService.isEditableBySecurityPerson(lab.getContactPerson() != null ? lab.getContactPerson().getZdbID() : null);

        model.addAttribute(LookupStrings.ERRORS, errors);
        model.addAttribute("members", profileRepository.getLabMembers(zdbID));
        model.addAttribute(LookupStrings.SELECTED_TAB, TAB_INDEX.INFORMATION.getLabel());
        model.addAttribute("positions", profileRepository.getLabPositions());
        model.addAttribute("countryList", profileService.getCountries());
        List<String> prefixes = featureRepository.getAllFeaturePrefixes();
        prefixes.add(0, "- None -");
        model.addAttribute("prefixes", prefixes);
        lab.setPrefix(featureRepository.getCurrentPrefixForLab(zdbID));

        newLab.setUrl(profileService.processUrl(newLab.getUrl()));

        //convert from none to null
        if (newLab.getContactPerson() != null &&
                StringUtils.equals(newLab.getContactPerson().getZdbID(), "none")) {
            newLab.setContactPerson(null);
        }

        final List<BeanFieldUpdate> fields = new ArrayList<>();
        try {
            fields.addAll(profileService.compareLabFields(lab, newLab));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            errors.reject("", "There was a problem updating your user record.");
        }

        if (errors.hasErrors()) {
            return "profile/profile-edit";
        }

        return profileService.handleInfoUpdate(errors, lab.getZdbID(), fields, securityPersonZdbId);

    }

    @RequestMapping(value = "/lab/view/{zdbID}", method = RequestMethod.GET)
    public String viewLab(@PathVariable String zdbID, Model model) {
        Lab lab = profileRepository.getLabById(zdbID);
        if (lab == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        boolean isOwner = profileService.isCurrentSecurityUserRoot();
        if (!isOwner && profileService.getCurrentSecurityUser() != null && lab.getContactPerson() != null) {
            isOwner = profileService.getCurrentSecurityUser().getZdbID().equals(lab.getContactPerson().getZdbID());
        }
        model.addAttribute(LookupStrings.IS_OWNER, isOwner);

        model.addAttribute(LookupStrings.FORM_BEAN, lab);
        List<PersonMemberPresentation> labMembers = profileRepository.getLabMembers(zdbID);
        model.addAttribute("members", labMembers);
        int numCoPIs = profileService.findMembersEquals(2, labMembers);
        model.addAttribute("hasCoPi", numCoPIs > 0);
        List<Publication> publications = profileRepository.getPublicationsForLab(zdbID);
        model.addAttribute("publications", publications);
        List<FeaturePrefix> featurePrefixes = featureRepository.getLabPrefixesById(lab.getZdbID(), false);
        logger.info("featurePrefixCount" + featurePrefixes.size());
        model.addAttribute("prefixes", featurePrefixes);
        model.addAttribute("country", profileService.getCountryDisplayName(lab.getCountry()));

        boolean noPrefixes = featurePrefixes.isEmpty();
        if (!noPrefixes) {
            int ctNoneActiveForSet = 0;
            for (FeaturePrefix fpf : featurePrefixes) {
                logger.info("featurePrefix is:" + fpf.getPrefixString());
                if (!fpf.isActiveForSet()) {
                    ctNoneActiveForSet++;
                }
            }

            if (ctNoneActiveForSet == featurePrefixes.size()) {
                noPrefixes = true;
            }
        }
        model.addAttribute("noPrefixes", noPrefixes);

        // a lab could have prefixes while having no features (example as of 2013-01-24: ZDB-LAB-111031-1
        model.addAttribute("numOfFeatures", RepositoryFactory.getFeatureRepository().getFeaturesForLabCount(zdbID));

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.LAB.getTitleString() + lab.getName());
        return "profile/profile-view";
    }


    @RequestMapping(value = "/lab/create", method = RequestMethod.GET)
    public String createLabSetup(Model model, @ModelAttribute("formBean") Lab lab) {
		model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Lab");
        return "profile/create-lab";
    }

    @RequestMapping(value = "/lab/create", method = RequestMethod.POST)
    public String createLab(@ModelAttribute("formBean") Lab lab, BindingResult bindingResult) {
        profileService.validateLab(lab, bindingResult);
        if (bindingResult.hasErrors()) {
            return "profile/create-lab";
        }
        HibernateUtil.createTransaction();
        profileService.createLab(lab);
        HibernateUtil.flushAndCommitCurrentSession();
        HibernateUtil.currentSession().refresh(lab);
        return "redirect:/action/profile/lab/edit/" + lab.getZdbID();
    }


}
