package org.zfin.framework.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Setter
@Getter
public class JsonResultResponse<T> {


    @JsonView(View.Default.class)
    private Collection<T> results = new ArrayList<T>();
    @JsonView(View.Default.class)
    private long total;
    @JsonView({View.Default.class})
    private int returnedRecords;
    @JsonView({View.Default.class})
    private String errorMessage = "";
    @JsonView({View.Default.class})
    private String note = "";
    @JsonView({View.Default.class})
    private String requestDuration;
    @JsonView({View.Default.class})
    private Request request;
    @JsonView({View.Default.class})
    private String apiVersion;
    @JsonView({View.Default.class})
    private String requestDate;
    @JsonView({View.Default.class})
    private Map<String, Object> supplementalData;
    @JsonIgnore
    private Pagination pagination;

    public JsonResultResponse() {
        requestDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public void calculateRequestDuration(LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = new Duration(startTime, endTime);
        requestDuration = duration.toString();
    }

    public void setResults(Collection<T> results) {
        this.results = results;
        if (results != null) {
            returnedRecords = results.size();
        } else {
            this.results = new ArrayList<T>();
        }
    }

    public void setHttpServletRequest(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        this.request = new Request();
        this.request.setUri(URLDecoder.decode(request.getRequestURI()));
        this.request.setParameterMap(request.getParameterMap());
    }

    public void calculateRequestDuration(long startTime) {
        // in seconds
        long duration = (System.currentTimeMillis() - startTime) / 1000;
        requestDuration = Long.toString(duration) + "s";

    }

    public void addSupplementalData(String attribute, Object object) {
        if (supplementalData == null) {
            supplementalData = new LinkedHashMap<>();
        }
        supplementalData.put(attribute, object);
    }

}
