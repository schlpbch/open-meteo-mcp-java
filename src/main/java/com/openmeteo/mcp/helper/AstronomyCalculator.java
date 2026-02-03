package com.openmeteo.mcp.helper;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for calculating astronomy data.
 * <p>
 * Provides sunrise, sunset, golden hour, blue hour, and photography timing data.
 * </p>
 */
public class AstronomyCalculator {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Calculate astronomy data for a location.
     *
     * @param latitude  Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @param timezone  Timezone identifier (e.g., "Europe/Zurich")
     * @return Map containing sunrise, sunset, day length, golden hour, blue hour, and photography windows
     */
    public static Map<String, Object> calculateAstronomyData(
            double latitude,
            double longitude,
            String timezone) {

        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();

        // Calculate sunrise and sunset using simplified algorithm
        SunTimes sunTimes = calculateSunTimes(latitude, longitude, today, zoneId);

        // Calculate day length
        Duration dayLength = Duration.between(sunTimes.sunrise, sunTimes.sunset);
        double dayLengthHours = dayLength.toMinutes() / 60.0;

        // Calculate golden hour (1 hour after sunrise, 1 hour before sunset)
        ZonedDateTime goldenHourMorningStart = sunTimes.sunrise;
        ZonedDateTime goldenHourMorningEnd = sunTimes.sunrise.plusHours(1);
        ZonedDateTime goldenHourEveningStart = sunTimes.sunset.minusHours(1);
        ZonedDateTime goldenHourEveningEnd = sunTimes.sunset;

        // Calculate blue hour (30 min before sunrise, 30 min after sunset)
        ZonedDateTime blueHourMorningStart = sunTimes.sunrise.minusMinutes(30);
        ZonedDateTime blueHourMorningEnd = sunTimes.sunrise;
        ZonedDateTime blueHourEveningStart = sunTimes.sunset;
        ZonedDateTime blueHourEveningEnd = sunTimes.sunset.plusMinutes(30);

        // Moon phase (simplified - based on day of month)
        String moonPhase = calculateMoonPhase(today);

        // Best photography windows
        List<Map<String, String>> photographyWindows = List.of(
                Map.of(
                        "period", "Morning Blue Hour",
                        "start", blueHourMorningStart.format(ISO_FORMATTER),
                        "end", blueHourMorningEnd.format(ISO_FORMATTER),
                        "type", "Twilight photography"
                ),
                Map.of(
                        "period", "Morning Golden Hour",
                        "start", goldenHourMorningStart.format(ISO_FORMATTER),
                        "end", goldenHourMorningEnd.format(ISO_FORMATTER),
                        "type", "Warm, soft lighting"
                ),
                Map.of(
                        "period", "Evening Golden Hour",
                        "start", goldenHourEveningStart.format(ISO_FORMATTER),
                        "end", goldenHourEveningEnd.format(ISO_FORMATTER),
                        "type", "Warm, soft lighting"
                ),
                Map.of(
                        "period", "Evening Blue Hour",
                        "start", blueHourEveningStart.format(ISO_FORMATTER),
                        "end", blueHourEveningEnd.format(ISO_FORMATTER),
                        "type", "Twilight photography"
                )
        );

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("sunrise", sunTimes.sunrise.format(ISO_FORMATTER));
        result.put("sunset", sunTimes.sunset.format(ISO_FORMATTER));
        result.put("day_length_hours", Math.round(dayLengthHours * 100.0) / 100.0);
        result.put("golden_hour", Map.of(
                "morning", Map.of(
                        "start", goldenHourMorningStart.format(ISO_FORMATTER),
                        "end", goldenHourMorningEnd.format(ISO_FORMATTER)
                ),
                "evening", Map.of(
                        "start", goldenHourEveningStart.format(ISO_FORMATTER),
                        "end", goldenHourEveningEnd.format(ISO_FORMATTER)
                )
        ));
        result.put("blue_hour", Map.of(
                "morning", Map.of(
                        "start", blueHourMorningStart.format(ISO_FORMATTER),
                        "end", blueHourMorningEnd.format(ISO_FORMATTER)
                ),
                "evening", Map.of(
                        "start", blueHourEveningStart.format(ISO_FORMATTER),
                        "end", blueHourEveningEnd.format(ISO_FORMATTER)
                )
        ));
        result.put("moon_phase", moonPhase);
        result.put("best_photography_windows", photographyWindows);

        return result;
    }

    /**
     * Calculate sunrise and sunset times using simplified algorithm.
     */
    private static SunTimes calculateSunTimes(double latitude, double longitude, LocalDate date, ZoneId zoneId) {
        // Simplified sunrise/sunset calculation
        // For production, consider using a library like SunCalc or similar

        int dayOfYear = date.getDayOfYear();
        double latRad = Math.toRadians(latitude);

        // Solar declination (simplified)
        double declination = Math.toRadians(23.45 * Math.sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))));

        // Hour angle
        double cosHourAngle = -Math.tan(latRad) * Math.tan(declination);
        cosHourAngle = Math.max(-1.0, Math.min(1.0, cosHourAngle)); // Clamp to valid range

        double hourAngle = Math.toDegrees(Math.acos(cosHourAngle));

        // Solar noon (simplified - assumes 12:00 local time)
        double solarNoon = 12.0 - (longitude / 15.0);

        // Sunrise and sunset times (in hours)
        double sunriseHour = solarNoon - (hourAngle / 15.0);
        double sunsetHour = solarNoon + (hourAngle / 15.0);

        // Convert to ZonedDateTime
        ZonedDateTime sunrise = date.atStartOfDay(zoneId).plusMinutes((long) (sunriseHour * 60));
        ZonedDateTime sunset = date.atStartOfDay(zoneId).plusMinutes((long) (sunsetHour * 60));

        return new SunTimes(sunrise, sunset);
    }

    /**
     * Calculate moon phase (simplified).
     */
    private static String calculateMoonPhase(LocalDate date) {
        // Simplified moon phase calculation based on lunar cycle (~29.5 days)
        // Reference: January 6, 2000 was a new moon
        LocalDate referenceNewMoon = LocalDate.of(2000, 1, 6);
        long daysSinceReference = java.time.temporal.ChronoUnit.DAYS.between(referenceNewMoon, date);
        double lunarCycle = 29.53058867; // Average lunar cycle in days
        double phase = (daysSinceReference % lunarCycle) / lunarCycle;

        if (phase < 0.0625 || phase >= 0.9375) {
            return "New Moon";
        } else if (phase < 0.1875) {
            return "Waxing Crescent";
        } else if (phase < 0.3125) {
            return "First Quarter";
        } else if (phase < 0.4375) {
            return "Waxing Gibbous";
        } else if (phase < 0.5625) {
            return "Full Moon";
        } else if (phase < 0.6875) {
            return "Waning Gibbous";
        } else if (phase < 0.8125) {
            return "Last Quarter";
        } else {
            return "Waning Crescent";
        }
    }

    /**
     * Record to hold sunrise and sunset times.
     */
    private record SunTimes(ZonedDateTime sunrise, ZonedDateTime sunset) {
    }
}
