package org.zfin.framework.presentation.server;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomySynonym;
import org.zfin.anatomy.presentation.SortAnatomySearchTerm;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.presentation.client.ItemSuggestion;
import org.zfin.framework.presentation.client.LookupService;
import org.zfin.framework.presentation.client.TermStatus;
import org.zfin.anatomy.presentation.SortAnatomySearchTerm;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomySynonym;
import org.zfin.repository.RepositoryFactory;
import org.zfin.repository.SessionCreator;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.people.Organization;
import org.zfin.ontology.GoTerm;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.mutant.Term;
import org.zfin.mutant.Feature;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.apache.log4j.Logger;

import java.util.*;


/**
 *
 */
public class LookupServiceImpl extends RemoteServiceServlet implements LookupService {

    private transient Logger logger = Logger.getLogger(LookupServiceImpl.class) ;


    /**
     * Gets suggestions from the anatomy repository.
     * Note that we do not use limits on the request for this implementation.
     */
    public SuggestOracle.Response getAnatomySuggestions(SuggestOracle.Request req,boolean wildCard) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        if (query.equals("xxx333")) {
            throw new RuntimeException("this is a test error");
        }

        SessionCreator.instantiateDBForHostedMode();


        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        List<AnatomyItem> anatomyItems= RepositoryFactory.getAnatomyRepository().getAnatomyItemsByName(query,false) ;
        Collections.sort(anatomyItems, new SortAnatomySearchTerm(query));
        if (wildCard == true && anatomyItems != null && anatomyItems.size() > 0) {
            suggestions.add(new ItemSuggestion("*" + query + "*", null));
        }
        for (AnatomyItem anatomyItem : anatomyItems) {
            String term = anatomyItem.getName();
            String suggestion = new String(term);

            Set<AnatomySynonym> synonyms = anatomyItem.getSynonyms();
            if (synonyms != null) {
                Iterator<AnatomySynonym> synonymIterator = synonyms.iterator();
                boolean notFound = false;
                while (synonymIterator.hasNext() && notFound == false) {
                    AnatomySynonym anatomySynonym = synonymIterator.next();
                    if (anatomySynonym.getName().contains(query)) {
                        suggestion += " [" + anatomySynonym.getName() + "]";
                        notFound = true;
                    }
                }
            }
            suggestions.add(new ItemSuggestion(suggestion.replaceAll("(?i)" + query, "<strong>$0</strong>"), term));
        }
        resp.setSuggestions(suggestions);
        logger.info("returned with no error: " + req + " " + suggestions.size() + " suggestions ");
        return resp;
    }

    public SuggestOracle.Response getSupplierSuggestions(SuggestOracle.Request req, boolean wildCard) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery();

        if (query.equals("xxx333")) {
            throw new RuntimeException("this is a test error");
        }

        SessionCreator.instantiateDBForHostedMode();

        ProfileRepository profileRep = RepositoryFactory.getProfileRepository();
        List<Organization> organizations = profileRep.getOrganizationsByName(query);

        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        for (Organization organization : organizations) {
            String term = organization.getName();
            suggestions.add(new ItemSuggestion(term.replaceAll("(?i)" + query, "<strong>$0</strong>"), term));
        }

        resp.setSuggestions(suggestions);
        logger.info("returned with no error: " + req + " " + suggestions.size() + " suggestions ");
        return resp;
    }


    public TermStatus validateAnatomyTerm(String term) {

        SessionCreator.instantiateDBForHostedMode() ;

        List<AnatomyItem> anatomyItems= RepositoryFactory.getAnatomyRepository().getAnatomyItemsByName(term,false) ;
        int foundInexactMatch = 0 ;
        for(AnatomyItem anatomyItem : anatomyItems){
            String name = anatomyItem.getName() ;
            if(name.equals(term)){
                return new TermStatus(TermStatus.TERM_STATUS_FOUND_EXACT,term,anatomyItem.getZdbID());
            } else if (foundInexactMatch < 1 || name.contains(term)) {
                ++foundInexactMatch;
            }
        }
        if(foundInexactMatch > 1){
           return new TermStatus(TermStatus.TERM_STATUS_FOUND_MANY,term);
        }
        return new TermStatus(TermStatus.TERM_STATUS_FOUND_NONE,term);
    }

    public SuggestOracle.Response getGOSuggestions(SuggestOracle.Request req,boolean wildCard) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery() ;

        if(query.equals("xxx333")){
            throw new RuntimeException("this is a test error") ;
        }

        SessionCreator.instantiateDBForHostedMode() ;


        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if(query.length()>2){
            for(GoTerm goTerm: RepositoryFactory.getMutantRepository().getGoTermsByName(query) ){
                suggestions.add(new ItemSuggestion(goTerm.getName().replaceAll(query,"<strong>"+query+"</strong>"),goTerm.getName())) ;
            }
        }
        if(wildCard==true){
            suggestions.add(new ItemSuggestion("*"+query+"*",null)) ;
        }
        resp.setSuggestions(suggestions);

        logger.info("returned with no error: "+ req + " "  +  suggestions.size() + " suggestions ");

        return resp ;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SuggestOracle.Response getQualitySuggestions(SuggestOracle.Request req,boolean wildCard) {
        MutantRepository repository = RepositoryFactory.getMutantRepository() ;
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery() ;

        if(query.equals("xxx333")){
            throw new RuntimeException("this is a test error") ;
        }

        SessionCreator.instantiateDBForHostedMode() ;


        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if(query.length()>2){
            for(Term term: repository.getQualityTermsByName(query) ){
                suggestions.add(new ItemSuggestion(term.getName().replaceAll(query,"<strong>"+query+"</strong>"),term.getName())) ;
            }
        }
        if(wildCard==true){
            suggestions.add(new ItemSuggestion("*"+query+"*",null)) ;
        }
        resp.setSuggestions(suggestions);

        logger.info("returned with no error: "+ req + " "  +  suggestions.size() + " suggestions ");

        return resp ;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SuggestOracle.Response getMarkerSuggestions(SuggestOracle.Request req, boolean wildCard) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery() ;

        if(query.equals("xxx333")){
            throw new RuntimeException("this is a test error") ;
        }

        SessionCreator.instantiateDBForHostedMode() ;


        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if(query.length()>0){
            for(Marker marker: RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(query) ){
                suggestions.add(new ItemSuggestion(
                        marker.getAbbreviation().replaceAll(query.replace("(","\\(").replace(")","\\)"),"<strong>"+query+"</strong>"),marker.getAbbreviation())) ;
            }
        }
        if(wildCard==true){
            suggestions.add(0,new ItemSuggestion("*"+query+"*",null)) ;
        }
        resp.setSuggestions(suggestions);

        logger.info("returned with no error: "+ req + " "  +  suggestions.size() + " suggestions ");

        return resp ;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SuggestOracle.Response getGenedomAndEFGSuggestions(SuggestOracle.Request req, boolean wildCard){
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery() ;
        SessionCreator.instantiateDBForHostedMode() ;


        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if(query.length()>0){
            MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
            List<Marker> markers = markerRepository.getMarkersByAbbreviationAndGroup(query, Marker.TypeGroup.GENEDOM_AND_EFG);
            for(Marker marker: markers){
                suggestions.add(new ItemSuggestion(marker.getAbbreviation().replaceAll(query,"<strong>"+query+"</strong>"),marker.getAbbreviation())) ;
            }
        }
        if(wildCard==true){
            suggestions.add(0,new ItemSuggestion("*"+query+"*",null)) ;
        }
        resp.setSuggestions(suggestions);

        logger.info("returned with no error: "+ req + " "  +  suggestions.size() + " suggestions ");

        return resp ;
    }

    public SuggestOracle.Response getFeatureSuggestions(SuggestOracle.Request req, boolean wildCard) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery() ;

        if(query.equals("xxx333")){
            throw new RuntimeException("this is a test error") ;
        }

        SessionCreator.instantiateDBForHostedMode() ;


        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        if(query.length()>0){
            for(Feature feature: RepositoryFactory.getMutantRepository().getFeaturesByAbbreviation(query) ){
                suggestions.add(new ItemSuggestion(
                        feature.getAbbreviation().replaceAll(query.replace("(","\\(").replace(")","\\)"),"<strong>"+query+"</strong>"),feature.getAbbreviation())) ;
            }
        }
        if(wildCard==true){
            suggestions.add(0,new ItemSuggestion("*"+query+"*",null)) ;
        }
        resp.setSuggestions(suggestions);

        logger.info("returned with no error: "+ req + " "  +  suggestions.size() + " suggestions ");

        return resp ;  //To change body of implemented methods use File | Settings | File Templates.
    }
}


