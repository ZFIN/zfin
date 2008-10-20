package org.zfin.antibody.repository;

import org.zfin.Species;
import org.zfin.infrastructure.AllMarkerNamesFastSearch;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.Isotype;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.FigureStatistics;
import org.zfin.publication.Publication;

import java.util.List;

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
    public Antibody getAntibodyByID(String zdbID);

    /**
     * Search for antibodies with given ab parameters and given labeling.
     *
     * @param antibody Antibody with search parameters.
     * @return list of antibodies
     */
    public List<Antibody> getAntibodies(AntibodySearchCriteria antibody);

    /**
     * Retrieve the total number of records.
     *
     * @param antibodySearchCriteria antibody criteria
     * @return integer
     */
    public int getNumberOfAntibodies(AntibodySearchCriteria antibodySearchCriteria);

    /**
     * Retrieves all possible species for antibodies.
     * Ordered by display order column.
     *
     * @return List of species
     */
    public List<Species> getHostSpeciesList();

    /**
     * Retrieves all species that are actually used by at least one antibody.
     * Ordered by display order column.
     *
     * @return List of species
     */
    public List<Species> getUsedHostSpeciesList();

    /**
     * Retrieves all species used as an immunogen for antibodies.
     * Ordered by display order column.
     *
     * @return List of species
     */
    public List<Species> getImmunogenSpeciesList();

    /**
     * Retrieve the number of antibodies found for a given ao term.
     *
     * @param aoTerm Anatomy Term
     * @return number of antibodies
     */
    int getAntibodiesByAOTermCount(AnatomyItem aoTerm);

    /**
     * Retrieve antibodies for a given ao term.
     * Only wild-type fish are cared for.
     *
     * @param aoTerm         Anatomy Term
     * @param paginationBean Pagination Bean
     * @return number of antibodies
     */
    List<Antibody> getAntibodiesByAOTerm(AnatomyItem aoTerm, PaginationBean paginationBean);

    /**
     * Counts distinct figures that are associated to an expression result with a given
     * ao term.
     *
     * @param antibody Antibody
     * @param aoTerm   AO Term
     * @param type:  true figures
     *               text only figures
     *               all figures (if null)
     * @return number
     */
    int getNumberOfFiguresPerAoTerm(Antibody antibody, AnatomyItem aoTerm, Figure.Type type);

    /**
     * Get all figures for a given antibody and ao term.
     *
     * @param antibody antibody
     * @param aoTerm   ao term
     * @return list of figures
     */
    public List<Figure> getFiguresPerAoTerm(Antibody antibody, AnatomyItem aoTerm);

    /**
     * Retrieve the number of distinct publications that have figures associated
     * with a given antibody and ao term.
     *
     * @param antibody Antibody
     * @param aoTerm   AO term
     * @return number of publications
     */
    int getNumberOfPublicationsWithFiguresPerAoTerm(Antibody antibody, AnatomyItem aoTerm);

    /**
     * Retrieve distinct publications for given antibody and ao term that have
     * figures associated.
     *
     * @param antibody Antibody
     * @param aoTerm   ao term
     * @return Publciation
     */
    List<Publication> getPublicationsWithFiguresPerAoTerm(Antibody antibody, AnatomyItem aoTerm);

    /**
     * Lookup all distinct antibodies that are referenced in a given publciation.
     *
     * @param publication Publication
     * @return list of antibodies
     */
    List<Antibody> getAntibodiesByPublication(Publication publication);


    /**
     * Retrieve antibody by name (same
     *
     * @param antibodyName antibody name
     * @return antibody object
     */
    Antibody getAntibodyByName(String antibodyName);


    /**
     * Retrieve all Antibodies that match in name or alias a given string
     * @param string matching string
     * @return list
     */
    List<AllMarkerNamesFastSearch> getAllNameAntibodyMatches(String string);
    
}
