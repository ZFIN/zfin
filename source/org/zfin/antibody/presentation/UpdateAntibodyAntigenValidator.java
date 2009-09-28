package org.zfin.antibody.presentation;

import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.repository.RepositoryFactory;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.infrastructure.DataAlias;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.apache.commons.lang.StringUtils;


public class UpdateAntibodyAntigenValidator implements Validator {
    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();
       private PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        public boolean supports(Class aClass) {
           return true;
       }


       public void validate(Object command, Errors errors) {
           AntibodyUpdateDetailBean formBean = (AntibodyUpdateDetailBean) command;
           PublicationValidator.validatePublicationID(formBean.getAntibodyDefPubZdbID(), formBean.AB_DEFPUB_ZDB_ID, errors);

           if (StringUtils.isEmpty(formBean.getNewAntigenGene())) {
                errors.rejectValue("newAntigenGene", "code", " Empty antigen gene is not allowed.");
        }

           if (!StringUtils.isEmpty(formBean.getNewAntigenGene())) {
               Marker m = mr.getMarkerByAbbreviation(formBean.getNewAntigenGene());
               if (m == null) {
                   errors.rejectValue("newAntigenGene", "code", formBean.getNewAntigenGene() + " is not a valid marker abbrev in ZFIN.");
               }
           }
           if (!StringUtils.isEmpty(formBean.getNewAntigenGene())) {
               Marker m = mr.getMarkerByAbbreviation(formBean.getNewAntigenGene());
               if (m != null) {
                 if(!(m.isInTypeGroup(Marker.TypeGroup.GENEDOM_AND_EFG))){
                   errors.rejectValue("newAntigenGene", "code", " Only GENES and EFG's can be added here.");
               }
               }

           }
         AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody antibodytoUpdate = antibodyRepository.getAntibodyByID(formBean.getAntibody().getZdbID());
        Marker m=mr.getMarkerByAbbreviation(formBean.getNewAntigenGene());
         if (m != null) {
        MarkerRelationship mrel=mr.getSpecificMarkerRelationship(antibodytoUpdate,m,MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
        if (mrel!=null) {
             errors.rejectValue("newAntigenGene", "code", " This antigen gene already exists for this antibody");
        }
         }


       }


   }


