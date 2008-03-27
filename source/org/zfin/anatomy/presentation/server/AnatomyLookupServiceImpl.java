package org.zfin.anatomy.presentation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.zfin.anatomy.presentation.client.AnatomyLookupService;
import org.zfin.anatomy.presentation.client.ItemSuggestion;
import org.zfin.anatomy.presentation.SortAnatomySearchTerm;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


/**
 *
 */
public class AnatomyLookupServiceImpl
        extends RemoteServiceServlet
        implements AnatomyLookupService {

    private transient AnatomyRepository ar = RepositoryFactory.getAnatomyRepository() ;

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


        List<SuggestOracle.Suggestion> suggestions = new ArrayList<SuggestOracle.Suggestion>();
        List<AnatomyItem> anatomyItems = new ArrayList<AnatomyItem>(); 
        if(query.length()>2){
            anatomyItems = ar.getAnatomyItemsByName(query,false) ;
            Collections.sort(anatomyItems, new SortAnatomySearchTerm(query));
        }
        suggestions.add(new ItemSuggestion("*"+query+"*",null)) ;
        for(AnatomyItem anatomyItem : anatomyItems){
            String term = anatomyItem.getName() ;
            suggestions.add(new ItemSuggestion(term.replaceAll(query,"<strong>"+query+"</strong>"),term)) ;
        }

        resp.setSuggestions(suggestions);



        return resp ;  //To change body of implemented methods use File | Settings | File Templates.
    }
}


