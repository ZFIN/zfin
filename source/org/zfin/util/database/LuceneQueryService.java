package org.zfin.util.database;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Service class to support Lucene Queries
 */
public class LuceneQueryService {

    // current lucene index
    private String indexDirectory;
    private Analyzer analyzer = new KeywordAnalyzer();
    private DirectoryReader reader;
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
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory)));
            isInitialized = true;
        } catch (Exception e) {
            LOG.warn("Ignoring. Make sure to re-initialize later.", e);
            createIndexFile();
        }
    }

    private void createIndexFile() {
        IndexWriter writer = null;
        try {
            FSDirectory directory = FSDirectory.open(Paths.get(indexDirectory));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            writer = new IndexWriter(directory, config);
            writer.commit();
            reader = DirectoryReader.open(directory);
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
            DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
            if (newReader != null) {
                reader.close();
                reader = newReader;
            }
        } catch (IOException e) {
            LOG.warn(e);
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public TopDocs getHitsFromAndQuery(Map<String, String> queryProperties) {
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
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
            TopDocs hits = searcher.search(query, 10000);
            LOG.info(hits.totalHits.value + " hits found");
            return hits;
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }

    }

    public Query addAndClauseToQuery(String fieldName, String fieldValue, Query query) {

        BooleanQuery.Builder prefixQueryBuilder = new BooleanQuery.Builder();
        if (analyzer == null) {
            throw new RuntimeException("Analyzer is null");
        }
        if (query == null) {
            throw new RuntimeException("query is null");
        }

        TokenStream tokenStream = analyzer.tokenStream(fieldName, new StringReader(fieldValue));

        if (tokenStream == null)
            throw new RuntimeException("tokenStream is null");

        try {
            CharTermAttribute termAtt = tokenStream.getAttribute(CharTermAttribute.class);
            tokenStream.reset();
            if (tokenStream.incrementToken()) {
                String text = termAtt.toString();
                TermQuery termQuery = new TermQuery(new Term(fieldName, text));
                prefixQueryBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
            }
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BooleanQuery.Builder fullQueryBuilder = new BooleanQuery.Builder();
        fullQueryBuilder.add(prefixQueryBuilder.build(), BooleanClause.Occur.MUST);
        fullQueryBuilder.add(query, BooleanClause.Occur.MUST);

        return fullQueryBuilder.build();
    }


    public TopDocs getHitsFromStartsWith(Map<String, String> queryProperties) {
        if (!isInitialized())
            return null;
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
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
            TopDocs hits = searcher.search(query, 10000);
            LOG.info(hits.totalHits.value + " hits found");
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
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        TokenStream tokenStream = analyzer.tokenStream(fieldName, new StringReader(queryString));
        try {
            CharTermAttribute termAtt = tokenStream.getAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String text = termAtt.toString();
                queryBuilder.add(new PrefixQuery(new Term(fieldName, text)), BooleanClause.Occur.MUST);
            }
            tokenStream.end();
        } finally {
            tokenStream.close();
        }
        return queryBuilder.build();
    }

    public void reLoadIndex() throws IOException {
        DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
        if (newReader != null) {
            reader.close();
            reader = newReader;
        }
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

    public DirectoryReader getIndexReader() {
        return reader;
    }
}
