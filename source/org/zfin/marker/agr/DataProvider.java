package org.zfin.marker.agr;

import org.zfin.sequence.ForeignDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum DataProvider {
    ZFIN("ZFIN", ForeignDB.AvailableName.ZFIN),
    PANTHER("PANTHER", ForeignDB.AvailableName.PANTHER),
    NCBI_GENE("NCBIGene", ForeignDB.AvailableName.GENE),
    UNITPROT_KB("UniProtKB", ForeignDB.AvailableName.UNIPROTKB, ForeignDB.AvailableName.UNIPROTKB_KW),
    ENSEMBL("Ensembl", ForeignDB.AvailableName.ENSEMBL, ForeignDB.AvailableName.ENSEMBL_CLONE, ForeignDB.AvailableName.ENSEMBL_GRCZ10_);

    private String displayName;
    private List<ForeignDB.AvailableName> nameList;

    DataProvider(String displayName, ForeignDB.AvailableName... names) {
        this.displayName = displayName;
        nameList = new ArrayList<>();
        nameList.addAll(Arrays.asList(names));
    }

    public static String getExternalDatabaseName(ForeignDB.AvailableName name) {
        for (DataProvider provider : values()) {
            if (provider.getNameList().contains(name))
                return provider.getDisplayName();
        }
        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<ForeignDB.AvailableName> getNameList() {
        return nameList;
    }
}
