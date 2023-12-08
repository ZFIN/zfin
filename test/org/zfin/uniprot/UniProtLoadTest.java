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

public class UniProtLoadTest extends AbstractDatabaseTest {

    /**
     * This test is built around a specific case that was a bit confusing. The UniProt record A4IGB0 had 2 genes
     * associated to it according to our legacy load. One of them was a GenBank match so it's okay to keep. The other
     * was probably matched by ZFIN ID but we don't have a matching RefSeq so we delete it.
     *
     */
    @Test
    public void handleLostUniprotWithGenPept() throws JsonProcessingException {
        UniProtLoadTask loadTask = new UniProtLoadTask("", "", "", false, "", "");
        loadTask.initialize();
        String record = testDat();
        loadTask.setContext(testContext());
        try (BufferedReader inputFileReader = new BufferedReader(new StringReader(record)) ) {
            Map<String, RichSequenceAdapter> entries = loadTask.readUniProtEntries(inputFileReader);
            Set<UniProtLoadAction> actions = loadTask.executePipeline(entries);
            assertEquals(2, actions.size());

            //one action is an info action (Previously Matched by GenBank: No RefSeq Match
            assertEquals(1, actions.stream().filter(action -> action.getSubType().equals(UniProtLoadAction.SubType.LOST_UNIPROT_PREV_MATCH_BY_GB)).count());

            //one action is a delete action
            assertEquals(1, actions.stream().filter(action -> action.getType().equals(UniProtLoadAction.Type.DELETE)).count());

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
                RN   [1] {ECO:0000313|RefSeq:NP_001077286.1}
                RP   NUCLEOTIDE SEQUENCE.
                RC   STRAIN=Singapore {ECO:0000313|RefSeq:NP_001077286.1};
                RX   PubMed=12477932; DOI=10.1073/pnas.242603899;
                RG   Mammalian Gene Collection Program Team;
                RA   Strausberg R.L., Feingold E.A., Grouse L.H., Derge J.G., Klausner R.D.,
                RA   Collins F.S., Wagner L., Shenmen C.M., Schuler G.D., Altschul S.F.,
                RA   Zeeberg B., Buetow K.H., Schaefer C.F., Bhat N.K., Hopkins R.F., Jordan H.,
                RA   Moore T., Max S.I., Wang J., Hsieh F., Diatchenko L., Marusina K.,
                RA   Farmer A.A., Rubin G.M., Hong L., Stapleton M., Soares M.B., Bonaldo M.F.,
                RA   Casavant T.L., Scheetz T.E., Brownstein M.J., Usdin T.B., Toshiyuki S.,
                RA   Carninci P., Prange C., Raha S.S., Loquellano N.A., Peters G.J.,
                RA   Abramson R.D., Mullahy S.J., Bosak S.A., McEwan P.J., McKernan K.J.,
                RA   Malek J.A., Gunaratne P.H., Richards S., Worley K.C., Hale S., Garcia A.M.,
                RA   Gay L.J., Hulyk S.W., Villalon D.K., Muzny D.M., Sodergren E.J., Lu X.,
                RA   Gibbs R.A., Fahey J., Helton E., Ketteman M., Madan A., Rodrigues S.,
                RA   Sanchez A., Whiting M., Madan A., Young A.C., Shevchenko Y., Bouffard G.G.,
                RA   Blakesley R.W., Touchman J.W., Green E.D., Dickson M.C., Rodriguez A.C.,
                RA   Grimwood J., Schmutz J., Myers R.M., Butterfield Y.S., Krzywinski M.I.,
                RA   Skalska U., Smailus D.E., Schnerch A., Schein J.E., Jones S.J., Marra M.A.;
                RT   "Generation and initial analysis of more than 15,000 full-length human and
                RT   mouse cDNA sequences.";
                RL   Proc. Natl. Acad. Sci. U.S.A. 99:16899-16903(2002).
                RN   [2] {ECO:0000313|EMBL:AAI35009.1}
                RP   NUCLEOTIDE SEQUENCE [LARGE SCALE MRNA].
                RC   STRAIN=Singapore local strain {ECO:0000313|EMBL:AAI35009.1};
                RC   TISSUE=Embryo {ECO:0000313|EMBL:AAI35009.1};
                RG   NIH - Zebrafish Gene Collection (ZGC) project;
                RL   Submitted (MAR-2007) to the EMBL/GenBank/DDBJ databases.
                RN   [3] {ECO:0000313|Ensembl:ENSDARP00000094962}
                RP   IDENTIFICATION.
                RC   STRAIN=Tuebingen {ECO:0000313|Ensembl:ENSDARP00000094962};
                RG   Ensembl;
                RL   Submitted (APR-2011) to UniProtKB.
                RN   [4] {ECO:0000313|Ensembl:ENSDARP00000094962}
                RP   NUCLEOTIDE SEQUENCE [LARGE SCALE GENOMIC DNA].
                RC   STRAIN=Tuebingen {ECO:0000313|Ensembl:ENSDARP00000094962};
                RX   PubMed=23594743; DOI=10.1038/nature12111;
                RA   Howe K., Clark M.D., Torroja C.F., Torrance J., Berthelot C., Muffato M.,
                RA   Collins J.E., Humphray S., McLaren K., Matthews L., McLaren S., Sealy I.,
                RA   Caccamo M., Churcher C., Scott C., Barrett J.C., Koch R., Rauch G.J.,
                RA   White S., Chow W., Kilian B., Quintais L.T., Guerra-Assuncao J.A., Zhou Y.,
                RA   Gu Y., Yen J., Vogel J.H., Eyre T., Redmond S., Banerjee R., Chi J., Fu B.,
                RA   Langley E., Maguire S.F., Laird G.K., Lloyd D., Kenyon E., Donaldson S.,
                RA   Sehra H., Almeida-King J., Loveland J., Trevanion S., Jones M., Quail M.,
                RA   Willey D., Hunt A., Burton J., Sims S., McLay K., Plumb B., Davis J.,
                RA   Clee C., Oliver K., Clark R., Riddle C., Elliot D., Threadgold G.,
                RA   Harden G., Ware D., Begum S., Mortimore B., Kerry G., Heath P.,
                RA   Phillimore B., Tracey A., Corby N., Dunn M., Johnson C., Wood J., Clark S.,
                RA   Pelan S., Griffiths G., Smith M., Glithero R., Howden P., Barker N.,
                RA   Lloyd C., Stevens C., Harley J., Holt K., Panagiotidis G., Lovell J.,
                RA   Beasley H., Henderson C., Gordon D., Auger K., Wright D., Collins J.,
                RA   Raisen C., Dyer L., Leung K., Robertson L., Ambridge K., Leongamornlert D.,
                RA   McGuire S., Gilderthorp R., Griffiths C., Manthravadi D., Nichol S.,
                RA   Barker G., Whitehead S., Kay M., Brown J., Murnane C., Gray E.,
                RA   Humphries M., Sycamore N., Barker D., Saunders D., Wallis J., Babbage A.,
                RA   Hammond S., Mashreghi-Mohammadi M., Barr L., Martin S., Wray P.,
                RA   Ellington A., Matthews N., Ellwood M., Woodmansey R., Clark G., Cooper J.,
                RA   Tromans A., Grafham D., Skuce C., Pandian R., Andrews R., Harrison E.,
                RA   Kimberley A., Garnett J., Fosker N., Hall R., Garner P., Kelly D., Bird C.,
                RA   Palmer S., Gehring I., Berger A., Dooley C.M., Ersan-Urun Z., Eser C.,
                RA   Geiger H., Geisler M., Karotki L., Kirn A., Konantz J., Konantz M.,
                RA   Oberlander M., Rudolph-Geiger S., Teucke M., Lanz C., Raddatz G.,
                RA   Osoegawa K., Zhu B., Rapp A., Widaa S., Langford C., Yang F.,
                RA   Schuster S.C., Carter N.P., Harrow J., Ning Z., Herrero J., Searle S.M.,
                RA   Enright A., Geisler R., Plasterk R.H., Lee C., Westerfield M.,
                RA   de Jong P.J., Zon L.I., Postlethwait J.H., Nusslein-Volhard C.,
                RA   Hubbard T.J., Roest Crollius H., Rogers J., Stemple D.L.;
                RT   "The zebrafish reference genome sequence and its relationship to the human
                RT   genome.";
                RL   Nature 496:498-503(2013).
                RN   [5] {ECO:0000313|RefSeq:NP_001077286.1}
                RP   IDENTIFICATION.
                RC   STRAIN=Singapore {ECO:0000313|RefSeq:NP_001077286.1};
                RG   RefSeq;
                RL   Submitted (MAR-2023) to UniProtKB.
                CC   ---------------------------------------------------------------------------
                CC   Copyrighted by the UniProt Consortium, see https://www.uniprot.org/terms
                CC   Distributed under the Creative Commons Attribution (CC BY 4.0) License
                CC   ---------------------------------------------------------------------------
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
                          "ZDB-PUB-230615-71"
                        ]
                      },
                      {
                        "accession": "A4IGB0",
                        "dataZdbID": "ZDB-GENE-041014-76",
                        "markerAbbreviation": "si:ch211-182e10.4",
                        "dbName": "UNIPROTKB",
                        "publicationIDs": [
                          "ZDB-PUB-230615-71"
                        ]
                      }
                    ]
                  },
                  "refseqDbLinks": {
                    "XP_003198008": [
                      {
                        "accession": "XP_003198008",
                        "dataZdbID": "ZDB-GENE-041014-76",
                        "markerAbbreviation": "si:ch211-182e10.4",
                        "dbName": "REFSEQ",
                        "publicationIDs": [
                          "ZDB-PUB-020723-3"
                        ]
                      }
                    ],
                    "XM_003197960": [
                      {
                        "accession": "XM_003197960",
                        "dataZdbID": "ZDB-GENE-041014-76",
                        "markerAbbreviation": "si:ch211-182e10.4",
                        "dbName": "REFSEQ",
                        "publicationIDs": [
                          "ZDB-PUB-020723-3"
                        ]
                      }
                    ],
                    "NM_001128575": [
                      {
                        "accession": "NM_001128575",
                        "dataZdbID": "ZDB-GENE-141215-12",
                        "markerAbbreviation": "si:ch73-42k18.1",
                        "dbName": "REFSEQ",
                        "publicationIDs": [
                          "ZDB-PUB-130725-2"
                        ]
                      }
                    ],
                    "NP_001122047": [
                      {
                        "accession": "NP_001122047",
                        "dataZdbID": "ZDB-GENE-141215-12",
                        "markerAbbreviation": "si:ch73-42k18.1",
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
