package org.zfin.uniquery.search;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.zfin.uniquery.SearchCategory;
import org.zfin.uniquery.ZfinAnalyzer;
import org.zfin.uniquery.presentation.SearchBean;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


/**
 * For each category (such as Genes/Markers/Clones),
 * we store the list of hits in that category.
 */
public class CategoryHits {
    private SearchCategory category; // a given category
    private Hits hits; // the hits related to the given category
    private List<Hit> hitsAsHTML; // the list of hits transformed into the HTML String

    public CategoryHits(SearchCategory category, Hits hits, Query query, int max_hits) throws Exception {

        this.category = category;
        this.hits = hits;
        ZfinAnalyzer analyzer = new ZfinAnalyzer();
        hitsAsHTML = new ArrayList<Hit>();
        Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(), new QueryScorer(query));

        /*
        * For all the hits related to this category, we iterate and transform those
        * hits objects into the HTML string that will be displayed on the screen.
        * We use the highlighter and query term to provide additional string formatting.
        */
        for (int i = 0; i < (hits.length() > max_hits ? max_hits : hits.length()); i++) {
            String text = hits.doc(i).get(SearchBean.BODY);
            String highlightedText = "";
            if (text != null) {
                TokenStream tokenStream = analyzer.tokenStream(SearchBean.BODY, new StringReader(text));
                highlightedText = highlighter.getBestFragments(tokenStream, text, 3, "<b> ... </b>");
                /* the next line is a RegEx replacement that means: 
                 * replace all occurrances of non-word characters except "<" at the beginning of a line 
                 */
                highlightedText = highlightedText.replaceFirst("^[^\\w<]*", "<b>... </b>");
                highlightedText = highlightedText.concat("<b> ...</b>");
            }

            // add the generated HTML string to the hitsAsHTML list
            this.hitsAsHTML.add(new Hit(hits.doc(i), hits.score(i), highlightedText));
        }
    }

    public SearchCategory getCategory() {
        return category;
    }

    public Hits getHits() {
        return hits;
    }

    public List<Hit> getHitsAsHTML() {
        return hitsAsHTML;
    }


}