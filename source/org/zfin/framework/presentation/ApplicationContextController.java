package org.zfin.framework.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;

/**
 * Controller that obtains the meta data for the database.
 */
@Controller
public class ApplicationContextController {

    @Autowired
    private HttpServletRequest request;

    @RequestMapping("/application-context")
    protected String showApplicationContext(@ModelAttribute("formBean") ApplicationContextBean form,
                                            Model model)
            throws Exception {

        WebApplicationContext context = RequestContextUtils.getWebApplicationContext(request);
        form.setApplicationContext(context);

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        model.addAttribute("runtimeMXBean", runtime);
        Field jvm = runtime.getClass().getDeclaredField("jvm");
        jvm.setAccessible(true);
        return "dev-tools/application-context.page";
    }


}

