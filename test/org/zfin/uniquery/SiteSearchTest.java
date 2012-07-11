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
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Test class for site search.
 */
public class SiteSearchTest {

    private String[] title = {"Gene", "Antibodies"};
    private String[] text = {"Gene clew is expressed in the eye and ear.",
            "Antibody ab-clew is use in immunohistochemistry, wer.antibody"};

    private IndexWriter index;
    private RAMDirectory ramDirectory;

    @Test
    public void indexText() {

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
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        } catch (ParseException pe) {
            pe.printStackTrace();
            fail();
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
    }

    private void createIndex() {
        for (int i = 0; i < title.length; i++) {
            Document doc = new Document();

            doc.add(new Field("url", "/action/marker/view", Field.Store.YES, Field.Index.TOKENIZED)); // store relative URLs

            doc.add(new Field("title", title[i], Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field("body", text[i], Field.Store.YES, Field.Index.TOKENIZED));

            try {
                // index the document (the results of parsing URL)
                index.addDocument(doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            index.optimize();
            index.close();
        } catch (IOException e) {
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
    public void setup() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        ramDirectory = new RAMDirectory();
        index = new IndexWriter(ramDirectory, analyzer, true);

    }

    @Test
    public void fancyExcludeDefinition() {
//        Pattern  pattern = Pattern.compile("\\p{ASCII}*marker/view\\p{ASCII}*ZDB-(!GENE|!ATB|\\p{ASCII}*)-\\p{ASCII}*") ;
        Pattern pattern = Pattern.compile("marker/view/ZDB-(?!(GENE|ATB))");
//        Pattern  pattern = Pattern.compile("marker/view") ;
        assertFalse(pattern.matcher("http://localhost/action/marker/view/ZDB-GENE-040425-396").find());
        assertFalse(pattern.matcher("http://localhost/action/marker/view/ZDB-ATB-040425-396").find());
        assertTrue(pattern.matcher("http://localhost/action/marker/view/ZDB-CDNA-040425-396").find());
    }

    @Test
    public void testGetDocMethod() {
        File file = new File("home", "WEB-INF");
        File categoryFile = new File(file, "conf");
        SiteSearchCategories.init(categoryFile.getAbsolutePath(), "site-search-categories.xml");
        String docType = SiteSearchCategories.getDocType("http://localhost/action/marker/view/ZDB-GENE-070117-46");
        assertEquals("GeneView", docType);
        docType = SiteSearchCategories.getDocType("http://localhost/action/marker/view/ZDB-CDNA-040425-396");
        assertEquals("MarkerView", docType);
        docType = SiteSearchCategories.getDocType("http://localhost/action/marker/view/ZDB-ATB-081002-20");
        assertEquals("AntibodyView", docType);
    }

}
