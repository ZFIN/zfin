package org.zfin.uniprot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.biojava.bio.BioException;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.task.UniProtLoadTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class UniProtLoadRemoveAttributionTest extends AbstractDatabaseTest {

    /**
     * If an UniProt record no longer has a RefSeq match, meaning we want delete it, but it already has a manual curation attribution,
     * we keep the uniprot record and its gene association, but remove the automatic attribution from the dblink.
     */
    @Test
    public void handleUniprotLoadDisagreesWithManualCuration() throws JsonProcessingException {
        UniProtLoadTask loadTask = new UniProtLoadTask("", "", "", false, "", "");
        loadTask.initialize();
        String record = testDat();
        loadTask.setContext(testContext());
        try (BufferedReader inputFileReader = new BufferedReader(new StringReader(record)) ) {
            Map<String, RichSequenceAdapter> entries = loadTask.readUniProtEntries(inputFileReader);
            Set<UniProtLoadAction> actions = loadTask.executePipeline(entries);
            assertEquals(1, actions.size());

            UniProtLoadAction action = actions.iterator().next();

            assertEquals(action.getType(), UniProtLoadAction.Type.DELETE);
            assertEquals(action.getSubType(), UniProtLoadAction.SubType.REMOVE_ATTRIBUTION);
            assertEquals(action.getAccession(), "A4IGB0");
            assertEquals(action.getGeneZdbID(), "ZDB-GENE-141215-12");

        } catch (IOException | BioException e) {
            throw new RuntimeException(e);
        }
    }

    private String testDat() {
        return """
                ID   A4IGB0_DANRE            Unreviewed;       521 AA.
                AC   A4IGB0; A0A8M1NDX1; E7F1Q2;
                DT   01-MAY-2007, integrated into UniProtKB/TrEMBL.
                DT   01-MAY-2007, sequence version 1.
                DT   28-JUN-2023, entry version 100.
                DE   SubName: Full=LOC553308 protein {ECO:0000313|EMBL:AAI35009.1};
                DE   SubName: Full=Si:ch211-182e10.4 {ECO:0000313|Ensembl:ENSDARP00000094962};
                DE   SubName: Full=Uncharacterized protein LOC553308 {ECO:0000313|RefSeq:NP_001077286.1};
                GN   Name=si:ch73-42k18.1 {ECO:0000313|Ensembl:ENSDARP00000094962};
                GN   Synonyms=LOC553308 {ECO:0000313|EMBL:AAI35009.1,
                GN   ECO:0000313|RefSeq:NP_001077286.1};
                OS   Danio rerio (Zebrafish) (Brachydanio rerio).
                OC   Eukaryota; Metazoa; Chordata; Craniata; Vertebrata; Euteleostomi;
                OC   Actinopterygii; Neopterygii; Teleostei; Ostariophysi; Cypriniformes;
                OC   Danionidae; Danioninae; Danio.
                OX   NCBI_TaxID=7955 {ECO:0000313|EMBL:AAI35009.1};
                DR   EMBL; CR384061; -; NOT_ANNOTATED_CDS; Genomic_DNA.
                DR   EMBL; BC135008; AAI35009.1; -; mRNA.
                DR   RefSeq; NP_001077286.1; NM_001083817.1.
                DR   Ensembl; ENSDART00000104187.6; ENSDARP00000094962.4; ENSDARG00000070661.6.
                DR   GeneID; 553308; -.
                DR   KEGG; dre:553308; -.
                DR   ZFIN; ZDB-GENE-141215-12; si:ch73-42k18.1.
                DR   HOGENOM; CLU_039471_2_0_1; -.
                DR   OrthoDB; 4256536at2759; -.
                DR   TreeFam; TF342704; -.
                DR   Proteomes; UP000000437; Chromosome 20.
                DR   Bgee; ENSDARG00000070661; Expressed in nervous system and 3 other tissues.
                DR   PANTHER; PTHR31025:SF19; SI:CH211-155I14.1-RELATED; 1.
                DR   PANTHER; PTHR31025; SI:CH211-196P9.1-RELATED; 1.
                PE   2: Evidence at transcript level;
                KW   Reference proteome {ECO:0000313|Proteomes:UP000000437}.
                FT   REGION          234..257
                FT                   /note="Disordered"
                FT                   /evidence="ECO:0000256|SAM:MobiDB-lite"
                SQ   SEQUENCE   521 AA;  58469 MW;  2FF725891333DF4F CRC64;
                   MASQEIMILR VILTEADIRK VTLTSKPCSV EDLINCLRNT LGLNYNFTLQ FRDPFFDHEF
                   CNVTALEELP EKPTVKIIPV LELVSVAEDE MQSSSEISSN APSTADTVLI SESPQKKKMP
                   WPDIFLIPKF SVDVEFRLRQ ANLIYLKDGT HLKMTKELKH DILQKLAETI YSFKAYPSAD
                   DLKGVAKALV NTHPCLQEPG SPSGHCGWTN SLKDKMGNYR SKMRSLGHTD VTVNAGKRGR
                   YSTSSDPPNK NIKKPRKGEV NYLPNLPSGH DTSSLELLRQ QLADETKKKK PDATFINQNM
                   DVTLSLRRQE VVINKPPVSQ ILQRWPALFT ESQVYQEFNR IVGKNLKQEF YGSLDHHCPQ
                   LIQIFRSKRG LTGQILSSLL LDAKASDLSD MRCVVVRGLP VLLGDDPSEF FKSCFASDDG
                   DSYQHVPVGI LNRENEDALQ PLSFRLHPSS VGIILEGNVV MDNIDNIPQA MCLLFGLTYA
                   LHLDYPKCMG NTLLFIQQVL LGLGKKELKG KILAVKNQLA M
                //
                """;
    }

    private UniProtLoadContext testContext() throws JsonProcessingException {
        String json = """
                {
                  "uniprotDbLinks": {
                    "A4IGB0": [
                      {
                        "accession": "A4IGB0",
                        "dataZdbID": "ZDB-GENE-141215-12",
                        "markerAbbreviation": "si:ch73-42k18.1",
                        "dbName": "UNIPROTKB",
                        "publicationIDs": [
                          "ZDB-PUB-220705-2",
                          "ZDB-PUB-230615-71"
                        ]
                      }
                    ]
                  },
                  "refseqDbLinks": {
                    "NP_00000000": [
                      {
                        "accession": "NP_00000000",
                        "dataZdbID": "ZDB-GENE-111111-1",
                        "markerAbbreviation": "bogusgene",
                        "dbName": "REFSEQ",
                        "publicationIDs": [
                          "ZDB-PUB-130725-2"
                        ]
                      }
                    ]
                  }
                }
                """;
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, UniProtLoadContext.class);
    }

}
