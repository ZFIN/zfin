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
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.zfin.uniquery.categories.SiteSearchCategories;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class for site search.
 */
public class SiteSearchTest {

    private String[] title = {"Gene", "Antibodies"};
    private String[] text = {"Gene clew is expressed in the eye and ear.",
            "Antibody ab-clew is use in immunohistochemistry, wer.antibody"};

    private IndexWriter index;
    private RAMDirectory ramDirectory;

    @Before
    public void setup() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        ramDirectory = new RAMDirectory();
        index = new IndexWriter(ramDirectory, analyzer, true);
    }

    @Test
    public void indexText() throws IOException, ParseException {

        createIndex();
        String queryString = "Antibody";
        IndexReader reader = null;
        Searcher searcher = null;
        try {
            reader = IndexReader.open(ramDirectory);
            searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("body", analyzer);
            Query query = parser.parse(queryString);

            //        BooleanQuery query = new BooleanQuery();
            //      query.add(new PrefixQuery(new Term("title", queryString)), BooleanClause.Occur.SHOULD);

            Hits hits = searcher.search(query);
            assertNotNull(hits);
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (searcher != null) {
                searcher.close();
            }
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

    @Test
    public void testGetDocMethod() {
        File file = new File("home", "WEB-INF");
        File categoryFile = new File(file, "conf");
        SiteSearchCategories.init(categoryFile.getAbsolutePath(), "site-search-categories.xml");
        String docType = SiteSearchCategories.getDocType("http://localhost/ZDB-GENE-070117-46");
        assertEquals("GeneView", docType);
        docType = SiteSearchCategories.getDocType("http://localhost/ZDB-CDNA-040425-396");
        assertEquals("MarkerView", docType);
        docType = SiteSearchCategories.getDocType("http://localhost/ZDB-ATB-081002-20");
        assertEquals("AntibodyView", docType);
        docType = SiteSearchCategories.getDocType("http://localhost/action/marker/sequence/view/ZDB-GENE-990415-72");
        assertEquals("GeneSequence", docType);
    }

    @Test
    public void checkGetUri() {
        String url = "http://localhost/action/marker/sequence/view/ZDB-GENE-990415-72";
        assertEquals("/action/marker/sequence/view/ZDB-GENE-990415-72", SiteSearchCategories.getUri(url));
        url = "HTTPS://credo.zfin.org:8080/action/marker/sequence/view/ZDB-GENE-990415-72";
        assertEquals("/action/marker/sequence/view/ZDB-GENE-990415-72", SiteSearchCategories.getUri(url));
    }

    private void createIndex() throws IOException {
        for (int i = 0; i < title.length; i++) {
            Document doc = new Document();

            doc.add(new Field("url", "/", Field.Store.YES, Field.Index.TOKENIZED)); // store relative URLs

            doc.add(new Field("title", title[i], Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field("body", text[i], Field.Store.YES, Field.Index.TOKENIZED));

            // index the document (the results of parsing URL)
            index.addDocument(doc);
        }
        index.optimize();
        index.close();
    }

}
