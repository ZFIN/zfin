package org.zfin.search.service

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.zfin.ZfinIntegrationSpec
import org.zfin.marker.Marker
import org.zfin.search.presentation.MarkerSearchCriteria
import spock.lang.Unroll


class MarkerSearchServiceSpec extends ZfinIntegrationSpec  {

    @Autowired MarkerSearchService markerSearchService

    private static Logger log = Logger.getLogger(MarkerSearchServiceSpec.class);

    @Unroll
    def "a search for #query finds some results"() {
        when: "a query is made"

        MarkerSearchCriteria criteria = new MarkerSearchCriteria()
        criteria.setName(query)
        markerSearchService.injectResults(criteria)

        then: "there are some results"
        criteria
        criteria.numFound > 0
        criteria.results

        where:
        query << ["fgf8a","ab1-dicer1","DKEY-1B17","adam15-001"]

    }

    @Unroll
    def "#query should return #type"() {
        when: "a query is made"

        MarkerSearchCriteria criteria = new MarkerSearchCriteria()
        criteria.setName(query)
        markerSearchService.injectResults(criteria)
        List<String> types = criteria.getTypesFound()*.getName()

        then: "types found should include the specified type"
        !types.isEmpty()
        types.containsAll(type.displayName)

        where:
        query       | type
        "fgf8a"     | Marker.TypeGroup.SEARCHABLE_PROTEIN_CODING_GENE
        "fgf8a"     | Marker.TypeGroup.SEARCHABLE_CDNA_EST
        "fgf8a"     | Marker.TypeGroup.SEARCHABLE_GENOMIC_CLONE
        "fgf8a"     | Marker.TypeGroup.SEARCHABLE_TRANSCRIPT
        "fgf8a"     | Marker.TypeGroup.SEARCHABLE_STR

        "fgf"       | Marker.TypeGroup.SEARCHABLE_PROTEIN_CODING_GENE
        "fgf"       | Marker.TypeGroup.SEARCHABLE_CDNA_EST
        "fgf"       | Marker.TypeGroup.SEARCHABLE_GENOMIC_CLONE
        "fgf"       | Marker.TypeGroup.SEARCHABLE_TRANSCRIPT
        "fgf"       | Marker.TypeGroup.SEARCHABLE_STR

        "fibroblast growth"       | Marker.TypeGroup.SEARCHABLE_PROTEIN_CODING_GENE
        "fibroblast growth"       | Marker.TypeGroup.SEARCHABLE_CDNA_EST
        "fibroblast growth"       | Marker.TypeGroup.SEARCHABLE_GENOMIC_CLONE
        "fibroblast growth"       | Marker.TypeGroup.SEARCHABLE_TRANSCRIPT
        "fibroblast growth"       | Marker.TypeGroup.SEARCHABLE_STR

        "acerebellar"       | Marker.TypeGroup.SEARCHABLE_PROTEIN_CODING_GENE
        "acerebellar"       | Marker.TypeGroup.SEARCHABLE_CDNA_EST
        "acerebellar"       | Marker.TypeGroup.SEARCHABLE_GENOMIC_CLONE
        "acerebellar"       | Marker.TypeGroup.SEARCHABLE_TRANSCRIPT
        "acerebellar"       | Marker.TypeGroup.SEARCHABLE_STR

        "mo1-fgf8a"   | Marker.TypeGroup.SEARCHABLE_STR

        "CH211-260P3" | Marker.TypeGroup.SEARCHABLE_PROTEIN_CODING_GENE
        "CH211-260P3" | Marker.TypeGroup.SEARCHABLE_GENOMIC_CLONE
        "CH211-260P3" | Marker.TypeGroup.SEARCHABLE_TRANSCRIPT
    }

}
