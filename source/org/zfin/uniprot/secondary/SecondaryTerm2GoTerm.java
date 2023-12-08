package org.zfin.uniprot.secondary;

public record SecondaryTerm2GoTerm(String dbAccession, String dbTermName, String goTermName, String goID, String termZdbID) {
    public String prefixedGoID() {
        return "GO:" + goID;
    }
}
