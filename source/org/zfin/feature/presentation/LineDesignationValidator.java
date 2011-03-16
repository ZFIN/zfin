package org.zfin.feature.presentation;


import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;


public class LineDesignationValidator implements Validator  {

       public boolean supports(Class clazz) {
          return LineDesignationBean.class.equals(clazz);
       }


       public void validate(Object command, Errors errors) {
           LineDesignationBean formBean = (LineDesignationBean) command;
           String lineDesig=formBean.getLineDesig();
           String lineLocation=formBean.getLineLocation();
           if (StringUtils.isEmpty(lineDesig)) {
            errors.rejectValue("lineDesig", "code", "Line Designation cannot be null.");
        }
            if (StringUtils.isEmpty(lineLocation)) {
            errors.rejectValue("lineLocation", "code", "Line Location cannot be null.");
        }

       }

    }


