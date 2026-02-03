package com.openmeteo.mcp.prompt;

import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for providing MCP workflow prompts.
 * <p>
 * Generates instruction templates that guide AI assistants through
 * multi-step workflows for weather-aware planning.
 * </p>
 */
@Service
public class PromptService {

    /**
     * Ski trip weather planning prompt.
     *
     * Generates a guide for checking snow conditions and weather for ski trips to Swiss resorts.
     *
     * Provides a workflow for assessing ski conditions combining
     * snow depth, weather forecasts, and safety considerations.
     *
     * WORKFLOW:
     * 1. Identify the resort location
     * 2. Check snow conditions with get_snow_conditions tool
     * 3. Check general weather with get_weather tool
     * 4. Assess ski conditions (Excellent/Good/Fair/Poor)
     * 5. Provide recommendations and gear suggestions
     *
     * @param resort Ski resort name (e.g., "Zermatt", "Verbier", "St. Moritz")
     * @param dates  Travel dates (e.g., "this weekend", "January 10-15")
     * @return Workflow instructions for AI assistant with multi-step ski condition assessment
     */
    @McpPrompt(name = "ski-trip-weather", description = """
            Generates a guide for checking snow conditions and weather for ski trips to Swiss resorts.

            Comprehensive workflow for assessing ski conditions combining snow depth, weather forecasts, and safety considerations.

            WORKFLOW:
            1. Identify the resort location (use search_location if needed)
            2. Check snow conditions with get_snow_conditions tool
               - Current snow depth (ideal: >50cm)
               - Recent snowfall (fresh powder: >10cm in last 24h)
               - Temperature (ideal: -10°C to -5°C)
            3. Check general weather with get_weather tool
               - Weather codes (reference weather://codes)
               - Wind speed and gusts (concerning if >50 km/h)
               - Visibility (important for safety)
               - Precipitation probability
            4. Assess ski conditions (Excellent/Good/Fair/Poor)
            5. Provide recommendations:
               - Best days to ski
               - Gear recommendations
               - Safety warnings
               - Alternative dates if conditions are poor

            RESOURCES: weather://codes, weather://swiss-locations, weather://parameters
            """)
    public String skiTripWeatherPrompt(String resort, String dates) {
        String resortInfo = (resort != null && !resort.isBlank())
                ? "for " + resort
                : "";
        String dateInfo = (dates != null && !dates.isBlank())
                ? "on " + dates
                : "";

        return String.format("""
                # Ski Trip Weather Planning %s %s

                Follow this workflow to assess ski conditions:

                ## Step 1: Identify the Resort Location
                - If resort name provided: Use search_location to find coordinates
                - If not provided: Ask user for resort name
                - Suggested resorts: Zermatt, Verbier, St. Moritz, Davos, Saas-Fee, Grindelwald
                - Tip: Swiss resort names can be searched directly

                ## Step 2: Check Snow Conditions
                Use get_snow_conditions tool with:
                - Resort coordinates
                - 7-day forecast
                - Include hourly data for detailed analysis

                Analyze:
                - Current snow depth (ideal: >50cm)
                - Recent snowfall (fresh powder: >10cm in last 24h)
                - Temperature (ideal: -10°C to -5°C)
                - Freezing level height (should be below resort elevation)

                ## Step 3: Check General Weather
                Use get_weather tool with:
                - Same coordinates
                - 7-day forecast
                - Include hourly data

                Analyze:
                - Weather code (reference weather://codes resource)
                - Wind speed and gusts (concerning if >50 km/h)
                - Visibility (important for safety)
                - Precipitation probability

                ## Step 4: Assess Ski Conditions
                Combine data to determine:
                - **Excellent**: Fresh powder (>10cm), -15°C to -5°C, clear skies, good visibility
                - **Good**: Good depth (>50cm), stable temps (<0°C), mostly clear
                - **Fair**: Minimal depth (>20cm), temps below freezing, acceptable visibility
                - **Poor**: Insufficient snow, warm temps (>5°C), poor weather, limited visibility

                ## Step 5: Provide Recommendations
                Based on %s:
                - Best days to ski (weather + snow quality)
                - Gear recommendations (layers, goggles for flat light, etc.)
                - Safety warnings (wind, visibility, avalanche risk)
                - Alternative dates if conditions are poor

                ## Resources
                - Weather codes: weather://codes
                - Swiss locations: weather://swiss-locations
                - Weather parameters: weather://parameters

                ## Example Response Format
                **Snow Conditions**: [Depth + recent snowfall]
                **Weather**: [Conditions + temperature range]
                **Ski Assessment**: [Excellent/Good/Fair/Poor with reasoning]
                **Best Days**: [Specific dates with why]
                **Recommendations**: [Gear, safety, alternatives]
                """,
                resortInfo,
                dateInfo,
                (dateInfo.isBlank() ? "the requested dates" : dates)
        ).trim();
    }

    /**
     * Outdoor activity planning prompt.
     *
     * Generates a weather-aware outdoor activity planning workflow for hiking, cycling, and other outdoor pursuits.
     *
     * Provides a workflow for weather-aware activity planning with
     * sensitivity assessments and safety recommendations.
     *
     * ACTIVITY SENSITIVITY LEVELS:
     * - High: Rock climbing, via ferrata, high-altitude hiking, water activities
     * - Medium: Day hiking, road cycling, trail running, camping
     * - Low: Walking, photography, mixed activities
     *
     * WORKFLOW:
     * 1. Understand activity weather sensitivity
     * 2. Get location coordinates
     * 3. Check weather forecast with get_weather tool
     * 4. Assess suitability (Ideal/Acceptable/Poor)
     * 5. Provide recommendations and gear suggestions
     *
     * @param activity  Activity type (hiking, cycling, climbing, camping)
     * @param location  Location for activity
     * @param timeframe When to do the activity (this weekend, next week)
     * @return Workflow instructions for AI assistant with weather-aware activity assessment
     */
    @McpPrompt(name = "plan-outdoor-activity", description = """
            Generates a weather-aware outdoor activity planning workflow for hiking, cycling, and other outdoor pursuits.

            ACTIVITY SENSITIVITY LEVELS:
            - High: Rock climbing, via ferrata, high-altitude hiking (>2500m), water activities
            - Medium: Day hiking (<2000m), road cycling, trail running, camping
            - Low: Walking, urban sightseeing, photography, mixed activities

            WORKFLOW:
            1. Understand activity weather sensitivity
            2. Get location coordinates (use search_location if needed)
            3. Check weather forecast with get_weather tool
               - Weather codes (reference weather://codes)
               - Temperature range
               - Precipitation probability
               - Wind conditions
               - UV index (for sun exposure activities)
            4. Assess suitability:
               - Ideal: Clear/partly cloudy, appropriate temps, low precipitation (<20%), moderate wind (<20 km/h)
               - Acceptable: Light clouds/mist, tolerable temps, low precipitation (<40%), safe winds
               - Poor: Rain/snow, extreme temps, high precipitation (>60%), strong winds (>30 km/h)
            5. Provide recommendations:
               - Best time windows (specific hours if available)
               - Gear recommendations (rain gear, sun protection, layers)
               - Safety considerations (lightning, heat/cold stress)
               - Backup plans or alternative activities

            RESOURCES: weather://codes, weather://parameters, weather://aqi-reference
            """)
    public String planOutdoorActivityPrompt(String activity, String location, String timeframe) {
        String activityInfo = (activity != null && !activity.isBlank())
                ? activity
                : "the requested activity";
        String locationInfo = (location != null && !location.isBlank())
                ? "in " + location
                : "";
        String timeInfo = (timeframe != null && !timeframe.isBlank())
                ? timeframe
                : "the requested timeframe";

        return String.format("""
                # Outdoor Activity Planning: %s %s

                Follow this workflow for weather-aware activity planning:

                ## Step 1: Understand Activity Weather Sensitivity

                **High Sensitivity** (weather-critical):
                - Rock climbing, Via ferrata
                - High-altitude hiking (>2500m)
                - Water activities (kayaking, sailing)
                - Mountain biking on technical trails

                **Medium Sensitivity** (weather-aware):
                - Day hiking below 2000m
                - Road cycling
                - Trail running
                - Camping

                **Low Sensitivity** (weather-flexible):
                - Walking, Urban sightseeing
                - Photography
                - Indoor/outdoor mix activities

                ## Step 2: Get Location Coordinates
                - Use search_location if needed
                - If %s: Look up coordinates
                - Consider elevation (affects temperature and conditions)

                ## Step 3: Check Weather Forecast
                Use get_weather tool with:
                - Location coordinates
                - Timeframe: %s
                - Include hourly data for timing flexibility

                Analyze:
                - Weather codes (reference weather://codes)
                - Temperature range
                - Precipitation probability
                - Wind conditions
                - UV index (for sun exposure activities)

                Optional: Use get_air_quality for:
                - Outdoor sports (breathing air quality matters)
                - Locations near urban areas
                - High UV index days

                ## Step 4: Assess Suitability

                **Ideal Conditions**:
                - Clear to partly cloudy (codes 0-3)
                - Appropriate temperature for activity
                - Low precipitation probability (<20%%)
                - Moderate wind (<20 km/h for most activities)

                **Acceptable Conditions**:
                - Light clouds or mist (codes 1-48)
                - Temperature within tolerable range
                - Low precipitation risk (<40%%)
                - Wind within safety limits

                **Poor Conditions**:
                - Rain/snow (codes 51+)
                - Extreme temperatures
                - High precipitation probability (>60%%)
                - Strong winds (>30 km/h)
                - Thunderstorm risk (codes 95-99)

                ## Step 5: Provide Recommendations

                For **%s**:
                - Best time windows (specific hours if hourly data available)
                - Gear recommendations (rain gear, sun protection, layers)
                - Safety considerations (lightning risk, heat/cold stress)
                - Backup plans or alternative activities
                - Alternative dates if conditions are poor

                ## Resources
                - Weather codes: weather://codes
                - Weather parameters: weather://parameters
                - AQI reference: weather://aqi-reference

                ## Example Response Format
                **Activity**: [Activity name + sensitivity level]
                **Location**: [Name + coordinates + elevation]
                **Weather**: [Conditions for %s]
                **Suitability**: [Ideal/Acceptable/Poor with reasoning]
                **Best Time**: [Specific hours or days]
                **Recommendations**: [Gear, safety, alternatives]
                """,
                activityInfo,
                locationInfo,
                locationInfo.isBlank() ? "location provided" : location,
                timeInfo,
                activityInfo,
                timeInfo
        ).trim();
    }

    /**
     * Weather-aware travel planning prompt.
     *
     * Provides a comprehensive guide for travel planning with weather integration and packing recommendations.
     *
     * Provides a workflow for travel planning with weather-based
     * packing recommendations and activity suggestions.
     *
     * WORKFLOW:
     * 1. Extract destination information and get coordinates
     * 2. Determine travel timeframe and forecast period
     * 3. Check destination weather with get_weather tool
     * 4. Provide weather-aware advice with temperature and precipitation guidance
     * 5. Suggest activities based on weather conditions
     * 6. Provide detailed packing list recommendations
     *
     * PACKING GUIDANCE BY WEATHER:
     * - Cold (<5°C): Warm layers, winter coat, gloves, hat
     * - Cool (5-15°C): Light jacket, layers, long pants
     * - Mild (15-20°C): Light layers, mix of short/long clothing
     * - Warm (20-25°C): Summer clothes, sun protection
     * - Hot (>25°C): Light clothing, sunscreen, hat, hydration
     *
     * @param destination Travel destination
     * @param travelDates When traveling (e.g., 'next week', 'January 10-15')
     * @param tripType    Type of trip (day trip, weekend, business, vacation)
     * @return Workflow instructions for AI assistant with comprehensive travel planning guidance
     */
    @McpPrompt(name = "weather-aware-travel", description = """
            Travel planning with weather integration: Provides a comprehensive guide for travel planning with weather integration.

            Complete travel planning workflow with temperature-based packing recommendations, activity suggestions, and weather-aware travel advice.

            WORKFLOW:
            1. Extract destination information and get coordinates (use search_location if needed)
               - Note elevation (affects temperature)
               - Consider multiple locations if multi-city trip
            2. Determine travel timeframe
               - Forecast range based on travel dates
               - Consider arrival/departure timing
            3. Check destination weather with get_weather tool
               - Temperature range (highs and lows)
               - Precipitation probability by day
               - Weather codes (reference weather://codes)
               - Wind conditions
               - UV index (for sunny destinations)
            4. Provide weather-aware advice:
               - Temperature Guidance:
                 - Cold (<5°C): Warm layers, winter coat, gloves, hat
                 - Cool (5-15°C): Light jacket, layers, long pants
                 - Mild (15-20°C): Light layers, mix of short/long clothing
                 - Warm (20-25°C): Summer clothes, sun protection
                 - Hot (>25°C): Light clothing, sunscreen, hat, hydration
               - Precipitation Packing:
                 - Clear/Sunny (0-1): Sunglasses, sunscreen, light clothing
                 - Partly Cloudy (2-3): Layers, light jacket, optional umbrella
                 - Rainy (51-82): Waterproof jacket, umbrella, water-resistant shoes
                 - Snowy (71-86): Winter coat, warm layers, waterproof boots
            5. Suggest activities based on weather conditions
            6. Provide detailed packing list recommendations

            RESOURCES: weather://codes, weather://parameters
            """)
    public String weatherAwareTravelPrompt(String destination, String travelDates, String tripType) {
        String destInfo = (destination != null && !destination.isBlank())
                ? destination
                : "the destination";
        String dateInfo = (travelDates != null && !travelDates.isBlank())
                ? travelDates
                : "the travel dates";
        String tripInfo = (tripType != null && !tripType.isBlank())
                ? tripType
                : "this trip";

        return String.format("""
                # Weather-Aware Travel Planning: %s

                Follow this workflow to provide weather-aware travel advice:

                ## Step 1: Extract Destination Information
                - Destination: %s
                - Use search_location to get coordinates
                - Note elevation (affects temperature)
                - Consider multiple locations if multi-city trip

                ## Step 2: Determine Travel Timeframe
                - Travel dates: %s
                - Forecast range: Use appropriate forecast_days
                - Consider arrival/departure timing

                ## Step 3: Check Destination Weather
                Use get_weather tool with:
                - Destination coordinates
                - Full travel period
                - Include hourly data for packing decisions

                Analyze:
                - Temperature range (highs and lows)
                - Precipitation probability by day
                - Weather codes (reference weather://codes)
                - Wind conditions
                - UV index (for sunny destinations)

                ## Step 4: Provide Weather-Aware Advice

                ### Temperature Guidance
                - **Cold (<5°C)**: Warm layers, winter coat, gloves, hat
                - **Cool (5-15°C)**: Light jacket, layers, long pants
                - **Mild (15-20°C)**: Light layers, mix of short/long clothing
                - **Warm (20-25°C)**: Summer clothes, sun protection
                - **Hot (>25°C)**: Light clothing, sunscreen, hat, hydration

                ### Precipitation Packing
                Based on weather codes:
                - **Clear/Sunny (0-1)**: Sunglasses, sunscreen, light clothing
                - **Partly Cloudy (2-3)**: Layers, light jacket (optional umbrella)
                - **Rainy (51-82)**: Waterproof jacket, umbrella, water-resistant shoes
                - **Snowy (71-86)**: Winter coat, warm layers, waterproof boots
                - **Stormy (95-99)**: Consider travel delays, full rain gear

                ### Trip Type Considerations
                For %s:
                - **Day Trip**: Focus on same-day conditions, pack light
                - **Weekend**: Pack for range of conditions, check both days
                - **Business**: Professional attire + weather adaptability
                - **Vacation**: Full range of clothing, plan activities by weather

                ## Step 5: Assess Travel Impact

                **No Impact**: Clear weather, mild temps, ideal for travel
                **Minor Impact**: Light rain, need umbrella, pack extra layer
                **Moderate Impact**: Heavy rain or wind, potential delays, adjust outdoor plans
                **Significant Impact**: Severe weather, consider rescheduling, safety concerns

                ## Step 6: Suggest Activities by Weather

                **Clear/Sunny**: Outdoor sightseeing, hiking, photography, beaches
                **Partly Cloudy**: Most outdoor activities, flexible planning
                **Rainy**: Museums, indoor attractions, covered markets
                **Snowy**: Winter sports, scenic mountain drives (check road conditions)
                **Stormy**: Indoor activities, postpone outdoor plans

                ## Resources
                - Weather codes: weather://codes
                - Swiss locations: weather://swiss-locations (if destination in Switzerland)
                - Weather parameters: weather://parameters

                ## Example Response Format
                **Destination**: [Name + coordinates]
                **Travel Dates**: [Date range]
                **Weather Summary**: [Overview of conditions]
                **Packing List**:
                - Clothing: [Temperature-appropriate items]
                - Accessories: [Weather-specific gear]
                - Footwear: [Appropriate shoes]
                **Travel Impact**: [None/Minor/Moderate/Significant]
                **Activity Suggestions**: [Weather-appropriate activities by day]
                **Warnings**: [Any weather-related travel concerns]
                """,
                destInfo,
                destInfo,
                dateInfo,
                tripInfo
        ).trim();
    }

    /**
     * Get all available prompts as a map.
     * <p>
     * Useful for MCP protocol registration.
     * </p>
     *
     * @return Map of prompt name to description
     */
    public Map<String, String> getAllPromptNames() {
        return Map.of(
                "ski-trip-weather", "Ski trip weather planning with snow conditions",
                "plan-outdoor-activity", "Weather-aware outdoor activity planning",
                "weather-aware-travel", "Travel planning with weather integration"
        );
    }
}
