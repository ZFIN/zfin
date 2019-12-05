package org.zfin.framework.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Setter
@Getter
public class JsonResultResponse<T> {


    @JsonView(View.Default.class)
    private List<T> results = new ArrayList<T>();
    @JsonView(View.Default.class)
    private int total;
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

    public void setResults(List<T> results) {
        this.results = results;
        if (results != null) {
            returnedRecords = results.size();
        } else {
            this.results = new ArrayList<T>();
        }
    }

    public void setHttpServletRequest(HttpServletRequest request) {
        if (request == null)
            return;
        this.request = new Request();
        this.request.setUri(URLDecoder.decode(request.getRequestURI()));
        this.request.setParameterMap(request.getParameterMap());
    }

    public void calculateRequestDuration(long startTime) {
        // in seconds
        long duration = (System.currentTimeMillis() - startTime) / 1000;
        requestDuration = Long.toString(duration) + "s";

    }

    @JsonView({View.Default.class})
    public String getNextPageURL() {
        String uri = request.getUri();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Map.Entry<String, List<String>> entry : request.getParameterMap().entrySet()) {
            entry.getValue().forEach(value -> params.add(entry.getKey(), value));
        }
        Optional<Map.Entry<String, List<String>>> entry = params.entrySet().stream().filter(stringListEntry -> stringListEntry.getKey().equals("page"))
                .findFirst();
        // replace current page with next page
        entry.ifPresent(page -> page.setValue(List.of(String.valueOf(pagination.getNextPage()))));

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .path(uri)
                .queryParams(params)
                .build();

        return uriComponents.toUriString();
    }

}
