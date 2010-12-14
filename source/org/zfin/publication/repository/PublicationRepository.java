package org.zfin.publication.repository;

import org.zfin.anatomy.CanonicalMarker;
import org.zfin.antibody.Antibody;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.feature.Feature;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.Morpholino;
import org.zfin.ontology.Term;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.repository.PaginationParameter;
import org.zfin.sequence.MarkerDBLink;

import java.util.List;

/**
 * Persistence class that deals with Publication objects.
 */
public interface PublicationRepository extends PaginationParameter {

    /**
     * Retrieve the number of publications that contain the
     * a term 'abstractText' in its abstract. This count is
     * done case insensitive.
     *
     * @param abstractText  text
     * @return number of publications
     */
    int getNumberOfPublications(String abstractText);

    /**
     * Retrieve all distinct publications that contain a high quality probe
     * with a rating of 4.
     *
     * @param anatomyTerm Anatomy Term
     * @return list of publications
     */
    List<Publication> getHighQualityProbePublications(Term anatomyTerm);

    /**
     * Retrieve all publication for a given geneID and anatomical structure.
     *
     * @param geneID gene ID
     * @param anatomyItemID term ID
     */
    List<Publication> getExpressedGenePublications(String geneID, String anatomyItemID);

    List<String> getSNPPublicationIDs(Marker marker);

    /**
     * retrieve the total number of publications for a given geneID and anatomical structure.
     *
     * @param geneID        gene zdbID
     * @param anatomyItemID anatomy ID
     * @return number
     */
    int getNumberOfExpressedGenePublications(String geneID, String anatomyItemID);

    /**
     * Retrieve the total number of publications for a given geneID and anatomical structure
     * that contain figures.
     *
     * @param geneID        gene zdbID
     * @param anatomyItemID anatomy ID
     * @return number
     */
    int getNumberOfExpressedGenePublicationsWithFigures(String geneID, String anatomyItemID);

    /**
     * Retrieve all publication that are annoted to genes expressed in a given
     * anatomical structure.
     *
     * @param anatomyItemID
     */
    List<Publication> getExpressedGenePublications(String anatomyItemID);

    /**
     * Retrieve the genes and CDNA/EST for the high-quality probes with
     * rating of 4.
     *
     * @param term anatomy term
     * @return list of High quality probes.
     */
    PaginationResult<HighQualityProbe> getHighQualityProbeNames(Term term);

    /**
     * Retrieve the genes and CDNA/EST for the high-quality probes with
     * rating of 4 (which equals 5 stars) associate to an anatomical structure.
     * Only return n records.
     *
     * @param term   anatomy term
     * @param maxRow max number of records
     * @return list of HighqQualityProbes
     */
    PaginationResult<HighQualityProbe> getHighQualityProbeNames(Term term, int maxRow);

    /**
     * Retrieve marker records that have a gene expression in the
     * anatomy term, identified by the zdbID. The maxRow number
     * limits the result set.
     *
     * @param zdbID  of the anatomy term.
     * @param maxRow  max number of records
     * @return list of markers
     */
    List<Marker> getAllExpressedMarkers(String zdbID, int maxRow);

    /**
     * Returns the appropriate # of records, as well as statistics on the total # of records.
     *
     * @param anatomyTerm term
     * @param firstRow  first   record
     * @param maxRow          last record
     * @return   marker statistics
     */
    PaginationResult<MarkerStatistic> getAllExpressedMarkers(Term anatomyTerm, int firstRow, int maxRow);

    /**
     * Returns the appropriate # of records, as well as statistics on the total # of records.
     *
     * @param anatomyTerm term
     * @return  marker statistics
     */
    PaginationResult<MarkerStatistic> getAllExpressedMarkers(Term anatomyTerm);


    /**
     * Count the number of figures from all publications that have a gene
     * expression in a given anatomy structure.
     *
     * @param anatomyTerm term
     * @return number
     */
    int getTotalNumberOfFiguresPerAnatomyItem(Term anatomyTerm);

    /**
     * Count the number of images from all publications that have a gene
     * expression in a given anatomy structure.
     *
     * @param anatomyTerm Anatomy Term
     * @return number
     */
    int getTotalNumberOfImagesPerAnatomyItem(Term anatomyTerm);

    /**
     * Retrieve a publication by its primary key.
     *
     * @param zdbID
     */
    Publication getPublication(String zdbID);

    /**
     * Retrieve a marker (gene) by its symbol name. If it is not unique a Hibernate runtime exception is thrown.
     *
     * @param symbol
     */
    Marker getMarker(String symbol);

    /**
     * Retrieve a marker by its zdbID
     *
     * @param zdbID
     */
    Marker getMarkerByZdbID(String zdbID);

    /**
     * Save a canonical marker.
     *
     * @param canon
     */
    void insertCanonicalMarker(CanonicalMarker canon);

    /**
     * Check if a publication with the specified primary key exists.
     *
     * @param canonicalPublicationZdbID
     */
    boolean publicationExists(String canonicalPublicationZdbID);


    /**
     * Figure getFigureById(String zdbID);
     * <p/>
     * Retrieve the figures that can be found for a given publication and gene.
     *
     * @param geneID
     * @param publicationID
     */
    List<Figure> getFiguresByGeneID(String geneID, String publicationID);

    Figure getFigureByID(String figureZdbID);

    List<Figure> getFiguresByGeneAndPublication(String geneID, String publicationID);

    /**
     * Return all figures for a specified gene, probe and anatommical structure.
     * Clone information is not required.
     *
     * @param gene   Gene
     * @param clone  Probe
     * @param aoTerm anatomical structure
     * @return list of figures
     */
    List<Figure> getFiguresPerProbeAndAnatomy(Marker gene, Marker clone, Term aoTerm);

    /**
     * Return all Publications for a specified gene, probe and anatommical structure with figures associated.
     *
     * @param gene    Gene
     * @param subGene Probe
     * @param aoTerm  anatomical structure
     * @return list of figures
     */
    List<Publication> getPublicationsWithFiguresPerProbeAndAnatomy(Marker gene, Marker subGene, Term aoTerm);

    /**
     * Retrieve the figures that can be found for a given publication and probe.
     *
     * @param probeID
     * @param publicationID
     */
    List<Figure> getFiguresByProbeAndPublication(String probeID, String publicationID);


    /**
     * Used to add a sorting string onto the query.
     *
     * @param orderVariable
     */
    void addOrdering(String orderVariable);

    void removeOrderByFields();


    /**
     * Retrieves publications with Accession Number's (pubmed Ids) but with null or 'none' DOIs.
     *
     * @param maxResults number
     * @return list
     */
    List<Publication> getPublicationsWithAccessionButNoDOI(int maxResults);


    /**
     * Saves a list of publications in one transaction.
     *
     * @param publicationList List of publication
     * @return boolean
     */
    boolean updatePublications(List<Publication> publicationList);

    /**
     * Retrieve list of figures for a given morpholino and anatomy term
     *
     * @param morpholino morpholino
     * @param term       anatomy term
     * @return list of figures.
     */
    List<Figure> getFiguresByMorpholinoAndAnatomy(Morpholino morpholino, Term term);

    /**
     * Retrieve list of figures for a given genotype and anatomy term
     *
     * @param geno genotype
     * @param term anatomy term
     * @return list of figures.
     */
    PaginationResult<Figure> getFiguresByGenoAndAnatomy(Genotype geno, Term term);

    PaginationResult<Figure> getFiguresByGeno(Genotype geno);

    PaginationResult<Figure> getFiguresByGenoExp(Genotype geno);

    PaginationResult<Figure> getFiguresByGenoMorph(Genotype geno);

    PaginationResult<Publication> getPublicationsWithFiguresbyGeno(Genotype genotype);

    PaginationResult<Publication> getPublicationsWithFiguresbyGenoExp(Genotype genotype);


    /**
     * @param genotype Genotype
     * @param aoTerm   ao term
     * @return Number of publications with figures per genotype and anatomy
     */
    PaginationResult<Publication> getPublicationsWithFigures(Genotype genotype, Term aoTerm);

    /**
     * @param genotype Genotype
     * @param aoTerm   ao term
     * @return Number of publications with figures per genotype and anatomy
     */
    int getNumPublicationsWithFiguresPerGenotypeAndAnatomy(Genotype genotype, Term aoTerm);

    /**
     * Retrieve the publications for the figures for a given morpholino and anatomy term
     *
     * @param morpholino Morpholino
     * @param aoTerm     anatomy Term
     * @return List of publications
     */
    List<Publication> getPublicationsWithFiguresPerMorpholinoAndAnatomy(Morpholino morpholino, Term aoTerm);

    /**
     * Retrieve figures for a given gene and anatomy term.
     *
     * @param gene        Gene
     * @param anatomyTerm anatomy
     * @return a set of figures
     */
    List<Figure> getFiguresByGeneAndAnatomy(Marker gene, Term anatomyTerm);


    Journal getJournalByTitle(String journalTitle);

    PaginationResult<Publication> getAllAssociatedPublicationsForMarker(Marker marker, int maxPubs);

    PaginationResult<Publication> getAllAssociatedPublicationsForFeature(Feature feature, int maxPubs);

    /**
     * Retrieve Figue by ID
     *
     * @param zdbID ID
     * @return Figure
     */
    Figure getFigure(String zdbID);

    Image getImageById(String zdbID);

    /**
     * Retrieve all Publications with Figures for a given marker and ao term.
     * Standard condition and wildtype fish
     *
     * @param marker      marker
     * @param anatomyTerm ao term
     * @return pagination result
     */
    PaginationResult<Publication> getPublicationsWithFigures(Marker marker, Term anatomyTerm);

    /**
     * Retrieve all experiments for a given publication.
     *
     * @param publicationID pub id
     * @return list of experiments
     */
    List<ExpressionExperiment> getExperiments(String publicationID);

    /**
     * Retrieve a list of distinct figures, labels not IDs
     *
     * @param publicationID pub id
     * @return list of labels
     */
    List<String> getDistinctFigureLabels(String publicationID);

    /**
     * Retrieve distinct list of genes that are attributed to a given
     * publication.
     *
     * @param pubID publication id
     * @return list of markers
     */
    List<Marker> getGenesByPublication(String pubID);

    /**
     * Retrieve distinct list of genes that are attributed to a given
     * publication and used in an experiment.
     *
     * @param pubID publication id
     * @return list of markers
     */
    List<Marker> getGenesByExperiment(String pubID);


    /**
     * Retrieves experiment that pertain to a given
     * publication
     * gene
     * fish
     *
     * @param publicationID publication
     * @param geneZdbID     gene ID
     * @param fishID        genotype ID
     * @return list of expression experiment
     */
    List<ExpressionExperiment> getExperimentsByGeneAndFish(String publicationID, String geneZdbID, String fishID);

    /**
     * Retrieve list of Genotypes being used in experiments for a given publication
     *
     * @param publicationID publication ID
     * @return list of genotype
     */
    List<Genotype> getFishUsedInExperiment(String publicationID);

    /**
     * Retrieve list of Genotypes being used in a publication
     *
     * @param publicationID publication ID
     * @return list of genotype
     */
    List<Genotype> getGenotypesInPublication(String publicationID);

    /**
     * Retrieve all experiments that pertain to a given publication.
     * It adds the Standard and Generic-control
     *
     * @param publicationID publication
     * @return listof experiments
     */
    List<Experiment> getExperimentsByPublication(String publicationID);

    /**
     * Retrieve a genotype for a given handle.
     *
     * @param nickname string
     * @return genotype
     */
    Genotype getGenotypeByHandle(String nickname);

    /**
     * Retrieve all Genotypes attributed to a given publication.
     *
     * @param publicationID pub id
     * @return list of genoypes
     */
    List<Genotype> getNonWTGenotypesByPublication(String publicationID);

    /**
     * Retrieve antibodies attributes to a given publication
     *
     * @param publicationID String
     * @return list of antibodies
     */
    List<Antibody> getAntibodiesByPublication(String publicationID);


    /**
     * Retrieve antibodies by publication and associated gene
     *
     * @param publicationID String
     * @param geneID        String
     * @return list of antibodies
     */
    List<Antibody> getAntibodiesByPublicationAndGene(String publicationID, String geneID);

    /**
     * Retrieve list of associated genes for given pub and antibody
     *
     * @param publicationID String
     * @param antibodyID    string
     * @return list of markers
     */
    List<Marker> getGenesByAntibody(String publicationID, String antibodyID);

    /**
     * Retrieve access numbers for given pub and gene.
     *
     * @param publicationID string
     * @param geneID        string
     * @return list of db links
     */
    List<MarkerDBLink> getDBLinksByGene(String publicationID, String geneID);

    /**
     * Retrieve db link object of a clone for a gene and pub.
     *
     * @param pubID  pub is
     * @param geneID gene ID
     * @return list of MarkerDBLinks
     */
    List<MarkerDBLink> getDBLinksForCloneByGene(String pubID, String geneID);

    /**
     * Retrieve all figures that are associated to a given publication.
     *
     * @param pubID publication ID
     * @return list of figures
     */
    List<Figure> getFiguresByPublication(String pubID);

    List<Publication> getPublicationsWithAccessionButNoDOIAndLessAttempts(int maxAttempts, int maxProcesses);

    List<Publication> addDOIAttempts(List<Publication> publicationList);

    PaginationResult<Publication> getAllAssociatedPublicationsForGenotype(Genotype genotype, int maxPubs);

}
