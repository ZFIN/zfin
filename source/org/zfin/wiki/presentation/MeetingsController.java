package org.zfin.wiki.presentation;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
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
import org.zfin.wiki.service.WikiWebService;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Log4j2
public class MeetingsController {

    private WikiWebService instance = WikiWebService.getInstance();

    public MeetingsController() throws WikiLoginException {
    }

    @Autowired
    HttpServletRequest request;

    @RequestMapping(value = "/wiki/{wikiSpaceName}")
    public JsonResultResponse<WikiPage> getMeetingsWikiPages(@PathVariable String wikiSpaceName,
                                                             @Version Pagination pagination) throws IOException {
        final String note = "Retrieving wiki pages from space: " + wikiSpaceName;
        log.info(note);
        long start = System.currentTimeMillis();
        String url1 = "https://zfin.atlassian.net/wiki/rest/api/content/search?cql=space%3D";
        url1 += wikiSpaceName;
        if (wikiSpaceName.equals("jobs") || wikiSpaceName.equals("news"))
            url1 += "%20AND%20type%3Dblogpost%20AND%20created%20%3E%3D%20now%28%22-120d%22%29%20ORDER%20BY%20created%20desc&limit=5&expand=history";
        else
            url1 += "%20AND%20type%3Dpage%20AND%20created%20%3E%3D%20now%28%22-120d%22%29%20ORDER%20BY%20created%20desc&limit=5&expand=history";
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
        return response;
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
