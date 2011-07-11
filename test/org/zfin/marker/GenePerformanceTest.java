package org.zfin.marker;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.zfin.AbstractUnitSmokeTest;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;

/**
 * Genes from:
 * http://fogbugz.zfin.org/default.asp?6887
 */
public class GenePerformanceTest extends AbstractUnitSmokeTest {

    private WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
    private Logger logger = Logger.getLogger(GenePerformanceTest.class) ;
    private List<String> avgGenes;
    private List<String> worstGenesOld;
    private List<String> worstGenesNew;
    private List<String> worstGenesDalias;
    private List<String> worstGenesDblink;
    private List<String> worstGenesFmrel;
    private List<String> worstGenesGO;
    private List<String> worstGenesMrel1;
    private List<String> worstGenesMrel2;
    private List<String> worstGenesOrtho;
    private List<String> worstGenesPhenoAnats;
    private List<String> worstGenesPhenoFigur;
    private List<String> worstGenesRecattrib;
    private List<String> worstGenesXpat;
    String javaPageUrl = "/action/marker/view/";
    final int maxSize = 200  ;

    public GenePerformanceTest() throws Exception{
        avgGenes = createList("test/gene-data/avg-genes.txt", 141);
        worstGenesOld = createList("test/gene-data/worst-genes-old.txt", 17);
        worstGenesNew = createList("test/gene-data/worst-genes-new.txt", 194);
        worstGenesDalias = createList("test/gene-data/worstGenesDalias.txt", 7);
        worstGenesDblink = createList("test/gene-data/worstGenesDblink.txt", 54);
        worstGenesFmrel = createList("test/gene-data/worstGenesFmrel.txt", 6);
        worstGenesGO = createList("test/gene-data/worstGenesGO.txt", 20);
        worstGenesMrel1 = createList("test/gene-data/worstGenesMrel1.txt", 37);
        worstGenesMrel2 = createList("test/gene-data/worstGenesMrel2.txt", 1);
        worstGenesOrtho = createList("test/gene-data/worstGenesOrtho.txt", 51);
        worstGenesPhenoAnats = createList("test/gene-data/worstGenesPhenoAnats.txt", 3);
        worstGenesPhenoFigur = createList("test/gene-data/worstGenesPhenoFigur.txt", 6);
        worstGenesRecattrib = createList("test/gene-data/worstGenesRecattrib.txt", 5);
        worstGenesXpat = createList("test/gene-data/worstGenesXpat.txt", 4);
    }

    private List<String> createList(String fileString, int i) throws  Exception{
        List<String> averageGenesFull = IOUtils.readLines(new FileInputStream(new File(fileString)));
        assertEquals(i,averageGenesFull.size());
        Collections.shuffle(averageGenesFull);
        return averageGenesFull.subList(0,averageGenesFull.size()>maxSize ? maxSize : averageGenesFull.size());
    }

    public String getApgPageUrl(){
        return  ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()+"?MIval=aa-markerview.apg&OID=";
    }

    public void testInit() throws Exception{
        String apgPageUrl = getApgPageUrl();
        webClient.getPage("http://" + domain + "/"+ apgPageUrl+worstGenesOld.get(0));
        webClient.getPage("http://" + domain + "/"+ javaPageUrl+worstGenesOld.get(0));
        assertTrue(true);
    }

    public void testWorstOldAPG() throws Exception{
        evaluateGeneApgList(worstGenesOld, "Worst Old");
    }

    public void testWorstOldJava() throws Exception{
        evaluateGeneJavaList(worstGenesOld, "Worst Old");
    }

    public void testAverageGenesAPG() throws Exception{
        evaluateGeneApgList(avgGenes, "Average");
    }

    public void testAverageGenesJava() throws Exception{
        evaluateGeneJavaList(avgGenes, "Average");
    }

    public void testWorstGeneNewAPG() throws Exception{
        evaluateGeneApgList(worstGenesNew, "Worst New");
    }

    public void testWorstGeneNewJava() throws Exception{
        evaluateGeneJavaList(worstGenesNew, "Worst New");
    }

    public void testDaliasAPG() throws Exception{
        evaluateGeneApgList(worstGenesDalias, "Worst Dalias");
    }

    public void testDaliasJava() throws Exception{
        evaluateGeneJavaList(worstGenesDalias, "Worst Dalias");
    }

    public void testDblinkAPG() throws Exception{
        evaluateGeneApgList(worstGenesDblink, "Worst Dblink");
    }

    public void testDblinkJava() throws Exception{
        evaluateGeneJavaList(worstGenesDblink, "Worst Dblink");
    }

    public void testFmrelAPG() throws Exception{
        evaluateGeneApgList(worstGenesFmrel, "Worst Fmrel");
    }

    public void testFmrelJava() throws Exception{
        evaluateGeneJavaList(worstGenesFmrel, "Worst Fmrel");
    }

    public void testGOAPG() throws Exception{
        evaluateGeneApgList(worstGenesGO, "Worst GO");
    }

    public void testGOJava() throws Exception{
        evaluateGeneJavaList(worstGenesGO, "Worst GO");
    }

    public void testMrel1APG() throws Exception{
        evaluateGeneApgList(worstGenesMrel1, "Worst Mrel1");
    }

    public void testMrel1Java() throws Exception{
        evaluateGeneJavaList(worstGenesMrel1, "Worst Mrel1");
    }

    public void testMrel2APG() throws Exception{
        evaluateGeneApgList(worstGenesMrel2, "Worst Mrel2");
    }

    public void testMrel2Java() throws Exception{
        evaluateGeneJavaList(worstGenesMrel2, "Worst Mrel2");
    }

    public void testOrthoAPG() throws Exception{
        evaluateGeneApgList(worstGenesOrtho, "Worst Ortho");
    }

    public void testOrthoJava() throws Exception{
        evaluateGeneJavaList(worstGenesOrtho, "Worst Ortho");
    }

    public void testPhenoAnatsAPG() throws Exception{
        evaluateGeneApgList(worstGenesPhenoAnats, "Worst PhenoAnats");
    }

    public void testPhenoAnatsJava() throws Exception{
        evaluateGeneJavaList(worstGenesPhenoAnats, "Worst PhenoAnats");
    }

    public void testPhenoFigurAPG() throws Exception{
        evaluateGeneApgList(worstGenesPhenoFigur, "Worst PhenoFigur");
    }

    public void testPhenoFigurJava() throws Exception{
        evaluateGeneJavaList(worstGenesPhenoFigur, "Worst PhenoFigur");
    }

    public void testRecattribAPG() throws Exception{
        evaluateGeneApgList(worstGenesRecattrib, "Worst Recattrib");
    }

    public void testRecattribJava() throws Exception{
        evaluateGeneJavaList(worstGenesRecattrib, "Worst Recattrib");
    }

    public void testXpatAPG() throws Exception{
        evaluateGeneApgList(worstGenesXpat, "Worst Xpat");
    }

    public void testXpatJava() throws Exception{
        evaluateGeneJavaList(worstGenesXpat, "Worst Xpat");
    }

    private void evaluateGeneApgList(List<String> evalList, String s) throws Exception {
        String apgPageUrl = getApgPageUrl();
        long startTime = System.currentTimeMillis();
        for(String geneId: evalList){
            HtmlPage page = webClient.getPage("http://" + domain + "/"+ apgPageUrl+geneId);
            assertTrue("Failed on page: " + geneId,page.getTitleText().startsWith("ZFIN: Gene:"));
            // logger.info("tested Gene: "+geneId);
        }
        long endTime = System.currentTimeMillis();
        float totalTime = (endTime-startTime) / 1000f ;
        float avgTime = totalTime / evalList.size();
        logger.info(s+" Gene APG Time: "+ avgTime + " (s) ");
    }


    private void evaluateGeneJavaList(List<String> evalList, String s) throws Exception {
        long startTime = System.currentTimeMillis();
        for(String geneId: evalList){
            HtmlPage page = webClient.getPage("http://" + domain + javaPageUrl+geneId);
            assertTrue("Failed on page: " + geneId,page.getTitleText().startsWith("ZFIN Gene:"));
            //  logger.info("tested Gene: "+geneId);
        }
        long endTime = System.currentTimeMillis();
        float totalTime = (endTime-startTime) / 1000f ;
        float avgTime = totalTime / evalList.size();
        logger.info(s+" Gene Java Time: "+ avgTime + " (s) ");
    }



}
