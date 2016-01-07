package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Collections;
import java.util.List;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 */
public class ExpressedTermDTO implements IsSerializable, Comparable<ExpressedTermDTO> {

    protected long id;
    protected String zdbID;
    protected EntityDTO entity;

    private boolean expressionFound = true;
    private EapQualityTermDTO qualityTerm;
    private List<EapQualityTermDTO> qualityTermDTOList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isExpressionFound() {
        return expressionFound;
    }

    public void setExpressionFound(boolean expressionFound) {
        this.expressionFound = expressionFound;
    }

    public String getDisplayName() {
        String display = entity.getDisplayName();
        if (isEap()) {
            display += "(";
            for (EapQualityTermDTO term : qualityTermDTOList) {
                display += term.getNickName();
                display += ", ";
            }
            display = display.substring(0, display.length() - 2);
            display += ")";
        }
        return display;
    }

    public String getHtmlDisplayName() {
        String display = entity.getDisplayName();
        if (isEap()) {
            display += "(";
            Collections.sort(qualityTermDTOList);
            for (EapQualityTermDTO term : qualityTermDTOList) {
                if (term.getNickName().contains("ok"))
                    display += "<span class=\"phenotype-normal\">" + term.getNickName() + "</span>";
                else
                    display += term.getNickName();
                display += ", ";
            }
            display = display.substring(0, display.length() - 2);
            display += ")";
        }
        return display;
    }

    public EntityDTO getEntity() {
        return entity;
    }

    public void setEntity(EntityDTO entity) {
        this.entity = entity;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public TermDTO getTermDTO(EntityPart entityPart) {
        switch (entityPart) {
            case ENTITY_SUPERTERM:
                if (entity == null)
                    return null;
                return entity.getSuperTerm();
            case ENTITY_SUBTERM:
                if (entity == null)
                    return null;
                return entity.getSubTerm();
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressedTermDTO termDTO = (ExpressedTermDTO) o;
/*
        Window.alert("term: quality: "+entity+": "+qualityTerm);
        Window.alert("quality to compare: "+entity+": "+termDTO.getQualityTerm());
*/
        if (expressionFound != termDTO.isExpressionFound())
            return false;

        if (!entity.equals(termDTO.getEntity()))
            return false;
        if (qualityTerm == null && termDTO.getQualityTerm() == null)
            return true;
        if (qualityTerm == null || termDTO.getQualityTerm() == null)
            return false;
        return qualityTerm.getNickName().equals(termDTO.getQualityTerm().getNickName());
    }

    @Override
    @SuppressWarnings({"NonFinalFieldReferencedInHashCode", "SuppressionAnnotation"})
    public int hashCode() {
        int result = entity.hashCode();
        if (qualityTerm != null)
            result += qualityTerm.hashCode();
        result += expressionFound ? 43 : 13;
        return result;
    }

    public String getUniqueID() {
        String uniqueID = "";
        if (!expressionFound)
            uniqueID += "not:";
        if (qualityTerm != null) {
            uniqueID += ":" + qualityTerm.getTerm().getOboID();
            uniqueID += ":" + qualityTerm.getTag();
        }
        return uniqueID + ":" + entity.getUniqueID();
    }

    public int compareTo(ExpressedTermDTO o) {
        if (o == null)
            return 1;
        if (entity.compareTo(o.getEntity()) != 0)
            return entity.compareTo(o.getEntity());
        if (qualityTerm == null && o.getQualityTerm() == null)
            return 0;
        if (qualityTerm == null)
            return -1;
        if (o.getQualityTerm() == null)
            return 1;
        return qualityTerm.getNickName().compareToIgnoreCase(o.getQualityTerm().getNickName());
    }

    /**
     * Checks equality based on term names only. This is needed for checking if a
     * new proposed post-composed term already exists.
     *
     * @param expressedTerm expressed Term
     * @return true or false
     */
    public boolean equalsByNameOnly(ExpressedTermDTO expressedTerm) {
        return entity.equalsByNameAndOntologyOnly(expressedTerm.getEntity());
    }

    public EapQualityTermDTO getQualityTerm() {
        return qualityTerm;
    }

    public void setQualityTerm(EapQualityTermDTO qualityTerm) {
        this.qualityTerm = qualityTerm;
    }

    public ExpressedTermDTO clone() {
        ExpressedTermDTO dto = new ExpressedTermDTO();
        dto.setZdbID(zdbID);
        dto.setId(id);
        dto.setExpressionFound(expressionFound);
        dto.setEntity(entity);
        dto.setQualityTerm(qualityTerm);
        return dto;
    }

    public boolean isPhenotype() {
        return qualityTerm != null && qualityTerm.getTerm() != null;
    }

    public void checkNotExpressed() {
        if (qualityTerm != null && qualityTerm.getTerm() == null)
            expressionFound = false;
    }

    public List<EapQualityTermDTO> getQualityTermDTOList() {
        return qualityTermDTOList;
    }

    public void setQualityTermDTOList(List<EapQualityTermDTO> qualityTermDTOList) {
        this.qualityTermDTOList = qualityTermDTOList;
    }

    public boolean isEap() {
        return qualityTermDTOList != null && qualityTermDTOList.size() > 0;
    }

    public boolean hasUniqueID(String id) {
        if (qualityTermDTOList == null || qualityTermDTOList.size() == 0)
            return getUniqueID().equals(id);
        for (EapQualityTermDTO dto : qualityTermDTOList) {
            ExpressedTermDTO newDto = this.clone();
            newDto.setQualityTerm(dto);
            if (newDto.getUniqueID().equals(id))
                return true;
        }
        return false;
    }

    public boolean isQualityAmeliorated() {
        return false;
    }
}