package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.zfin.sequence.SNPMarkerSequence;

import java.util.List;

/**
 */
@Component
public class SnpMarkerBean extends MarkerBean {

    private Logger logger = LogManager.getLogger(SnpMarkerBean.class);

    private String variant;
    private SNPMarkerSequence sequence;
    private String ncbiBlastUrl;
    private String snpBlastUrl;

    /**
     * Most of the time there will only be a single sequence.
     *
     * @return
     */
    public SNPMarkerSequence getSequence() {
        return sequence;
    }


    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }


    public void setSequence(SNPMarkerSequence sequence) {
        this.sequence = sequence;
    }

    public String getNcbiBlastUrl() {
        return ncbiBlastUrl;
    }

    public void setNcbiBlastUrl(String ncbiBlastUrl) {
        this.ncbiBlastUrl = ncbiBlastUrl;
    }

    public String getSnpBlastUrl() {
        return snpBlastUrl;
    }

    public void setSnpBlastUrl(String snpBlastUrl) {
        this.snpBlastUrl = snpBlastUrl;
    }
}