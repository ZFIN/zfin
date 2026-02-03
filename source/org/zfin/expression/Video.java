package org.zfin.expression;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "video")
@Getter
@Setter
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_pk_id")
    private Long id;

    @Column(name = "video_path_to_file", nullable = false)
    private String videoFilename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_image_still_zdb_id")
    private Image still;
}
