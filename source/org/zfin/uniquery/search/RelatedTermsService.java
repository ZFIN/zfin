package org.zfin.uniquery.search;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.ontology.MatchingTerm;
import org.zfin.ontology.MatchingTermService;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.repository.RepositoryFactory;
import org.zfin.uniquery.SiteSearchService;
import org.zfin.uniquery.ZfinAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Service
public class RelatedTermsService {

    private static final Logger LOG = Logger.getLogger(RelatedTermsService.class);
    @Autowired
    private SiteSearchService siteSearchService;

    /**
     * Searches ontology terms for matching tokens.
     * It returns all tokens in a list if at least one term has all tokens in its name.
     *
     * @param queryString string
     * @return map
     */
    public List<String> getAllAnatomyHits(String queryString) {
        List<String> results = new ArrayList<String>(2);

        List<String> tokens = getTokens(queryString);
        boolean firstRecord = true;
        Set<TermDTO> terms = new HashSet<TermDTO>();
        for (String token : tokens) {
            if (firstRecord) {
                MatchingTermService matcher = new MatchingTermService(-1);
                for (MatchingTerm term : matcher.getMatchingTerms(token, Ontology.ANATOMY)) {
                    terms.add(term.getTerm());
                }
                firstRecord = false;
            } else {
                Iterator<TermDTO> iterator = terms.iterator();
                while (iterator.hasNext()) {
                    if (!iterator.next().getTermName().toLowerCase().contains(token.toLowerCase()))
                        iterator.remove();
                }
            }
            if (!terms.isEmpty()) {
                token = token.replace("''", "'");
                results.add(token);
            } else
                return null;
        }
        return results;
    }


    /**
     * Search all_map_names and term tables for exact name/symbol match
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
                    if (siteSearchService.hasHits(alias))
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

