package org.zfin.framework.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.web.client.RestTemplate;
import org.zfin.AbstractSmokeTest;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class HealthEndpointSmokeTest extends AbstractSmokeTest {

    public HealthEndpointSmokeTest(WebClient webClient) {
        super(webClient);
    }

    @Test
    public void healthEndpointReturnsPacificTimestamp() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String url = nonSecureUrlDomain + "/action/devtool/health";

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        assertNotNull("Health response should not be null", response);
        assertThat(response.get("status"), is("UP"));

        String serverTimestamp = (String) response.get("timestamp");
        assertNotNull("Timestamp should be present", serverTimestamp);

        //Timezone should be Los Angeles
        String serverTimeZone = response.get("jvmTimezone").toString();
        assertEquals("Server timezone should be Los Angeles", "America/Los_Angeles", serverTimeZone);
    }
}
