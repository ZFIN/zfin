package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;
import org.zfin.publication.Publication;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkerReferenceBean {

    @JsonView(View.API.class)
    private String zdbID;

    @JsonView(View.API.class)
    private String title;

    @JsonView(View.API.class)
    private String dataZdbID;

    public static MarkerReferenceBean convert(Publication publication) {
        MarkerReferenceBean bean = new MarkerReferenceBean();
        bean.setZdbID(publication.getZdbID());
        bean.setTitle(publication.getTitle());
        return bean;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarkerReferenceBean that = (MarkerReferenceBean) o;
        return Objects.equals(zdbID, that.zdbID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zdbID);
    }
}
