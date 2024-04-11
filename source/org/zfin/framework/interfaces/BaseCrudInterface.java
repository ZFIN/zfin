package org.zfin.framework.interfaces;

import com.fasterxml.jackson.annotation.JsonView;
import org.alliancegenome.curation_api.view.View;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.api.ObjectResponse;
import org.zfin.framework.api.SearchResponse;
import org.zfin.framework.entity.BaseEntity;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.HashMap;

@RequestMapping("/api/indexer")
public interface BaseCrudInterface<E extends BaseEntity> {

    @POST
    @Path("/")
    @JsonView({org.zfin.framework.api.View.Default.class})
    public ObjectResponse<E> create(E entity);

    @POST
    @Path("/find")
    @JsonView(View.FieldsAndLists.class)
    public SearchResponse<E> find(@DefaultValue("0") @QueryParam("page") Integer page, @DefaultValue("10") @QueryParam("limit") Integer limit, @RequestBody HashMap<String, Object> params);

}
