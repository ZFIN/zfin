package org.zfin.marker;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "gene_description")
@Setter
@Getter
public class AllianceGeneDesc {

    @Id
    @Column(name = "gd_pk_id")
    private Long id;
    @Column(name = "gd_gene_zdb_id")
    private String gene;
    @Column(name = "gd_go_function_description")
    private String gdFuncDesc;
    @Column(name = "gd_go_process_description")
    private String gdProcDesc;
    @Column(name = "gd_go_component_description")
    private String gdGoCompDesc;
    @Column(name = "gd_do_description")
    private String gdDoDesc;
    @Column(name = "gd_do_experimental_description")
    private String gdDoExpDesc;
    @Column(name = "gd_do_biomarker_description")
    private String gdDoBioMkrDesc;
    @Column(name = "gd_do_orthology_description")
    private String gdDoOrthoDesc;
    @Column(name = "gd_orthology_description")
    private String gdOrthoDesc;
    @Column(name = "gd_description")
    private String gdDesc;

}
