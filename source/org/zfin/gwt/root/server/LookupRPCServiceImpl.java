package org.zfin.gwt.root.server;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyService;
import org.zfin.anatomy.presentation.AnatomyAutoCompleteTerm;
import org.zfin.anatomy.presentation.AnatomyPresentation;
import org.zfin.anatomy.presentation.RelationshipPresentation;
import org.zfin.anatomy.presentation.SortAnatomySearchTerm;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.gwt.root.dto.Ontology;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.TermInfo;
import org.zfin.gwt.root.dto.TermStatus;
import org.zfin.gwt.root.ui.ItemSuggestion;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Feature;
import org.zfin.mutant.Term;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.GoTerm;
import org.zfin.ontology.OntologyService;
import org.zfin.ontology.presentation.OntologyAutoCompleteTerm;
import org.zfin.ontology.presentation.TermComparator;
import org.zfin.people.Organization;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 */
public class LookupRPCServiceImpl extends RemoteServiceServlet implements LookupRPCService {

    private Logger logger = Logger.getLogger(LookupRPCServiceImpl.class);
    private AnatomyRepository anatomyRep = RepositoryFactory.getAnatomyRepository();
    private InfrastructureRepository infrastructureRep = RepositoryFactory.getInfrastructureRepository();

    /**
     * Gets suggestions from the anatomy repository.
     * Note that we do not use limits on the request for this implementation.
     */
    public SuggestOracle.Response getAnatomySuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        List<AnatomyItem> anatomyItems = RepositoryFactory.getAnatomyRepository().getAnatomyItemsByName(query, false);
        Collections.sort(anatomyItems, new SortAnatomySearchTerm(query));
        if (CollectionUtils.isNotEmpty(anatomyItems)) {
            List<AnatomyAutoCompleteTerm> terms = AnatomyPresentation.getAnatomyTermList(anatomyItems, query);
            for (AnatomyAutoCompleteTerm term : terms) {
                String suggestion = term.getTermName();
                // include synonym if match is on it
                if (!term.isMatchOnTermName()) {
                    suggestion += " [" + term.getSynonymName() + "]";
                }
                String displayName = suggestion.replaceAll("(?i)" + query, "<strong>$0</strong>");
                displayName = createListItem(displayName, "AO", term.getID());
                suggestions.add(new ItemSuggestion(displayName, term.getTermName()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " +suggestions.size() + " suggestions for " + req);
        return resp;
    }

    private String createListItem(String displayName, String ontologyName, String termID) {
        StringBuilder builder = new StringBuilder();
        builder.append("<span onmouseover=showTermInfoString('");
        builder.append(ontologyName);
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
        logger.info("found " +suggestions.size() + " suggestions for " + req);
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
    public SuggestOracle.Response getOntologySuggestions(SuggestOracle.Request request, boolean wildCard, Ontology ontology) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = request.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        org.zfin.ontology.Ontology ontologyEnum = org.zfin.ontology.Ontology.getOntology(ontology.getDBName());
        List<GenericTerm> genericTerms = RepositoryFactory.getInfrastructureRepository().getTermsByName(query, ontologyEnum);
        Collections.sort(genericTerms, new TermComparator(query));
        if (wildCard && genericTerms != null && genericTerms.size() > 0) {
            suggestions.add(new ItemSuggestion("*" + query + "*", null));
        }

        if (genericTerms != null) {
            List<OntologyAutoCompleteTerm> terms = OntologyService.getTermList(genericTerms, query);
            for (OntologyAutoCompleteTerm term : terms) {
                String suggestion = term.getTermName();
                // include synonym if match is on it
                if (!term.isMatchOnTermName()) {
                    suggestion += " [" + term.getSynonymName() + "]";
                }
                String displayName = suggestion.replaceAll("(?i)" + query, "<strong>$0</strong>");
                displayName = createListItem(displayName, ontology.getDisplayName(), term.getID());
                suggestions.add(new ItemSuggestion(displayName, term.getTermName()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " +suggestions.size() + " suggestions for " + request);
        return resp;

    }

    public SuggestOracle.Response getGOSuggestions(SuggestOracle.Request request) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String termQuery = request.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if (termQuery.length() > 2) {
            List<GoTerm> goTerms = RepositoryFactory.getMutantRepository().getGoTermsByName(termQuery);
            Collections.sort(goTerms, new TermComparator(termQuery));

            for (GoTerm goTerm : goTerms) {
                suggestions.add(new ItemSuggestion(goTerm.getName().replaceAll(termQuery, "<strong>" + termQuery + "</strong>"), goTerm.getName()));
            }
        }
        resp.setSuggestions(suggestions);

        logger.info("found " +suggestions.size() + " suggestions for " + request);
        return resp;
    }

    public SuggestOracle.Response getQualitySuggestions(SuggestOracle.Request req) {
        MutantRepository repository = RepositoryFactory.getMutantRepository();
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if (query.length() > 2) {
            for (Term term : repository.getQualityTermsByName(query)) {
                suggestions.add(new ItemSuggestion(term.getName().replaceAll(query, "<strong>" + query + "</strong>"), term.getName()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " +suggestions.size() + " suggestions for " + req);
        return resp;
    }

    public SuggestOracle.Response getMarkerSuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if (query.length() > 0) {
            for (Marker marker : RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(query)) {
                suggestions.add(new ItemSuggestion(
                        marker.getAbbreviation().replaceAll(query.replace("(", "\\(").replace(")", "\\)"), "<strong>" + query + "</strong>"), marker.getAbbreviation()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " +suggestions.size() + " suggestions for " + req);
        return resp;
    }

    public SuggestOracle.Response getGenedomAndEFGSuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if (query.length() > 0) {
            MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
            List<Marker> markers = markerRepository.getMarkersByAbbreviationAndGroup(query, Marker.TypeGroup.GENEDOM_AND_EFG);
            for (Marker marker : markers) {
                suggestions.add(new ItemSuggestion(marker.getAbbreviation().replaceAll(query, "<strong>" + query + "</strong>"), marker.getAbbreviation()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " +suggestions.size() + " suggestions for " + req);
        return resp;
    }

    public SuggestOracle.Response getFeatureSuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if (query.length() > 0) {
            for (Feature feature : RepositoryFactory.getMutantRepository().getFeaturesByAbbreviation(query)) {
                suggestions.add(new ItemSuggestion(
                        feature.getAbbreviation().replaceAll(query.replace("(", "\\(").replace(")", "\\)"), "<strong>" + query + "</strong>"), feature.getAbbreviation()));
            }
        }
        resp.setSuggestions(suggestions);
        logger.info("found " +suggestions.size() + " suggestions for " + req);
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
    public TermInfo getTermInfo(Ontology ontology, String termID) {
        if (ontology == null) {
            logger.warn("No ontology provided");
            return null;
        }
        if (StringUtils.isEmpty(termID)) {
            logger.warn("No termID provided");
            return null;
        }
        switch (ontology) {
            case ANATOMY:
                return getAnatomyTerminfo(termID);
            case GO_CC:
                return getGenericTermInfo(termID, ontology);
            case GO:
                return getGenericTermInfo(termID, ontology);
        }
        return null;
    }

    /**
     * Retrieve the term info for a given ontology and term name.
     *
     * @param ontology Ontology
     * @param termName term Name
     */
    public TermInfo getTermInfoByName(Ontology ontology, String termName) {
        if (ontology == null) {
            logger.warn("No ontology provided");
            return null;
        }
        if (StringUtils.isEmpty(termName)) {
            logger.warn("No termID provided");
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

    private TermInfo getAnatomyTerminfo(String termID) {
        AnatomyItem term;
        if (termID.indexOf(ActiveData.Type.ANAT.toString()) > -1)
            term = anatomyRep.getAnatomyTermByID(termID);
        else
            term = anatomyRep.getAnatomyTermByOboID(termID);
        if (term == null) {
            logger.warn("No term " + termID + " found!");
            return null;
        }
        TermInfo rootTermInfo = convertAnatomyToTermInfo(term);
        addRelatedAnatomyTerms(term, rootTermInfo);
        return rootTermInfo;

    }

    private TermInfo getAnatomyTerminfoByName(String termName) {

        AnatomyItem term = anatomyRep.getAnatomyItem(termName);
        if (term == null) {
            logger.warn("No term " + termName + " found!");
            return null;
        }
        TermInfo rootTermInfo = convertAnatomyToTermInfo(term);
        addRelatedAnatomyTerms(term, rootTermInfo);
        return rootTermInfo;
    }

    private TermInfo getGenericTermInfo(String termID, Ontology ontology) {
        GenericTerm term;
        if (termID.indexOf(ActiveData.Type.TERM.toString()) > -1)
            term = infrastructureRep.getTermByID(termID);
        else
            term = infrastructureRep.getTermByOboID(termID);
        if (term == null) {
            logger.warn("No term " + termID + " found!");
            return null;
        }
        TermInfo rootTermInfo = convertGenericTermToTermInfo(term, ontology, true);
        addRelatedTerms(term, rootTermInfo, ontology);
        return rootTermInfo;

    }

    private TermInfo getGenericTermInfoByName(String termName, Ontology ontology) {
        org.zfin.ontology.Ontology ontol = OntologyService.convertOntology(ontology);
        GenericTerm term = infrastructureRep.getTermByName(termName, ontol);
        if (term == null) {
            logger.warn("No term " + termName + " found!");
            return null;
        }
        TermInfo rootTermInfo = convertGenericTermToTermInfo(term, ontology, true);
        addRelatedTerms(term, rootTermInfo, ontology);
        return rootTermInfo;

    }

    private void addRelatedTerms(GenericTerm term, TermInfo rootTermInfo, Ontology ontology) {
        List<org.zfin.ontology.RelationshipPresentation> relationships = OntologyService.getRelatedTerms(term);
        if (relationships != null) {
            for (org.zfin.ontology.RelationshipPresentation relationship : relationships) {
                List<GenericTerm> terms = relationship.getItems();
                for (GenericTerm item : terms) {
                    TermInfo info = convertGenericTermToTermInfo(item, ontology, false);
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
        TermInfo info = new TermInfo();
        info.setID(term.getOboID());
        info.setName(term.getName());
        info.setSynonyms(AnatomyPresentation.createFormattedSynonymList(term));
        info.setDefinition(term.getDefinition());
        info.setStartStage(term.getStart().getAbbreviation());
        info.setEndStage(term.getEnd().getAbbreviation());
        info.setOntology(Ontology.ANATOMY);
        return info;
    }

    private TermInfo convertGenericTermToTermInfo(GenericTerm term, Ontology ontology, boolean includeSynonyms) {
        TermInfo info = new TermInfo();
        info.setID(term.getOboID());
        info.setName(term.getTermName());
        if (includeSynonyms)
            info.setSynonyms(OntologyService.createFormattedSynonymList(term));
        info.setDefinition(term.getDefinition());
        info.setComment(term.getComment());
        info.setOntology(ontology);
        return info;
    }

    @Override
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

    @Override
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


