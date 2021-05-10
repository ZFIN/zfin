package org.zfin.profile.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.profile.Company;
import org.zfin.profile.Lab;
import org.zfin.profile.Organization;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;

import java.util.List;

/**
 * This class is a controller for the ajax that edits members.
 */
@Controller
@RequestMapping(value = "/profile")
public class OrganizationMembersController {

    private Logger logger = LogManager.getLogger(OrganizationMembersController.class);

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileService profileService;


    @RequestMapping(value = "/list-members/{labZdbID}", method = RequestMethod.GET)
    public
    @ResponseBody
    List<PersonMemberPresentation> listMembers(@PathVariable String labZdbID) {
        if (labZdbID.startsWith("ZDB-LAB")) {
            return profileRepository.getLabMembers(labZdbID);
        } else if (labZdbID.startsWith("ZDB-COMPANY")) {
            return profileRepository.getCompanyMembers(labZdbID);
        } else {
            throw new RuntimeException("unable to find members for type of [" + labZdbID + "]");
        }
    }

    @RequestMapping(value = "/add-member/{personZdbID}/organization/{organizationZdbID}/position/{position}/name/{name}"
            , method = RequestMethod.POST)
    public
    @ResponseBody
    String addMember(
            @PathVariable String personZdbID,
            @PathVariable String organizationZdbID,
            @PathVariable Integer position,
            @PathVariable String name

    ) {
        HibernateUtil.createTransaction();
/*
       //leaving this commented out in case I need it for an updates table insert
        String positionTitle;
        Organization organization = profileRepository.getOrganizationByZdbID(organizationZdbID);
        if (organization.getCompany()) {
            positionTitle = profileService.getCompanyPositionString(position);
        } else {
            positionTitle = profileService.getLabPositionString(position);
        }
*/

        logger.debug(personZdbID);
        if (StringUtils.equals(personZdbID,"undefined")) {

            List<Person> people = profileRepository.getPeopleByFullName(name);
            if (people.size() > 1)
                return "\"" + name + "\" is not a unique name, select based on the [ID] in the autocomplete menu";

            if (people.size() == 0)
                return "\"" + name + "\" not found";
            else
                personZdbID = people.get(0).getZdbID();
        }

        final PersonMemberPresentation personMemberPresentation = new PersonMemberPresentation();
        personMemberPresentation.setPersonZdbID(personZdbID);
        personMemberPresentation.setOrganizationZdbID(organizationZdbID);
        personMemberPresentation.setPosition(position);
        personMemberPresentation.setAddFunction();
        boolean status = profileService.addPersonToOrganization(personMemberPresentation);
        HibernateUtil.currentSession().getTransaction().commit();

        return "";
    }

    @RequestMapping(value = "/change-position/{personZdbID}/organization/{organizationZdbID}/position/{position}"
            , method = RequestMethod.POST)
    public
    @ResponseBody
    boolean changeOrganizationPosition(
            @PathVariable String personZdbID,
            @PathVariable String organizationZdbID,
            @PathVariable Integer position
    ) {
        HibernateUtil.createTransaction();

        //leaving commented out in case I need it for an updates table statement
/*        String positionTitle;
        Organization organization = profileRepository.getOrganizationByZdbID(organizationZdbID);
        if (organization.getCompany()) {
            positionTitle = profileService.getCompanyPositionString(position);
        } else {
            positionTitle = profileService.getLabPositionString(position);
        }*/

        final PersonMemberPresentation personMemberPresentation = new PersonMemberPresentation();
        personMemberPresentation.setPersonZdbID(personZdbID);
        personMemberPresentation.setOrganizationZdbID(organizationZdbID);
        personMemberPresentation.setPosition(position);
        boolean status = profileService.changeOrganizationPosition(personMemberPresentation);

        HibernateUtil.currentSession().getTransaction().commit();
        return status ;
    }


    // looks up folks
    @RequestMapping(value = "/set-address/{personZdbId}/address/{sourceID}", method = RequestMethod.PUT)
    public
    @ResponseBody
    int setAddress(@PathVariable String personZdbId, @PathVariable("sourceID") String sourceZdbID) {
        Organization organization = profileRepository.getOrganizationByZdbID(sourceZdbID);
        Person person = profileRepository.getPerson(personZdbId);

        HibernateUtil.createTransaction();
        int returnValue = profileService.setPersonAddressToOrganizationAddress(person, organization);
        HibernateUtil.flushAndCommitCurrentSession();

        return returnValue;
    }

    // looks up labs and companies
    @RequestMapping(value = "/find-member", method = RequestMethod.GET)
    public
    @ResponseBody
    List<PersonLookupEntry> lookupMembers(@RequestParam("term") String lookupString) {
        List<PersonLookupEntry> personMemberPresentationList = profileRepository.getPersonNamesForString(lookupString);


        //this is a bit of a kludge, throw the zdb_id in just in case there's two users with the same name
        if (Person.isCurrentSecurityUserRoot()) {
            for (PersonLookupEntry personLookupEntry : personMemberPresentationList) {
                if (lookupEntryListContainsName(personMemberPresentationList, personLookupEntry.getValue()))
                personLookupEntry.setLabel(personLookupEntry.getLabel() + " [" + personLookupEntry.getId() + "]");
            }
        }

        return personMemberPresentationList;
    }

    private boolean lookupEntryListContainsName(List<PersonLookupEntry> personLookupEntryList, String value) {
        int nameCount = 0;
        for (PersonLookupEntry personLookupEntry : personLookupEntryList) {
            if (StringUtils.equals(personLookupEntry.getValue(), value))
                    nameCount++;
        }
        if (nameCount > 1)
            return true;
        else
            return false;
    }



    @RequestMapping(value = "/delete-member/{personZdbID}/organization/{organizationZdbID}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    int deleteMember(@PathVariable String personZdbID, @PathVariable String organizationZdbID) throws Exception{
        HibernateUtil.createTransaction();

        //if the person being removed is the contact person, set the contact person to null
        if (organizationZdbID.startsWith("ZDB-LAB")) {
            Lab lab = profileRepository.getLabById(organizationZdbID);
            if (lab.getContactPerson() != null && lab.getContactPerson().getZdbID().equals(personZdbID)) {
                lab.setContactPerson(null);
            }

        } else if (organizationZdbID.startsWith("ZDB-COMPANY")) {
            Company company = profileRepository.getCompanyById(organizationZdbID);
            if (company.getContactPerson() != null && company.getContactPerson().getZdbID().equals(personZdbID)) {
                company.setContactPerson(null);
            }
        }


        final PersonMemberPresentation personMemberPresentation = new PersonMemberPresentation();
        personMemberPresentation.setPersonZdbID(personZdbID);
        personMemberPresentation.setOrganizationZdbID(organizationZdbID);
        personMemberPresentation.setRemoveFunction();
        boolean result = profileService.removePersonFromOrganization(personMemberPresentation);


        HibernateUtil.currentSession().flush();
        HibernateUtil.currentSession().getTransaction().commit();
        
        return (result ? 1 : 0);
    }


    @RequestMapping(value = "/set-members-to-address/{organizationZdbID}", method = RequestMethod.PUT)
    public
    @ResponseBody
    int setMembersToAddress(@PathVariable String organizationZdbID) {
        HibernateUtil.createTransaction();
        int result = profileService.setMembersToOrganizationAddress(organizationZdbID);

        final PersonMemberPresentation personMemberPresentation = new PersonMemberPresentation();
        personMemberPresentation.setOrganizationZdbID(organizationZdbID);
        personMemberPresentation.setAllAddressToOrganization() ;
        HibernateUtil.currentSession().flush();
        HibernateUtil.currentSession().getTransaction().commit();

        return result;
    }

}
