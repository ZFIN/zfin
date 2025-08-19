package org.zfin.mapping;

import lombok.Getter;
import lombok.Setter;
import org.zfin.gbrowse.GBrowseTrack;
import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.ontology.GenericTerm;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;

import java.io.Serializable;
import java.util.List;

import static org.zfin.mapping.GenomeLocation.Source.*;

/**
 * Genome Location entity for NCBI, Vega, Ensembl and other sources for physical location.
 */
@Setter
@Getter
public class GenomeLocation implements Serializable, Comparable<GenomeLocation> {

    public static String GRCZ11 = "GRCz11";
    public static String GRCZ10 = "GRCz10";
    public static String ZV9 = "Zv9";

    protected long ID;
    protected Integer start;
    protected Integer end;
    protected String entityID;
    protected String chromosome;
    protected Source source;
    protected String detailedSource;
    protected String accessionNumber;
    protected GenomeBrowserMetaData metaData;


    protected Publication attribution;
    protected GBrowseTrack gbrowseTrack;
    protected String assembly;
    private GenericTerm evidence;

    public String getUrl() {
        if (List.of(ZFIN, ZFIN_Zv9, ZFIN_NCBI).contains(source)) {
            return "/action/jbrowse/byName?name=" + accessionNumber + "&source=" + source.name();
        } else {
            return source.getUrl() + accessionNumber;
        }
    }

    public GenomeBrowserTrack getGenomeBrowserTrack() {
        if (gbrowseTrack == null) {
            return null;
        }
        return GBrowseTrack.convertGBrowseTrackToGenomeBrowserTrack(gbrowseTrack);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenomeLocation that = (GenomeLocation) o;

        if (chromosome != null ? !chromosome.equals(that.chromosome) : that.chromosome != null) return false;
        if (end != null) {
            if (!end.equals(that.end)) return false;
        }
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (start != null) {
            if (!start.equals(that.start)) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (start != null) {
            result = 31 * result + start.hashCode();
        }
        if (end != null) {
            result = 31 * result + end.hashCode();
        }
        result = 31 * result + (chromosome != null ? chromosome.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(GenomeLocation o) {
        if (o == null) {
            return -1;
        }
        return source.compareTo(o.getSource());
    }

    public enum Source {
        DIRECT("DirectSubmission", true, "Direct Data Submission", null),
        ZFIN_NCBI("ZFIN", true, "ZFIN Gbrowse", "/" + ZfinPropertiesEnum.GBROWSE_PATH_FROM_ROOT + "?name="),
        ZFIN("ZfinGbrowseStartEndLoader", true, "ZFIN Gbrowse", "/" + ZfinPropertiesEnum.GBROWSE_PATH_FROM_ROOT + "?name="),
        ENSEMBL("EnsemblStartEndLoader", true, "Ensembl", "http://www.ensembl.org/Danio_rerio/Location/View?db=core;g="),
        NCBI_LOADER("NCBILoader", true, "NCBI Map Viewer", "http://www.ncbi.nlm.nih.gov/genome/gdv/browser/?assm=GCF_049306965.1&context=gene&id="),
        NCBI("NCBIStartEndLoader", true, "NCBI Map Viewer", "http://www.ncbi.nlm.nih.gov/genome/gdv/browser/?assm=GCF_000002035.6&context=gene&id="),
        VEGA("VegaStartEndLoader", true, "Vega", "http://vega.sanger.ac.uk/Danio_rerio/Location/View?db=core;g="),
        //NCBI("NCBIStartEndLoader", true, "NCBI Map Viewer", "http://www.ncbi.nlm.nih.gov/mapview/map_search.cgi?direct=on&idtype=gene&id="),
        UCSC("UCSCStartEndLoader", true, "UCSC", "https://genome.ucsc.edu/cgi-bin/hgTracks?org=Zebrafish&db=danRer11&position="),
        ZFIN_Zv9("ZfinGbrowseZv9StartEndLoader", true, "ZFIN Zv9 GBrowse", "/" + ZfinPropertiesEnum.GBROWSE_ZV9_PATH_FROM_ROOT + "?name="),
        //        ZFIN_GRCz10("ZfinGbrowseGRCz10StartEndLoader", true, "ZFIN GRCz10 GBrowse", "/" + ZfinPropertiesEnum.GBROWSE_GRCZ10_PATH_FROM_ROOT + "?name="),
        AGP_LOAD("AGP Load", true, "AGP File Load", null),
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
            if (StringUtils.isEmpty(name)) {
                return null;
            }
            for (Source source : values()) {
                if (source.name.equals(name)) {
                    return source;
                }
            }
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
