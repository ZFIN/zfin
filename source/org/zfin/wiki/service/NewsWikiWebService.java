package org.zfin.wiki.service;

import org.springframework.stereotype.Service;
import org.zfin.wiki.RemoteBlogEntrySummary;
import org.zfin.wiki.WikiLoginException;

/**
 */
@Service
public class NewsWikiWebService extends WikiWebService{

    public NewsWikiWebService() throws WikiLoginException{
    }

    // TODO: a good place to use AOP
    public RemoteBlogEntrySummary[] getNewsForSpace(String space) throws Exception{
        login();
        RemoteBlogEntrySummary[] summaries = service.getBlogEntries(token, space) ;
        return  summaries ;
    }

}
