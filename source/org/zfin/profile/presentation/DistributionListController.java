package org.zfin.profile.presentation;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.profile.repository.ProfileRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class DistributionListController {

    @Autowired
    ProfileRepository profileRepository;

    @RequestMapping("/distribution-list")
    public void getDistributionList(HttpServletResponse response,
                                    @RequestParam(required = false, defaultValue = "") String subset) throws IOException {

        SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = fileNameFormat.format(new Date());

        List<String> distList;
        String prefix;
        if (subset.equals("pi")) {
            distList = profileRepository.getPiDistributionList();
            prefix = "PI_dist";
        } else if (subset.equals("usa")) {
            distList = profileRepository.getUsaDistributionList();
            prefix = "USA_dist";
        } else {
            distList = profileRepository.getDistributionList();
            prefix = "dist";
        }

        response.addHeader("Content-disposition", String.format("attachment;filename=%s_%s.txt", prefix, fileName));
        response.setContentType("txt/plain");
        IOUtils.write(StringUtils.join(distList, "\n"), response.getOutputStream(), StandardCharsets.ISO_8859_1);
        response.flushBuffer();
    }
}
