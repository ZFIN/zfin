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

    @RequestMapping(value = {
            "2006_tutorial/tutorial.html",
            "anatomy.html",
            "anatomy/24hrs/**.html",
            "anatomy/48hrs/**.html",
            "anatomy/72hrs/**.html",
            "anatomy/120hrs/**.html",
            "anatomy/dev_atlas.html",
            "anatomy/dict/caud_fin/**.html",
            "anatomy/dict/ear/**.html",
            "anatomy/dict/hair_cell/**.html",
            "anatomy/dict/lat_line/**.html",
            "anatomy/dict/meeting_sum.html",
            "anatomy/dict/mem.html",
            "anatomy/dict/oto/**.html",
            "anatomy/dict/semi_canal/**.html",
            "anatomy/dict/sens/**.html",
            "anatomy/dict/stato/**.html",
            "anatomy/dict/Trevarrow.html",
            "author_guide_checklist.html",
            "author_guidelines.html",
            "blast_info.html",
            "catch/**.html",
            "dbase/PAPERS/sdb/**.html",
            "dbase/PAPERS/Web97/**.html",
            "dbase/PAPERS/Webnet97/**.html",
            "dbase/PAPERS/WWW6/**.html",
            "dbase/PAPERS/**.html",
            "defs.html",
            "glossary.html",
            "monitor/**.html",
            "monitor/vol1/**.html",
            "monitor/vol2.1/**.html",
            "monitor/vol2.2/**.html",
            "monitor/vol2.3/**.html",
            "monitor/vol2.4/**.html",
            "monitor/vol3.1/**.html",
            "monitor/vol3.2/**.html",
            "monitor/vol3.3/**.html",
            "monitor/vol3.4/**.html",
            "monitor/vol3.5/**.html",
            "monitor/vol3.6/**.html",
            "monitor/vol4.1/**.html",
            "monitor/vol5.1/**.html",
            "monitor/vol6.1/**.html",
            "monitor/vol7.1/**.html",
            "movies/movies.html",
            "news/**.html",
            "nomen.html",
            "sequence/**.html",
            "stars.html",
            "syntax_help.html",
            "zfbook/chapt1/**.html",
            "zfbook/chapt2/**.html",
            "zfbook/chapt3/**.html",
            "zfbook/chapt4/**.html",
            "zfbook/chapt5/**.html",
            "zfbook/chapt6.html",
            "zfbook/chapt7/**.html",
            "zfbook/chapt8/**.html",
            "zfbook/chapt9/**.html",
            "zfbook/chapt10.html",
            "zfbook/cont.html",
            "zfbook/lab_desig.html",
            "zfbook/stages/**.html",
            "zfbook/stages/figs/**.html",
            "zfbook/zfbk.html",
            "zfbook/zfmabs.html",
            "zfbook/zfstrn.html",
            "zfin_stats.html",
            "zfprbs.html"
    })
    public String viewStaticFile(Model model,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        path = FilenameUtils.removeExtension(path.substring(9)).replace("/", "--");
        return "zf_info/" + path;
    }

}
