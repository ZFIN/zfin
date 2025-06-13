package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.zfin.properties.ZfinPropertiesEnum;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CloneJBrowseController {

    private Logger logger = LogManager.getLogger(CloneJBrowseController.class);

    private @Autowired
    HttpServletRequest request;

    @RequestMapping("/jbrowse/proxy/**")
    public ResponseEntity<String> getProxy(Model model) {
        String pathInfo = request.getPathInfo();
        String relativePath = pathInfo.replace("/api/jbrowse/proxy/jbrowse/", "");
        String queryString = request.getQueryString() == null ?
                "" :
                "?" + request.getQueryString();

        String proxyDestinationUri = ZfinPropertiesEnum.JBROWSE_BASE_URL.value() +
                "/" +
                relativePath +
                queryString;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        ResponseEntity<String> response;
        try {
            response = restTemplate.getForEntity(proxyDestinationUri, String.class);
        } catch (HttpClientErrorException e) {
            response = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        }

        logger.debug("PROXY RESPONSE (" + proxyDestinationUri + ") " + response.getStatusCode());

        return response;
    }

}
