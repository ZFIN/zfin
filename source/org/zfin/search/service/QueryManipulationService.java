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
        query = processSequence(query);
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

    public String processSequence(String query) {
        char first=query.charAt(0);
        if((Character.isUpperCase(first))&&(query.contains("."))) {
            //this allows field specific searching on gene fields like affected_gene or misexpressed_gene

            return query.split("\\.", 2)[0];
        }
        else{
            return query;
        }

    }
    //Case 12299, '(-' in construct names needs to be scaped
    public String processConstructDash(String query) {
        if (query.startsWith("Tg")) {
            return query.replaceAll("\\(-", "\\(\\\\-");
        } else { return query; }

    }

}
