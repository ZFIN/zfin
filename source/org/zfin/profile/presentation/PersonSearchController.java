package org.zfin.profile.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;

import java.util.List;

@Controller
@RequestMapping(value = "/profile")
public class PersonSearchController {

    @Autowired
    private ProfileRepository profileRepository;



    // for value
    @RequestMapping(value = "/person/search", method = RequestMethod.GET)
    public String personSearch(Model model,PersonSearchBean formBean) {
        formBean.setMaxDisplayRecords("25");
        model.addAttribute(LookupStrings.FORM_BEAN, formBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Person Search");

        return "profile/person-search" ;
    }

    @RequestMapping(value = "/person/search/execute", method = RequestMethod.GET)
    public String personSearchSubmit(
            Model model
            , PersonSearchBean searchBean
    ) {
        // pass in letter and people
        PaginationResult<Person> personPaginationResult = profileRepository.searchPeople(searchBean);
        model.addAttribute("people", personPaginationResult.getPopulatedResults());
        searchBean.setTotalRecords(personPaginationResult.getTotalCount());

        // pass in "type" and organizations
        model.addAttribute(LookupStrings.FORM_BEAN, searchBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Person Search");

        return "profile/person-search";

    }


    @RequestMapping("/person/all-people/{lastNameStartsWith}")
    public String listAllLabs
            (
                    Model model
                    , @PathVariable String lastNameStartsWith
            ) {
        List<Person> personList = profileRepository.getPersonByLastNameStartsWith(lastNameStartsWith);
        model.addAttribute("letter",lastNameStartsWith) ;
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.PERSON.getTitleString()
                + " Last Name Starts with " + lastNameStartsWith);
        model.addAttribute("people", personList);
        return "profile/list-all-people";
    }
}
