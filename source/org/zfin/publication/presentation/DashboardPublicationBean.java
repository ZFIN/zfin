package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.curation.presentation.CurationStatusDTO;
import org.zfin.expression.Image;
import org.zfin.framework.api.View;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class DashboardPublicationBean {

    @JsonView(View.API.class) private String zdbId;
    @JsonView(View.API.class) private String title;
    @JsonView(View.API.class) private String citation;
    @JsonView(View.API.class) private String authors;
    @JsonView(View.API.class) private String abstractText;
    @JsonView(View.API.class) private List<Image> images;
    @JsonView(View.API.class) private String pdfPath;
    @JsonView(View.API.class) private CurationStatusDTO status;
    @JsonView(View.API.class) private Date lastCorrespondenceDate;
    @JsonView(View.API.class) private int numberOfCorrespondences;
    @JsonView(View.API.class) private List<ProcessingTaskBean> processingTasks;
    @JsonView(View.API.class) private boolean canShowImages;
    @JsonView(View.API.class) private List<String> relatedLinks;

}
