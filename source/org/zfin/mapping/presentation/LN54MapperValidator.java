package org.zfin.mapping.presentation;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 */
public class LN54MapperValidator implements Validator {
    public boolean supports(Class aClass) {
        return LN54MapperBean.class.equals(aClass) ;
    }

    public void validate(Object o, Errors errors) {
        LN54MapperBean ln54MappingBean = (LN54MapperBean) o ;

        // validate name
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"name","not used","Required field");
        if(ln54MappingBean.getName().length()>=30){
            errors.rejectValue("name","not used","Name must be less than 30 characters.");
        }

        // validate email
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"email","not used","Required field");
        String email = ln54MappingBean.getEmail() ;
        Pattern p = Pattern.compile(".+@.+\\.[a-z]+");

        //Match the given string with the pattern
        Matcher m = p.matcher(email);
        if(false==m.matches()){
            errors.rejectValue("email","not used","Malformed email.");
        }

        // validate characterMarker
        ValidationUtils.rejectIfEmptyOrWhitespace(errors,"scoringVector","not used","Required field");
        if(errors.getFieldError("scoringVector")==null){
            String characterMarker = ln54MappingBean.getScoringVector() ;
            if(characterMarker.length()!=93){
                errors.rejectValue("scoringVector","not used","Length of string must be exactly 93, not "+characterMarker.length()+".");
            }

            for(Character c : characterMarker.toCharArray()){
                if( false==( c.equals('0') || c.equals('1') || c.equals('2') ) ){
                    errors.rejectValue("scoringVector","not used","Must only contain '0','1', or '2', not " + c+".");
                    break;
                }
            }
        }

        // validate optional marker name
        if(ln54MappingBean.getMarkerName().length()>=30){
            errors.rejectValue("markerName","not used","Makrer name must be less than 30 characters.");
        }
    }
}
