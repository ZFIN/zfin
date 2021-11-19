package org.zfin.nomenclature.repair;

import java.util.Objects;

//basically a struct for the "feature name" that is a component of genotype names
public class GenotypeFeatureName {

    public String geneName;
    public String transGene;
    public boolean geneHeterozygous;
    public String secondGene;
    public String secondTransGene;
    public String firstAllele;
    public String secondAllele;
    public String firstAlleleTransGene;
    public String secondAlleleTransGene;
    public boolean alleleHeterozygous;
    public String originalRepresentation;

    public GenotypeFeatureName(String geneName, String transGene, boolean geneHeterozygous, String secondGene, String secondTransGene, String firstAllele, String secondAllele, String firstAlleleTransGene, String secondAlleleTransGene, boolean alleleHeterozygous, String originalRepresentation) {
        this.geneName = geneName;
        this.transGene = transGene;
        this.geneHeterozygous = geneHeterozygous;
        this.secondGene = secondGene;
        this.secondTransGene = secondTransGene;
        this.firstAllele = firstAllele;
        this.secondAllele = secondAllele;
        this.firstAlleleTransGene = firstAlleleTransGene;
        this.secondAlleleTransGene = secondAlleleTransGene;
        this.alleleHeterozygous = alleleHeterozygous;
        this.originalRepresentation = originalRepresentation;
    }

    public String toString() {
        return geneName +
                (transGene != null ? transGene : "") +
                (geneHeterozygous ? "/+" : "") +
                (secondGene != null ? "/" + secondGene : "") +
                (secondTransGene != null ? secondTransGene : "") +
                (firstAllele != null ? "<sup>" + firstAllele : "") +
                (firstAlleleTransGene != null ? firstAlleleTransGene : "") +
                (secondAllele != null ? "/" + secondAllele : "") +
                (secondAlleleTransGene != null ? secondAlleleTransGene : "") +
                (alleleHeterozygous ? "/+" : "") +
                (firstAllele != null ? "</sup>" : "")
                ;
    }

    public boolean containsTransgenics() {
        return transGene != null ||
                secondTransGene != null ||
                firstAlleleTransGene != null ||
                secondAlleleTransGene != null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof GenotypeFeatureName)) return false;
        GenotypeFeatureName otherMyClass = (GenotypeFeatureName)other;
        return this.originalRepresentation.equals(otherMyClass.originalRepresentation);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        return prime + Objects.hashCode(this.originalRepresentation);
    }

}
