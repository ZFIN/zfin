package org.zfin.profile.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.presentation.PreviousNameLight;
import org.zfin.profile.*;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.profile.service.Country;
import org.zfin.profile.service.ProfileService;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;

/**
 */
@Controller
@RequestMapping(value = "/profile")
public class PersonController {

    private Logger logger = Logger.getLogger(PersonController.class);

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileService profileService;

    @Qualifier("createPersonValidator")
    @Autowired
    private CreatePersonValidator createPersonValidator;


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }


    public static enum TAB_INDEX {
        INFORMATION("information", 0), BIOGRAPHY("biography", 1), PUBLICATIONS("publications", 2), LOGIN("login", 3), PICTURE("picture", 4);

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

    @RequestMapping(value = "/person/edit/{zdbID}", method = RequestMethod.GET)
    public String editView(@PathVariable String zdbID, Model model) {
        String securityPersonZdbID = profileService.isEditableBySecurityPerson(zdbID);

        Person person = profileRepository.getPerson(zdbID);
        model.addAttribute(LookupStrings.FORM_BEAN, person);
        model.addAttribute("securityPersonZdbID", securityPersonZdbID);
        List<Country> countries = profileService.getCountries(Locale.ENGLISH);
        Map<String, String> country = new HashMap<String, String>(countries.size());
        country.put("","");
        for (Country countriesMap : countries) {
            country.put(countriesMap.getCountryCode(), countriesMap.getName());
        }
        model.addAttribute("countryList",country);
        boolean showDeceasedCheckBox = false;
        if (profileService.getCurrentSecurityUser() != null   // it's a logged-in user
                && profileService.isCurrentSecurityUserRoot()) {  //  the user logged in as root
            showDeceasedCheckBox = true;
        }
        model.addAttribute("showDeceasedCheckBox", showDeceasedCheckBox);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.PERSON.getTitleString() + person.getFullName());
        return "profile/profile-edit.page";
    }


    @RequestMapping(value = "/person/update-biography/{zdbID}", method = RequestMethod.POST)
    public String editBiography(@PathVariable String zdbID, Model model, Person newPerson, Errors errors) throws Exception {
        final String securityPersonZdbId = profileService.isEditableBySecurityPerson(zdbID);
        final Person person = profileRepository.getPerson(zdbID);

        model.addAttribute(LookupStrings.FORM_BEAN, person);
        model.addAttribute(LookupStrings.ERRORS, errors);
        model.addAttribute(LookupStrings.SELECTED_TAB, TAB_INDEX.BIOGRAPHY.getLabel());

        if (errors.hasErrors()) {
            return "profile/profile-edit.page";
        }

        final List<BeanFieldUpdate> fields = new ArrayList<>();
        try {
            fields.addAll(profileService.compareBiographyField(person, newPerson));
        } catch (Exception e) {
            logger.error(e.getStackTrace());

            errors.reject("", "There was a problem updating your user record.");
        }

        if (errors.hasErrors()) {
            return "profile/profile-edit.page";
        }

        return profileService.handleInfoUpdate(errors/*, profileTopic*/, person.getZdbID(), fields, securityPersonZdbId);

    }

    @RequestMapping(value = "/person/update-publications/{zdbID}", method = RequestMethod.POST)
    public String editPublications(@PathVariable String zdbID, Model model, Person newPerson, Errors errors) throws Exception {

        final String securityPersonZdbId = profileService.isEditableBySecurityPerson(zdbID);
        final Person person = profileRepository.getPerson(zdbID);

        model.addAttribute(LookupStrings.FORM_BEAN, person);
        model.addAttribute(LookupStrings.ERRORS, errors);
        model.addAttribute(LookupStrings.SELECTED_TAB, TAB_INDEX.PUBLICATIONS.getLabel());

        if (errors.hasErrors()) {
            return "profile/profile-edit.page";
        }

        final List<BeanFieldUpdate> fields = new ArrayList<>();
        try {

            fields.addAll(profileService.comparePublicationsField(person, newPerson));
        } catch (Exception e) {
            logger.error("fail", e);

            errors.reject("", "There was a problem updating your user record.");
        }

        if (errors.hasErrors()) {
            return "profile/profile-edit.page";
        }

        return profileService.handleInfoUpdate(errors/*, profileTopic*/, person.getZdbID(), fields, securityPersonZdbId);
    }



    @RequestMapping(value = "/person/edit-user-details/{zdbID}", method = RequestMethod.POST)
    public String submitAccountInfoDetails(@PathVariable String zdbID, Model model, Person newPerson, Errors errors)
            throws Exception {
        final String securityPersonZdbId = profileService.isEditableBySecurityPerson(zdbID);
        final Person person = profileRepository.getPerson(zdbID);
        AccountInfo newAccountInfo = newPerson.getAccountInfo();
        model.addAttribute(LookupStrings.SELECTED_TAB, TAB_INDEX.LOGIN.getLabel());

        model.addAttribute(LookupStrings.FORM_BEAN, person);
        model.addAttribute(LookupStrings.ERRORS, errors);

        AccountInfo oldAccountInfo = person.getAccountInfo();
        // TODO: this should already be there . . . maybe remove if we do a join sub-class
        oldAccountInfo.setZdbID(person.getZdbID());

        if (StringUtils.isEmpty(newAccountInfo.getLogin()) || newAccountInfo.getLogin().length() < 2) {
            errors.reject("", "Login must not be empty and must be greater than 2 characters.");
        }

        if (StringUtils.isNotEmpty(newAccountInfo.getPass1())) {
            if (StringUtils.isEmpty(newAccountInfo.getPass2())
                    || false == newAccountInfo.getPass2().equals(newAccountInfo.getPass1())) {
                errors.reject("", "Passwords must match");
            }
            newAccountInfo.setPassword(profileService.encodePassword(newAccountInfo.getPass1()));
        } else {
            newAccountInfo.setPassword(oldAccountInfo.getPassword());
        }

        if (errors.hasErrors()) {
            return "profile/profile-edit.page";
        }


        final List<BeanFieldUpdate> fields = new ArrayList<>();
        try {

            fields.addAll(profileService.compareAccountInfoFields(oldAccountInfo, newAccountInfo));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            errors.reject("", "There was a problem updating your user record.");
        }

        if (errors.hasErrors()) {
            return "profile/profile-edit.page";
        }

        try {
            HibernateUtil.createTransaction();

            profileService.updateAccountInfoWithFields(person.getZdbID(), fields, securityPersonZdbId);
            HibernateUtil.currentSession().flush();

            final ProfileUpdateMessageBean accountInfoUpdateMessageBean = new ProfileUpdateMessageBean();
            accountInfoUpdateMessageBean.setProfileType(ProfileUpdateMessageBean.ProfileType.ACCOUNT_INFO);
            accountInfoUpdateMessageBean.setZdbIdToEdit(person.getZdbID());
            accountInfoUpdateMessageBean.setFields(fields);
            accountInfoUpdateMessageBean.setSecurityPersonZdbID(securityPersonZdbId);
            HibernateUtil.currentSession().flush();
            HibernateUtil.currentSession().getTransaction().commit();

            return "profile/profile-edit.page";
        } catch (ConstraintViolationException cve) {
            logger.error("Constraint violation when updating person", cve);
            HibernateUtil.rollbackTransaction();

            for (ConstraintViolation constraintViolation : cve.getConstraintViolations()) {
                if (constraintViolation.getInvalidValue().toString().length() > 0) {
                    errors.rejectValue(constraintViolation.getPropertyPath().toString(), ""
                            , constraintViolation.getPropertyPath() + " field may need help:  "
//                        + "] value["+constraintViolation.getInvalidValue()+"].  "
//                        + constraintViolation.getMessageTemplate());
                            + constraintViolation.getMessage());
                }
            }
            return "profile/profile-edit.page";
        } catch (Exception e) {
            logger.error("Failed to update person", e);
            HibernateUtil.rollbackTransaction();
            errors.reject("", "There was a problem updated the user record");
            return "profile/profile-edit.page";
        }

    }

    @RequestMapping(value = "/person/edit/{zdbID}", method = RequestMethod.POST)
    public String submitEdit
            (@PathVariable String zdbID,
             @RequestParam(value = "tab", required = false) String tab,
             Model model,
             Person newPerson,
             Errors errors)
            throws Exception {
        final String securityPersonZdbId = profileService.isEditableBySecurityPerson(zdbID);
        final Person person = profileRepository.getPerson(zdbID);

        model.addAttribute(LookupStrings.FORM_BEAN, person);
        model.addAttribute(LookupStrings.ERRORS, errors);

        if (!StringUtils.isEmpty(tab)) {
            model.addAttribute(LookupStrings.SELECTED_TAB, tab);
        } else {
            model.addAttribute(LookupStrings.SELECTED_TAB, TAB_INDEX.INFORMATION.getLabel());
        }


        newPerson.setUrl(profileService.processUrl(newPerson.getUrl()));

        if (errors.hasErrors()) {
            return "profile/profile-edit.page";
        }

        final List<BeanFieldUpdate> fields = new ArrayList<>();
        try {
            newPerson.generateNameVariations();
            fields.addAll(profileService.comparePersonFields(person, newPerson));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            errors.reject("", "There was a problem updating your user record.");
        }

        if (errors.hasErrors()) {
            return "profile/profile-edit.page";
        }

        return profileService.handleInfoUpdate(errors/*, profileTopic*/, person.getZdbID(), fields, securityPersonZdbId);

    }

    @RequestMapping(value = "/person/view/{zdbID}", method = RequestMethod.GET)
    public String viewPerson
            (@PathVariable String
                     zdbID, Model
                    model) {
        Person person = profileRepository.getPerson(zdbID);
        if (person == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        model.addAttribute(LookupStrings.FORM_BEAN, person);

        boolean isOwner = profileService.isCurrentSecurityUserRoot();
        if (!isOwner && profileService.getCurrentSecurityUser() != null) {
            isOwner = profileService.getCurrentSecurityUser().getZdbID().equals(zdbID);
        }
        model.addAttribute(LookupStrings.IS_OWNER, isOwner);
        List<CompanyPresentation> companies = profileRepository.getCompanyForPersonId(zdbID);
        model.addAttribute("companies", companies);
        List<LabPresentation> labs = profileRepository.getLabsForPerson(zdbID);
        model.addAttribute("labs", labs);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.PERSON.getTitleString() + person.getFullName());
        return "profile/profile-view.page";
    }

    @RequestMapping(value = "/person/create", method = RequestMethod.GET)
    public String createPersonSetup
            (@RequestParam(value = "organization", required = false) String organizationZdbId,
             Model model, Person
                    person, Errors
                    errors) {
        model.addAttribute(LookupStrings.FORM_BEAN, person);

        if (!StringUtils.isEmpty(organizationZdbId))
            return createPersonSetupWithOrganization(organizationZdbId, model, person, errors);

        return "profile/create-person.page";
    }


    /*
     * Create a new person using an existing organization (lab/company) as a starting place     
     */
    @RequestMapping(value = "/person/create/{organization}", method = RequestMethod.GET)
    public String createPersonSetupWithOrganization
    (@PathVariable("organization") String organizationZdbID, Model
            model, Person
            person, Errors
            errors) {
        model.addAttribute(LookupStrings.FORM_BEAN, person);

        logger.debug("passed in an organization");

        //todo: this is probably a little over-duplicated, I only care about the org type
        //for the sake of which position list to get...
        if (organizationZdbID != null && organizationZdbID.startsWith("ZDB-LAB")) {
            Lab lab = profileRepository.getLabById(organizationZdbID);
            if (lab != null) {
                model.addAttribute("organization", lab);
                model.addAttribute("positions", profileRepository.getLabPositions());
            }

        } else if (organizationZdbID != null && organizationZdbID.startsWith("ZDB-COMPANY")) {
            Company company = profileRepository.getCompanyById(organizationZdbID);
            if (company != null) {
                model.addAttribute("organization", company);
                model.addAttribute("positions", profileRepository.getCompanyPositions());
            }

        }

        return "profile/create-person.page";
    }


    @RequestMapping(value = "/person/create", method = RequestMethod.POST)
    public String createPersonFromOrganization(
            Model model, Person
            person, Errors
            errors) {

        model.addAttribute(LookupStrings.FORM_BEAN, person);
        // do a very little amount of validation
        createPersonValidator.validate(person, errors);
        if (errors.hasErrors()) {
            model.addAttribute(LookupStrings.ERRORS, errors);
            return "profile/create-person.page";
        }

        Organization organization = profileRepository.getOrganizationByZdbID(person.getOrganizationZdbId());


        if (organization != null && person.getPosition() != null) {
            PersonMemberPresentation pmp = new PersonMemberPresentation();
            pmp.setPersonZdbID(person.getZdbID());
            pmp.setOrganizationZdbID(person.getOrganizationZdbId());
            pmp.setPosition(person.getPosition());

            if (organization.getCompany()) {
                pmp.setPositionString(profileService.getCompanyPositionString(pmp.getPosition()));
            } else {
                pmp.setPositionString(profileService.getLabPositionString(pmp.getPosition()));
            }


            HibernateUtil.createTransaction();
            profileService.createPerson(person, pmp);
            HibernateUtil.flushAndCommitCurrentSession();

            HibernateUtil.currentSession().refresh(person);
        } else {
            HibernateUtil.createTransaction();
            profileService.createPerson(person);
            HibernateUtil.flushAndCommitCurrentSession();

            HibernateUtil.currentSession().refresh(person);
        }
        return "redirect:/action/profile/person/edit/" + person.getZdbID();
    }


}
