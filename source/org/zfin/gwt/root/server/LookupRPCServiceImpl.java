package org.zfin.gwt.root.server;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.antibody.Antibody;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.ui.AttributionModule;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ItemSuggestOracle;
import org.zfin.gwt.root.ui.ItemSuggestion;
import org.zfin.gwt.root.ui.LookupComposite;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Feature;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.*;
import org.zfin.people.Organization;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;


/**
 *
 */
public class LookupRPCServiceImpl extends RemoteServiceServlet implements LookupRPCService {

    private static final Logger logger = Logger.getLogger(LookupRPCServiceImpl.class);
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
        String termID = term.getID();
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
    public TermStatus validateAnatomyTerm(String term) {

        MatchingTermService service = new MatchingTermService();
        Set<MatchingTerm> terms = service.getMatchingTerms(Ontology.ANATOMY, term);

        int foundInexactMatch = 0;
        for (MatchingTerm anatomyItem : terms) {
            String name = anatomyItem.getTerm().getTermName();
            if (name.equals(term)) {
                return new TermStatus(TermStatus.Status.FOUND_EXACT, term, anatomyItem.getTerm().getID());
            } else if (foundInexactMatch < 1 || name.contains(term)) {
                ++foundInexactMatch;
            }
        }
        if (foundInexactMatch > 1) {
            return new TermStatus(TermStatus.Status.FOUND_MANY, term);
        }
        return new TermStatus(TermStatus.Status.FOUND_NONE, term);
    }

    /**
     * Checks if a given marker exists in ZFIN>
     *
     * @param markerAbbreviation abbreviation
     * @return term status
     */
    public TermStatus validateMarkerTerm(String markerAbbreviation) {

        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(markerAbbreviation);
        if (marker != null) {
            return new TermStatus(TermStatus.Status.FOUND_EXACT, markerAbbreviation, marker.getZdbID());
        }

        List<Marker> markers = RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(markerAbbreviation);
        if (markers.size() == 1) {
            return new TermStatus(TermStatus.Status.FOUND_EXACT, markerAbbreviation, markers.get(0).getZdbID());
        } else if (markers.size() == 0) {
            return new TermStatus(TermStatus.Status.FOUND_NONE, markerAbbreviation);
        } else {
            return new TermStatus(TermStatus.Status.FOUND_MANY, markerAbbreviation);
        }
    }

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table)
     *
     * @param request        request
     * @param showTermDetail true or false
     * @param ontology       ontology name
     * @return suggestions
     */
    public SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, boolean showTermDetail, OntologyDTO ontology,boolean useIDAsValue) {
        return getOntologySuggestions(request, showTermDetail, DTOConversionService.convertToOntology(ontology),useIDAsValue);
    }

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table)
     *
     * @param request        request
     * @param showTermDetail create mouseOver JS script to show term detail
     * @param ontology       ontology name
     * @return suggestions
     */
    private SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, boolean showTermDetail, Ontology ontology,boolean useIDAsValue) {
        HibernateUtil.currentSession();
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = request.getQuery().trim();

        Collection<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 2) {
            MatchingTermService matcher = new MatchingTermService(request.getLimit());
            highlighter.setMatch(query);
//            Pattern p = Pattern.compile("("+query+")",Pattern.CASE_INSENSITIVE);
            for (MatchingTerm term : matcher.getMatchingTerms(ontology, query)) {
                String suggestion = term.getMatchingTermDisplay();
                String displayName = highlighter.highlight(suggestion) ;
//                String displayName = p.matcher(suggestion).replaceAll("<strong>$1</strong>") ;
                String termValue = (useIDAsValue ?  term.getTerm().getID() : term.getTerm().getTermName()) ;
                if (showTermDetail){
                    displayName = createListItem(displayName, term.getTerm());
                }

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
    public SuggestOracle.Response getMarkerSuggestions(SuggestOracle.Request req, Map<String,String> options) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            String markerView ;
            for (Marker marker : RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(query)) {
                markerView = marker.getAbbreviation().replaceAll(query.replace("(", "\\(").replace(")", "\\)"), "<strong>" + query + "</strong>") ;
                if(options!=null && Boolean.valueOf(options.get(LookupComposite.SHOW_TYPE))){
                    markerView += " ["+marker.getType() + "]" ;
                }
                suggestions.add(new ItemSuggestion(markerView , marker.getAbbreviation()));
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
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
            List<Marker> markers = markerRepository.getMarkersByAbbreviationAndGroup(query, Marker.TypeGroup.GENEDOM_AND_EFG);
            for (Marker marker : markers) {
                suggestions.add(new ItemSuggestion(marker.getAbbreviation().replaceAll(query, "<strong>" + query + "</strong>"), marker.getAbbreviation()));
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
            for (Feature feature : RepositoryFactory.getMutantRepository().getFeaturesByAbbreviation(query)) {
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
    public TermInfo getTermInfo(OntologyDTO ontology, String termID) {
        if (ontology == null) {
            logger.warn("No ontology provided");
            return null;
        }
        if (StringUtils.isEmpty(termID)) {
            logger.warn("No termID provided");
            return null;
        }
        return getGenericTermInfo(termID, ontology);
    }

    /**
     * Retrieve the term info for a given ontology and term name.
     *
     * @param ontology Ontology
     * @param termName term Name
     */
    public TermInfo getTermInfoByName(OntologyDTO ontology, String termName) {
        return getGenericTermInfoByName(termName, ontology);
    }

    /**
     * Retrieve the Term Info of a given term.
     * ToDo: This has logic to go out for ANAT and GO to different tables.
     * Need to consolidate this into one table TERM.
     *
     * @param termID   Term ID
     * @param ontology Ontology
     * @return Term Info
     */
    private TermInfo getGenericTermInfo(String termID, OntologyDTO ontology) {
        Term term;
        if (termID.indexOf(ActiveData.Type.TERM.toString()) > -1)
            term = getInfrastructureRepository().getTermByID(termID);
        else if (termID.indexOf(ActiveData.Type.ANAT.toString()) > -1) {
            term = getAnatomyRepository().getAnatomyTermByID(termID);
            term = getOntologyRepository().getTermByOboID(term.getOboID());
        } else {
            term = getOntologyRepository().getTermByOboID(termID);
        }

        if (term == null) {
            logger.warn("No term " + termID + " found!");
            return null;
        }
        TermInfo rootTermInfo = DTOConversionService.convertToTermInfo(term, ontology, true);
        addRelatedTerms(term, rootTermInfo, ontology);
        return rootTermInfo;

    }

    private TermInfo getGenericTermInfoByName(String termName, OntologyDTO ontologyDTO) {
        Ontology ontology = DTOConversionService.convertToOntology(ontologyDTO);
        if (ontology == null) {
            logger.warn("No Ontology [" + ontologyDTO.getOntologyName() + "] found!");
            return null;
        }
        Term ontologyTerm = OntologyManager.getInstance().getTermByName(ontology, termName,true);
        if (ontologyTerm == null) {
            logger.warn("No term " + termName + " in ontology [" + ontology.getOntologyName() + " in Ontology Manager found!");
            return null;
        }
        GenericTerm term = getInfrastructureRepository().getTermByID(ontologyTerm.getID());
        if (term == null) {
            logger.warn("No term " + termName + " found!");
            return null;
        }
        TermInfo rootTermInfo = DTOConversionService.convertToTermInfo(term, ontologyDTO, true);
        addRelatedTerms(term, rootTermInfo, ontologyDTO);
        return rootTermInfo;

    }

    private void addRelatedTerms(Term term, TermInfo rootTermInfo, OntologyDTO ontology) {
        List<org.zfin.ontology.RelationshipPresentation> relationships = OntologyService.getRelatedTerms(term);
        if (relationships != null) {
            for (org.zfin.ontology.RelationshipPresentation relationship : relationships) {
                List<Term> terms = relationship.getItems();
                for (Term item : terms) {
                    TermInfo info = DTOConversionService.convertToTermInfo(item, ontology, false);
                    rootTermInfo.addRelatedTermInfo(relationship.getType(), info);
                }
            }
        }
    }

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

        List<Marker> markers = RepositoryFactory.getMarkerRepository().getMarkerForAttribution(publicationZdbID);
        if (CollectionUtils.isNotEmpty(markers)) {
            RelatedEntityDTO spacer = new RelatedEntityDTO();
            spacer.setName(AttributionModule.RemoveHeader.MARKER.toString());
            relatedEntityDTOs.add(spacer);
        }
        for (Marker m : markers) {
            MarkerDTO markerDTO = DTOConversionService.convertToMarkerDTO(m) ;
            markerDTO.setName(m.getAbbreviation() + "["+m.getType() +"]");
            relatedEntityDTOs.add(markerDTO);
        }


        List<Feature> features = RepositoryFactory.getMutantRepository().getFeaturesForAttribution(publicationZdbID);
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
}


