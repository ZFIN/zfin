package org.zfin.datatransfer.microarray;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class GeoMicorarrayEntriesBean {

    private Set<String> accessions = new HashSet<String>();
    private Set<String> geneSymbols = new HashSet<String>();
    private Set<String> markerZdbIDs = new HashSet<String>();

    public void addAccession(String accession){
        accessions.add(accession);
    }

    public void addGeneSymbol(String geneSymbol){
        geneSymbols.add(geneSymbol);
    }

    public void addMarkerZdbID(String markerZdbID){
        markerZdbIDs.add(markerZdbID);
    }

    public Set<String> getAccessions() {
        return accessions;
    }

    public Set<String> getGeneSymbols() {
        return geneSymbols;
    }

    public Set<String> getMarkerZdbIDs() {
        return markerZdbIDs;
    }
}
