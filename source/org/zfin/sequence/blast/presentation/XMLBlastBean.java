package org.zfin.sequence.blast.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.sequence.blast.BlastQueryThreadCollection;
import org.zfin.sequence.blast.BlastThreadService;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.BlastResultBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class XMLBlastBean extends BlastInfoBean implements Cloneable {

    public final static String BLAST_PREFIX = "blast";
    public final static String BLAST_SUFFIX = ".xml";

    private List<Database> actualDatabaseTargets;
    private List<XMLBlastBean> otherQueries;

    private Logger logger = Logger.getLogger(XMLBlastBean.class) ;


    public static enum QueryTypes {
        FASTA, SEQUENCE_ID, UPLOAD
    }

    int numChunks;
    int sliceNumber;

    // interface parameter
    private String queryType = QueryTypes.FASTA.toString();

    private int refreshTime = 10; // refresh time in seconds

    // *blast* parameters
    private String querySequence;
    private Integer queryFrom;
    private Integer queryTo;
    private String program;
    private String dataLibraryString; // can represent more than one libraries
    private String sequenceID;
    private String sequenceType;  // nt or pt
    private String sequenceFile;  // for uploads
    private String matrix;  // location of matrix library
    private Double expectValue; //
    private Integer wordLength;

    // other options
    private Boolean dust = false;
    private Boolean poly_a = false;
    private Boolean seg = false;
    private Boolean xnu = false;
    private Boolean shortAndNearlyExact = false;

    private View alignmentView = View.PAIRWISE;

    // results
    private File resultFile;
    private BlastOutput blastOutput;
    private BlastResultBean blastResultBean;
    private String errorString = "";

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public List<Database> getActualDatabaseTargets() {
        return actualDatabaseTargets;
    }

    public void setActualDatabaseTargets(List<Database> actualDatabaseTargets) {
        this.actualDatabaseTargets = actualDatabaseTargets;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public void setProgram(Program program) {
        this.program = program.getValue();
    }

    public String getQuerySequence() {
        return querySequence;
    }

    public void setQuerySequence(String querySequence) {
        this.querySequence = querySequence;
    }

    public Double getExpectValue() {
        return expectValue;
    }

    public void setExpectValue(Double expectValue) {
        this.expectValue = expectValue;
    }

    public View getAlignmentView() {
        return alignmentView;
    }

    public void setAlignmentView(View alignmentView) {
        this.alignmentView = alignmentView;
    }

    public String getDataLibraryString() {
        return dataLibraryString;
    }

    public String getSequenceID() {
        return sequenceID;
    }

    public void setSequenceID(String sequenceID) {
        this.sequenceID = sequenceID;
    }

    public void setDataLibraryString(String dataLibraryString) {
        this.dataLibraryString = dataLibraryString;
    }

    public String getTicketNumber() {
        return getTicketNumber(resultFile);
    }

    public void setTicketNumber(String ticketNumber) {
        resultFile = new File(BLAST_PREFIX + ticketNumber + BLAST_SUFFIX);
    }

    public static String getTicketNumber(File file) {
        if (file == null) {
            return null;
        }
        // remove blast from the start and the suffix from the end
        String ticket = file.getName();
        if(ticket.startsWith(BLAST_PREFIX)){
            ticket = ticket.substring(BLAST_PREFIX.length());
        }
        if(ticket.contains(BLAST_SUFFIX)){
            ticket = ticket.substring(0,ticket.length()-BLAST_SUFFIX.length()) ;
        }
        return ticket;
    }

    public File getResultFile() {
        return resultFile;
    }

    public void setResultFile(File resultFile) {
        this.resultFile = resultFile;
    }

    public String getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(String sequenceType) {
        this.sequenceType = sequenceType;
    }

    public String getSequenceFile() {
        return sequenceFile;
    }

    public void setSequenceFile(String sequenceFile) {
        this.sequenceFile = sequenceFile;
    }

    public String getMatrix() {
        return matrix;
    }

    public void setMatrix(String matrix) {
        this.matrix = matrix;
    }

    public Boolean getDust() {
        return dust;
    }

    public void setDust(Boolean dust) {
        this.dust = dust;
    }

    public Boolean getPoly_a() {
        return poly_a;
    }

    public void setPoly_a(Boolean poly_a) {
        this.poly_a = poly_a;
    }

    public Boolean getSeg() {
        return seg;
    }

    public void setSeg(Boolean seg) {
        this.seg = seg;
    }

    public Boolean getXnu() {
        return xnu;
    }

    public void setXnu(Boolean xnu) {
        this.xnu = xnu;
    }

    public Boolean getShortAndNearlyExact() {
        return shortAndNearlyExact;
    }

    public void setShortAndNearlyExact(Boolean shortAndNearlyExact) {
        this.shortAndNearlyExact = shortAndNearlyExact;
    }

    public Integer getWordLength() {
        return wordLength;
    }

    public void setWordLength(Integer wordLength) {
        this.wordLength = wordLength;
    }

    public Integer getQueryFrom() {
        return queryFrom;
    }

    public void setQueryFrom(Integer queryFrom) {
        this.queryFrom = queryFrom;
    }

    public Integer getQueryTo() {
        return queryTo;
    }

    public void setQueryTo(Integer queryTo) {
        this.queryTo = queryTo;
    }

//    public boolean isGraphDisplay() {
//        return graphDisplay;
//    }
//
//    public void setGraphDisplay(boolean graphDisplay) {
//        this.graphDisplay = graphDisplay;
//    }

    public boolean isFileExists() {
        return resultFile != null
                && resultFile.exists()
                && false == BlastThreadService.isJobInQueue(this, BlastQueryThreadCollection.getInstance().getQueue());
    }

    public BlastOutput getBlastOutput() {
        return blastOutput;
    }

    public void setBlastOutput(BlastOutput blastOutput) {
        this.blastOutput = blastOutput;
    }

    public BlastResultBean getBlastResultBean() {
        return blastResultBean;
    }

    public void setBlastResultBean(BlastResultBean blastResultBean) {
        this.blastResultBean = blastResultBean;
    }

    public List<XMLBlastBean> getOtherQueries() {
        return otherQueries;
    }

    public void setOtherQueries(List<XMLBlastBean> otherQueries) {
        this.otherQueries = otherQueries;
    }

    public int getNumChunks() {
        return numChunks;
    }

    public void setNumChunks(int numChunks) {
        this.numChunks = numChunks;
    }

    public int getSliceNumber() {
        return sliceNumber;
    }

    public void setSliceNumber(int sliceNumber) {
        this.sliceNumber = sliceNumber;
    }

    public List<String> getOtherTickets() {
        List<String> tickets = new ArrayList<String>();
        if (false == CollectionUtils.isEmpty(otherQueries)) {
            for (XMLBlastBean otherQuery : otherQueries) {
                tickets.add(otherQuery.getTicketNumber());
            }
        }
        return tickets;
    }

    public static String getEmptyZFINParametersAsXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ZFINParameters/>").append("\n");
        return sb.toString();
    }

    public void getOtherTicketsAsXML(StringBuilder sb) {
        sb.append("<OtherTickets>").append("\n");
        if (false == CollectionUtils.isEmpty(otherQueries)) {
            for (XMLBlastBean otherQuery : otherQueries) {
                sb.append("<Ticket>")
                        .append(otherQuery.getTicketNumber())
                        .append("</Ticket>")
                        .append("\n");
            }
        }
        sb.append("</OtherTickets>").append("\n");
    }

    protected void getTargetDatabasesAsXML(StringBuilder sb) {
        sb.append("<TargetDatabases>");
        if (false == CollectionUtils.isEmpty(actualDatabaseTargets)) {
            for (Database database : actualDatabaseTargets) {
                sb.append("<TargetDatabase>")
                        .append(database.getAbbrev().toString())
                        .append("</TargetDatabase>");
            }
        }
        sb.append("</TargetDatabases>");
        sb.append("\n");
    }

    private void getStringAsXML(String elementName, String value, StringBuilder sb) {
        if (StringUtils.isNotEmpty(value)) {
            sb.append("<").append(elementName).append(">")
                    .append(value)
                    .append("</").append(elementName).append(">")
                    .append("\n");
        }
    }

    private void getNumberAsXML(String elementName, Integer number, StringBuilder sb) {
        if (number != null && number > 0) {
            sb.append("<").append(elementName).append(">")
                    .append(number)
                    .append("</").append(elementName).append(">")
                    .append("\n");
        }
    }

    public String getZFINParametersAsXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ZFINParameters>");
        getOtherTicketsAsXML(sb);
        getTargetDatabasesAsXML(sb);
        getStringAsXML("DataLibrary", getDataLibraryString(), sb);
        getNumberAsXML("WordLength", getWordLength(), sb);
        getNumberAsXML("SubSequenceFrom", getQueryFrom(), sb);
        getNumberAsXML("SubSequenceTo", getQueryTo(), sb);
        getStringAsXML("QueryType", getQueryType(), sb);
        getStringAsXML("SequenceFASTA", getQuerySequence(), sb);
        getStringAsXML("SequenceID", getSequenceID(), sb);
        getStringAsXML("SequenceFile", getSequenceFile(), sb);
        getNumberAsXML("PolyAFilter", (getPoly_a() ? 1 : 0), sb);
        getStringAsXML("ErrorData", getErrorString(), sb);
        sb.append("</ZFINParameters>").append("\n");
        return sb.toString();
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    /**
     * @return A deep copy of this file
     */
    @Override
    public XMLBlastBean clone() {
        XMLBlastBean xmlBlastBean = new XMLBlastBean();
        xmlBlastBean.setActualDatabaseTargets(actualDatabaseTargets);
        xmlBlastBean.setDataLibraryString(dataLibraryString);
        xmlBlastBean.setProgram(program);
        xmlBlastBean.setNumChunks(numChunks);
        xmlBlastBean.setQuerySequence(querySequence);
        xmlBlastBean.setAlignmentView(alignmentView);
        xmlBlastBean.setDust(dust);
        xmlBlastBean.setResultFile(resultFile);
        xmlBlastBean.setExpectValue(expectValue);
        xmlBlastBean.setMatrix(matrix);
        xmlBlastBean.setOtherQueries(otherQueries);
        xmlBlastBean.setPoly_a(poly_a);
        xmlBlastBean.setQueryFrom(queryFrom);
        xmlBlastBean.setQueryTo(queryTo);
        xmlBlastBean.setSeg(seg);
        xmlBlastBean.setSequenceFile(sequenceFile);
        xmlBlastBean.setSequenceID(sequenceID);
        xmlBlastBean.setSequenceType(sequenceType);
        xmlBlastBean.setWordLength(wordLength);
        xmlBlastBean.setXnu(xnu);
        return xmlBlastBean;
    }

    public static enum View {
        PAIRWISE("0"),
        XML("7"),
        TABULAR("8"),
        TABULAR_WCOMMENTS("9"),;

        private String value;

        View() {
            value = "0";
        }

        View(String i) {
            value = i;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * todo: map to database . type eventually
     */
    public static enum SequenceType {
        NUCLEOTIDE("nt"),
        PROTEIN("pt"),;
        private String value;

        SequenceType() {
            value = "nt";
        }

        SequenceType(String i) {
            value = i;
        }

        public String getValue() {
            return value;
        }


        public static SequenceType getSequenceType(String type) {
            for (SequenceType t : values()) {
                if (t.getValue().equals(type))
                    return t;
            }
            throw new RuntimeException("No SequenceType of string " + type + " found.");
        }
    }

    public static enum Matrix {
        BLOSUM62,
        BLOSUM45,
        BLOSUM80,
        PAM30,
        PAM70;

        private String value;

        Matrix() {
            value = "";
        }

        Matrix(String i) {
            value = i;
        }

        public String getValue() {
            return value;
        }
    }

    public static enum Program {
        BLASTN("blastn", "Nucleotide - Nucleotide"),
        BLASTP("blastp", "Protein - Protein"),
        BLASTX("blastx", "trans. Nucleotide - Protein"),
        TBLASTN("tblastn", "Protein - trans. Nucleotide"),
        TBLASTX("tblastx", "trans. Nucleotide - trans. Nucleotide"),;

        private String value;
        private String label;

        Program() {
            value = "blastn";
            label = "Nucleotide - Nucleotide";
        }

        Program(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public static Program getProgram(String type) {
            for (Program t : values()) {
                if (t.getValue().equals(type))
                    return t;
            }
            throw new RuntimeException("No SequenceType of string " + type + " found.");
        }

        public static SequenceType getSequenceTypeForProgram(String program) {
            return getSequenceTypeForProgram(getProgram(program));
        }

        public static SequenceType getSequenceTypeForProgram(Program program) {
            if (program == BLASTN
                    ||
                    program == BLASTX
                    ||
                    program == TBLASTX
                    ) {
                return SequenceType.NUCLEOTIDE;
            } else {
                return SequenceType.PROTEIN;
            }
        }

    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }

    /**
     * We really care about the resultFiles only unless once is null.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof XMLBlastBean) {
            XMLBlastBean xmlBlastBean = (XMLBlastBean) o;
            if (xmlBlastBean.getResultFile() == null || resultFile == null) {
                return super.equals(o);
            } else if (xmlBlastBean.getResultFile().getName().equals(resultFile.getName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "XMLBlastBean{" +
//                "actualDatabaseTargets=" + actualDatabaseTargets +
                "otherQueries=" + (otherQueries != null ? otherQueries.size() : "null") +
                ", numChunks=" + numChunks +
                ", sliceNumber=" + sliceNumber +
                ", queryType='" + queryType + '\'' +
                ", querySequence='" + querySequence + '\'' +
                ", queryFrom=" + queryFrom +
                ", queryTo=" + queryTo +
                ", program='" + program + '\'' +
                ", dataLibraryString='" + dataLibraryString + '\'' +
                ", sequenceID='" + sequenceID + '\'' +
                ", sequenceType='" + sequenceType + '\'' +
                ", sequenceFile='" + sequenceFile + '\'' +
                ", matrix='" + matrix + '\'' +
                ", expectValue=" + expectValue +
                ", wordLength=" + wordLength +
                ", dust=" + dust +
                ", poly_a=" + poly_a +
                ", seg=" + seg +
                ", xnu=" + xnu +
                ", alignmentView=" + alignmentView +
                ", resultFile=" + resultFile +
                ", blastOutput=" + blastOutput +
                ", blastResultBean=" + blastResultBean +
                ", errorString=" + errorString +
                '}';
    }

}
