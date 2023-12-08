package org.zfin.uniprot.handlers;

import org.biojavax.*;
import org.biojavax.ontology.ComparableTerm;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.UniProtFormatZFIN;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;

import java.util.Map;
import java.util.Set;

import static org.zfin.uniprot.UniProtTools.setAccession;
import static org.zfin.uniprot.UniProtTools.transformCrossRefNoteSetByTerm;

/**
 * Removes the version number from the RefSeq accession.
 * For example, NM_001002225.1 becomes NM_001002225
 */
public class RemoveVersionHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        for (String acc : uniProtRecords.keySet()) {
            //find matching RefSeq accession
            RichSequenceAdapter loadFileSequence = uniProtRecords.get(acc);

            //transform the RefSeq accessions to omit the version number
            Set<RankedCrossRef> rankedXrefs = loadFileSequence.getRankedCrossRefs();

            for (RankedCrossRef rankedXref : rankedXrefs) {
                if (rankedXref.getCrossRef().getDbname().equals("RefSeq")) {
                    CrossRef xref = rankedXref.getCrossRef();
                    String refSeqAccession = xref.getAccession();
                    //change the refseq accession to omit the version number
                    String refSeqAccessionWithoutVersion = refSeqAccession.replaceAll("\\.\\d+$", "");

                    //do the same thing for the noteset which is where additional accessions are stored
                    setAccession(xref, refSeqAccessionWithoutVersion);
                    transformCrossRefNoteSetByTerm(xref, (ComparableTerm) UniProtFormatZFIN.Terms.getAdditionalAccessionTerm(),
                            s -> s.replaceAll("\\.\\d+$", ""));

                }
            }

            uniProtRecords.put(acc, loadFileSequence);
        }
    }
}
