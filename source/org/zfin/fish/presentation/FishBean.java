package org.zfin.fish.presentation;

import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.fish.FeatureGene;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Container for fish search.
 */
public class FishBean extends AbstractFishViewBean {

    private List<FishExperiment> fishExperimentsList;
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

    public List<FishExperiment> getGenotypeExperimentsList() {
        return fishExperimentsList;
    }

    public void setGenotypeExperimentsList(List<FishExperiment> fishExperimentsList) {
        this.fishExperimentsList = fishExperimentsList;
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
