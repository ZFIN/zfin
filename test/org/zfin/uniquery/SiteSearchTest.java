package org.zfin.uniquery;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test class for site search.
 */
public class SiteSearchTest {

    private String[] title = {"Gene", "Antibodies"};
    private String[] text = {"Gene clew is expressed in the eye and ear.",
            "Antibody ab-clew is use in immunohistochemistry, wer.antibody"};
    private IndexWriter index;
    String indexDir = "indexer-test";
    File indexDirectory = new File(indexDir);

    FSDirectory fsDirectory;

    @Test
    public void IndexText() {
        createIndex();
        // search Index

        String queryString = "Antibody";
        Hits hits;
        IndexReader reader = null;
        Searcher searcher = null;
        try {
            reader = IndexReader.open(fsDirectory);
            searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("body", analyzer);
            Query query = parser.parse(queryString);

            //        BooleanQuery query = new BooleanQuery();
            //      query.add(new PrefixQuery(new Term("title", queryString)), BooleanClause.Occur.SHOULD);

            hits = searcher.search(query);

            System.out.println("End of search");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException pe) {
            pe.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (searcher != null)
                    searcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("End of search");


    }

    private void createIndex() {
        for (int i = 0; i < title.length; i++) {
            Document doc = new Document();

            doc.add(new Field("url", "webdriver?MIVAL=markerview.apg", Field.Store.YES, Field.Index.TOKENIZED)); // store relative URLs

            doc.add(new Field("title", title[i], Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field("body", text[i], Field.Store.YES, Field.Index.TOKENIZED));

            try {
                // index the document (the results of parsing URL)
                index.addDocument(doc);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            index.optimize();
            index.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();

        // Store the index in memory:
        Directory directory = new RAMDirectory();
        // To store an index on disk, use this instead:
        //Directory directory = FSDirectory.getDirectory("/tmp/testindex");
        IndexWriter iwriter = new IndexWriter(directory, analyzer, true);
        iwriter.setMaxFieldLength(25000);
        Document doc = new Document();
        String text = "This is the text to be indexed.";
        doc.add(new Field("fieldname", text, Field.Store.YES,
                Field.Index.TOKENIZED));
        iwriter.addDocument(doc);
        iwriter.optimize();
        iwriter.close();

        // Now search the index:
        IndexSearcher isearcher = new IndexSearcher(directory);
        // Parse a simple query that searches for "text":
        QueryParser parser = new QueryParser("fieldname", analyzer);
        Query query = parser.parse("text");
        Hits hits = isearcher.search(query);
        assertEquals(1, hits.length());
        // Iterate through the results:
        for (int i = 0; i < hits.length(); i++) {
            Document hitDoc = hits.doc(i);
            assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
        }
        isearcher.close();
        directory.close();
    }


    @Before
    public void before() throws Exception {
        indexDirectory.deleteOnExit();
        fsDirectory = FSDirectory.getDirectory(indexDirectory);
        FSDirectory.setDisableLocks(true);

        if (!indexDirectory.exists()) {
            boolean success = indexDirectory.mkdir();
            if (!success)
                System.out.println("Could not create indexer directory");
        }

        try {
            index = new IndexWriter(new File(indexDir), new StandardAnalyzer(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @After
    public void after() {
        boolean success = indexDirectory.delete();
        if (!success)
            System.out.println("Could not delete indexer directory");
    }

}
