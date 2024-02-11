package spacextra.utility;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import spacextra.abilities.ExtractionSource;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * @author Ontheheavens
 * @since 10.02.2024
 */
public final class Common {

    private static final Random random = new Random();

    private Common() {}

    public static boolean isInsideExtractionSource() {
        List<CampaignTerrainAPI> terrains = Common.getTerrainsWithPlayerFleet();
        if (terrains == null) return false;
        for (CampaignTerrainAPI terrain : terrains) {
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();
            Set<String> allSourceIDs = ExtractionSource.getAllSourceIDs();
            if (allSourceIDs.contains(terrainPlugin.getTerrainId())) {
                return true;
            }
        }
        return false;
    }

    public static List<CampaignTerrainAPI> getTerrainsWithPlayerFleet() {
        SectorAPI sector = Global.getSector();
        CampaignFleetAPI playerFleet = sector.getPlayerFleet();

        if (playerFleet == null) {
            return null;
        }

        LocationAPI containingLocation = playerFleet.getContainingLocation();
        List<CampaignTerrainAPI> allTerrains = containingLocation.getTerrainCopy();

        List<CampaignTerrainAPI> result = Caches.getEmptyTerrainCollection();
        for (CampaignTerrainAPI terrain : allTerrains) {
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();
            if (terrainPlugin.containsEntity(playerFleet)) {
                result.add(terrain);
            }
        }

        return result;
    }

    public static float getRandomized(float value, float minRange, float maxRange) {
        float min = value - minRange;
        float max = value + maxRange;
        return min + random.nextFloat() * (max - min);
    }

    public static float getRandomizedGaussian(float center, float min, float max, float deviation) {
        float standardDeviation = (max - min) / deviation;

        float randomizedValue;
        do {
            randomizedValue = (float) (random.nextGaussian() * standardDeviation + center);
        } while (randomizedValue < min || randomizedValue > max);

        return randomizedValue;
    }

    public static String getRoundedToWhole(float value) {
        return String.format("%d", Math.round(value));
    }

}
