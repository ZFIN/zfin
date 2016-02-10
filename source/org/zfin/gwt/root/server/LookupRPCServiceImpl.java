package org.zfin.gwt.root.server;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.antibody.Antibody;
import org.zfin.feature.Feature;
import org.zfin.gwt.curation.ui.AttributionModule;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.gwt.root.ui.ItemSuggestOracle;
import org.zfin.gwt.root.ui.ItemSuggestion;
import org.zfin.gwt.root.ui.LookupComposite;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.*;
import org.zfin.profile.Organization;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getFeatureRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;


/**
 *
 */
public class LookupRPCServiceImpl extends ZfinRemoteServiceServlet implements LookupRPCService {

    private static final Logger logger = Logger.getLogger(LookupRPCServiceImpl.class);
    private static final String QUERY_PREFIX = ":";
    private transient PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private transient Highlighter highlighter = new Highlighter();

    private static final int NUMBER_OF_SUGGESTIONS = ItemSuggestOracle.DEFAULT_LIMIT;

    public PublicationDTO getPublicationAbstract(String zdbID) {

        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return null;
        }
//
        PublicationDTO publicationAbstractDTO = new PublicationDTO();
        publicationAbstractDTO.setZdbID(publication.getZdbID());
        publicationAbstractDTO.setTitle(publication.getTitle());
        publicationAbstractDTO.setAuthors(publication.getAuthors());
        publicationAbstractDTO.setDoi(publication.getDoi());
        publicationAbstractDTO.setAbstractText(publication.getAbstractText());
        publicationAbstractDTO.setAccession(publication.getAccessionNumber());
        publicationAbstractDTO.setCitation(publication.getCitation());
        publicationAbstractDTO.setMiniRef(publication.getShortAuthorList());


        return publicationAbstractDTO;
    }


    public SuggestOracle.Response getSupplierSuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        if (query.equals("xxx333")) {
            throw new RuntimeException("this is a test error");
        }

        ProfileRepository profileRep = RepositoryFactory.getProfileRepository();
        List<Organization> organizations = profileRep.getOrganizationsByName(query);

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<>();
        for (Organization organization : organizations) {
            String suggestion = organization.getName();
            String displayName = suggestion.replace(query, "<strong>" + query + "</strong>");
            ItemSuggestion itemSuggestion = new ItemSuggestion(displayName, suggestion);
            suggestions.add(itemSuggestion);
        }

        resp.setSuggestions(suggestions);
        logger.info("found " + suggestions.size() + " suggestions for " + req);
        return resp;
    }

    /**
     * Checks if a given term is a valid anatomy term or is part of a matching term.
     * 1) Exact match
     * 2) No matching term found
     * 3) match on more than one term.
     *
     * @param term term name
     * @return TermStatus
     */
    public TermStatus validateTerm(String term, OntologyDTO ontologyDto) {

        if (ActiveData.isValidActiveData(term, ActiveData.Type.TERM)) {
            TermDTO termObject = OntologyManager.getInstance().getTermByID(term, ontologyDto);
            if (termObject != null)
                return new TermStatus(TermStatus.Status.FOUND_EXACT, termObject.getName(), termObject.getZdbID());
        } else {
            Ontology ontology = DTOConversionService.convertToOntology(ontologyDto);
            TermDTO termObject = OntologyManager.getInstance().getTermByName(term.trim(), ontology);
            if (termObject != null)
                return new TermStatus(TermStatus.Status.FOUND_EXACT, term, termObject.getZdbID());
        }
        return new TermStatus(TermStatus.Status.FOUND_NONE, term);
    }

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table)
     *
     * @param request  request
     * @param ontology ontology name
     * @return suggestions
     */
    public SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, OntologyDTO ontology, boolean useIDAsValue) {
        return getOntologySuggestions(request, DTOConversionService.convertToOntology(ontology), useIDAsValue);
    }

    @Override
    public SuggestOracle.Response getTermCompletionWithData(SuggestOracle.Request request, OntologyDTO ontology, boolean useIdAsValue) {
        return getOntologySuggestions(request, DTOConversionService.convertToOntology(ontology), useIdAsValue, true);
    }

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table)
     *
     * @param request  request
     * @param ontology ontology name
     * @return suggestions
     */
    private SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, Ontology ontology, boolean useIDAsValue) {
        return getOntologySuggestions(request, ontology, useIDAsValue, false);
    }

    private SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, Ontology ontology, boolean useIDAsValue, boolean termsWithDataOnly) {
//        HibernateUtil.currentSession();
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = request.getQuery().trim();

        // check if an ontology is provided as a prefix in the query string
        // if so, strip off that prefix and switch over to new ontology
        if (query.contains(QUERY_PREFIX)) {
            int indexOfColon = query.indexOf(QUERY_PREFIX);
            String ontologyString = query.substring(0, indexOfColon);
            if (Ontology.getOntology(ontologyString) != null) {
                ontology = Ontology.getOntology(ontologyString);
                query = query.substring(indexOfColon + 1);
            }
        }

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 2) {
            // We add one in order to add an additional term that is not displayed.
            // When it comes back we can add the '...' implying that there are more.
            // Unfortunately, Response does not have an easy fix for this other than exceeding the Response. 
            MatchingTermService matcher = new MatchingTermService(request.getLimit() + 1);
            highlighter.setMatch(query);
            String previousSuggestionString = "";
            int index = 0;
            ItemSuggestion previousSuggestion = null;
            for (MatchingTerm term : matcher.getMatchingTerms(query, ontology)) {
                if (termsWithDataOnly && !OntologyDataManager.getInstance().hasExpressionOrPhenotypeData(term.getTerm()))
                    continue;
                String suggestion = term.getMatchingTermDisplay();
                // add comment field with pure term name for display
                StringBuilder builder = highlighter.hidePureTermNameHtml(term.getTerm().getTermName());
                String displayName = highlighter.highlight(suggestion);
                builder.append(displayName);
                String termValue = term.getTerm().getZdbID();
                StringBuilder fillBuilder = new StringBuilder(builder);
                fillBuilder.append(" [");
                fillBuilder.append(term.getTerm().getOntology().getDisplayName());
                fillBuilder.append("]");
                ItemSuggestion fullItemSuggestion = new ItemSuggestion(fillBuilder.toString(), termValue);
                if (previousSuggestionString.equals(suggestion)) {
                    suggestions.remove(index - 1);
                    suggestions.add(previousSuggestion);
                    suggestions.add(fullItemSuggestion);
                } else {
                    suggestions.add(new ItemSuggestion(builder.toString(), termValue));
                }
                previousSuggestionString = suggestion;
                previousSuggestion = fullItemSuggestion;
                index++;
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " + suggestions.size() + " suggestions for " + request);
        return resp;
    }


    @Override
    public SuggestOracle.Response getAntibodySuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();
        highlighter.setMatch(query);

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<>();
        if (query.length() > 0) {
            for (Antibody antibody : RepositoryFactory.getAntibodyRepository().getAntibodiesByName(query)) {
                String antibodyAbbreviation = antibody.getAbbreviation();
                StringBuilder builder = highlighter.hidePureTermNameHtml(antibodyAbbreviation);
                builder.append(highlighter.highlight(antibodyAbbreviation));
                suggestions.add(new ItemSuggestion(builder.toString(), antibody.getAbbreviation()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " + suggestions.size() + " suggestions for " + req);
        return resp;
    }

    /**
     * Retrieve a list of marker entities whose marker abbreviation matches a given query string.
     *
     * @param req request that holds the query string.
     * @return response
     */
    public SuggestOracle.Response getMarkerSuggestions(SuggestOracle.Request req, Map<String, String> options) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();
        highlighter.setMatch(query);

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            for (Marker marker : RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(query)) {
                String markerAbbreviation = marker.getAbbreviation();
                StringBuilder builder = highlighter.hidePureTermNameHtml(markerAbbreviation);
                builder.append(highlighter.highlight(markerAbbreviation));
                if (options != null && Boolean.valueOf(options.get(LookupComposite.SHOW_TYPE))) {
                    builder.append(" [" + marker.getType() + "]");
                }
                suggestions.add(new ItemSuggestion(builder.toString(), markerAbbreviation));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " + suggestions.size() + " suggestions for " + req);
        return resp;
    }

    /**
     * Retrieve a list of genes or EFGs whose abbreviations match a given query string.
     *
     * @param req request that holds the query string.
     * @return response
     */
    public SuggestOracle.Response getGenedomAndEFGSuggestions(SuggestOracle.Request req) {
        return getMarkerSuggestionsForType(req, Marker.TypeGroup.GENEDOM_AND_EFG);
    }

    /**
     * Retrieve a list of genes or EFGs whose abbreviations match a given query string.
     *
     * @param req request that holds the query string.
     * @return response
     */
    public SuggestOracle.Response getGenedomSuggestions(SuggestOracle.Request req) {
        return getMarkerSuggestionsForType(req, Marker.TypeGroup.GENEDOM);
    }

    public SuggestOracle.Response getMarkerSuggestionsForType(SuggestOracle.Request req, Marker.TypeGroup typeGroup) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();
        highlighter.setMatch(query);

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
            List<Marker> markers = markerRepository.getMarkersByAbbreviationAndGroup(query, typeGroup);
            for (Marker marker : markers) {
                String markerAbbreviation = marker.getAbbreviation();
                StringBuilder builder = highlighter.hidePureTermNameHtml(markerAbbreviation);
                builder.append(highlighter.highlight(markerAbbreviation));
                suggestions.add(new ItemSuggestion(builder.toString(), markerAbbreviation
                ));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " + suggestions.size() + " suggestions for " + req);
        return resp;
    }


    public SuggestOracle.Response getConstructSuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();
        highlighter.setMatch(query);

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
            List<Marker> markers = markerRepository.getConstructsByAttribution(query);
            for (Marker marker : markers) {
                StringBuilder builder = highlighter.hidePureTermNameHtml(marker.getAbbreviation());
                builder.append(highlighter.highlight(marker.getAbbreviation()));
                suggestions.add(new ItemSuggestion(builder.toString(), marker.getZdbID()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " + suggestions.size() + " suggestions for " + req);
        return resp;
    }

    /**
     * Retrieve a list of features whose feature abbreviations match a given query string.
     *
     * @param req request that holds the query string.
     * @return response
     */
    public SuggestOracle.Response getFeatureSuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();
        highlighter.setMatch(query);

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            for (Feature feature : getFeatureRepository().getFeaturesByAbbreviation(query)) {
                String featureAbbreviation = feature.getAbbreviation();
                StringBuilder builder = highlighter.hidePureTermNameHtml(featureAbbreviation);
                builder.append(highlighter.highlight(featureAbbreviation));
                suggestions.add(new ItemSuggestion(builder.toString(), feature.getAbbreviation()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " + suggestions.size() + " suggestions for " + req);
        return resp;
    }

    /**
     * Retrieve the term info for a given term id and ontology.
     * This can either be the zdb ID or the obo id.
     *
     * @param ontology ontology
     * @param termID   term id
     * @return term info if no term found return null
     */
    public TermDTO getTermInfo(OntologyDTO ontology, String termID) {
        if (ontology == null) {
            logger.warn("No ontology provided");
            return null;
        }
        if (StringUtils.isEmpty(termID)) {
            logger.warn("No termID provided");
            return null;
        }
        return getTermInfo(termID, ontology);
    }

    /**
     * Retrieve the Term Info of a given term.
     *
     * @param termID   Term ID
     * @param ontology Ontology
     * @return Term Info
     */
    private TermDTO getTermInfo(String termID, OntologyDTO ontology) {

        if (ActiveData.isValidActiveData(termID, ActiveData.Type.ANAT))
            logger.error("Encountered an obsoleted anatomy term id: " + termID);

        TermDTO term = OntologyManager.getInstance().getTermByID(termID, ontology);
        if (term == null) {
            logger.warn("No term " + termID + " found!");
            return null;
        }

        if (term.getOntology() == OntologyDTO.MPATH) {
            term.setOntology(OntologyDTO.MPATH_NEOPLASM);
        }
        return term;

    }

    public List<PublicationDTO> getRecentPublications(String key) {
        List<Publication> mostRecentPubs = PublicationService.getRecentPublications(getServletContext(), key);
        List<PublicationDTO> publicationDTOs = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(mostRecentPubs)) {
            for (Publication publication : mostRecentPubs) {
                PublicationDTO publicationDTO = new PublicationDTO();
                publicationDTO.setZdbID(publication.getZdbID());
                publicationDTO.setTitle(publication.getTitle());
                publicationDTOs.add(publicationDTO);
            }
        }
        return publicationDTOs;
    }

    public PublicationDTO addRecentPublication(String zdbID, String key) {
        if (StringUtils.isNotEmpty(zdbID)) {
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(zdbID);
            PublicationService.addRecentPublications(getServletContext(), publication, key);

            PublicationDTO publicationDTO = new PublicationDTO();
            publicationDTO.setZdbID(publication.getZdbID());
            publicationDTO.setTitle(publication.getTitle());
            publicationDTO.setAuthors(publication.getAuthors());
            publicationDTO.setMiniRef(publication.getShortAuthorList());
            return publicationDTO;
        } else {
            return null;
        }
    }

    @Override
    public List<RelatedEntityDTO> getAttributionsForPub(String publicationZdbID) {
        List<RelatedEntityDTO> relatedEntityDTOs = new ArrayList<>();

        List<Marker> markers = RepositoryFactory.getMarkerRepository().getMarkersForAttribution(publicationZdbID);
        if (CollectionUtils.isNotEmpty(markers)) {
            RelatedEntityDTO spacer = new RelatedEntityDTO();
            spacer.setName(AttributionModule.RemoveHeader.MARKER.toString());
            relatedEntityDTOs.add(spacer);
        }
        for (Marker m : markers) {
            MarkerDTO markerDTO = DTOConversionService.convertToMarkerDTO(m);
            markerDTO.setName(m.getAbbreviation() + "[" + m.getType() + "]");
            relatedEntityDTOs.add(markerDTO);
        }


        List<Feature> features = getFeatureRepository().getFeaturesForAttribution(publicationZdbID);
        if (CollectionUtils.isNotEmpty(features)) {
            RelatedEntityDTO spacer = new RelatedEntityDTO();
            spacer.setName(AttributionModule.RemoveHeader.FEATURE.toString());
            relatedEntityDTOs.add(spacer);
        }
        for (Feature f : features) {
            RelatedEntityDTO featureDTO = new RelatedEntityDTO();
            featureDTO.setZdbID(f.getZdbID());
            featureDTO.setName(DTOConversionService.unescapeString(f.getName()));
            relatedEntityDTOs.add(featureDTO);
        }

        List<Genotype> genotypes = getMutantRepository().getGenotypesForAttribution(publicationZdbID);
        if (CollectionUtils.isNotEmpty(genotypes)) {
            RelatedEntityDTO spacer = new RelatedEntityDTO();
            spacer.setName(AttributionModule.RemoveHeader.GENOTYPE.toString());
            relatedEntityDTOs.add(spacer);
        }
        for (Genotype g : genotypes) {
            RelatedEntityDTO genotypeDTO = new RelatedEntityDTO();
            genotypeDTO.setZdbID(g.getZdbID());
            genotypeDTO.setName(DTOConversionService.unescapeString(g.getHandle()));
            relatedEntityDTOs.add(genotypeDTO);
        }
        List<Fish> fishList = getMutantRepository().getFishList(publicationZdbID);
        if (CollectionUtils.isNotEmpty(fishList)) {
            RelatedEntityDTO spacer = new RelatedEntityDTO();
            spacer.setName(AttributionModule.RemoveHeader.FISH.toString());
            relatedEntityDTOs.add(spacer);
            for (Fish fish : fishList) {
                RelatedEntityDTO genotypeDTO = new RelatedEntityDTO();
                genotypeDTO.setZdbID(fish.getZdbID());
                genotypeDTO.setName(fish.getName());
                relatedEntityDTOs.add(genotypeDTO);
            }
        }
        return relatedEntityDTOs;
    }


    public Map<String, String> getAllZfinProperties() {
        Map<String, String> allZfinProperties = new HashMap<>();
        for (ZfinPropertiesEnum zfinProperties : ZfinPropertiesEnum.values()) {
            allZfinProperties.put(zfinProperties.name(), zfinProperties.value());
        }
        return allZfinProperties;
    }


    public TermDTO getTermByName(OntologyDTO ontologyDTO, String value) throws TermNotFoundException {
        value = cleanTermName(value);
        Ontology ontology = DTOConversionService.convertToOntology(ontologyDTO);
        if (ontology == null)
            throw new TermNotFoundException("No Ontology provided for query term string " + value);

        TermDTO term = null;
        // In case the term name is the ZDB_TERM id
        if (ActiveData.isValidActiveData(value, ActiveData.Type.TERM)) {
            term = OntologyManager.getInstance().getTermByID(value);
            return term;
        }

        for (Iterator<Ontology> iterator = ontology.getIndividualOntologies().iterator();
             iterator.hasNext() && term == null; ) {
            ontology = iterator.next();
            term = OntologyManager.getInstance().getTermByName(value, ontology, true);
        }

        // Still null then maybe the Ontology Manager is not yet instantiated with this ontology.
        if (term == null) {
            if (ActiveData.isValidActiveData(value, ActiveData.Type.TERM)) {
                logger.debug("Term [" + value + "] not found in OntologyManager: Trying to retrieve from database.");
                term = DTOConversionService.convertToTermDTOWithDirectRelationships(RepositoryFactory.getOntologyRepository().getTermByZdbID(value));
            }
            if (term == null) {
                logger.debug("Failed to find term [" + value + "]");
                throw new TermNotFoundException("Failed to find term [" + value + "] in <" + ontologyDTO.getDisplayName() + ">");
            }
        }
        return term;
    }

    private String cleanTermName(String value) {
        int indexOfparen = value.indexOf("[");
        if (indexOfparen > -1)
            value = value.substring(0, indexOfparen - 1);
        value = value.replace(MatchingTerm.OBSOLETE_SUFFIX, "");
        return value;
    }

    /**
     * Check if a given term name is a quality relational term
     *
     * @param termName term name
     */
    @Override
    public boolean isTermRelationalQuality(String termName) {
        if (termName == null)
            return false;

        TermDTO term;
        if (ActiveData.isValidActiveData(termName, ActiveData.Type.TERM))
            term = OntologyManager.getInstance().getTermByID(termName, OntologyDTO.QUALITY);
        else
            term = OntologyManager.getInstance().getTermByName(termName, Ontology.QUALITY);
        return term != null && term.isPartOfSubset(SubsetDTO.RELATIONAL_SLIM);

    }

    /**
     * Retrieve the ontology for a given term ID
     *
     * @param termID term id
     */
    @Override
    public OntologyDTO getOntology(String termID) {
        if (termID == null)
            return null;

        TermDTO term = OntologyManager.getInstance().getTermByID(termID);
        return term.getOntology();
    }
}


