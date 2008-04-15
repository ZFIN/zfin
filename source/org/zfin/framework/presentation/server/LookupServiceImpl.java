package org.zfin.framework.presentation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.framework.presentation.client.LookupService;
import org.zfin.framework.presentation.client.ItemSuggestion;
import org.zfin.anatomy.presentation.SortAnatomySearchTerm;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomySynonym;
import org.zfin.repository.RepositoryFactory;
import org.zfin.repository.SessionCreator;
import org.apache.log4j.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.geneontology.oboedit.datamodel.Synonym;

import java.util.*;


/**
 *
 */
public class LookupServiceImpl
        extends RemoteServiceServlet
        implements LookupService {

    private transient AnatomyRepository ar = RepositoryFactory.getAnatomyRepository() ;
    private transient Logger logger = Logger.getLogger(LookupServiceImpl.class) ;



    /**  Gets suggestions from the anatomy repository.  
     * Note that we do not use limits on the request for this implementation.
     *
     */
    public SuggestOracle.Response getSuggestions(SuggestOracle.Request req) {
        SuggestOracle.Response resp = new SuggestOracle.Response();
        String query = req.getQuery() ;

        if(query.equals("xxx333")){
            throw new RuntimeException("this is a test error") ; 
        }

        SessionCreator.instantiateDBForHostedMode() ; 


        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        List<AnatomyItem> anatomyItems = new ArrayList<AnatomyItem>();
        if(query.length()>2){
            anatomyItems = ar.getAnatomyItemsByName(query,false) ;
            Collections.sort(anatomyItems, new SortAnatomySearchTerm(query));
        }
        suggestions.add(new ItemSuggestion("*"+query+"*",null)) ;
        for(AnatomyItem anatomyItem : anatomyItems){
            String term = anatomyItem.getName() ;
            String suggestion = new String(term) ;

            Set<AnatomySynonym> synonyms = anatomyItem.getSynonyms( ) ;
            if(synonyms!=null){
                Iterator<AnatomySynonym> synonymIterator = synonyms.iterator() ;
                boolean notFound = false ;
                while(synonymIterator.hasNext() && notFound==false){
                    AnatomySynonym anatomySynonym = synonymIterator.next() ;
                    if( anatomySynonym.getName().contains(query)){
                       suggestion +=  " ["+ anatomySynonym.getName() +"]"  ;
                       notFound = true ;
                    }
                }
            }
            suggestions.add(new ItemSuggestion(suggestion.replaceAll(query,"<strong>"+query+"</strong>"),term)) ;
        }

        resp.setSuggestions(suggestions);



        return resp ;  //To change body of implemented methods use File | Settings | File Templates.
    }
}


