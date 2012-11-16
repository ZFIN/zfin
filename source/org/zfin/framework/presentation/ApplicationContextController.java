package org.zfin.framework.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.support.RequestContextUtils;
import sun.management.VMManagement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Controller that obtains the meta data for the database.
 */
public class ApplicationContextController extends AbstractCommandController {

    public ApplicationContextController() {
        setCommandClass(ApplicationContextBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        ApplicationContextBean form = (ApplicationContextBean) command;
        WebApplicationContext context = RequestContextUtils.getWebApplicationContext(request);
        form.setApplicationContext(context);

        ModelAndView modelAndView = new ModelAndView("application-context-info", LookupStrings.FORM_BEAN, form);
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

        modelAndView.addObject("runtimeMXBean", runtime);

        Field jvm = runtime.getClass().getDeclaredField("jvm");
        jvm.setAccessible(true);
        VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
        Method pidMethod = mgmt.getClass().getDeclaredMethod("getProcessId");
        pidMethod.setAccessible(true);
        int pid = (Integer) pidMethod.invoke(mgmt);
        modelAndView.addObject("pid", pid);
        return modelAndView;
    }


}

