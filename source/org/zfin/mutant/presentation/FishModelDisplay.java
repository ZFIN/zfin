package org.zfin.mutant.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.expression.Experiment;
import org.zfin.framework.api.View;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Disease model which groups by Publications
 */
@Setter
@Getter
@Entity
@Table(schema = "ui", name = "zebrafish_models_display")
public class FishModelDisplay implements Comparable<FishModelDisplay> {

	@Id
	@JsonView(View.API.class)
	@SequenceGenerator(name="seq-gen",sequenceName="zebrafish_models_display_seq", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq-gen")
	@Column(name = "zmd_id", nullable = false)
	private long id;

	@JsonView({View.ExpressedGeneAPI.class, View.API.class})
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "zmd_fish_zdb_id")
	private Fish fish;

	@Transient
	@JsonView(View.API.class)
	private Set<Publication> publications;
	@JsonView(View.API.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "zmd_fishox_zdb_id")
	private FishExperiment fishModel;
	@JsonView(View.API.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "zmd_term_zdb_id")
	private GenericTerm disease;

	@JsonView(View.API.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "zmd_experiment_zdb_id")
	private Experiment experiment;

	@JsonView(View.API.class)
	@Column(name = "zmd_pub_count")
	protected int numberOfPublications = 1;

	@JsonView(View.ExpressedGeneAPI.class)
	@Column(name = "zmd_evidence_search")
	private String evidenceSearch;

	@Transient
	private int numberOfFigs;

	@Column(name = "zmd_fish_search")
	private String fishSearch;

	@Column(name = "zmd_condition_search")
	private String conditionSearch;

	@Column(name = "zmd_order")
	private Integer order;

	@JsonView(View.API.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "zmd_pub_zdb_id")
	protected Publication singlePublication;

	@JsonView(View.API.class)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(schema = "ui", name = "zebrafish_models_evidence_association",
		joinColumns = {@JoinColumn(name = "omea_zebfrafish_model_id", nullable = false, updatable = false, insertable = false)},
		inverseJoinColumns = {@JoinColumn(name = "omea_term_zdb_id", nullable = false, updatable = false, insertable = false)})
	private Set<GenericTerm> evidenceCodes;

	public FishModelDisplay(FishExperiment fishModel) {
		this.fishModel = fishModel;
	}

	public FishModelDisplay(Fish fish) {
		this.fish = fish;
	}

	public FishModelDisplay() {
	}

	public Publication getPublication() {
		if (CollectionUtils.isEmpty(publications))
			return null;
		return publications.iterator().next();
	}

	public void addPublication(Publication publication) {
		if (publications == null)
			publications = new HashSet<>();
		publications.add(publication);
	}

	@Override
	public int compareTo(FishModelDisplay o) {
		if (o == null || o.getFishModel() == null) {
			return 1;
		}
		return fishModel.compareTo(o.getFishModel());
	}
}
