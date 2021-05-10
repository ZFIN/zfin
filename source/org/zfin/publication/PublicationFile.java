package org.zfin.publication;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.zfin.framework.api.View;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "publication_file")
public class PublicationFile implements Comparable<PublicationFile> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pf_pk_id")
    @JsonView(View.Default.class)
    private long id;

    @ManyToOne
    @JoinColumn(name = "pf_pub_zdb_id")
    private Publication publication;

    @Column(name = "pf_file_name")
    private String fileName;

    @Column(name = "pf_original_file_name")
    @JsonView(View.Default.class)
    private String originalFileName;

    @ManyToOne
    @JoinColumn(name = "pf_file_type_id")
    @JsonView(View.Default.class)
    private PublicationFileType type;

    @JsonView(View.Default.class)
    public String getFullPath() {
        return ZfinPropertiesEnum.PDF_LOAD + "/" + fileName;
    }

    @Override
    public int compareTo(PublicationFile o) {
        int typeCompare = type.compareTo(o.getType());
        if (typeCompare != 0) {
            return typeCompare;
        }
        return ObjectUtils.compare(originalFileName, o.getOriginalFileName());
    }
}
