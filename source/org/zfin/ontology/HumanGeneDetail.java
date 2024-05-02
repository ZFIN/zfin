package org.zfin.ontology;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

import jakarta.persistence.*;

@Entity
@Table(name = "human_gene_detail")
public class HumanGeneDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.API.class)
    @Column(name = "hgd_gene_id")
    private String id;
    @JsonView(View.API.class)
    @Column(name = "hgd_gene_name")
    private String name;
    @JsonView(View.API.class)
    @Column(name = "hgd_gene_symbol")
    private String symbol;

    public HumanGeneDetail() {}
    public HumanGeneDetail(String geneMimNumber) {
        this.id = geneMimNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
