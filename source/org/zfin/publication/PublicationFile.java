package org.zfin.publication;

import javax.persistence.*;

@Entity
@Table(name = "publication_file")
public class PublicationFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pf_pk_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "pf_pub_zdb_id")
    private Publication publication;

    @Column(name = "pf_file_name")
    private String fileName;

    @Column(name = "pf_original_file_name")
    private String originalFileName;

    @ManyToOne
    @JoinColumn(name = "pf_file_type_id")
    private PublicationFileType type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public PublicationFileType getType() {
        return type;
    }

    public void setType(PublicationFileType type) {
        this.type = type;
    }
}
