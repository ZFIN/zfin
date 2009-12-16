package org.zfin.sequence;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostForeignTest {

    private final Logger logger = Logger.getLogger(PostForeignTest.class) ;

    private final static String SEARCH_TEXT = "ABCD" ;

    @Test
    public void queryToNCBI(){
        try {
            // Send data
            URL url = new URL("http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?PAGE=MegaBlast&PROGRAM=blastn&BLAST_PROGRAMS=megaBlast&PAGE_TYPE=BlastSearch&SHOW_DEFAULTS=on&DBSEARCH=true&QUERY="+
                    SEARCH_TEXT);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            String webpageText = "" ;
            while ((line = rd.readLine()) != null) {
                webpageText += line ;
            }
            String findString ="<textarea id=\"seq\" class=\"reset\" rows=\"5\" cols=\"80\" name=\"QUERY\" >";
            int index = webpageText.indexOf(findString) ;
            wr.close();
            rd.close();

            assertTrue("findString exists",index > 100) ;
            String matchWord = webpageText.substring(index+findString.length(),index+findString.length()+ SEARCH_TEXT.length()) ;
            assertEquals("match word", SEARCH_TEXT,matchWord) ;
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString()) ;
        }

    }


    @Test
    public void postToNCBI(){
        try {
            // Construct data
            String data = URLEncoder.encode("QUERY", "UTF-8") + "=" + URLEncoder.encode(SEARCH_TEXT, "UTF-8");
            data += "&" + URLEncoder.encode("PAGE", "UTF-8") + "=" + URLEncoder.encode("MegaBlast", "UTF-8");
            data += "&" + URLEncoder.encode("PROGRAM", "UTF-8") + "=" + URLEncoder.encode("blastn", "UTF-8");
            data += "&" + URLEncoder.encode("BLAST_PROGRAMS", "UTF-8") + "=" + URLEncoder.encode("megaBlast", "UTF-8");
            data += "&" + URLEncoder.encode("PAGE_TYPE", "UTF-8") + "=" + URLEncoder.encode("BlastSearch", "UTF-8");
            data += "&" + URLEncoder.encode("SHOW_DEFAULTS", "UTF-8") + "=" + URLEncoder.encode("on", "UTF-8");
            data += "&" + URLEncoder.encode("DBSEARCH", "UTF-8") + "=" + URLEncoder.encode("true", "UTF-8");

            // Send data
            URL url = new URL("http://www.ncbi.nlm.nih.gov/blast/Blast.cgi");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            String webpageText = "" ;
            while ((line = rd.readLine()) != null) {
                webpageText += line ;
            }
            String findString ="<textarea id=\"seq\" class=\"reset\" rows=\"5\" cols=\"80\" name=\"QUERY\" >";
            int index = webpageText.indexOf(findString) ;

            wr.close();
            rd.close();

            assertTrue("findString exists",index > 100) ;
            String matchWord = webpageText.substring(index+findString.length(),index+findString.length()+ SEARCH_TEXT.length()) ;
            assertEquals("match word", SEARCH_TEXT,matchWord) ;
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }

    }

}
