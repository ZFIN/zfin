package org.zfin.gwt.root.server;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyService;
import org.zfin.anatomy.presentation.RelationshipPresentation;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ItemSuggestOracle;
import org.zfin.gwt.root.ui.ItemSuggestion;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Feature;
import org.zfin.ontology.*;
import org.zfin.people.Organization;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.BODtoConversionService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.*;


/**
 *
 */
public class LookupRPCServiceImpl extends RemoteServiceServlet implements LookupRPCService {

    private static final Logger LOG = Logger.getLogger(LookupRPCServiceImpl.class);

    private static final int NUMBER_OF_SUGGESTIONS = ItemSuggestOracle.DEFAULT_LIMIT;

    private String createListItem(String displayName, Term term) {
        OntologyDTO ontologyDTO = BODtoConversionService.getOntologyDTO(term.getOntology());
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
            String term = organization.getName();
            suggestions.add(new ItemSuggestion(term.replaceAll("(?i)" + query, "<strong>$0</strong>"), term));
        }

        resp.setSuggestions(suggestions);
        LOG.info("found " +suggestions.size() + " suggestions for " + req);
        return resp;
    }


    public TermStatus validateAnatomyTerm(String term) {

        List<AnatomyItem> anatomyItems = RepositoryFactory.getAnatomyRepository().getAnatomyItemsByName(term, false);
        int foundInexactMatch = 0;
        for (AnatomyItem anatomyItem : anatomyItems) {
            String name = anatomyItem.getName();
            if (name.equals(term)) {
                return new TermStatus(TermStatus.Status.FOUND_EXACT, term, anatomyItem.getZdbID());
            } else if (foundInexactMatch < 1 || name.contains(term)) {
                ++foundInexactMatch;
            }
        }
        if (foundInexactMatch > 1) {
            return new TermStatus(TermStatus.Status.FOUND_MANY, term);
        }
        return new TermStatus(TermStatus.Status.FOUND_NONE, term);
    }

    public TermStatus validateMarkerTerm(String term) {

        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(term);
        if (marker != null) {
            return new TermStatus(TermStatus.Status.FOUND_EXACT, term, marker.getZdbID());
        }

        List<Marker> markers = RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(term);
        if (markers.size() == 1) {
            return new TermStatus(TermStatus.Status.FOUND_EXACT, term, markers.get(0).getZdbID());
        } else if (markers.size() == 0) {
            return new TermStatus(TermStatus.Status.FOUND_NONE, term);
        } else {
            return new TermStatus(TermStatus.Status.FOUND_MANY, term);
        }
    }

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table)
     *
     * @param request  request
     * @param wildCard true or false
     * @param ontology ontology name
     * @return suggestions
     */
    public SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, boolean wildCard, OntologyDTO ontology) {
        return getOntologySuggestions(request, BODtoConversionService.getOntology(ontology));
    }

    /**
     * Retrieve terms from a given ontology (via the gDAG ontology table)
     *
     * @param request  request
     * @param ontology ontology name
     * @return suggestions
     */
    public SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, Ontology ontology) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = request.getQuery().trim();

        Collection<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 2) {
            for (MatchingTerm term : OntologyManager.getInstance().getMatchingTerms(ontology, query)) {
                String suggestion = term.getMatchingTermDisplay();
                String displayName = suggestion.replace(query, "<strong>" + query + "</strong>");
                displayName = createListItem(displayName, term.getTerm());
                suggestions.add(new ItemSuggestion(displayName, term.getTerm().getTermName()));
            }
        }
        resp.setSuggestions(suggestions);
        LOG.info("found " + suggestions.size() + " suggestions for " + request);
        return resp;
    }

    public SuggestOracle.Response getMarkerSuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>(NUMBER_OF_SUGGESTIONS);
        if (query.length() > 0) {
            for (Marker marker : RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(query)) {
                suggestions.add(new ItemSuggestion(
                        marker.getAbbreviation().replaceAll(query.replace("(", "\\(").replace(")", "\\)"), "<strong>" + query + "</strong>"), marker.getAbbreviation()));
            }
        }
        resp.setSuggestions(suggestions);
        LOG.info("found " +suggestions.size() + " suggestions for " + req);
        return resp;
    }

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
        LOG.info("found " +suggestions.size() + " suggestions for " + req);
        return resp;
    }

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
        LOG.info("found " +suggestions.size() + " suggestions for " + req);
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
            LOG.warn("No ontology provided");
            return null;
        }
        if (StringUtils.isEmpty(termID)) {
            LOG.warn("No termID provided");
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
        if (ontology == null) {
            LOG.warn("No ontology provided");
            return null;
        }
        if (StringUtils.isEmpty(termName)) {
            LOG.warn("No termID provided");
            return null;
        }
        switch (ontology) {
            case ANATOMY:
                return getAnatomyTerminfoByName(termName);
            case GO_CC:
                return getGenericTermInfoByName(termName, ontology);
            case GO:
                return getGenericTermInfoByName(termName, ontology);
        }
        return null;
    }

    private TermInfo getAnatomyTerminfoByName(String termName) {

        AnatomyItem term = getAnatomyRepository().getAnatomyItem(termName);
        if (term == null) {
            LOG.warn("No term " + termName + " found!");
            return null;
        }
        TermInfo rootTermInfo = convertAnatomyToTermInfo(term);
        addRelatedAnatomyTerms(term, rootTermInfo);
        return rootTermInfo;
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
            LOG.warn("No term " + termID + " found!");
            return null;
        }
        TermInfo rootTermInfo = BODtoConversionService.getTermInfo(term, ontology, true);
        addRelatedTerms(term, rootTermInfo, ontology);
        return rootTermInfo;

    }

    private TermInfo getGenericTermInfoByName(String termName, OntologyDTO ontologyDTO) {
        Ontology ontology = OntologyService.convertOntology(ontologyDTO);
        GenericTerm term = getInfrastructureRepository().getTermByName(termName, ontology);
        if (term == null) {
            LOG.warn("No term " + termName + " found!");
            return null;
        }
        TermInfo rootTermInfo = BODtoConversionService.getTermInfo(term, ontologyDTO, true);
        addRelatedTerms(term, rootTermInfo, ontologyDTO);
        return rootTermInfo;

    }

    private void addRelatedTerms(Term term, TermInfo rootTermInfo, OntologyDTO ontology) {
        List<org.zfin.ontology.RelationshipPresentation> relationships = OntologyService.getRelatedTerms(term);
        if (relationships != null) {
            for (org.zfin.ontology.RelationshipPresentation relationship : relationships) {
                List<Term> terms = relationship.getItems();
                for (Term item : terms) {
                    TermInfo info = BODtoConversionService.getTermInfo(item, ontology, false);
                    rootTermInfo.addRelatedTermInfo(relationship.getType(), info);
                }
            }
        }
    }

    private void addRelatedAnatomyTerms(AnatomyItem term, TermInfo rootTermInfo) {
        List<RelationshipPresentation> rels = AnatomyService.getRelations(term);
        if (rels != null) {
            for (RelationshipPresentation relationship : rels) {
                List<AnatomyItem> terms = relationship.getItems();
                for (AnatomyItem item : terms) {
                    TermInfo info = convertAnatomyToTermInfo(item);
                    rootTermInfo.addRelatedTermInfo(relationship.getType(), info);
                }
            }
        }
    }

    private TermInfo convertAnatomyToTermInfo(AnatomyItem term) {
        TermInfo info = BODtoConversionService.getTermInfo(term, OntologyDTO.ANATOMY, true);
        info.setStartStage(term.getStart().getAbbreviation());
        info.setEndStage(term.getEnd().getAbbreviation());
        return info;
    }


    public List<PublicationDTO> getRecentPublications(String key) {
        List<Publication> mostRecentsPubs = PublicationService.getRecentPublications(getServletContext(),key);
        List<PublicationDTO> publicationDTOs = new ArrayList<PublicationDTO>();

        if (CollectionUtils.isNotEmpty(mostRecentsPubs)){
            for(Publication publication: mostRecentsPubs){
                PublicationDTO publicationDTO = new PublicationDTO();
                publicationDTO.setZdbID(publication.getZdbID());
                publicationDTO.setTitle(publication.getTitle());
                publicationDTOs.add(publicationDTO);
            }
        }
        return publicationDTOs ;
    }

    public PublicationDTO addRecentPublication(String zdbID,String key) {
        if (StringUtils.isNotEmpty(zdbID)) {
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(zdbID);
            PublicationService.addRecentPublications(getServletContext(),publication,key);

            PublicationDTO publicationDTO = new PublicationDTO();
            publicationDTO.setZdbID(publication.getZdbID());
            publicationDTO.setTitle(publication.getTitle());
            publicationDTO.setAuthors(publication.getAuthors());
            publicationDTO.setMiniRef(publication.getShortAuthorList());
            return publicationDTO;
        }
        else{
            return null ;
        }
    }
}


