package org.zfin.sequence.blast.presentation;

import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.Database;

import java.util.HashMap;
import java.util.Map;

/**
 * Processes a sequence in order to send the post to the appropriate external blast database.
 * <p/>
 * To process the request we need:
 * - database abbreviation to post against
 * - sequence from the database
 * <p/>
 * To get the sequence from the database we need:
 * - accession
 * - reference database (to get the blast database)
 * <p/>
 * To get the above, we can use a dblink, an accession_bk, an acccession (if there is only one entry, which their usually is) + (refDB)
 * <p/>
 * Request must have accession, refDB zdbID, blastDB zdbID.
 */
public abstract class AbstractExternalBlastController extends AbstractController {


    public static Map<String, String> getHiddenVariables(Sequence sequence,Database database,boolean isShortSequence) {

        Map<String, String> hiddenVariables = new HashMap<String, String>();

        if (database.getType() == Database.Type.NUCLEOTIDE) {
//            for vega
            if (database.getAbbrev() == Database.AvailableAbbrev.VEGA_BLAST) {
                hiddenVariables.put("_query_sequence", sequence.getData());
                hiddenVariables.put("species", "Danio_rerio");
                hiddenVariables.put("database_dna", "LATESTGP"); // genomic sequence
                hiddenVariables.put("database", "dna");
                hiddenVariables.put("query", "dna");
                hiddenVariables.put("method", "BLASTN");
                if (isShortSequence) {
                    hiddenVariables.put("sensitivity", "OLIGO");
                } else {
                    hiddenVariables.put("sensitivity", "LOW");
                }
            }
//            <%--for ncbi--%>
            else if (database.getAbbrev() == Database.AvailableAbbrev.BLAST) {
                hiddenVariables.put("QUERY", sequence.getData());
                hiddenVariables.put("PAGE", "Nucleotides");
                hiddenVariables.put("PROGRAM", "blastn");
                hiddenVariables.put("qorganism", "Danio rerio (taxid:7955)");
                hiddenVariables.put("DATABASE", "nr");
                hiddenVariables.put("EQ_MENU", "Danio rerio (taxid:7955)");
            }
//            <%--for ncbi MegaBlast --%>
            else if (database.getAbbrev() == Database.AvailableAbbrev.MEGA_BLAST) {
                hiddenVariables.put("QUERY", sequence.getData());
                hiddenVariables.put("PAGE", "MegaBlast");
                hiddenVariables.put("PROGRAM", "blastn");
                hiddenVariables.put("qorganism", "Danio rerio (taxid:7955)");
                hiddenVariables.put("DATABASE", "nr");
                hiddenVariables.put("EQ_MENU", "Danio rerio (taxid:7955)");
            }
            // ensemble
            else if (database.getAbbrev() == Database.AvailableAbbrev.ENSEMBL) {
                hiddenVariables.put("_query_sequence", sequence.getData());
                hiddenVariables.put("species", "Danio_rerio");
                if (isShortSequence) {
                    hiddenVariables.put("method", "BLASTN");
                    hiddenVariables.put("sensitivity", "OLIGO");
                } else {
                    hiddenVariables.put("method", "BLAT");
                    hiddenVariables.put("sensitivity", "LOW");
                }
            }
            // ucsc
            else if (database.getAbbrev() == Database.AvailableAbbrev.UCSC_BLAT) {
                hiddenVariables.put("org", "Zebrafish");
                hiddenVariables.put("type", "hyperlink");
                hiddenVariables.put("userSeq", sequence.getData());
            }
            // internal blast
            else if (database.getAbbrev() == Database.AvailableAbbrev.ZFIN_CDNA_SEQ) {
                hiddenVariables.put("program", "blastn");
                hiddenVariables.put("sequenceType", "nt");
                hiddenVariables.put("queryType", "FASTA");
                hiddenVariables.put("dataLibraryString", database.getAbbrev().toString());
                hiddenVariables.put("querySequence", sequence.getDefLine() + "\n" + sequence.getData());
                hiddenVariables.put("expectValue", "1.0E-25");
                hiddenVariables.put("wordLength", "11");
                hiddenVariables.put("dust", "true");
                hiddenVariables.put("poly_a", "false");
                hiddenVariables.put("seg", "false");
                hiddenVariables.put("xnu", "false");
            } else if (database.getAbbrev() == Database.AvailableAbbrev.ZFIN_MICRORNA
                    || database.getAbbrev() == Database.AvailableAbbrev.ZFIN_MIRNA_MATURE
                    || database.getAbbrev() == Database.AvailableAbbrev.ZFIN_MIRNA_STEMLOOP
                    || database.getAbbrev() == Database.AvailableAbbrev.CURATEDMICRORNAMATURE
                    || database.getAbbrev() == Database.AvailableAbbrev.CURATEDMICRORNASTEMLOOP
                    || database.getAbbrev() == Database.AvailableAbbrev.ZFIN_MIRNA_MATURE
                    || database.getAbbrev() == Database.AvailableAbbrev.ZFIN_MRPH
                    || database.getAbbrev() == Database.AvailableAbbrev.LOADEDMICRORNAMATURE
                    || database.getAbbrev() == Database.AvailableAbbrev.LOADEDMICRORNASTEMLOOP
                    ) {
                hiddenVariables.put("dataLibraryString", database.getAbbrev().toString());
                hiddenVariables.put("shortAndNearlyExact", "true");
                hiddenVariables.put("expectValue", "10");
                hiddenVariables.put("wordLength", "3");
                hiddenVariables.put("seg", "true");
                hiddenVariables.put("xnu", "true");
                hiddenVariables.put("queryType", "FASTA");
                hiddenVariables.put("querySequence", sequence.getData());

            }
        } else if (database.getType() == Database.Type.PROTEIN) {
            // internal blast
            if (database.getAbbrev() == Database.AvailableAbbrev.ZFIN_ALL_AA) {
                hiddenVariables.put("program", "blastp");
                hiddenVariables.put("sequenceType", "pt");
                hiddenVariables.put("queryType", "FASTA");
                hiddenVariables.put("dataLibraryString", database.getAbbrev().toString());
                hiddenVariables.put("querySequence", sequence.getDefLine() + "\n" + sequence.getData());
                hiddenVariables.put("expectValue", "10");
                hiddenVariables.put("wordLength", "3");
                hiddenVariables.put("dust", "false");
                hiddenVariables.put("poly_a", "false");
                hiddenVariables.put("seg", "true");
                hiddenVariables.put("xnu", "true");
            }
            // for ncbi?
            else if (database.getAbbrev() == Database.AvailableAbbrev.BLASTP) {
                hiddenVariables.put("QUERY", sequence.getData());
            }
            // ensemble?
            else if (database.getAbbrev() == Database.AvailableAbbrev.PBLAST) {
                hiddenVariables.put("sequence", sequence.getData());
            }
        }

        return hiddenVariables;
    }
}