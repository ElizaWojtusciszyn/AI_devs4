package aidevs.course.s01e02.tools;

import lombok.AllArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CalculateLocationTool {

    private final CalculateLocation calculateLocation;

    // Współrzędne elektrowni — zmień na właściwe
    private static final double POWER_PLANT_LAT = 52.2297;
    private static final double POWER_PLANT_LON = 21.0122;
    private static final double THRESHOLD_KM = 5.0;

    @Tool(description = "Checks if given coordinates are very close to the power plant. Returns distance in km and whether location is considered very close.")
    public String calculate(
            @ToolParam(description = "Latitude of the location to check")
            double latitude,
            @ToolParam(description = "Longitude of the location to check")
            double longitude
    ) {
        double distance = calculateLocation.distanceKm(latitude, longitude, POWER_PLANT_LAT, POWER_PLANT_LON);
        boolean veryClose = calculateLocation.isVeryClose(latitude, longitude, POWER_PLANT_LAT, POWER_PLANT_LON, THRESHOLD_KM);

        return "Distance to power plant: %.2f km. Very close: %s".formatted(distance, veryClose);
    }
}
