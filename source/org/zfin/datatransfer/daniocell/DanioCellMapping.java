package org.zfin.datatransfer.daniocell;
//daniocell_mapping

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "daniocell_mapping")
@Data
public class DanioCellMapping {

    public static final String URL_BASE = "https://daniocell.nichd.nih.gov/gene/";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dcm_id")
    private long id;

    @Column(name = "dcm_symbol")
    private String symbol;
    @Column(name = "dcm_mrkr_zdb_id")
    private String markerZdbID;

    public String getFullUrl() {
        //escapedSymbol should only contain A-Z, a-z, 0-9, space, and parentheses
        String escapedSymbol = getSymbol().replaceAll("[^A-Za-z0-9 \\(\\)]", "-");
        String capitalFirstLetter = escapedSymbol.substring(0, 1).toUpperCase();
        return URL_BASE + capitalFirstLetter + "/" + escapedSymbol + "/" + escapedSymbol + ".html";
    }
}
