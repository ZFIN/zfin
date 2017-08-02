package org.zfin.sequence;


import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;


public class GenomeFeature {

    private String seqid;
    private String source;
    private String type;
    private String start;
    private String end;
    private String score;
    private String strand;
    private String phase;
    private Map<String, String> attributes;

    public static String ID = "ID";
    public static String GENE_ID = "gene_id";
    public static String PARENT = "Parent";
    public static String ZDB_ID = "zdb_id";


    public GenomeFeature(String gff3Line ) {
        String[] fields = gff3Line.split("\\t");
        seqid = fields[0];
        source = fields[1];
        type   = fields[2];
        start  = fields[3];
        end    = fields[4];
        score  = fields[5];
        strand = fields[6];
        phase  = fields[7];
        attributes = parseAttributes(fields[8]);

    }

    private Map<String,String> parseAttributes(String attributeLine) {
        return Arrays.asList(attributeLine.split(";")).stream()
                .map(element -> element.split("="))
                .filter(elem -> elem.length == 2)
                .collect(Collectors.toMap(e -> e[0], e -> e[1]));
    }


    public String getId() {
        String id = attributes.get(ID);
        if (id != null) { return id; }

        id = attributes.get(GENE_ID);
        if (id != null) { return id; }

        //give up
        return id;
    }

    public String getParent() {
        return attributes.get(PARENT);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(seqid);
        sb.append("\t");
        sb.append(source);
        sb.append("\t");
        sb.append(type);
        sb.append("\t");
        sb.append(start);
        sb.append("\t");
        sb.append(end);
        sb.append("\t");
        sb.append(score);
        sb.append("\t");
        sb.append(strand);
        sb.append("\t");
        sb.append(phase);
        sb.append("\t");
        sb.append(flattenAttributes());

        return sb.toString();
    }


    public String flattenAttributes() {
        return attributes.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(";"));
    }

    public void addAttribute(String label, String value) {
        attributes.put(label, value);
    }

    public String getSeqid() {
        return seqid;
    }

    public void setSeqid(String seqid) {
        this.seqid = seqid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
