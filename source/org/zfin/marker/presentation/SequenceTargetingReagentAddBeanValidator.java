package org.zfin.marker.presentation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.ZfinStringUtils;

import java.util.List;

public class SequenceTargetingReagentAddBeanValidator implements Validator {

    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private ProfileRepository pr = RepositoryFactory.getProfileRepository();

    public boolean supports(Class aClass) {
        return SequenceTargetingReagentAddBean.class.isAssignableFrom(aClass);
    }

    public void validate(Object command, Errors errors) {
        SequenceTargetingReagentAddBean formBean = (SequenceTargetingReagentAddBean) command;

        String pubId = formBean.getPublicationID();
        PublicationValidator.validatePublicationID(pubId, "publicationID", errors);

        String strName = formBean.getName();
        if (StringUtils.isEmpty(strName)) {
            errors.rejectValue("name", "str.name.empty");
        } else if (mr.isMarkerExists(strName) || mr.getMarkerByName(strName) != null) {
            errors.rejectValue("name", "str.name.inuse");
        }

        String strSequence = formBean.getSequence();
        String strType = formBean.getStrType();
        Marker.Type type = Marker.Type.getTypeByPrefix(strType);
        validateSequence(errors, "sequence", strSequence, type);
        String targetGeneSymbol = formBean.getTargetGeneSymbol();
        if (StringUtils.isEmpty(targetGeneSymbol)) {
            errors.rejectValue("targetGeneSymbol", "str.target.empty");
        } else  {
            List<String> geneSymbols = List.of(targetGeneSymbol.split(","));
            for(String geneSymbol : geneSymbols){
                if (mr.getMarkerByAbbreviation(geneSymbol) == null) {
                    errors.rejectValue("targetGeneSymbol", "str.target.notfound");
                }
            }
            formBean.setTargetGeneSymbols(geneSymbols);
        }
        if (type == Marker.Type.MRPHLNO) {
            for(String geneSymbol : formBean.getTargetGeneSymbols()){
                if(mr.getMarkerByAbbreviation(geneSymbol).isInTypeGroup(Marker.TypeGroup.NONTSCRBD_REGION)){
                    errors.rejectValue("targetGeneSymbol", "str.target.invalid");
                }
            }
        }
        String strSupplier = formBean.getSupplier();
        if (!StringUtils.isEmpty(strSupplier)) {
            if (pr.getOrganizationByName(strSupplier) == null) {
                errors.rejectValue("supplier", "str.supplier.notfound");
            }
        }

        if (StringUtils.isBlank(strType)) {
            errors.rejectValue("strType", "str.type.empty");
        } else {
            //Marker.Type type = Marker.Type.getType(strType);
            String strSecondSequence = formBean.getSequence2();
            if (type == Marker.Type.TALEN) {
                validateSequence(errors, "sequence2", strSecondSequence, type);
            }

            SequenceTargetingReagent existingWithSequences;
            if (type == Marker.Type.TALEN) {
                existingWithSequences = mr.getSequenceTargetingReagentBySequence(type, strSequence, strSecondSequence);
            } else {
                existingWithSequences = mr.getSequenceTargetingReagentBySequence(type, strSequence);
            }
            if (existingWithSequences != null) {
                Object[] args = new Object[]{existingWithSequences.getName()};
                String defaultMessage = "Sequence is already used";
                errors.rejectValue("sequence", "str.sequence.inuse", args, defaultMessage);
                errors.rejectValue("sequence2", "str.sequence.inuse", args, defaultMessage);
            }
        }
    }

    private void validateSequence(Errors errors, String field, String sequence, Marker.Type type) {
        if (StringUtils.isBlank(sequence)) {
            errors.rejectValue(field, "str.sequence.empty");
        } else if (!ZfinStringUtils.isValidNucleotideSequence(sequence, type)) {
            errors.rejectValue(field, "str.sequence.characters");
        }
    }
}

