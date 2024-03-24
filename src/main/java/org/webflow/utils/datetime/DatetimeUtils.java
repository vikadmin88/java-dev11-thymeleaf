package org.webflow.utils.datetime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DatetimeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatetimeUtils.class.getCanonicalName());
    private static final DateTimeFormatter DATETIME_OUTPUT_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String PATTERN_UTC = " UTC";
    private static final String PATTERN_UTC_PLUS = PATTERN_UTC + "+";
    private static final String PATTERN_UTC_MINUS = PATTERN_UTC + "-";
    private DatetimeUtils() {
    }

    public static String getZonedDateTime(int zoneOffset) {
        Instant now = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.of("UTC"));

        if (zoneOffset >= -12 && zoneOffset <= 14) {
            LOGGER.info("Zone is valid: in range [-12...14]: {}", zoneOffset);
            if (zoneOffset > 0) {
                return zdt.plusHours(zoneOffset).format(DATETIME_OUTPUT_PATTERN) + PATTERN_UTC_PLUS + zoneOffset;
            } else if (zoneOffset < 0) {
                zoneOffset *= -1;
                return zdt.minusHours(zoneOffset).format(DATETIME_OUTPUT_PATTERN) + PATTERN_UTC_MINUS + zoneOffset;
            }
        }
        return zdt.format(DATETIME_OUTPUT_PATTERN) + PATTERN_UTC;
    }
}
