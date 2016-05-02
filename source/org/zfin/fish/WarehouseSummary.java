package org.zfin.fish;

import javax.persistence.*;
import java.util.Date;

/**
 * Business Class that maps to the fish mart release tracking table.
 */
@Entity
@Table(name = "warehouse_run_tracking")
public class WarehouseSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrt_pk_id")
    private long ID;
    @Column(name = "wrt_last_loaded_date")
    private Date releaseDate;
    @Column(name = "wrt_mart_name")
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
        FISH_MART("fish mart"),
        CONSTRUCT_MART("construct mart");

        private String type;

        private Mart(String type) {
            this.type = type;
        }

        public String getName() {
            return type;
        }
    }

}
