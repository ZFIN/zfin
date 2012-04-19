package org.zfin.fish;

import java.util.Date;

/**
 * Business Class that maps to the fish mart release tracking table.
 */
public class WarehouseSummary {

    private long ID;
    private Date releaseDate;
    private String martName;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getMartName() {
        return martName;
    }

    public void setMartName(String martName) {
        this.martName = martName;
    }

    public enum Mart {
        FISH_MART("fish mart");

        private String type;

        private Mart(String type) {
            this.type = type;
        }

        public String getName() {
            return type;
        }
    }

}
