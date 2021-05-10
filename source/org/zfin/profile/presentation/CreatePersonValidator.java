package org.zfin.profile.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;

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
    }
}
