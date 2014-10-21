package org.zfin.search.service;


import org.springframework.stereotype.Service;

@Service
public class QueryManipulationService {

    public String processQueryString(String query) {
        if (query == null)
            return null;
        query = processIdIbd(query);
        return query;
    }

    //Case 11289, id:ibd will search in the id field, escaping as id\: for only id:ibd
    public String processIdIbd(String query) {
        return query.replaceAll("id:ibd","id\\\\:ibd");
    }

}
