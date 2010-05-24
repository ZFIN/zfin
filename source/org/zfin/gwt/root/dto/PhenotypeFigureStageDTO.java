package org.zfin.gwt.root.dto;

import org.zfin.gwt.root.util.NumberAwareStringComparatorDTO;

/**
 * Data Transfer Object corresponding to a unique combination of
 * Experiment, Figure and Stage range.
 */
public class PhenotypeFigureStageDTO extends AbstractFigureStageDTO<PhenotypeTermDTO> implements Comparable<PhenotypeFigureStageDTO>{

    private GenotypeDTO genotype;
    private EnvironmentDTO environment;

    public GenotypeDTO getGenotype() {
        return genotype;
    }

    public void setGenotype(GenotypeDTO genotype) {
        this.genotype = genotype;
    }

    public EnvironmentDTO getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentDTO environment) {
        this.environment = environment;
    }

    @Override
    public String getUniqueID() {
        StringBuilder sb = new StringBuilder(genotype.getZdbID());
        sb.append(":");
        sb.append(environment.getZdbID());
        sb.append(":");
        sb.append(super.getUniqueID());
        return sb.toString();
    }

    /**
     * This is a compound ID of the format:
     * experimentID:figureID:startStageID:endStageID.
     *
     * @param uniqueID concatenated unique ID.
     */
    @Override
    public void setUniqueID(String uniqueID) {
        String[] ids = uniqueID.split(":");
        if (ids.length != 5)
            throw new RuntimeException("unique id '" + uniqueID + "'not in the format genotypeID:environmentID:figureID:startStageID:endStageID");
        if (genotype == null)
            genotype = new GenotypeDTO();
        genotype.setZdbID(ids[0]);
        if (environment == null)
            environment = new EnvironmentDTO();
        environment.setZdbID(ids[1]);
        figure.setZdbID(ids[2]);
        if (start == null)
            start = new StageDTO();
        if (end == null)
            end = new StageDTO();
        start.setZdbID(ids[3]);
        end.setZdbID(ids[4]);
    }

    public int compareTo(PhenotypeFigureStageDTO efs) {
        if (efs == null)
            return -1;
        if (figure.getLabel() == null && efs.getFigure().getLabel() != null)
            return -1;
        if (figure.getLabel() != null && efs.getFigure().getLabel() == null)
            return 1;

        if ((figure.getLabel() != null && efs.getFigure().getLabel() != null)) {
            if (!figure.getLabel().equals(efs.getFigure().getLabel()) &&
                    (figure.getLabel() != null && efs.getFigure().getLabel() != null)){
                NumberAwareStringComparatorDTO comparator = new NumberAwareStringComparatorDTO();
                return comparator.compare(figure.getLabel(),efs.getFigure().getLabel());
            }
        }
        if (!genotype.equals(efs.getGenotype()))
            return genotype.compareTo(efs.getGenotype());
        if (!environment.equals(efs.getEnvironment()))
            return environment.compareTo(efs.getEnvironment());
        if (!start.getName().equals(efs.getStart().getName()))
            return (int) (start.getStartHours() * 10 - efs.getStart().getStartHours() * 10);
        if (!end.getName().equals(efs.getEnd().getName()))
            return (int) (end.getStartHours() * 10 - efs.getEnd().getStartHours() * 10);
        return 0;
    }

    
}