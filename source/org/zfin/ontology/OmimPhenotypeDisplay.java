package org.zfin.ontology;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.orthology.Ortholog;
import org.zfin.sequence.DBLink;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * OMIM Phenotype
 */
@Setter
@Getter
@Entity
@Table(name = "UI.OMIM_PHENOTYPE_DISPLAY")
public class OmimPhenotypeDisplay implements Serializable {

	@Id
	@JsonView(View.API.class)
	@SequenceGenerator(name="omim_display_display_seq",sequenceName="omim_display_display_seq", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "omim_display_display_seq")
	@Column(name = "opd_id", nullable = false)
	private long id;

	@JsonView(View.API.class)
	@Transient
	private String omim;
	@JsonView(View.API.class)
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
	private HumanGeneDetail homoSapiensGene;

	@Transient
	public DBLink getHumanAccession() {
		return humanAccession;
	}

	@JsonView(View.API.class)
	@Column(name = "opd_omim_accession")
	public String omimAccession;

	@JsonView(View.API.class)
	@Transient
	private Ortholog orthology;
	@Transient
	private DBLink humanAccession;

	@JsonView(View.API.class)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "UI.OMIM_ZFIN_ASSOCIATION",
			// TODO (ZFIN-9354): hibernate migration change, confirm logic still valid
			// Fixes this error: org.hibernate.AnnotationException: Join column '...' on collection property 'org.zfin...' must be defined with the same insertable and updatable attributes
		joinColumns = {@JoinColumn(name = "oza_human_phenotype_id", nullable = false, updatable = false, insertable = false)},
		inverseJoinColumns = {@JoinColumn(name = "oza_zfin_gene_zdb_id", nullable = false, updatable = false, insertable = false)})
	private List<Marker> zfinGene;
	@JsonView(View.API.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "opd_term_zdb_id")
	private GenericTerm disease;

	@Column(name = "opd_zfin_gene_symbols_search")
	private String zfinGeneSymbolSearch;

}
