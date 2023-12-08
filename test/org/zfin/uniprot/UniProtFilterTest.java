package org.zfin.uniprot;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.uniprot.task.UniProtReleaseDiffTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UniProtFilterTest extends AbstractDatabaseTest {


    @Test
    public void testFilteringOfApiDownloads() throws IOException {
        //create a temp file
        File tempFile = File.createTempFile("test1", ".dat");
        tempFile.deleteOnExit();

        File tempFile2 = File.createTempFile("test2", ".dat");
        tempFile2.deleteOnExit();

        FileUtils.writeStringToFile(tempFile, testDat1());
        FileUtils.writeStringToFile(tempFile2, testDat2());

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File outputTempFile = File.createTempFile("test-output-" + timestamp, ".dat");

        UniProtReleaseDiffTask.combineAndFilterInputFileSet(
                List.of(tempFile, tempFile2),
                outputTempFile.toPath()
        );
        assertTrue(outputTempFile.exists());
        assertTrue(outputTempFile.length() > 0);
    }

    @Test
    public void testFilteringRemovesDuplicates() throws IOException {
        //create a temp file
        File tempFile = File.createTempFile("test1", ".dat");
        tempFile.deleteOnExit();

        File tempFile2 = File.createTempFile("test2", ".dat");
        tempFile2.deleteOnExit();

        FileUtils.writeStringToFile(tempFile, testDat1());
        FileUtils.writeStringToFile(tempFile2, testDat2());
        FileUtils.writeStringToFile(tempFile2, testDat1(), true);

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File outputTempFile = File.createTempFile("test-output-" + timestamp, ".dat");

        try {
            UniProtReleaseDiffTask.combineAndFilterInputFileSet(
                    List.of(tempFile, tempFile2),
                    outputTempFile.toPath()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(outputTempFile.exists());
        assertTrue(outputTempFile.length() > 0);

        String contents = FileUtils.readFileToString(outputTempFile);

        //count instances of duplicate entry
        int count = 0;
        BufferedReader reader = new BufferedReader(new StringReader(contents));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("AC   A4IGB0")) {
                count++;
            }
        }
        assertEquals(1, count);

    }

    private String testDat1() {
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

    private String testDat2() {
        return """
                ID   A0A0D5W690_DANRE          Unreviewed;         937 AA.
                AC   A0A0D5W690;
                DT   27-MAY-2015, integrated into UniProtKB/TrEMBL.
                DT   27-MAY-2015, sequence version 1.
                DT   28-JUN-2023, entry version 44.
                DE   RecName: Full=RNA helicase {ECO:0000256|ARBA:ARBA00012552};
                DE            EC=3.6.4.13 {ECO:0000256|ARBA:ARBA00012552};.
                GN   Name=rigi {ECO:0000313|ZFIN:ZDB-GENE-090821-7}; Synonyms=RIG-I {ECO:0000313|RefSeq:NP_001293024.1}, RIG-Ia
                GN   {ECO:0000313|RefSeq:NP_001293024.1}, RIG-Ib
                GN   {ECO:0000313|RefSeq:NP_001293024.1}, RIG-Ic
                GN   {ECO:0000313|RefSeq:NP_001293024.1}, RIG-Id
                GN   {ECO:0000313|RefSeq:NP_001293024.1}; OrderedLocusNames=ddx58 {ECO:0000313|RefSeq:NP_001293024.1};
                OS   Danio rerio (Zebrafish) (Brachydanio rerio).
                OC   .
                OX   NCBI_TaxID=7955;
                CC   -!- CATALYTIC ACTIVITY:
                CC       Reaction=ATP + H2O = ADP + H(+) + phosphate; Xref=Rhea:RHEA:13065,
                CC         ChEBI:CHEBI:15377, ChEBI:CHEBI:15378, ChEBI:CHEBI:30616,
                CC         ChEBI:CHEBI:43474, ChEBI:CHEBI:456216; EC=3.6.4.13;
                CC         Evidence={ECO:0000256|ARBA:ARBA00029316};
                CC       PhysiologicalDirection=left-to-right; Xref=Rhea:RHEA:13066;
                CC         Evidence={ECO:0000256|ARBA:ARBA00029316};
                CC   -!- SUBCELLULAR LOCATION: Cytoplasm {ECO:0000256|ARBA:ARBA00004496}.
                CC   -!- SIMILARITY: Belongs to the helicase family. RLR subfamily.
                CC       {ECO:0000256|ARBA:ARBA00006866}.
                CC   ---------------------------------------------------------------------------
                CC   Copyrighted by the UniProt Consortium, see https://www.uniprot.org/terms
                CC   Distributed under the Creative Commons Attribution (CC BY 4.0) License
                CC   ---------------------------------------------------------------------------
                DR   EMBL; KM281808; AJZ72649.1; -; mRNA.
                DR   RefSeq; NP_001293024.1; NM_001306095.1.
                DR   GeneID; 100333797; -.
                DR   ZFIN; ZDB-GENE-090821-7; rigi.
                DR   GO; GO:0005829; C:cytosol; IDA:ZFIN.
                DR   GO; GO:0005634; C:nucleus; IDA:ZFIN.
                DR   GO; GO:0005524; F:ATP binding; IEA:UniProtKB-KW.
                DR   GO; GO:0003677; F:DNA binding; IEA:InterPro.
                DR   GO; GO:0016787; F:hydrolase activity; IEA:UniProtKB-KW.
                DR   GO; GO:0046872; F:metal ion binding; IEA:UniProtKB-KW.
                DR   GO; GO:0003723; F:RNA binding; IEA:UniProtKB-KW.
                DR   GO; GO:0003724; F:RNA helicase activity; IEA:UniProtKB-EC.
                DR   GO; GO:0019221; P:cytokine-mediated signaling pathway; IDA:ZFIN.
                DR   GO; GO:0051607; P:defense response to virus; IDA:ZFIN.
                DR   GO; GO:0007249; P:I-kappaB kinase/NF-kappaB signaling; IDA:ZFIN.
                DR   GO; GO:0006355; P:regulation of DNA-templated transcription; IDA:ZFIN.
                DR   GO; GO:0010468; P:regulation of gene expression; IDA:ZFIN.
                DR   GO; GO:0039529; P:RIG-I signaling pathway; IDA:ZFIN.
                DR   GO; GO:0060337; P:type I interferon-mediated signaling pathway; IMP:ZFIN.
                DR   GO; GO:0060333; P:type II interferon-mediated signaling pathway; IDA:ZFIN.
                DR   InterPro; IPR031964; CARD_dom.
                DR   InterPro; IPR042145; CARD_RIG-I_r2.
                DR   InterPro; IPR011029; DEATH-like_dom_sf.
                DR   InterPro; IPR006935; Helicase/UvrB_N.
                DR   InterPro; IPR014001; Helicase_ATP-bd.
                DR   InterPro; IPR001650; Helicase_C.
                DR   InterPro; IPR027417; P-loop_NTPase.
                DR   InterPro; IPR041204; RIG-I-like_C.
                DR   InterPro; IPR038557; RLR_C_sf.
                DR   InterPro; IPR021673; RLR_CTR.
                DR   Pfam; PF16739; CARD_2; 2.
                DR   Pfam; PF00271; Helicase_C; 1.
                DR   Pfam; PF04851; ResIII; 1.
                DR   Pfam; PF18119; RIG-I_C; 1.
                DR   Pfam; PF11648; RIG-I_C-RD; 1.
                DR   PROSITE; PS51192; HELICASE_ATP_BIND_1; 1.
                DR   PROSITE; PS51194; HELICASE_CTER; 1.
                DR   PROSITE; PS51789; RLR_CTR; 1.
                PE   2: Evidence at transcript level;
                KW   Antiviral defense {ECO:0000256|ARBA:ARBA00023118}; ATP-binding {ECO:0000256|ARBA:ARBA00022840}; Cytoplasm {ECO:0000256|ARBA:ARBA00022490}; Helicase {ECO:0000256|ARBA:ARBA00022806, ECO:0000313|RefSeq:NP_001293024.1}; Hydrolase {ECO:0000256|ARBA:ARBA00022801}; Immunity {ECO:0000256|ARBA:ARBA00022859}; Innate immunity {ECO:0000256|ARBA:ARBA00022588}; Metal-binding {ECO:0000256|ARBA:ARBA00022723}; Nucleotide-binding {ECO:0000256|ARBA:ARBA00022741}; Phosphoprotein
                KW   {ECO:0000256|ARBA:ARBA00022553}; Reference proteome {ECO:0000313|Proteomes:UP000000437}; RNA-binding {ECO:0000256|ARBA:ARBA00022884}; Zinc {ECO:0000256|ARBA:ARBA00022833}.
                """;
    }
}
