package org.zfin.util.database;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

/**
 * Service class to support Lucene Queries
 */
public class LuceneQueryService {

    // current lucene index
    private String indexDirectory;
    private Analyzer analyzer = new KeywordAnalyzer();
    private IndexReader reader;
    boolean isInitialized = false;

    static {
        BooleanQuery.setMaxClauseCount(200000);
    }

    private static Logger LOG = LogManager.getLogger(LuceneQueryService.class);

    public LuceneQueryService(String indexDirectory) {
        this.indexDirectory = indexDirectory;
        initSummary();
    }

    protected void initSummary() {
        if (indexDirectory == null) {
            LOG.warn("No indexDirectory provided. Class is not initialized properly");
            return;
        }
        try {
            File indexDir = new File(indexDirectory);
            if (!indexDir.exists()) {
                LOG.warn("indexDirectory " + indexDirectory + " not found. Class is not initialized properly");
                return;
            }
            reader = IndexReader.open(indexDir);
            isInitialized = true;
        } catch (Exception e) {
            LOG.warn("Ignoring. Make sure to re-initialize later.", e);
            createIndexFile();
        }
    }

    private void createIndexFile() {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(indexDirectory, analyzer, true);
            reader = IndexReader.open(indexDirectory);
            isInitialized = true;
        } catch (IOException e) {
            LOG.error(e);
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void reopenIndex() {
        try {
            reader.reopen();
        } catch (IOException e) {
            LOG.warn(e);
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Hits getHitsFromAndQuery(Map<String, String> queryProperties) {
        try {
            Searcher searcher = new IndexSearcher(reader);
            Query query = null;
            int index = 0;
            for (Map.Entry<String, String> entry : queryProperties.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue().trim();
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
            for (Map.Entry<String, String> entry : queryProperties.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue().trim();
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

    // cleanup all handles to index files.
    protected void close() throws Throwable {
        reader.close();
    }

    public void changeIndex(String fullPathMatchingIndexDirectory) {

        try {
            close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        this.indexDirectory = fullPathMatchingIndexDirectory;
        initSummary();

    }

    public IndexReader getIndexReader() {
        return reader;
    }
}
