package org.zfin.webdriver.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.util.servlet.ServletService;
import org.zfin.webdriver.WebExplode;
import org.zfin.webdriver.repository.WebExplodeRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

/**
 */
public class WebdriverController extends AbstractCommandController {

    private WebExplodeRepository webExplodeRepository;
    private String salt;

    private static final Logger LOG = Logger.getLogger(WebdriverController.class);

    public WebdriverController() {
        setCommandClass(WebExplode.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        WebExplode explode = (WebExplode) command;
        StringBuilder parameters;
/*
        if (isPost(request)) {
*/
            parameters = new StringBuilder(createQueryString(request));
/*
        } else {
            parameters = new StringBuilder(request.getQueryString());
        }
*/
        addCookiesToQueryString(request, parameters);
        addPasswordEncryption(request, parameters);
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        String contents = null;
        try {
            tx = session.beginTransaction();
            contents = webExplodeRepository.getWebExplodeContents(explode.getName(), parameters.toString());
            tx.commit();
        } catch (SQLException sql) {
            tx.rollback();
            LOG.error(sql);
        } catch (HibernateException e) {
            tx.rollback();
            LOG.error(e);
        }

        explode.setContents(contents);
        return new ModelAndView("webdriver.page", LookupStrings.FORM_BEAN, explode);
    }

    private void addPasswordEncryption(HttpServletRequest request, StringBuilder parameters) {
        String passwordFirst = request.getParameter("password");
        String passwordSecond = request.getParameter("password2");
        if (!StringUtils.isEmpty(passwordFirst) && !StringUtils.isEmpty(passwordSecond)) {
            Md5PasswordEncoder encoder = new Md5PasswordEncoder();
            String encodedPass = encoder.encodePassword(passwordFirst, salt);
            parameters.append("&encryptedPassword=");
            parameters.append(encodedPass);
        }
    }

    /**
     * This turns the cookies into name-value pairs and adds them to the query string
     * which is passed on to datablade via the db call.
     *
     * @param request    HttpServletRequest
     * @param parameters query string
     */
    private void addCookiesToQueryString(HttpServletRequest request, StringBuilder parameters) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return;
        for (Cookie cookie : cookies) {
            String cookieName = cookie.getName();
            if (ServletService.JSESSIONID.equals(cookieName))
                continue;
            String value = cookie.getValue();
            parameters.append("&");
            parameters.append(cookieName);
            parameters.append("=");
            parameters.append(value);
        }
    }

    private String createQueryString(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        if (params == null)
            return null;
        Iterator<String> iter = params.keySet().iterator();
        if (iter == null)
            return null;

        StringBuilder queryString = new StringBuilder("");
        while (iter.hasNext()) {
            String name = iter.next();
            String[] vals = params.get(name);
            if (vals != null) {
                for (String val : vals) {
                    queryString.append("&");
                    queryString.append(name);
                    queryString.append("=");
                    queryString.append(val);
                }
            }
        }
        return queryString.toString();
    }

    private boolean isPost(HttpServletRequest request) {
        return request.getMethod().equals("POST");
    }


    public WebExplodeRepository getWebExplodeRepository() {
        return webExplodeRepository;
    }

    public void setWebExplodeRepository(WebExplodeRepository webExplodeRepository) {
        this.webExplodeRepository = webExplodeRepository;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
