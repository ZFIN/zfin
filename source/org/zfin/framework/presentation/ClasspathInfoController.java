package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Action class retrieve classes used in the application.
 */
public class ClasspathInfoController extends AbstractCommandController {

    public ClasspathInfoController(){
        setCommandClass(ClasspathInfoBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        ClasspathInfoBean formBean = (ClasspathInfoBean) command;

        retrieveBootClasses(formBean);
        retrieveExtensionClasses(formBean);
        retrieveApplicationClasses(formBean);
        retrieveWebInfClasses(formBean);

        if (formBean.isInquireClassName()) {

            String className = formBean.getClassName();
            if (!StringUtils.isEmpty(className)) {
                Object o = null;
                boolean successful = false;
                try {
                    o = Class.forName(className.trim()).newInstance();
                    successful = true;
                } catch (InstantiationException e) {
                    formBean.setErrorMessage("Class could not be instantiated because class is either an interface or an abstract class");
                } catch (IllegalAccessException e) {
                    formBean.setErrorMessage("Class could not be instantiated because of restricted access. Probably, the class does not have a default constructor.");
                } catch (ClassNotFoundException e) {
                    formBean.setErrorMessage("Class could not be found in the classpath");
                }
                if (successful) {
                    Class<? extends Object> aClass = o.getClass();
                    formBean.setFullClassName(aClass.getName());
                    ClassLoader classLoader = aClass.getClassLoader();
                    formBean.setClassLoaderName(classLoader.getClass().getName());
                    List<ClassLibraryWrapper> parents = createClassLoaderParents(classLoader);
                    formBean.setClassLoaderParents(parents);
                    URL location = o.getClass().getProtectionDomain().getCodeSource().getLocation();
                    formBean.setClassFileName(location.getFile());
                }
            }
        }

        return new ModelAndView("classpath-info", "classpathForm", formBean);
    }

    private List<ClassLibraryWrapper> createClassLoaderParents(ClassLoader classLoader) {
        List<ClassLibraryWrapper> list = new ArrayList<ClassLibraryWrapper>();
        while (classLoader != null) {
            ClassLoader cl = classLoader.getParent();
            if (cl == null)
                break;
            ClassLibraryWrapper wrapper = new ClassLibraryWrapper();
            wrapper.setLibaryFileName(cl.getClass().getName());
            list.add(wrapper);
            classLoader = cl;
        }
        return list;
    }

    private void retrieveWebInfClasses(ClasspathInfoBean classpathForm) {
        String webDir = ZfinPropertiesEnum.WEBROOT_DIRECTORY.value();
        File webInf = new File(webDir, "WEB-INF");
        File classes = new File(webInf, "classes");
        File lib = new File(webInf, "lib");
        List<ClassLibraryWrapper> fullList = new ArrayList<ClassLibraryWrapper>();
        ClassLibraryWrapper wrapper = new ClassLibraryWrapper();
        wrapper.setLibaryFileName(classes.getAbsolutePath());
        fullList.add(wrapper);
        List<ClassLibraryWrapper> list = getJars(lib.getAbsolutePath());
        fullList.addAll(list);
        classpathForm.setClassesLibraries(fullList);
    }

    private void retrieveApplicationClasses(ClasspathInfoBean classpathForm) {
        String applicationClasspath = System.getProperty("java.class.path");
        List<ClassLibraryWrapper> list = createListOfJars(applicationClasspath);
        classpathForm.setApplicationLibraries(list);
    }

    private void retrieveExtensionClasses(ClasspathInfoBean classpathForm) {
        String bootClasspath = System.getProperty("java.ext.dirs");
        classpathForm.setExtensionLibraries(getJars(bootClasspath));
    }

    private void retrieveBootClasses(ClasspathInfoBean classpathForm) {
        String bootClasspath = System.getProperty("sun.boot.class.path");
        List<ClassLibraryWrapper> list = createListOfJars(bootClasspath);
        classpathForm.setBootLibraries(list);

    }

    private List<ClassLibraryWrapper> createListOfJars(String bootClasspath) {
        String fileSeparator = System.getProperty("path.separator");

        List<ClassLibraryWrapper> list = new ArrayList<ClassLibraryWrapper>();
        StringTokenizer tokenizer = new StringTokenizer(bootClasspath, fileSeparator);
        while (tokenizer.hasMoreElements()) {
            ClassLibraryWrapper wrapper = new ClassLibraryWrapper();
            wrapper.setLibaryFileName((String) tokenizer.nextElement());
            list.add(wrapper);
        }
        return list;
    }

    public List<ClassLibraryWrapper> getJars(String directoryName) {
        File directory = new File(directoryName);

        if (!directory.exists()) {
            return null;
        } else {
            List<ClassLibraryWrapper> list = new ArrayList<ClassLibraryWrapper>();
            String[] allFiles = directory.list();
            if (allFiles != null) {
                for (String allFile : allFiles) {
                    if (allFile.endsWith(".jar") || allFile.endsWith(".zip")) {
                        File f = new File(directory, allFile);
                        ClassLibraryWrapper wrapper = new ClassLibraryWrapper();
                        wrapper.setFile(f);
                        wrapper.setLibaryFileName(f.getAbsolutePath());
                        list.add(wrapper);
                    }
                }
            }
            return list;
        }
    }


}
