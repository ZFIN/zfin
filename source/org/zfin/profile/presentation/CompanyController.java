package org.zfin.profile.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.feature.FeaturePrefix;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.profile.Company;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.List;

/**
 */
@Controller
@RequestMapping(value = "/profile")
public class CompanyController {

    private Logger logger = Logger.getLogger(CompanyController.class);

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private ProfileService profileService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    public enum TAB_INDEX {
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

    @RequestMapping(value = "/company/edit/{zdbID}", method = RequestMethod.GET)
    public String editView(@PathVariable String zdbID, Model model) {
        Company company = profileRepository.getCompanyById(zdbID);


        String ownerZdbId = profileService.isEditableBySecurityPerson(company.getContactPerson());// will throw runtime otherwise
        boolean isOwner = profileService.isCurrentSecurityUserRoot();
        if (!isOwner && profileService.getCurrentSecurityUser() != null) {
            isOwner = profileService.getCurrentSecurityUser().getZdbID().equals(company.getContactPerson().getZdbID());
        }
        model.addAttribute(LookupStrings.IS_OWNER, isOwner);

        model.addAttribute(LookupStrings.FORM_BEAN, company);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.COMPANY.getTitleString() + company.getName());
        model.addAttribute("members", profileRepository.getCompanyMembers(zdbID));
        model.addAttribute("positions", profileRepository.getCompanyPositions());
        model.addAttribute("countryList", profileService.getCountries());
        List<String> prefixes = featureRepository.getAllFeaturePrefixes();
        prefixes.add(0, "- None -");
        model.addAttribute("prefixes", prefixes);
        company.setPrefix(featureRepository.getCurrentPrefixForLab(zdbID));
        return "profile/profile-edit.page";
    }


    @RequestMapping(value = "/company/edit/{zdbID}", method = RequestMethod.POST)
    public String submitEdit(@PathVariable String zdbID, Model model, Company newCompany, Errors errors)
            throws Exception {
        final Company company = profileRepository.getCompanyById(zdbID);
        final String securityPersonZdbId = profileService.isEditableBySecurityPerson(company.getContactPerson());

        model.addAttribute(LookupStrings.FORM_BEAN, company);
        model.addAttribute(LookupStrings.ERRORS, errors);
        model.addAttribute("members", profileRepository.getCompanyMembers(zdbID));
        model.addAttribute("positions", profileRepository.getCompanyPositions());
        model.addAttribute("countryList", profileService.getCountries());
        List<String> prefixes = featureRepository.getAllFeaturePrefixes();
        prefixes.add(0, "- None -");
        model.addAttribute("prefixes", prefixes);
        company.setPrefix(featureRepository.getCurrentPrefixForLab(zdbID));

        newCompany.setUrl(profileService.processUrl(newCompany.getUrl()));

        //convert from none to null
        if (newCompany.getContactPerson() != null &&
                StringUtils.equals(newCompany.getContactPerson().getZdbID(), "none")) {
            newCompany.setContactPerson(null);
        }

        model.addAttribute(LookupStrings.SELECTED_TAB, TAB_INDEX.INFORMATION.getLabel());

        if (errors.hasErrors()) {
            return "profile/profile-edit.page";
        }

        final List<BeanFieldUpdate> fields = new ArrayList<>();
        try {
            fields.addAll(profileService.compareCompanyFields(company, newCompany));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            errors.reject("", "There was a problem updating your user record.");
        }

        if (errors.hasErrors()) {
            return "profile/profile-edit.page";
        }

        return profileService.handleInfoUpdate(errors, company.getZdbID(), fields, securityPersonZdbId);
    }

    @RequestMapping(value = "/company/view/{zdbID}", method = RequestMethod.GET)
    public String viewCompany(String zdbID, Model model) {
        Company company = profileRepository.getCompanyById(zdbID);
        if (company == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        model.addAttribute(LookupStrings.FORM_BEAN, company);

        boolean isOwner = profileService.isCurrentSecurityUserRoot();
        if (!isOwner && profileService.getCurrentSecurityUser() != null && company.getContactPerson() != null) {
            isOwner = profileService.getCurrentSecurityUser().getZdbID().equals(company.getContactPerson().getZdbID());
        }
        model.addAttribute(LookupStrings.IS_OWNER, isOwner);

        model.addAttribute("members", profileRepository.getCompanyMembers(zdbID));
        List<Publication> publications = profileRepository.getPublicationsForCompany(zdbID);
        model.addAttribute("publications", publications);
        List<FeaturePrefix> featurePrefixes = featureRepository.getLabPrefixes(company.getName(), false);
        model.addAttribute("prefixes", featurePrefixes);
        model.addAttribute("country", profileService.getCountryDisplayName(company.getCountry()));

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.COMPANY.getTitleString() + company.getName());
        return "profile/profile-view.page";
    }


    @RequestMapping(value = "/company/create", method = RequestMethod.GET)
    public String createCompanySetup(Model model, Company company) {
        model.addAttribute(LookupStrings.FORM_BEAN, company);
        return "profile/create-company.page";
    }

    @RequestMapping(value = "/company/create", method = RequestMethod.POST)
    public String createLab(Company company) {
        HibernateUtil.createTransaction();
        profileService.createCompany(company);
        HibernateUtil.flushAndCommitCurrentSession();
        HibernateUtil.currentSession().refresh(company);
        return "redirect:/action/profile/company/edit/" + company.getZdbID();
    }

}
