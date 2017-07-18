package org.zfin.uniquery

import org.springframework.beans.factory.annotation.Autowired
import org.zfin.ZfinIntegrationSpec
import org.zfin.search.service.ResultService
import org.zfin.search.presentation.SearchResult
import org.zfin.search.Category
import spock.lang.Unroll

import static org.junit.Assume.assumeThat
import static org.hamcrest.Matchers.*


class ResultAttributesSpec extends ZfinIntegrationSpec {

    @Autowired
    ResultService resultService



    @Unroll
    def "Result attributes for #category #id #fieldLabel should contain #partialValue "() {
        when: "result is created and injected with attributes"

        assumeThat("phenox ids are transient", id, not(startsWith("phenox-")))
        assumeThat("xpatex ids are transient", id, not(startsWith("xpatex-")))

        SearchResult result = new SearchResult(id: id, categories: [category])
        resultService.injectAttributes(result)

        then: "the given attribute should contain the given value"
        result.attributes[fieldLabel]
        result.attributes[fieldLabel].toString().contains(partialValue)

        where:
        category           | id                         | fieldLabel                     | partialValue
        Category.GENE.name | "ZDB-GENE-990415-72"       | ResultService.PREVIOUS_NAME    | "cb110"
        Category.GENE.name | "ZDB-GENE-990415-72"       | ResultService.GENE_NAME        | "fibroblast"
        Category.GENE.name | "ZDB-GENE-990415-72"       | ResultService.GENE_NAME        | "genedom"
        Category.GENE.name | "ZDB-GENE-990415-72"       | ResultService.LOCATION         | "13"


        Category.MUTANT.name | "ZDB-ALT-050914-2"         | ResultService.SYNONYMS         | "mcr"
        Category.MUTANT.name | "ZDB-ALT-050914-2"         | ResultService.AFFECTED_GENES   | "apc"
        Category.MUTANT.name | "ZDB-ALT-050914-2"         | ResultService.TYPE             | "Point Mutation"
        //Category.MUTANT.name | "ZDB-ALT-111020-3"         | ResultService.SCREEN           | "Burgess / Lin"

        Category.CONSTRUCT.name | "ZDB-TGCONSTRCT-070117-24" | ResultService.SYNONYMS         | "NICD"
        Category.CONSTRUCT.name | "ZDB-TGCONSTRCT-070117-24" | ResultService.NOTE             | "This construct utilizes five copies of an optimized UAS from yeast and the minimal E1b promoter to drive the expression of a protein in which six myc epitopes are fused to the intracellular domain of the zebrafish notch1a protein."
        //todo: sequence attribute test goes here

        Category.SEQUENCE_TARGETING_REAGENT.name | "ZDB-MRPHLNO-051220-2"    | ResultService.SYNONYMS       | "six3-AMO"
        Category.SEQUENCE_TARGETING_REAGENT.name | "ZDB-MRPHLNO-051220-2"    | ResultService.TYPE           | "Morpholino"
        Category.SEQUENCE_TARGETING_REAGENT.name | "ZDB-MRPHLNO-051220-2"    | ResultService.TARGETS        | "six3a"
        Category.SEQUENCE_TARGETING_REAGENT.name | "ZDB-MRPHLNO-051220-2"    | ResultService.TARGETS        | "six3b"
        Category.SEQUENCE_TARGETING_REAGENT.name | "ZDB-MRPHLNO-051220-2"    | ResultService.SEQUENCE       | "GCTCTAAAGGAGACCTGAAAACCAT"
        Category.SEQUENCE_TARGETING_REAGENT.name | "ZDB-TALEN-131125-1"      | ResultService.TYPE           | "TALEN"
        Category.SEQUENCE_TARGETING_REAGENT.name | "ZDB-TALEN-131125-1"      | ResultService.TARGETS        | "apoea"
        Category.SEQUENCE_TARGETING_REAGENT.name | "ZDB-TALEN-131125-1"      | ResultService.SEQUENCE       | "TCATGAAGTTTGTGGCTGT"
        Category.SEQUENCE_TARGETING_REAGENT.name | "ZDB-TALEN-131125-1"      | ResultService.SEQUENCE       | "TTTCCTACCTGAAATGAC"

        Category.PHENOTYPE.name | "phenox-25276" | ResultService.GENOTYPE      | "mib"
        Category.PHENOTYPE.name | "phenox-25276" | ResultService.GENOTYPE      | "ta52b"
        Category.PHENOTYPE.name | "phenox-25276" | ResultService.STAGE         | "Gastrula"
        Category.PHENOTYPE.name | "phenox-25276" | ResultService.STAGE         | "10-13 somites"
        Category.PHENOTYPE.name | "phenox-25276" | ResultService.CONDITIONS    | "standard"
        Category.PHENOTYPE.name | "phenox-25276" | ResultService.PHENOTYPE     | "axis elongation"
        Category.PHENOTYPE.name | "phenox-25276" | ResultService.PHENOTYPE     | "embryo development"

        "Expression" | "xpatex-472"   | ResultService.GENE        | "fgf8a"
        "Expression" | "xpatex-93955" | ResultService.ANTIBODY    | "Ab1-isl"
        "Expression" | "xpatex-472"   | ResultService.PROBE       | "cb110"
        "Expression" | "xpatex-472"   | ResultService.GENOTYPE    | "AB/Tuebingen"
        "Expression" | "xpatex-472"   | ResultService.CONDITIONS  | "standard or control"
        "Expression" | "xpatex-472"   | ResultService.EXPRESSION  | "axis"
        "Expression" | "xpatex-472"   | ResultService.EXPRESSION  | "1-4 somites"

/*
        "Marker / Clone" | "" | ResultService.CLONE_PROBLEM_TYPE | ""
        "Marker / Clone" | "" | ResultService.SYNONYMS | ""
        "Marker / Clone" | "" | ResultService.LOCATION | ""
        "Marker / Clone" | "" | ResultService.CLONE_CONTAINS_GENES | ""
        "Marker / Clone" | "" | ResultService.CLONE_ENCODED_BY_GENES | ""
        "Marker / Clone" | "" | ResultService.QUALITY | ""
*/



    }


    //todo: add an additional test to make sure that records with null/empty attributes don't blow up?



}

