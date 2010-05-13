package org.zfin.gwt.root.dto;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class PhenotypePileStructureDTO extends AbstractPileStructureDTO implements Comparable<PhenotypePileStructureDTO> {

    private PhenotypeTermDTO phenotypeTerm;

    public PhenotypeTermDTO getPhenotypeTerm() {
        return phenotypeTerm;
    }

    public void setPhenotypeTerm(PhenotypeTermDTO phenotypeTerm) {
        this.phenotypeTerm = phenotypeTerm;
    }

    @Override
    public PhenotypePileStructureDTO copy() {
        PhenotypePileStructureDTO dto = new PhenotypePileStructureDTO();
        dto.setZdbID(zdbID);
        return dto;
    }

    public int compareTo(PhenotypePileStructureDTO o) {
        if (o == null)
            return -1;
        if (o.getPhenotypeTerm() == null)
            return -1;
        if (phenotypeTerm == null)
            return 1;
        return phenotypeTerm.compareTo(o.getPhenotypeTerm());
    }
}
