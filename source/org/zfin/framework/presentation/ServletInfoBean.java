package org.zfin.framework.presentation;

import javax.servlet.ServletContext;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 */
public class ServletInfoBean {

    private HashSet attributes;
    private ServletContext context;

    public HashMap getAttributes(){
        Enumeration num = context.getAttributeNames();
        HashMap set = new HashMap();
        if(num != null){
            while (num.hasMoreElements()){
                String name = (String) num.nextElement();
                set.put(name, context.getAttribute(name));
            }
        }
        return set;
    }

    public HashMap getInitializationParameters(){
        Enumeration num = context.getInitParameterNames();
        HashMap set = new HashMap();
        if(num != null){
            while (num.hasMoreElements()){
                String name = (String) num.nextElement();
                set.put(name, context.getInitParameter(name));
            }
        }
        return set;
    }

    public ServletContext getContext() {
        return context;
    }

    public void setContext(ServletContext context) {
        this.context = context;
    }

    public String getRealPath(){
        return context.getRealPath("");
    }

    public HashSet getClasspath(){
        HashSet<String> set = new HashSet<String>();
        String classpath = (String) context.getAttribute("org.apache.catalina.jsp_classpath");
        String delimiter = System.getProperty("path.separator");
        if(classpath != null){
            StringTokenizer st = new StringTokenizer(classpath, delimiter);
            while(st.hasMoreTokens()){
                set.add(st.nextToken());
            }
        }
        return set;
    }
}
