package org.zfin.mutant.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.expression.Experiment;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

/**
 * Disease model which groups by Publications
 */
@Setter
@Getter
public class FishModelDisplay implements Comparable<FishModelDisplay> {

	@JsonView(View.API.class)
	private Set<Publication> publications;
	@JsonView(View.API.class)
	private FishExperiment fishModel;
	@JsonView(View.API.class)
	private GenericTerm disease;

	@JsonView(View.API.class)
	private Fish fish;

	@JsonView(View.API.class)
	private Experiment experiment;

	@JsonView(View.API.class)
	protected int numberOfPublications = 1;

	@JsonView(View.API.class)
	protected Publication singlePublication;


	public FishModelDisplay(FishExperiment fishModel) {
		this.fishModel = fishModel;
	}

	public FishModelDisplay(Fish fish) {
		this.fish = fish;
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
