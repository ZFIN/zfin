package org.zfin.feature.presentation;


import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.repository.RepositoryFactory;


public class AlleleLineDesignationValidator implements Validator  {

       public boolean supports(Class clazz) {
          return CreateAlleleDesigFormBean.class.equals(clazz);
       }


       public void validate(Object command, Errors errors) {
           CreateAlleleDesigFormBean formBean = (CreateAlleleDesigFormBean) command;
           String lineDesig=formBean.getLineDesig();
           String lineLocation=formBean.getLineLocation();
           if (StringUtils.isEmpty(lineDesig)) {
            errors.rejectValue("lineDesig", "code", "Line Designation cannot be null.");
        }
          if (!StringUtils.isEmpty(lineDesig)){
           String newPrefix= RepositoryFactory.getFeatureRepository().getPrefix(lineDesig);
           if (newPrefix != null){
               errors.rejectValue("lineDesig","code", "This feature prefix is already taken by another lab");
               return;
           }
          }
            if (StringUtils.isEmpty(lineLocation)) {
            errors.rejectValue("lineLocation", "code", "Line Location cannot be null.");
        }

       }

    }


