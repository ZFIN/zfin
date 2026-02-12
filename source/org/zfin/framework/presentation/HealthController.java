package org.zfin.framework.presentation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

@RestController
@RequestMapping("/devtool")
public class HealthController {

    @RequestMapping("/health")
    @ResponseBody
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        Date now = new Date();

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        result.put("timestamp", iso.format(now));
        result.put("jvmTimezone", TimeZone.getDefault().getID());
        result.put("status", "UP");

        return result;
    }
}
