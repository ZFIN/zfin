package org.zfin.datatransfer.ncbi;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(schema = "external_resource", name = "ncbi_danio_rerio_gene_info")
public class NcbiGeneInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "gene_id", nullable = false)
    private String geneId;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "locus_tag")
    private String locusTag;

    @Column(name = "synonyms")
    private String synonyms;

    @Column(name = "db_xrefs")
    private String dbXrefs;

    @Column(name = "chromosome")
    private String chromosome;

    @Column(name = "map_location")
    private String mapLocation;

    @Column(name = "description")
    private String description;

    @Column(name = "type_of_gene")
    private String typeOfGene;

    @Column(name = "symbol_from_nomenclature_authority")
    private String symbolFromNomenclatureAuthority;

    @Column(name = "full_name_from_nomenclature_authority")
    private String fullNameFromNomenclatureAuthority;

    @Column(name = "nomenclature_status")
    private String nomenclatureStatus;

    @Column(name = "other_designations")
    private String otherDesignations;

    @Column(name = "modification_date")
    private String modificationDate;

    @Column(name = "feature_type")
    private String featureType;
}
