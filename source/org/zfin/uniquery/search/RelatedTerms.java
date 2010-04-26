package org.zfin.uniquery.search;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.uniquery.SiteSearchService;
import org.zfin.uniquery.ZfinAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelatedTerms {

    private static final Logger LOG = Logger.getLogger(RelatedTerms.class);

    /**
     * Searches anatomy tokens for a match of a given query string.
     * Returns results {token=hits} as a Hashtable,
     * where hits is an ArrayList.
     *
     * @param queryString string
     * @return map
     */
    public Map<String, List<String>> getAllAnatomyHits(String queryString) {
        Map<String, List<String>> results = new HashMap<String, List<String>>();

        List<String> tokens = getTokens(queryString);
        for (String token : tokens) {
            List<String> anatomyHits = getAnatomyHits(token);
            if (!anatomyHits.isEmpty()) {
                token = token.replace("''", "'");
                results.put(token, anatomyHits);
            }
        }
        return results;
    }


    /**
     * Searches anatomy tokens for a match of a given token.
     * Returns zdb_ids as an ArrayList.
     *
     * @param token string
     * @return list
     */
    public List<String> getAnatomyHits(String token) {

        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        return infrastructureRepository.getAnatomyTokens(token);
    }

    /**
     * Search all_map_names and anatomy_item tables for exact name/symbol match
     * on markers/clones/genes/mutants/anatomy terms. We could only use base
     * tables, may update when Fish tables consolidated.
     * Return zdb id if match, otherwise empty string.
     *
     * @param queryTerm string
     * @return string
     */
    public static String getBestMatchId(String queryTerm) {

        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        List<String> list = infrastructureRepository.getBestNameMatch(queryTerm);
        if (list == null || list.size() == 0)
            return "";

        return list.get(0);
    }

    /**
     * Searches alias tokens for a match of a given query string.
     * Returns results {token=hits} as a Hashtable,
     * where hits is an ArrayList.
     *
     * @param queryString query string
     * @return map
     */
    public HashMap<String, List<String[]>> getAllAliasHits(String queryString) {

        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        List<String> aliases = infrastructureRepository.getDataAliasesWithAbbreviation(queryString);
        List<String> validAliases = new ArrayList<String>(2);

        for (String alias : aliases) {
            // do not add aliases that are zdb Ids, they would not lead to a proper alternative search
            if (!alias.startsWith("ZDB-")) {
                // do not add alias if it is the same string ignoring the case
                if (!queryString.toLowerCase().equals(alias)) {
                    // check if the new alias has actual hits to offer...
                    if (SiteSearchService.hasHits(alias))
                        validAliases.add(alias);
                }
            }

        }
        if (validAliases.size() == 0)
            return null;

        List<String[]> abbrevHits = new ArrayList<String[]>();
        for (String alias : validAliases) {
            String[] hit = {alias, queryString.toLowerCase()};
            abbrevHits.add(hit);
        }

        HashMap<String, List<String[]>> results = new HashMap<String, List<String[]>>();
        if (!abbrevHits.isEmpty()) {
            results.put(queryString, abbrevHits);
        }

        return results;
    }

    /**
     * Parses query string and returns tokens as an ArrayList.
     *
     * @param queryString query string
     * @return list
     */
    public ArrayList<String> getTokens(String queryString) {
        queryString = filterIllegals(queryString);
        ArrayList<String> results = new ArrayList<String>();
        Analyzer analyzer = new ZfinAnalyzer();
        String field = "body";
        if (queryString == null) {
            return results;
        }
        TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(queryString));
        try {
            Token token;
            while ((token = tokenStream.next()) != null) {
                String tokenText = new String(token.termBuffer(), 0, token.termLength());
                if (!results.contains(tokenText)) {
                    results.add(tokenText);
                }
            }
        } catch (IOException e) {
            LOG.error(e);
            return null;
        }
        results.trimToSize();
        //Collections.sort(results);  /* is sorting query tokens necessary ?  No? */
        return results;
    }


    /**
     * Parses test string and removes illegal characters (for database query.)
     *
     * @param text test string
     * @return string
     */
    public String filterIllegals(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("'", "''");
        text = text.replaceAll("<sup>", " ");
        text = text.replaceAll("</sup>", " ");
        return text;
    }

}

