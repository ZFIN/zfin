package org.zfin.expression;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.util.Set;

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

    /**
     * Movie containers stored under the load-up areas. Used to recognise a still whose
     * img_image names the movie itself rather than a picture -- see
     * {@link Image#isInlineVideo()}.
     */
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "mov", "m4v", "ogv", "avi");

    public static boolean isVideoFilename(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        return extension != null && VIDEO_EXTENSIONS.contains(extension.toLowerCase());
    }
}
