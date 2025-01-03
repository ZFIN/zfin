package org.zfin.antibody.repository;

import org.zfin.Species;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureType;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Map;

/**
 * Main repository.
 */
public interface AntibodyRepository {

	/**
	 * Retrieve an antibody by ID.
	 *
	 * @param zdbID primary key
	 * @return antibody
	 */
	Antibody getAntibodyByID(String zdbID);

	/**
	 * Search for antibodies with given ab parameters and given labeling.
	 *
	 * @param antibody Antibody with search parameters.
	 * @return list of antibodies
	 */
	PaginationResult<Antibody> getAntibodies(AntibodySearchCriteria antibody);

	/**
	 * Retrieve the total number of records.
	 *
	 * @param antibodySearchCriteria antibody criteria
	 * @return integer
	 */
	int getNumberOfAntibodies(AntibodySearchCriteria antibodySearchCriteria);

	/**
	 * Retrieves all possible species for antibodies.
	 * Ordered by display order column.
	 *
	 * @return List of species
	 */
	List<Species> getHostSpeciesList();

	/**
	 * Retrieves all species that are actually used by at least one antibody.
	 * Ordered by display order column.
	 *
	 * @return List of species
	 */
	List<Species> getUsedHostSpeciesList();

	/**
	 * Retrieves all species used as an immunogen for antibodies.
	 * Ordered by display order column.
	 *
	 * @return List of species
	 */
	List<Species> getImmunogenSpeciesList();

	/**
	 * Retrieve distinct publications for given antibody and ao term that have
	 * figures associated.
	 *
	 * @param antibody Antibody
	 * @param aoTerm   ao term
	 * @return Pagination Result object
	 */
	PaginationResult<Publication> getPublicationsWithFigures(Antibody antibody, GenericTerm aoTerm);

	PaginationResult<Publication> getPublicationsProbeWithFigures(Marker probe, GenericTerm aoTerm);

	/**
	 * Lookup all distinct antibodies that are referenced in a given publication.
	 *
	 * @param publication Publication
	 * @return list of antibodies
	 */
	List<Antibody> getAntibodiesByPublication(Publication publication);


	/**
	 * Retrieve antibody by abbrev
	 *
	 * @param antibodyAbbrev Antibody abbreviation.
	 * @return antibody Returned antibody.  Null if not found.
	 */
	Antibody getAntibodyByAbbrev(String antibodyAbbrev);

	/**
	 * Retrieve antibody by name (same
	 *
	 * @param antibodyName antibody name
	 * @return antibody object
	 */
	Antibody getAntibodyByName(String antibodyName);

	List<AntibodyStatistics> getAntibodyStatisticsPaginated(GenericTerm aoTerm, PaginationBean pagination, List<String> termIDs, boolean includeSubstructures);

	List<HighQualityProbe> getProbeStatisticsPaginated(GenericTerm aoTerm, PaginationBean pagination, List<String> termIDs, boolean includeSubstructures);

	int getAntibodyCount(Term anatomyItem, boolean includeSubstructure, Pagination pagination);

	int getProbeCount(Term aoTerm, boolean includeSubstructures, Pagination pagination);

	List<String> getPaginatedAntibodyIds(Term aoTerm, boolean includeSubstructures, Pagination pagination);

	List<String> getPaginatedHighQualityProbeIds(Term aoTerm, boolean includeSubstructures, Pagination pagination);

	/**
	 * Retrieve all antibodies sorted by name.
	 *
	 * @return All antibodies
	 */
	List<Antibody> getAllAntibodies();

	List<Antibody> getAntibodiesByName(String query);


    Map<String, List<Marker>> getAntibodyAntigenGeneMap(List<String> antibodyIDs);

	Map<Publication, List<Antibody>> getAntibodiesFromAllPublications();
}
