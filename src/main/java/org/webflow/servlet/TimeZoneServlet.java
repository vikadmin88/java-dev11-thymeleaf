package org.webflow.servlet;

import java.io.*;
import java.util.*;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.web.servlet.IServletWebApplication;
import org.webflow.utils.datetime.DatetimeUtils;
import org.webflow.utils.http.HttpParamsUtils;
import org.webflow.utils.template.TemplateUtils;

import static org.thymeleaf.web.servlet.JakartaServletWebApplication.buildApplication;

@WebServlet(name = "datetime-servlet", value = "/time")
public class TimeZoneServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeZoneServlet.class.getCanonicalName());
    private String datetime;
    private ITemplateEngine templateEngine;
    private IServletWebApplication application;

    public void init() {
        LOGGER.info("Init servlet & template engine ...");
        ServletContext servletContext = getServletContext();
        application = buildApplication(servletContext);
        templateEngine = TemplateUtils.buildTemplateEngine(
                application,
                "/WEB-INF/templates/",
                ".html");
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=utf-8");
        String timeZoneParam = "timezone";
        String cookieZoneName = "lastTimezone";

        String queryStr = req.getQueryString();
        LOGGER.info("Query string (raw): {}", queryStr);

        int tz = 0;
        String paramValue = getParamValue(queryStr, timeZoneParam);
        if (!paramValue.isEmpty()) {
            tz = getTimeZoneFromParamVal(paramValue);
        } else {
            paramValue = getCookiesValue(cookieZoneName, req);
            tz = getTimeZoneFromParamVal(paramValue);
        }
        datetime = DatetimeUtils.getZonedDateTime(tz);

        Context simpleContext = new Context(
                req.getLocale(),
                Map.of("datetime", datetime)
        );
        templateEngine.process("datetime", simpleContext, resp.getWriter());
        resp.getWriter().close();
    }

    private int getTimeZoneFromParamVal(String paramValue) {
        if (paramValue.isEmpty()) return 0;
        int ret = 0;
        String tz = paramValue.replaceFirst("UTC", "");
        LOGGER.info("Parsed timezone: {}", tz);

        try {
            ret = Integer.parseInt(tz);
        } catch (NumberFormatException e) {
            LOGGER.error("Timezone is not int num: {}", e.getMessage());
        }
        return ret;
    }

    private static String getParamValue(String queryStr, String paramName) {
        Map<String, List<String>> params = HttpParamsUtils.getRawParamsMapByQueryStr(queryStr);
        LOGGER.info("List all params: {}", params);
        if (params != null && params.containsKey(paramName)) {
            LOGGER.info("Parsed param: {}", paramName);
            return params.get(paramName).get(0);
        }
        return "";
    }

    private static String getCookiesValue(String paramName, HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        Optional<Cookie> cookieOp = Arrays.stream(cookies)
                .filter(it -> it.getName().equals(paramName))
                .findFirst();
        return cookieOp.isPresent() ? cookieOp.get().getValue() : "";
    }


    public void destroy() {
        LOGGER.info("Destroy servlet...");
        datetime = null;
        templateEngine = null;
        application = null;
    }
}