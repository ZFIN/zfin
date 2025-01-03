package org.zfin.infrastructure.presentation;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/zf_info")
public class StaticFileController {

    //TODO: spring migration change, test this method works for all levels of html files
//    @RequestMapping("/**/{filename:.+}.html")
//    public String viewStaticFile(Model model,
//                                 HttpServletRequest request,
//                                 HttpServletResponse response) throws IOException {
//        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
//        path = FilenameUtils.removeExtension(path.substring(9)).replace("/", "--");
//        return "zf_info/" + path;
//    }

}
