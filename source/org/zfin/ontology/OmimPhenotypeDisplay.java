package org.zfin.ontology;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.orthology.Ortholog;
import org.zfin.sequence.DBLink;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * OMIM Phenotype
 */
@Setter
@Getter
@Entity
@Table(name = "UI_OMIM_PHENOTYPE_DISPLAY")
public class OmimPhenotypeDisplay implements Serializable {

	@Id
	@JsonView(View.API.class)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "opd_id", nullable = false)
	private long id;

	@JsonView(View.API.class)
	@Transient
	private String omim;
	@Column(name = "opd_omim_term_name")
	private String name;
	@JsonView(View.API.class)
	@Transient
	private ArrayList<String> humanGene;
	@JsonView(View.API.class)
	@Transient
	private String symbol;

	@JsonView(View.API.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "opd_human_gene_id")
	private HumanGeneDetail humanGeneDetail;

	@Transient
	public DBLink getHumanAccession() {
		return humanAccession;
	}

	@JsonView(View.API.class)
	@Transient
	public String omimAccession;

	@JsonView(View.API.class)
	@Transient
	private Ortholog orthology;
	@Transient
	private DBLink humanAccession;

	@JsonView(View.API.class)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "UI_OMIM_ZFIN_ASSOCIATION", joinColumns = {
		@JoinColumn(name = "oza_human_phenotype_id", nullable = false, updatable = false)},
		inverseJoinColumns = {@JoinColumn(name = "oza_zfin_gene_zdb_id",
			nullable = false, updatable = false)})
	private List<Marker> zfinGene;
	@JsonView(View.API.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "opd_term_zdb_id")
	private GenericTerm term;

	@Column(name = "opd_zfin_gene_symbols_search")
	private String zfinGeneSymbolSearch;

}
