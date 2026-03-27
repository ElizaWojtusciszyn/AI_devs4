package aidevs.course.s01e02.tools;

import aidevs.course.s01e02.clients.CalculateLocation;
import aidevs.course.tools.ITool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class CalculateLocationTool implements ITool {

    private final CalculateLocation calculateLocation;

    public record CalculateDistanceRequest(
            @ToolParam(description = "Latitude of first location (e.g. person's location)") double lat1,
            @ToolParam(description = "Longitude of first location (e.g. person's location)") double lon1,
            @ToolParam(description = "Latitude of second location (e.g. power plant)") double lat2,
            @ToolParam(description = "Longitude of second location (e.g. power plant)") double lon2
    ) {}

    @Tool(description = "Calculates distance in km between two geographic coordinates using Haversine formula. Use this to check how far a person's location is from a power plant.")
    public String calculateDistance(CalculateDistanceRequest request) {
        double distance = calculateLocation.distanceKm(request.lat1(), request.lon1(), request.lat2(), request.lon2());
        log.info("=== DISTANCE [%s] ===".formatted(distance));
        return String.valueOf(distance);
    }
}
