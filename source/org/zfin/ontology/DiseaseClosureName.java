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
import java.util.Objects;

/**
 * OMIM Phenotype
 */
@Setter
@Getter
@Entity
@Table(schema = "ui", name = "all_terms_contains")
public class DiseaseClosureName implements Serializable {

	@Id
	@ManyToOne
	@JoinColumn(name = "atc_term_zdb_id")
	private GenericTerm child;

	@Column(name = "atc_parent_names")
	private String parentNames;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DiseaseClosureName that)) return false;
		return Objects.equals(getChild(), that.getChild()) && Objects.equals(getParentNames(), that.getParentNames());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getChild(), getParentNames());
	}
}
