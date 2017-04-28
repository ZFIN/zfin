package org.zfin.search.service;


import org.springframework.stereotype.Service;

@Service
public class QueryManipulationService {

    public String processQueryString(String query) {
        if (query == null)
            return null;
        query = processIdIbd(query);
        query = processNcbiAccession(query);
        query = processConstructDash(query);
        query = processZfinPrefix(query);

        return query;
    }

    //Case 11289, id:ibd will search in the id field, escaping as id\: for only id:ibd
    public String processIdIbd(String query) {
        return query.replaceAll("id:ibd","id\\\\:ibd");
    }

    //Case
    public String processNcbiAccession(String query) {

        //this allows field specific searching on gene fields like affected_gene or misexpressed_gene
        if (query.contains("_gene")) { return query; }

        return query.replaceAll("gene:"," gene\\\\:");
    }

    
    //Case 12299, '(-' in construct names needs to be scaped
    public String processConstructDash(String query) {
        if (query.startsWith("Tg")) {
            return query.replaceAll("\\(-", "\\(\\\\-");
        } else { return query; }

    }

    //Case INF-2933
    public String processZfinPrefix(String query) { return query.replaceAll("ZFIN:", ""); }

}
