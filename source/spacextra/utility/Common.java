package spacextra.utility;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import spacextra.abilities.calculations.ExtractionSource;

import java.util.List;
import java.util.Random;

/**
 * @author Ontheheavens
 * @since 10.02.2024
 */
public final class Common {

    private static final Random random = new Random();

    private Common() {}

    public static float randomFloat() {
        return random.nextFloat();
    }

    public static boolean isInsideExtractionSource() {
        ExtractionSource[] extractionSources = ExtractionSource.values();
        for (ExtractionSource source : extractionSources) {
            if (Common.isInsideSpecificSource(source)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInsideSpecificSource(ExtractionSource source) {
        List<CampaignTerrainAPI> terrains = Common.getTerrainsWithPlayerFleet();
        if (terrains == null || terrains.isEmpty()) return false;
        for (CampaignTerrainAPI terrain : terrains) {
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();
            String sourceTerrainID = source.getTerrainID();
            if (sourceTerrainID.equals(terrainPlugin.getTerrainId())) {
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

    public static CargoAPI getPlayerCargo() {
        SectorAPI sector = Global.getSector();
        FactionAPI playerFaction = sector.getPlayerFaction();
        CampaignFleetAPI fleet = sector.getPlayerFleet();
        return fleet.getCargo();
    }

    public static boolean isInsideCore(Vector2f hyperspaceLocation) {
        return Common.getCoreFactor(hyperspaceLocation) >= 1.0f;
    }

    @SuppressWarnings("OverlyComplexArithmeticExpression")
    private static float getCoreFactor(Vector2f hyperspaceLocation) {
        Vector2f min = Misc.getCoreMin();
        Vector2f max = Misc.getCoreMax();
        Vector2f center = Misc.getCoreCenter();

        float f = 1.4f;
        float a = (max.x - min.x) * 0.5f * f;
        float b = (max.y - min.y) * 0.5f * f;
        float x = hyperspaceLocation.x - center.x;
        float y = hyperspaceLocation.y - center.y;

        float test = ((x * x) / (a * a)) + ((y * y) / (b * b));
        float result;
        if (test >= 1.0f) {
            result = 0.0f;
        } else if (test <= 0.75f) {
            result = 1.0f;
        } else {
            result = 1.0f - (test - 0.75f) / 0.25f;
        }
        return result;
    }

    public static <T> T chooseRandom(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int randomIndex = random.nextInt(list.size());
        return list.get(randomIndex);
    }

}
