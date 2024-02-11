package spacextra.abilities;

import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ontheheavens
 * @since 10.02.2024
 */
public enum ExtractionSource {

    ASTEROID_FIElD("asteroid_field", "Asteroid Field"),
    ASTEROID_BELT("asteroid_belt", "Asteroid Belt"),
    RING("ring", "Ring System"),
    NEBULA("nebula", "Nebula");

    @SuppressWarnings("StaticCollection")
    private static final Set<String> ALL_SOURCE_IDS = new HashSet<>();

    static {
        for (ExtractionSource source : ExtractionSource.values()) {
            ALL_SOURCE_IDS.add(source.terrainID);
        }

        ASTEROID_FIElD.resources.put(Commodities.ORE, 0.6f);
        ASTEROID_FIElD.resources.put(Commodities.RARE_ORE, 0.1f);

        ASTEROID_BELT.resources.put(Commodities.ORE, 0.8f);
        ASTEROID_BELT.resources.put(Commodities.RARE_ORE, 0.2f);

        RING.resources.put(Commodities.ORE, 0.4f);
        RING.resources.put(Commodities.RARE_ORE, 0.1f);
        RING.resources.put(Commodities.VOLATILES, 0.05f);

        NEBULA.resources.put(Commodities.VOLATILES, 0.2f);
    }

    private final String terrainID;

    private final String displayName;

    /**
     * Keys are commodity IDs.
     */
    private final Map<String, Float> resources = new HashMap<>();

    ExtractionSource(String terrain, String displayName) {
        this.terrainID = terrain;
        this.displayName = displayName;
    }

    public String getTerrainID() {
        return terrainID;
    }

    public String getDisplayName() {
        return displayName;
    }

    Map<String, Float> getResources() {
        return resources;
    }

    public static Set<String> getAllSourceIDs() {
        return ALL_SOURCE_IDS;
    }

}
