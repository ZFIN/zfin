package org.zfin.ontology;

import javax.persistence.*;

@Entity
@Table(name = "human_gene_detail")
public class HumanGeneDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hgd_gene_id")
    private String geneMimNumber;
    @Column(name = "hgd_gene_name")
    private String geneName;
    @Column(name = "hgd_gene_symbol")
    private String geneSymbol;

    public HumanGeneDetail() {}
    public HumanGeneDetail(String geneMimNumber) {
        this.geneMimNumber = geneMimNumber;
    }

    public String getGeneMimNumber() {
        return geneMimNumber;
    }

    public void setGeneMimNumber(String geneMimNumber) {
        this.geneMimNumber = geneMimNumber;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }
}
