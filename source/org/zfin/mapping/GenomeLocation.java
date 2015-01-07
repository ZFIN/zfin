package org.zfin.mapping;

import org.zfin.gwt.root.util.StringUtils;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.Serializable;

/**
 * Genome Location entity for NCBI, Vega, Ensembl and other sources for physical location.
 */
public class GenomeLocation implements Serializable, Comparable<GenomeLocation> {

    // total length being displayed in gBrowse
    public static final long TOTAL_LENGTH = 500000L;

    protected long ID;
    protected String start;
    protected String end;
    protected String entityID;
    protected String chromosome;
    protected Source source;
    protected String detailedSource;
    protected String accessionNumber;
    protected GenomeBrowserMetaData metaData;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public GenomeBrowserMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(GenomeBrowserMetaData metaData) {
        this.metaData = metaData;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getUrl() {
        return source.getUrl() + accessionNumber;
    }

    public String getDetailedSource() {
        return detailedSource;
    }

    public void setDetailedSource(String detailedSource) {
        this.detailedSource = detailedSource;
    }

    public Long getCenteredStart() {
        if (start == null)
            return null;
        long startLoc = Long.parseLong(start);
        if (end == null)
            return null;
        long endLoc = Long.parseLong(end);
        long middlePoint = startLoc + (endLoc - startLoc) / 2;
        if (startLoc < TOTAL_LENGTH) {
            return 0L;
        } else {
            return middlePoint - TOTAL_LENGTH / 2;
        }
    }

    public Long getCenteredEnd() {
        if (start == null)
            return null;
        long startLoc = Long.parseLong(start);
        if (end == null)
            return null;
        long endLoc = Long.parseLong(end);
        long middlePoint = startLoc + (endLoc - startLoc) / 2;
        return middlePoint + TOTAL_LENGTH / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenomeLocation that = (GenomeLocation) o;

        if (chromosome != null ? !chromosome.equals(that.chromosome) : that.chromosome != null) return false;
        if (end != null)
            if (!end.equals(that.end)) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (start != null)
            if (!start.equals(that.start)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (start != null)
            result = 31 * result + start.hashCode();
        if (end != null)
            result = 31 * result + end.hashCode();
        result = 31 * result + (chromosome != null ? chromosome.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(GenomeLocation o) {
        if (o == null)
            return -1;
        return source.compareTo(o.getSource());
    }

    public enum Source {
        ZFIN("ZfinGbrowseStartEndLoader", true, "ZFIN Gbrowse", "/" + ZfinPropertiesEnum.GBROWSE_PATH_FROM_ROOT + "?name="),
        ENSEMBL("EnsemblStartEndLoader", true, "Ensembl", "http://www.ensembl.org/Danio_rerio/Location/View?db=core;g="),
        VEGA("VegaStartEndLoader", true, "Vega", "http://vega.sanger.ac.uk/Danio_rerio/Location/View?db=core;g="),
        NCBI("NCBIStartEndLoader", true, "NCBI Map Viewer", "http://www.ncbi.nlm.nih.gov/mapview/map_search.cgi?direct=on&idtype=gene&id="),
        UCSC("UCSCStartEndLoader", true, "UCSC", "http://genome.ucsc.edu/cgi-bin/hgTracks?org=Zebrafish&db=danRer7&position="),
        GENERAL_LOAD("General Load", false, "General Load", null),
        OTHER_MAPPING("other map location", false, "Other Mapping", null);

        private String name;
        private String displayName;
        private String url;
        private boolean physicalMappingLocation;

        Source(String name, boolean physical, String display, String url) {
            this.name = name;
            this.physicalMappingLocation = physical;
            this.displayName = display;
            this.url = url;
        }


        public boolean isPhysicalMappingLocation() {
            return physicalMappingLocation;
        }

        public boolean is2ndDegreePhysicalMappingLocation() {
            return name.equals(GENERAL_LOAD.getName());
        }

        public static Source getSource(String name) {
            name = name.trim();
            if (StringUtils.isEmpty(name))
                return null;
            for (Source source : values())
                if (source.name.equals(name))
                    return source;
            return null;
        }

        public boolean isGeneticMappingLocation() {
            return !physicalMappingLocation;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getUrl() {
            return url;
        }

    }
}
