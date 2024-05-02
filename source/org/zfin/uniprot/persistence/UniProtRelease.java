package org.zfin.uniprot.persistence;

import lombok.Getter;
import lombok.Setter;
import org.zfin.properties.ZfinPropertiesEnum;

import jakarta.persistence.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Date;

@Entity
@Table(name = "uniprot_release")
@Getter
@Setter
public class UniProtRelease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upr_id")
    private Long upr_id;

    @Column(name = "upr_date")
    private Date date;

    @Column(name = "upr_size")
    private Long size;

    @Column(name = "upr_md5")
    private String md5;

    @Column(name = "upr_path")
    private String path;

    @Column(name = "upr_download_date")
    private Date downloadDate;

    @Column(name = "upr_release_number")
    private String releaseNumber;

    @Column(name = "upr_notes")
    private String notes;

    @Column(name = "upr_processed_date")
    private Date processedDate;

    @Column(name = "upr_secondary_load_date")
    private Date secondaryLoadDate;

    public File getLocalFile() {
        Path parentDirectoryPath = (new File(ZfinPropertiesEnum.UNIPROT_RELEASE_ARCHIVE_DIR.value())).toPath();
        return new File(parentDirectoryPath.resolve(path).toString());
    }
}
