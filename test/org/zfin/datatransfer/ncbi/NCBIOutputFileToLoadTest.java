package org.zfin.datatransfer.ncbi;


import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.*;

public class NCBIOutputFileToLoadTest {

    @Test
    public void testAddingNcbiGeneIDCollision() throws IOException {
        NCBIOutputFileToLoad fileOutput = new NCBIOutputFileToLoad();
        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "123456", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_RNA)
        );
        List<NCBIOutputFileToLoad.LoadFileRow> rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "123456");

        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "111111", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_RNA)
        );
        rows = fileOutput.getRows();
        assertEquals(1, rows.size());

        //The first one should be replaced because PUB_MAPPED_BASED_ON_RNA has equal priority to PUB_MAPPED_BASED_ON_RNA (self)
        assertEquals(rows.get(0).accession(), "111111");
    }


    @Test
    public void testAddingNcbiGeneIDCollision2() throws IOException {
        NCBIOutputFileToLoad fileOutput = new NCBIOutputFileToLoad();
        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "123456", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT)
        );
        List<NCBIOutputFileToLoad.LoadFileRow> rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "123456");

        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "111111", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_RNA)
        );
        rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "111111");
    }

    @Test
    public void testAddingNcbiGeneIDCollision3() throws IOException {
        NCBIOutputFileToLoad fileOutput = new NCBIOutputFileToLoad();
        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "123456", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_RNA)
        );
        List<NCBIOutputFileToLoad.LoadFileRow> rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "123456");

        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "111111", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT)
        );
        rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "123456");
    }



    @Test
    public void testAddingNcbiGeneIDCollision4() throws IOException {
        NCBIOutputFileToLoad fileOutput = new NCBIOutputFileToLoad();
        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "123456", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_VEGA)
        );
        List<NCBIOutputFileToLoad.LoadFileRow> rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "123456");

        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "111111", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT)
        );
        rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "111111");
    }


    @Test
    public void testAddingNcbiGeneIDCollision5() throws IOException {
        NCBIOutputFileToLoad fileOutput = new NCBIOutputFileToLoad();
        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "123456", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT)
        );
        List<NCBIOutputFileToLoad.LoadFileRow> rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "123456");

        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "111111", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_VEGA)
        );
        rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "123456");
    }

    /**
     * Test adding NCBI Gene IDs for different ZFIN Gene IDs but same NCBI Gene ID.
     * For now, there's no prevention of this, but we may want to add logic later.
     * @throws IOException
     */
    @Test
    @Ignore
    public void testAddingNcbiGeneIDCollisionOnNCBISide() throws IOException {
        NCBIOutputFileToLoad fileOutput = new NCBIOutputFileToLoad();
        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-1", "123456", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT)
        );
        List<NCBIOutputFileToLoad.LoadFileRow> rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "123456");

        fileOutput.addRow(
                new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000000-2", "123456", 670, FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_VEGA)
        );
        rows = fileOutput.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).accession(), "123456");
    }

    @Test
    public void testAddingManyAccessions() throws IOException {
        String expectedOutput = """
            ZDB-GENE-000112-47|30754|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000112-47|NP_571543||517|ZDB-FDBCONT-040412-39|ZDB-PUB-020723-3
            ZDB-GENE-000125-12|794176|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000125-4|30120|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000128-11|30416|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000128-13|30417|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000128-8|30394|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000201-13|30652|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000201-18|30728|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000201-9|30630|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000208-13|30186|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000208-17|30248|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000208-18|30617|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000208-20|30115|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000208-21|30453|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            ZDB-GENE-000208-23|561679|||ZDB-FDBCONT-040412-1|ZDB-PUB-020723-3
            """;


        NCBIOutputFileToLoad fileOutput = new NCBIOutputFileToLoad();
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000112-47", "30754", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000112-47", "NP_571543", 517, "ZDB-FDBCONT-040412-39", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000125-12", "794176", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000125-4", "30120", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000128-11", "30416", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000128-13", "30417", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000128-8", "30394", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000201-13", "30652", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000201-18", "30728", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000201-9", "30630", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000208-13", "30186", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000208-17", "30248", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000208-18", "30617", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000208-20", "30115", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000208-21", "30453", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));
        fileOutput.addRow(new NCBIOutputFileToLoad.LoadFileRow("ZDB-GENE-000208-23", "561679", null, "ZDB-FDBCONT-040412-1", "ZDB-PUB-020723-3"));

        assertEquals(expectedOutput, fileOutput.getOutput());
    }


}
