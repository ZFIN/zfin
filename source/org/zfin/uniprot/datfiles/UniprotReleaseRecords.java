package org.zfin.uniprot.datfiles;

import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.*;

public class UniprotReleaseRecords implements Iterable<RichSequenceAdapter> {
    Map<String, RichSequenceAdapter> uniprotRecordsIndexedByUniprotAccession;
    Map<String, List<RichSequenceAdapter>> uniprotRecordsIndexedByGeneZdbID;


    public UniprotReleaseRecords() {
        this.uniprotRecordsIndexedByUniprotAccession = new HashMap<>();
    }
    public UniprotReleaseRecords(Map<String, RichSequenceAdapter> uniprotRecordsIndexedByUniprotAccession) {
        this.uniprotRecordsIndexedByUniprotAccession = uniprotRecordsIndexedByUniprotAccession;
    }

    public RichSequenceAdapter getByAccession(String accession) {
        return uniprotRecordsIndexedByUniprotAccession.get(accession);
    }

    public Set<String> getAccessions() {
        return uniprotRecordsIndexedByUniprotAccession.keySet();
    }

    public Set<Map.Entry<String, RichSequenceAdapter>> getEntriesByAccession() {
        return uniprotRecordsIndexedByUniprotAccession.entrySet();
    }

    public long size() {
        return uniprotRecordsIndexedByUniprotAccession.size();
    }

    @Override
    public Iterator<RichSequenceAdapter> iterator() {
        return uniprotRecordsIndexedByUniprotAccession.values().iterator();
    }

    public List<RichSequenceAdapter> getByGeneZdbID(String dataZdbID) {
        if (uniprotRecordsIndexedByGeneZdbID == null) {
            initializeUniprotRecordsIndexedByZdbID();
        }
        List<RichSequenceAdapter> foundRecords = uniprotRecordsIndexedByGeneZdbID.get(dataZdbID);
        if (foundRecords == null) {
            return Collections.emptyList();
        }
        return foundRecords;
    }

    public Optional<RichSequenceAdapter> getFirstByGeneZdbID(String dataZdbID) {
        List<RichSequenceAdapter> seqs = getByGeneZdbID(dataZdbID);
        if (seqs == null || seqs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(seqs.get(0));
    }

    private void initializeUniprotRecordsIndexedByZdbID() {
        uniprotRecordsIndexedByGeneZdbID = new HashMap<>();
        for(RichSequenceAdapter seq : uniprotRecordsIndexedByUniprotAccession.values()) {
            List<String> zdbIDs = seq.getCrossRefIDsByDatabase(RichSequenceAdapter.DatabaseSource.ZFIN);
            for(String zdbID : zdbIDs) {
                List<RichSequenceAdapter> seqs = uniprotRecordsIndexedByGeneZdbID.get(zdbID);
                if (seqs == null) {
                    seqs = new ArrayList<>();
                    uniprotRecordsIndexedByGeneZdbID.put(zdbID, seqs);
                }
                seqs.add(seq);
            }
        }
    }

    public String getUniprotFormatByAccession(String accession) {
        RichSequenceAdapter record = this.getByAccession(accession);
        if (record == null) {
            return "";
        }
        return record.toUniProtFormat();
    }
}
