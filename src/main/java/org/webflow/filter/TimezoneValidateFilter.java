package org.webflow.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.web.servlet.IServletWebApplication;
import org.webflow.utils.datetime.DatetimeUtils;
import org.webflow.utils.http.HttpParamsUtils;
import org.webflow.utils.template.TemplateUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.thymeleaf.web.servlet.JakartaServletWebApplication.buildApplication;

@WebFilter(urlPatterns = "/time/*")
public class TimezoneValidateFilter extends HttpFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimezoneValidateFilter.class.getCanonicalName());
    private ITemplateEngine templateEngine;
    private IServletWebApplication application;
    private static final String cookieZoneName = "lastTimezone";


    public void init() {
        LOGGER.info("Init filter & template engine ...");
        ServletContext servletContext = getServletContext();
        application = buildApplication(servletContext);
        templateEngine = TemplateUtils.buildTemplateEngine(
                application,
                "/WEB-INF/templates/filters/",
                ".html");
    }
    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        resp.setContentType("text/html; charset=utf-8");
        String timeZoneParam = "timezone";

        String queryStr = req.getQueryString();
        LOGGER.info("Query string: {}", queryStr);

        if (isTimeZoneParamValid(queryStr, timeZoneParam, resp)) {
            LOGGER.info("TimeZone valid: {}", queryStr);
            chain.doFilter(req, resp);
            return;
        }

        LOGGER.error("Using default UTC timezone (0)");
        String datetime = DatetimeUtils.getZonedDateTime(0);
        Context simpleContext = new Context(
                req.getLocale(),
                Map.of("datetime", datetime,"errorMessage", "Invalid timezone")
        );
        templateEngine.process("datetime_filter", simpleContext, resp.getWriter());
        resp.getWriter().close();
    }

    private static boolean isTimeZoneParamValid(String queryStr, String timeZoneParam, HttpServletResponse resp) {
        Map<String, List<String>> params = getRawQueryStringMap(queryStr);
        if (params == null) return true;

        if (params.containsKey(timeZoneParam)) {
            String tz = params.get(timeZoneParam)
                    .get(0)
                    .replaceFirst("UTC", "");
            LOGGER.info("Parsed timezone: {}", tz);

            try {
                int zone = Integer.parseInt(tz);
                if (zone >= -12 && zone <= 14) {
                    LOGGER.info("TimeZone valid: {}", zone);
                    resp.addCookie(new Cookie(cookieZoneName, params.get(timeZoneParam).get(0)));
                    return true;
                }
            } catch (NumberFormatException e) {
                LOGGER.error("Timezone is not int num: {}", e.getMessage());
            }
        }
        LOGGER.error("TimeZone invalid!");
        return false;
    }

    private static Map<String, List<String>> getRawQueryStringMap(String queryStr) {
        if (queryStr == null) {
            LOGGER.error("queryStr is null!");
            return null;
        }
        Map<String, List<String>> params = HttpParamsUtils.getRawParamsMapByQueryStr(queryStr);
        LOGGER.info("List all params (raw): {}", params);
        return params;
    }

    public void destroy() {
        LOGGER.info("Destroy filter...");
        templateEngine = null;
        application = null;
    }
}
