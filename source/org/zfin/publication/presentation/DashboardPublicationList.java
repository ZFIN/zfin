package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class DashboardPublicationList {

    @JsonView(View.API.class) int totalCount;
    @JsonView(View.API.class) List<DashboardPublicationBean> publications;
    @JsonView(View.API.class) Map<String, Long> statusCounts;

}
