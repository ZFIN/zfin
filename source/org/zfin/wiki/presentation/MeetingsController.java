package org.zfin.wiki.presentation;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.wiki.WikiLoginException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Log4j2
public class MeetingsController {

    private static final long RESULTS_LIMIT = 5;

    public MeetingsController() throws WikiLoginException {
    }

    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse httpResponse;

    @RequestMapping(value = "/wiki/{wikiSpaceName}")
    public JsonResultResponse<WikiPage> getMeetingsWikiPages(@PathVariable String wikiSpaceName,
                                                             @Version Pagination pagination) throws IOException, URISyntaxException {
        final String note = "Retrieving wiki pages from space: " + wikiSpaceName;
        log.info(note);
        long start = System.currentTimeMillis();
        String url1 = buildConfluenceAPIEndpoint(wikiSpaceName);
        JSONObject json = readJsonFromUrl(url1);
        List<WikiPage> list = ((JSONArray) json.get("results")).toList().stream()
                .map(o -> {
                    HashMap<String, Object> map = (HashMap<String, Object>) o;
                    WikiPage page = new WikiPage();
                    page.setTitle((String) map.get("title"));
                    page.setId(Long.parseLong((String) map.get("id")));
                    String url = (String) ((Map) map.get("_links")).get("webui");
                    page.setUrl("https://zfin.atlassian.net/wiki"+url);
                    return page;
                }).collect(Collectors.toList());

        JsonResultResponse<WikiPage> response = new JsonResultResponse<>();
        response.setPagination(pagination);
        response.setResults(list);
        response.setHttpServletRequest(request);
        response.calculateRequestDuration(start);
        response.setNote(note);
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    private static String buildConfluenceAPIEndpoint(String wikiSpaceName) throws URISyntaxException {
        // Validate wikiSpaceName
        Set<String> allowedSpaces = Set.of("jobs", "news", "meetings");
        if (!allowedSpaces.contains(wikiSpaceName)) {
            throw new IllegalArgumentException("Invalid wiki space: " + wikiSpaceName);
        }

        String pageType = Set.of("jobs", "news").contains(wikiSpaceName) ? "blogpost" : "page";

        String cql = String.format("space=%s AND type=%s AND created >= now(\"-120d\") ORDER BY created desc",
                wikiSpaceName, pageType);

        URIBuilder builder = new URIBuilder("https://zfin.atlassian.net/wiki/rest/api/content/search")
                .addParameter("cql", cql)
                .addParameter("limit", String.valueOf(RESULTS_LIMIT))
                .addParameter("expand", "history");

        return builder.build().toString().replaceAll("\\+", "%20");
    }

    @Setter
    @Getter
    public
    class WikiPage {
        private long id;
        private String title;
        private String url;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

}
