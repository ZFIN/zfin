package org.zfin.sequence.reno;

import org.apache.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This abstract class represents a single reno run.
 * Each run has a Set of RunCandidate's as well as information pertaining to the
 * run, itself.
 */
@Entity
@Table(name = "run")
@DiscriminatorColumn(name = "run_type")
public abstract class Run {

    @Transient
    Logger logger = Logger.getLogger(Run.class);

    public enum Type {

        REDUNDANCY("Redundancy"),
        NOMENCLATURE("Nomenclature");

        //        private final String value  ;
        private String value;

        Type(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }


        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No run type of string " + type + " found.");
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "RUN"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "run_zdb_id")
    private String zdbID;
    @Column(name = "run_name")
    private String name;
    @Column(name = "run_program")
    private String program;
    @Column(name = "run_date")
    private Date date;
    @Transient
    private String markerComment;
    @Column(name = "run_blastdb")
    private String blastDatabase;
    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RunCandidate> candidates = new HashSet<>();
    @ManyToOne
    @JoinColumn(name = "run_nomen_pub_zdb_id")
    private Publication nomenclaturePublication;

    /**
     * Total number of RunCandidates in queue (not locked or finished)
     *
     * @return number of queue candidates
     */
    public int getQueueCandidateCount() {
        int count = 0;
        for (RunCandidate rc : candidates) {
            if (!rc.isDone() && !rc.isLocked())
                count++;
        }
        return count;
    }


    /**
     * Total number of locked candidates.
     *
     * @return number of pending candidates
     */
    public int getPendingCandidateCount() {
        int count = 0;
        for (RunCandidate rc : candidates) {
            if (!rc.isDone() && rc.isLocked())
                count++;
        }
        return count;
    }

    /**
     * Total number of RunCandidates marked as finished
     *
     * @return number of finished candidates
     */
    public int getFinishedCandidateCount() {
        int count = 0;
        for (RunCandidate rc : candidates) {
            if (rc.isDone())
                count++;
        }
        return count;
    }

    public boolean isRedundancy() {
        return getType() == Type.REDUNDANCY;
    }

    public boolean isNomenclature() {
        return getType() == Type.NOMENCLATURE;
    }


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public abstract Type getType();


    public String getMarkerComment() {
        return markerComment;
    }

    public void setMarkerComment(String markerComment) {
        this.markerComment = markerComment;
    }

    public String getBlastDatabase() {
        return blastDatabase;
    }

    public void setBlastDatabase(String blastDatabase) {
        this.blastDatabase = blastDatabase;
    }

    public Set<RunCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(Set<RunCandidate> candidates) {
        this.candidates = candidates;
    }


    public Publication getNomenclaturePublication() {
        return nomenclaturePublication;
    }

    public void setNomenclaturePublication(Publication nomenclaturePublication) {
        this.nomenclaturePublication = nomenclaturePublication;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Run [BO]");
        sb.append("\n\t");
        sb.append("zdbID: ").append(zdbID);
        sb.append("\n\t");
        sb.append("name: ").append(name);
        sb.append("\n\t");
        sb.append("program: ").append(program);
        sb.append("\n\t");
        sb.append("date: ").append(date);
        sb.append("\n\t");
        sb.append("blastDatabase: ").append(blastDatabase);
        sb.append("\n\t");
        sb.append("nomenclature publication: ").append(nomenclaturePublication);
        return sb.toString();
    }


}


