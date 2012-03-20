package org.zfin.util.database;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

/**
 * Service class to support Lucene Queries
 */
public class LuceneQueryService {


    private String indexDirectory;
    private Analyzer analyzer = new KeywordAnalyzer();
    private IndexReader reader;
    boolean isInitialized = false;

    private static Logger LOG = Logger.getLogger(LuceneQueryService.class);

    public LuceneQueryService(String indexDirectory) {
        this.indexDirectory = indexDirectory;
        initSummary();
    }

    protected void initSummary() {
        try {
            reader = IndexReader.open(indexDirectory);
            isInitialized = true;
        } catch (Exception e) {
            LOG.warn("Ignoring. Make sure to re-initialize later.", e);
            createIndexFile();
        }
    }

    private void createIndexFile() {
        try {
            IndexWriter writer = new IndexWriter(indexDirectory, analyzer, true);
            reader = IndexReader.open(indexDirectory);
            isInitialized = true;
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    public void reopenIndex() {
        try {
            reader.reopen();
        } catch (IOException e) {
            LOG.warn(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Hits getHitsFromAndQuery(Map<String, String> queryProperties) {
        try {
            IndexReader reader = IndexReader.open(indexDirectory);
            Searcher searcher = new IndexSearcher(reader);
            Query query = null;
            int index = 0;
            for (String fieldName : queryProperties.keySet()) {
                String fieldValue = queryProperties.get(fieldName).trim();
                if (index++ == 0)
                    query = new TermQuery(new Term(fieldName, fieldValue));
                else
                    query = addAndClauseToQuery(fieldName, fieldValue, query);
            }
            Hits hits = searcher.search(query);
            LOG.info(hits.length() + " hits found");
            return hits;
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }

    }

    public Query addAndClauseToQuery(String fieldName, String fieldValue, Query query) {

        BooleanQuery prefixQuery = new BooleanQuery();
        if (analyzer == null) {
            throw new RuntimeException("Analyzer is null");
        }
        if (query == null) {
            throw new RuntimeException("query is null");
        }

        TokenStream tokenStream = analyzer.tokenStream(fieldName, new StringReader(fieldValue));

        if (tokenStream == null)
            throw new RuntimeException("tokenStream is null");
        Token token;
        try {
            token = tokenStream.next();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (token != null) {
            TermQuery termQuery = new TermQuery(new Term(fieldName, new String(token.termBuffer(), 0, token.termLength())));
            prefixQuery.add(termQuery, BooleanClause.Occur.SHOULD);
        }

        BooleanQuery fullQuery = new BooleanQuery();
        fullQuery.add(prefixQuery, BooleanClause.Occur.MUST);
        fullQuery.add(query, BooleanClause.Occur.MUST);

        return fullQuery;
    }


    public Hits getHitsFromStartsWith(Map<String, String> queryProperties) {
        if (!isInitialized())
            return null;
        try {
            IndexReader reader = IndexReader.open(indexDirectory);
            Searcher searcher = new IndexSearcher(reader);
            Query query = null;
            int index = 0;
            for (String fieldName : queryProperties.keySet()) {
                String fieldValue = queryProperties.get(fieldName).trim();
                if (index++ == 0)
                    query = getStartsWithQuery(fieldName, fieldValue);
                else
                    query = addAndClauseToQuery(fieldName, fieldValue, query);
            }
            Hits hits = searcher.search(query);
            LOG.info(hits.length() + " hits found");
            return hits;
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }

    }

    public int getNumberOfDocuments() {
        return reader.numDocs();
    }

    private Query getStartsWithQuery(String fieldName, String queryString) throws IOException {
        BooleanQuery query = new BooleanQuery();
        TokenStream tokenStream = analyzer.tokenStream(fieldName, new StringReader(queryString));
        Token tok;
        while ((tok = tokenStream.next()) != null) {
            query.add(new PrefixQuery(new Term(fieldName, new String(tok.termBuffer(), 0, tok.termLength()))), BooleanClause.Occur.MUST);
        }
        return query;
    }

    public void reLoadIndex() throws IOException {
        reader.reopen();
    }

    public String getIndexDirectory() {
        return indexDirectory;
    }
}
