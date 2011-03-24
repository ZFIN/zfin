package org.zfin.gwt.root.server;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.antibody.Antibody;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.ui.AttributionModule;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.gwt.root.ui.ItemSuggestOracle;
import org.zfin.gwt.root.ui.ItemSuggestion;
import org.zfin.gwt.root.ui.LookupComposite;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.*;
import org.zfin.people.Organization;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getAnatomyRepository;


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


    private String createListItem(String displayName, Term term) {
        OntologyDTO ontologyDTO = DTOConversionService.convertToOntologyDTO(term.getOntology());
        String termID = term.getZdbID();
        StringBuilder builder = new StringBuilder(60);
        builder.append("<span onmouseover=showTermInfoString('");
        builder.append(ontologyDTO.getOntologyName());
        builder.append("','");
        builder.append(termID);
        builder.append("')  class='autocomplete-plain'>");
        builder.append(displayName);
        builder.append("</span>");
        return builder.toString();
    }

    public SuggestOracle.Response getSupplierSuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        if (query.equals("xxx333")) {
            throw new RuntimeException("this is a test error");
        }

        ProfileRepository profileRep = RepositoryFactory.getProfileRepository();
        List<Organization> organizations = profileRep.getOrganizationsByName(query);

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
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

        int foundInexactMatch = 0;
        if (ActiveData.isValidActiveData(term, ActiveData.Type.TERM)) {
            TermDTO termObject = OntologyManager.getInstance().getTermByID(term, ontologyDto);
            if (termObject != null)
                return new TermStatus(TermStatus.Status.FOUND_EXACT, termObject.getName(), termObject.getZdbID());
            else
                foundInexactMatch = 0;
        } else {
            MatchingTermService service = new MatchingTermService();
            Ontology ontology = DTOConversionService.convertToOntology(ontologyDto);
            Set<MatchingTerm> terms = service.getMatchingTerms(term, ontology);

            for (MatchingTerm anatomyItem : terms) {
                String name = anatomyItem.getTerm().getName();
                if (name.equals(term)) {
                    return new TermStatus(TermStatus.Status.FOUND_EXACT, term, anatomyItem.getTerm().getZdbID());
                } else if (foundInexactMatch < 1 || name.contains(term)) {
                    ++foundInexactMatch;
                }
            }
        }
        if (foundInexactMatch > 1) {
            return new TermStatus(TermStatus.Status.FOUND_MANY, term);
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

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table)
     *
     * @param request  request
     * @param ontology ontology name
     * @return suggestions
     */
    private SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, Ontology ontology, boolean useIDAsValue) {
        HibernateUtil.currentSession();
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

        Collection<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 2) {
            // We add one in order to add an additional term that is not displayed.
            // When it comes back we can add the '...' implying that there are more.
            // Unfortunately, Response does not have an easy fix for this other than exceeding the Response. 
            MatchingTermService matcher = new MatchingTermService(request.getLimit() + 1);
            highlighter.setMatch(query);
            for (MatchingTerm term : matcher.getMatchingTerms(query, ontology)) {
                String suggestion = term.getMatchingTermDisplay();
                String displayName = highlighter.highlight(suggestion);
                String termValue = (useIDAsValue ? term.getTerm().getZdbID() : term.getTerm().getName());
                suggestions.add(new ItemSuggestion(displayName, termValue));
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

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if (query.length() > 0) {
            for (Antibody antibody : RepositoryFactory.getAntibodyRepository().getAntibodiesByName(query)) {
                suggestions.add(new ItemSuggestion(
                        antibody.getAbbreviation().replaceAll(query.replace("(", "\\(").replace(")", "\\)"), "<strong>" + query + "</strong>"), antibody.getAbbreviation()));
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

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            String markerView;
            for (Marker marker : RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(query)) {
                markerView = marker.getAbbreviation().replaceAll(query.replace("(", "\\(").replace(")", "\\)"), "<strong>" + query + "</strong>");
                if (options != null && Boolean.valueOf(options.get(LookupComposite.SHOW_TYPE))) {
                    markerView += " [" + marker.getType() + "]";
                }
                suggestions.add(new ItemSuggestion(markerView, marker.getAbbreviation()));
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

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
            List<Marker> markers = markerRepository.getMarkersByAbbreviationAndGroup(query, typeGroup);
            for (Marker marker : markers) {
                suggestions.add(new ItemSuggestion(marker.getAbbreviation().replaceAll(query, "<strong>" + query + "</strong>"), marker.getAbbreviation()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " + suggestions.size() + " suggestions for " + req);
        return resp;
    }


    public SuggestOracle.Response getConstructSuggestions(SuggestOracle.Request req, String pubZdbID) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
            List<Marker> markers = markerRepository.getMarkersByAbbreviationGroupAndAttribution(query, Marker.TypeGroup.CONSTRUCT, pubZdbID);
            Highlighter highlighter = new Highlighter(query);
            for (Marker marker : markers) {
//                suggestions.add(new ItemSuggestion(marker.getAbbreviation().replaceAll(query.replace("(", "\\(").replace(")", "\\)"), "<strong>" + query + "</strong>"), marker.getAbbreviation()));
                suggestions.add(new ItemSuggestion(highlighter.highlight(marker.getAbbreviation()), marker.getAbbreviation()));
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

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            for (Feature feature : RepositoryFactory.getFeatureRepository().getFeaturesByAbbreviation(query)) {
                suggestions.add(new ItemSuggestion(
                        feature.getAbbreviation().replaceAll(query.replace("(", "\\(").replace(")", "\\)"), "<strong>" + query + "</strong>"), feature.getAbbreviation()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " + suggestions.size() + " suggestions for " + req);
        return resp;
    }

    /**
     * Retrieve the terminfo for a given term id and ontology.
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
        TermDTO term;
        if (termID.indexOf(ActiveData.Type.ANAT.toString()) > -1) {
            AnatomyItem anatomyItem = getAnatomyRepository().getAnatomyTermByID(termID);
            term = OntologyManager.getInstance().getTermByID(anatomyItem.getOboID());
        } else {
            term = OntologyManager.getInstance().getTermByID(termID);
        }

        if (term == null) {
            logger.warn("No term " + termID + " found!");
            return null;
        }
//        TermInfoDTO rootTermInfoDTO = DTOConversionService.convertToTermInfoFromTermInfoDTO(term, ontology, true);
//        TermInfoDTO rootTermInfoDTO = DTOConversionService.convert(term, ontology, true);
//        addRelatedTerms(term, rootTermInfoDTO, ontology);
//        return rootTermInfoDTO;
        return term ;

    }

//    private void addRelatedTerms(TermDTO term, TermInfoDTO rootTermInfoDTO, OntologyDTO ontology) {
//        List<org.zfin.ontology.RelationshipPresentation> relationships = OntologyService.getRelatedTerms(term);
//        if (relationships != null) {
//            for (org.zfin.ontology.RelationshipPresentation relationship : relationships) {
//                List<Term> terms = relationship.getItems();
//                for (Term item : terms) {
//                    TermInfoDTO infoDTO = DTOConversionService.convertToTermInfo(item, ontology, false);
//                    rootTermInfoDTO.addRelatedTermInfo(relationship.getType(), infoDTO);
//                }
//            }
//        }
//    }

    public List<PublicationDTO> getRecentPublications(String key) {
        List<Publication> mostRecentPubs = PublicationService.getRecentPublications(getServletContext(), key);
        List<PublicationDTO> publicationDTOs = new ArrayList<PublicationDTO>();

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
        List<RelatedEntityDTO> relatedEntityDTOs = new ArrayList<RelatedEntityDTO>();

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


        List<Feature> features = RepositoryFactory.getFeatureRepository().getFeaturesForAttribution(publicationZdbID);
        if (CollectionUtils.isNotEmpty(features)) {
            RelatedEntityDTO spacer = new RelatedEntityDTO();
            spacer.setName(AttributionModule.RemoveHeader.FEATURE.toString());
            relatedEntityDTOs.add(spacer);
        }
        for (Feature f : features) {
            relatedEntityDTOs.add(DTOConversionService.convertToFeatureDTO(f));
        }

        List<Genotype> genotypes = RepositoryFactory.getMutantRepository().getGenotypesForAttribution(publicationZdbID);
        if (CollectionUtils.isNotEmpty(genotypes)) {
            RelatedEntityDTO spacer = new RelatedEntityDTO();
            spacer.setName(AttributionModule.RemoveHeader.GENOTYPE.toString());
            relatedEntityDTOs.add(spacer);
        }
        for (Genotype g : genotypes) {
            relatedEntityDTOs.add(DTOConversionService.convertToGenotypeDTO(g));
        }


        return relatedEntityDTOs;
    }


    public Map<String, String> getAllZfinProperties() {
        Map<String, String> allZfinProperties = new HashMap<String, String>();
        for (ZfinPropertiesEnum zfinProperties : ZfinPropertiesEnum.values()) {
            allZfinProperties.put(zfinProperties.name(), zfinProperties.value());
        }
        return allZfinProperties;
    }


    public TermDTO getTermByName(OntologyDTO ontologyDTO, String value) {
        Ontology ontology = DTOConversionService.convertToOntology(ontologyDTO);

        TermDTO term = null;
        // In case the term name is the ZDB_TERM id
        if (ActiveData.isValidActiveData(value, ActiveData.Type.TERM)) {
            term = OntologyManager.getInstance().getTermByID(value);
            return term;
        }

        for (Iterator<Ontology> iterator = ontology.getIndividualOntologies().iterator();
             iterator.hasNext() && term == null;) {
            ontology = iterator.next();
            term = OntologyManager.getInstance().getTermByName(value, ontology, true);
        }

        // Still null then maybe the Ontology Manager is not yet instantiated with this ontology.
        if (term == null) {
            if (ActiveData.isValidActiveData(value, ActiveData.Type.TERM)) {
                logger.info("Term [" + value + "] not found in OntologyManager: Trying to retrieve from database.");
                term = DTOConversionService.convertToTermDTOWithDirectRelationships(RepositoryFactory.getOntologyRepository().getTermByZdbID(value));
            }
            if (term == null) {
                logger.info("Failed to find term [" + value + "]");
                return null;
            }
        }
        return term;
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
            term = OntologyManager.getInstance().getTermByID( termName, OntologyDTO.QUALITY);
        else
            term = OntologyManager.getInstance().getTermByName(termName, Ontology.QUALITY);
        return term != null && term.isPartOfSubset(SubsetDTO.RELATIONAL_SLIM);

    }
}


