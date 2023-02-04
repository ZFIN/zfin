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
@Table(name = "UI_ALL_TERMS_CONTAINS")
public class DiseaseClosureName implements Serializable {

	@Id
	@ManyToOne
	@JoinColumn(name = "atc_term_zdb_id")
	private GenericTerm child;

	@Column(name = "atc_parent_names")
	private String parentNames;
}
