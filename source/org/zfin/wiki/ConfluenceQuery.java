package org.zfin.wiki;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zfin.wiki.presentation.WikiPage;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class ConfluenceQuery {
    private static final int RESULTS_LIMIT = 5;
    private static final int CACHE_TIME_IN_SECONDS = 60 * 5;
    private static final boolean POPULATE_LABELS = false; // Set to true to fetch labels for each page (from separate API call per page)

    //TODO: replace this custom cache with Spring's @Cacheable when we get our gradle build updated
    private static final Duration CACHE_TTL = Duration.ofSeconds(CACHE_TIME_IN_SECONDS);
    private static final Map<String, List<WikiPage>> cache = new HashMap<>();
    private static final Map<String, Instant> cacheTimestamps = new HashMap<>();

    public List<WikiPage> getWikiPagesForSpaceUsingCache(String space)
            throws URISyntaxException, IOException {
        Instant lastFetchTime = cacheTimestamps.get(space);
        Instant now = Instant.now();

        if (lastFetchTime != null && Duration.between(lastFetchTime, now).compareTo(CACHE_TTL) < 0) {
            return cache.get(space);
        }

        List<WikiPage> list = getWikiPagesForSpaceOnCacheMiss(space);
        cache.put(space, list);
        cacheTimestamps.put(space, now);
        return list;
    }

    private List<WikiPage> getWikiPagesForSpaceOnCacheMiss(String space) throws URISyntaxException, IOException {
        String url = buildConfluenceAPIEndpoint(space);
        System.out.println("Confluence API URL: " + url);
        JSONObject json = readJsonFromUrl(url);
        List<WikiPage> list = convertApiResultToWikiPageList(json);

        if (POPULATE_LABELS) {
            addLabelsToWikiPages(list);
        }
        list = filterWikiPages(list);
        return list;
    }

    private List<WikiPage> convertApiResultToWikiPageList(JSONObject json) {
        return ((JSONArray) json.get("results")).toList().stream()
                .map(ConfluenceQuery::mapConfluenceJsonResultToWikiPage)
                .collect(Collectors.toList());
    }

    private static WikiPage mapConfluenceJsonResultToWikiPage(Object o) {
        HashMap<String, Object> map = (HashMap<String, Object>) o;
        WikiPage page = new WikiPage();
        page.setTitle((String) map.get("title"));
        page.setId(Long.parseLong((String) map.get("id")));
        String urlSubPath = (String) ((Map) map.get("_links")).get("webui");
        page.setUrl("https://zfin.atlassian.net/wiki" + urlSubPath);
        String dateIsoString = (String) ((Map)map.get("history")).get("createdDate"); //eg. "2024-06-10T14:23:45.678Z"
        Date createdDate = Date.from(Instant.parse(dateIsoString));
        page.setCreated(createdDate);
        return page;
    }

    private String buildConfluenceAPIEndpoint(String wikiSpaceName) throws URISyntaxException {
        // Validate wikiSpaceName
        Set<String> allowedSpaces = Set.of("jobs", "news", "meetings");
        if (!allowedSpaces.contains(wikiSpaceName)) {
            throw new IllegalArgumentException("Invalid wiki space: " + wikiSpaceName);
        }

        String pageType = Set.of("jobs", "news").contains(wikiSpaceName) ? "blogpost" : "page";

        // CQL to filter by creation date (last 120 days). We could remove the 120 days filter if we want all pages.
        // Then filter later based on labels or date.
        String cql = String.format("space=%s AND type=%s AND created >= now(\"-120d\") ORDER BY created desc",
                wikiSpaceName, pageType);

        URIBuilder builder = new URIBuilder("https://zfin.atlassian.net/wiki/rest/api/content/search")
                .addParameter("cql", cql)
                .addParameter("limit", String.valueOf(RESULTS_LIMIT))
                .addParameter("expand", "history,labels");

        String url = builder.build().toString();
        return url;
    }

    private void addLabelsToWikiPages(List<WikiPage> list) {
        Date now = new Date();

        for (WikiPage wikiPage : list) {
            try {
                String labelsUrl = String.format("https://zfin.atlassian.net/wiki/rest/api/content/%d/label", wikiPage.getId());
                System.out.println("Fetching labels from: " + labelsUrl);
                JSONObject labelsJson = readJsonFromUrl(labelsUrl);
                List<String> labels = ((JSONArray) labelsJson.get("results")).toList().stream()
                        .map(o -> {
                            HashMap<String, Object> map = (HashMap<String, Object>) o;
                            return (String) map.get("name");
                        })
                        .collect(Collectors.toList());
                wikiPage.setLabels(labels);
            } catch (IOException e) {
                System.err.println("Failed to fetch labels for page ID " + wikiPage.getId() + ": " + e.getMessage());
            }
        }
        Date afterRun = new Date();
        System.out.println("Time taken (ms): " + (afterRun.getTime() - now.getTime()));

    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    private List<WikiPage> filterWikiPages(List<WikiPage> pages) {
        return pages.stream()
                .filter(page -> {
                    //for now, we include anything more recent than 120 days or if it has any labels
                    long daysSinceCreation = ChronoUnit.DAYS.between(
                            page.getCreated().toInstant(),
                            Instant.now()
                    );
                    return daysSinceCreation <= 120 || (page.getLabels() != null && !page.getLabels().isEmpty());
                })
                .collect(Collectors.toList());
    }
}
