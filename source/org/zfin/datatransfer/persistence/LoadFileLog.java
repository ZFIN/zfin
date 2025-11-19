package org.zfin.datatransfer.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "load_file_log")
@Getter
@Setter
public class LoadFileLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lfl_id")
    private Long lfl_id;

    @Column(name = "lfl_load_name")
    private String loadName;

    @Column(name = "lfl_filename")
    private String filename;

    @Column(name = "lfl_source")
    private String source;

    @Column(name = "lfl_date")
    private Date date;

    @Column(name = "lfl_size")
    private Long size;

    @Column(name = "lfl_md5")
    private String md5;

    @Column(name = "lfl_path")
    private String path;

    @Column(name = "lfl_download_date")
    private Date downloadDate;

    @Column(name = "lfl_release_number")
    private String releaseNumber;

    @Column(name = "lfl_notes")
    private String notes;

    @Column(name = "lfl_processed_date")
    private Date processedDate;

}
