package org.zfin.fish.presentation;

import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.fish.FeatureGene;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Container for fish search.
 */
public class FishBean extends AbstractFishViewBean {

    private MartFish fish;
    private List<GenotypeExperiment> genotypeExperimentsList;
    private List<Genotype> genotypes;
    private int totalNumberOfPublications;
    private int totalNumberOfPhenotypes;
    private List<SequenceTargetingReagent> sequenceTargetingReagents;
    private List<FeatureGene> genomicFeatures;

    public AuditLogItem getLatestUpdate() {
        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        return alr.getLatestAuditLogItem(genotype.getZdbID());
    }


    public List<Genotype> getGenotypes() {
        return genotypes;
    }

    /**
     * If the genotype is not set check the list of genotypes which is most likely
     * only one distinct genotype.
     * // ToDo: cleanup: remove genotypes in lieu of genotype.
     * @return Genotype
     */
    public Genotype getGenotype(){
        if(genotype != null)
            return genotype;
        if(genotypes != null)
            genotype = genotypes.get(0);
        return genotype;
    }

    public List<GenotypeExperiment> getGenotypeExperimentsList() {
        return genotypeExperimentsList;
    }

    public void setGenotypeExperimentsList(List<GenotypeExperiment> genotypeExperimentsList) {
        this.genotypeExperimentsList = genotypeExperimentsList;
    }

    public int getTotalNumberOfPublications() {
        return totalNumberOfPublications;
    }

    public void setTotalNumberOfPublications(int totalNumberOfPublications) {
        this.totalNumberOfPublications = totalNumberOfPublications;
    }

    public void setGenotypes(List<Genotype> genotypes) {
        this.genotypes = genotypes;

    }

    public MartFish getFish() {

        return fish;
    }


    public void setFish(MartFish fish) {
        this.fish = fish;
    }

    public List<SequenceTargetingReagent> getSequenceTargetingReagents() {
        return sequenceTargetingReagents;
    }

    public void setSequenceTargetingReagents(List<SequenceTargetingReagent> sequenceTargetingReagents) {
        this.sequenceTargetingReagents = sequenceTargetingReagents;
    }

    public int getTotalNumberOfPhenotypes() {
        return totalNumberOfPhenotypes;
    }

    public void setTotalNumberOfPhenotypes(int totalNumberOfPhenotypes) {
        this.totalNumberOfPhenotypes = totalNumberOfPhenotypes;
    }

    public List<FeatureGene> getGenomicFeatures() {
        return genomicFeatures;
    }

    public void setGenomicFeatures(List<FeatureGene> genomicFeatures) {
        this.genomicFeatures = genomicFeatures;
    }

}
