package org.zfin.profile.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.util.OrcidUtil;

/**
 */
@Component(value = "createPersonValidator")
public class CreatePersonValidator implements Validator{

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(Person.class);
    }

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public void validate(Object target, Errors errors) {
        Person person = (Person) target ;

        if(StringUtils.isEmpty(person.getPass1())){
            errors.reject("","Password must not be empty.");
        }

        if(StringUtils.isEmpty(person.getPutativeLoginName()) && StringUtils.isEmpty(person.getEmail())){
            errors.reject("","Must specify a login name or email.");
        }

        if(profileRepository.userExists(person.getPutativeLoginName())){
            errors.reject("", "A User with that login name already exists.");
        }

        if(profileRepository.userExists(person.getEmail())){
            errors.reject("", "A User with that email login already exists.");
        }

        if(StringUtils.isEmpty(person.getFirstName())){
            errors.rejectValue("firstName","","Must specify a first name.");
        }

        if(StringUtils.isEmpty(person.getLastName())){
            errors.rejectValue("lastName","","Must specify a last name.");
        }

        if(profileRepository.emailExists(person.getEmail())){
            errors.reject("", "A User with that email already exists.");
        }

        // An unreadable ORCID would otherwise be silently dropped when it is canonicalized, or
        // breach the 19 character limit on the column, which this path does not catch.
        if(StringUtils.isNotEmpty(person.getOrcidID())){
            if(!OrcidUtil.isValid(person.getOrcidID())){
                errors.rejectValue("orcidID","","ORCID iD must be 16 digits in the form 0000-0002-1825-0097.");
            } else {
                // An ORCID identifies one researcher, so two person records must never share one.
                Person existing = profileRepository.getPersonByOrcid(person.getOrcidID());
                if(existing != null){
                    errors.rejectValue("orcidID","","That ORCID iD already belongs to "
                            + existing.getFullName() + " (" + existing.getZdbID()
                            + "). Update that record rather than creating a second one.");
                }
            }
        }
    }
}
