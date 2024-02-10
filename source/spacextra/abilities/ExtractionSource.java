package spacextra.abilities;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ontheheavens
 * @since 10.02.2024
 */
public enum ExtractionSource {
    ASTEROID_FIElD("asteroid_field"),
    ASTEROID_BELT("asteroid_belt"),
    RING("ring"),
    NEBULA("nebula");

    @SuppressWarnings("StaticCollection")
    private static final Set<String> ALL_SOURCE_IDS = new HashSet<>();

    static {
        for (ExtractionSource source : ExtractionSource.values()) {
            ALL_SOURCE_IDS.add(source.terrainID);
        }
    }

    public String getTerrainID() {
        return terrainID;
    }

    private final String terrainID;

    ExtractionSource(String terrain) {
        this.terrainID = terrain;
    }

    public static Set<String> getAllSourceIDs() {
        return ALL_SOURCE_IDS;
    }

}
