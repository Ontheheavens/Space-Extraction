package spacextra.utility;

import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import spacextra.abilities.ExtractionSource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 10.02.2024
 */
public final class DialogUtilities {

    private DialogUtilities() {}

    public static Map<String, Integer> getExtractionCapabilities() {
        Map<String, Integer> result = new LinkedHashMap<>();

        result.put(Commodities.CREW, 100);
        result.put(Commodities.HEAVY_MACHINERY, 50);

        return result;
    }

    public static String getPrimaryTargetName(ExtractionSource source) {
        String result = "main system body";
        if (source == null) {
            return result;
        }
        switch (source) {
            case ASTEROID_BELT:
                result = "asteroid belt";
                break;
            case ASTEROID_FIElD:
                result = "asteroid field";
                break;
            case RING:
                result = "ring system";
                break;
            case NEBULA:
                result = "nebula cloud";
                break;
        }
        return result;
    }

    public static ExtractionSource getPrimaryTargetTerrain(Iterable<CampaignTerrainAPI> targetTerrains) {
        for (ExtractionSource source : ExtractionSource.values()) {
            if (DialogUtilities.isInsideSpecificSource(targetTerrains, source)) {
                return source;
            }
        }
        return null;
    }

    private static boolean isInsideSpecificSource(Iterable<CampaignTerrainAPI> targetTerrains,
                                                  ExtractionSource targetSource) {
        for (CampaignTerrainAPI terrain : targetTerrains) {
            CampaignTerrainPlugin plugin = terrain.getPlugin();
            String terrainId = plugin.getTerrainId();
            if (terrainId.equals(targetSource.getTerrainID())) {
                return true;
            }
        }
        return false;
    }

}
