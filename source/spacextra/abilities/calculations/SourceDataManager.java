package spacextra.abilities.calculations;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.util.Misc;
import spacextra.utility.Common;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 11.02.2024
 */
@SuppressWarnings("StaticCollection")
public final class SourceDataManager {

    /**
     * Keys are location IDs, values are maps with terrain ID keys and richness values.
     */
    private static Map<String, Map<String, Float>> richnessMap;

    /**
     * Keys are terrain IDs, values are exhaustion modifiers.
     */
    private static Map<String, Float> exhaustionMap;

    private static final float MIN_RICHNESS = 0.2f;
    private static final float MAX_RICHNESS = 1.8f;

    private static final float EXHAUSTION_INCREMENT = 0.05f;
    private static final String RICHNESS_ID = "spacextra_richness";
    private static final String EXHAUSTION_ID = "spacextra_exhaustion";

    private SourceDataManager() {}

    public static float getRichness(LocationAPI location, CampaignTerrainAPI terrain) {
        SectorAPI sector = Global.getSector();
        Map<String, Object> persistentData = sector.getPersistentData();
        SourceDataManager.ensureRichnessExistence(persistentData);

        String locationID = location.getId();
        String terrainID = terrain.getId();
        if (!richnessMap.containsKey(locationID)) {
            richnessMap.put(locationID, new HashMap<String, Float>());
        }
        Map<String, Float> locationTerrains = richnessMap.get(locationID);
        if (!locationTerrains.containsKey(terrainID)) {
            locationTerrains.put(terrainID, SourceDataManager.generateRichness(location, terrain));
        }
        persistentData.put(RICHNESS_ID, richnessMap);
        float baseRichness = locationTerrains.get(terrainID);
        SourceDataManager.ensureExhaustionExistence();
        Float exhaustionModifier = exhaustionMap.get(terrainID);
        if (exhaustionModifier == null) {
            exhaustionModifier = 1.0f;
            exhaustionMap.put(terrainID, exhaustionModifier);
            SourceDataManager.overwriteExhaustion();
        }
        return baseRichness * exhaustionModifier;
    }

    @SuppressWarnings({"NonThreadSafeLazyInitialization", "unchecked"})
    private static void ensureRichnessExistence(Map<String, Object> persistentData) {
        if (richnessMap == null) {
            richnessMap = (Map<String, Map<String, Float>>) persistentData.get(RICHNESS_ID);
            if (richnessMap == null) {
                richnessMap = new HashMap<>();
            }
        }
    }

    @SuppressWarnings({"NonThreadSafeLazyInitialization", "unchecked"})
    private static void ensureExhaustionExistence() {
        if (exhaustionMap == null) {
            SectorAPI sector = Global.getSector();
            Map<String, Object> persistentData = sector.getPersistentData();
            exhaustionMap = (Map<String, Float>) persistentData.get(EXHAUSTION_ID);
            if (exhaustionMap == null) {
                exhaustionMap = new HashMap<>();
            }
        }
    }

    private static void overwriteExhaustion() {
        SectorAPI sector = Global.getSector();
        Map<String, Object> persistentData = sector.getPersistentData();
        persistentData.put(EXHAUSTION_ID, exhaustionMap);
    }

    public static void increaseExhaustion(Iterable<CampaignTerrainAPI> terrains) {
        SourceDataManager.ensureExhaustionExistence();
        for (CampaignTerrainAPI terrain : terrains) {
            String terrainID = terrain.getId();
            if (!exhaustionMap.containsKey(terrainID)) {
                exhaustionMap.put(terrainID, 1.0f);
            }
            Float existingValue = exhaustionMap.get(terrainID);
            float changeResult = SourceDataManager.getExhaustionChange(existingValue);
            exhaustionMap.put(terrainID, changeResult);
        }
        SourceDataManager.overwriteExhaustion();
    }

    private static float getExhaustionChange(Float existingValue) {
        MutableStat extractionEfficiency = ExtractionCapability.getExtractionEfficiency();
        float computedEfficiency = extractionEfficiency.getModifiedValue();

        float crewedMachinery = ExtractionCapability.getPlayerCrewedMachinery();
        float machineryFactor = crewedMachinery / 1000.0f;

        float difference = (EXHAUSTION_INCREMENT * machineryFactor) * computedEfficiency;
        float changeResult = existingValue - difference;
        if (changeResult < 0.0f) {
            changeResult = 0.0f;
        }
        return changeResult;
    }

    static void restoreExhaustionGlobally() {
        SourceDataManager.ensureExhaustionExistence();
        for (Map.Entry<String, Float> entry : exhaustionMap.entrySet()) {
            Float exhaustion = entry.getValue();
            if (exhaustion < 1.0f) {
                float value = exhaustion + EXHAUSTION_INCREMENT;
                if (value > 1.0f) {
                    value = 1.0f;
                }
                exhaustionMap.put(entry.getKey(), value);
            }
        }
        SourceDataManager.overwriteExhaustion();
    }

    private static float generateRichness(LocationAPI location, CampaignTerrainAPI terrain) {
        float result = Common.getRandomizedGaussian(1.0f, MIN_RICHNESS, MAX_RICHNESS, 0.6f);
        if (location instanceof StarSystemAPI) {
            StarSystemAPI starSystem = (StarSystemAPI) location;
            SectorEntityToken hyperspaceAnchor = starSystem.getHyperspaceAnchor();
            if (Common.isInsideCore(hyperspaceAnchor.getLocationInHyperspace())) {
                if (result > 0.8f && Misc.random.nextFloat() < 0.35f) {
                    result = 0.2f + Misc.random.nextFloat() * 0.4f;
                }
            }
        }
        return result;
    }

    public static Color mapRichnessToColor(float richness) {
        return SourceDataManager.mapRichnessToColorProcedural(richness);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private static Color mapRichnessToColorProcedural(float value) {
        Color scarce = Misc.getNegativeHighlightColor();
        Color normal = Misc.getTextColor();
        Color rich = Misc.getPositiveHighlightColor();

        float minValue = MIN_RICHNESS;
        float maxValue = MAX_RICHNESS;

        float normalMin = 0.9f;
        float normalMax = 1.1f;

        Color result;

        if (value >= normalMin && value <= normalMax) {
            result = normal;
        } else if (value < normalMin) {
            result = SourceDataManager.interpolateColorByValueRange(value, scarce, normal,
                    normalMin, normalMin - minValue, minValue);
        } else {
            result = SourceDataManager.interpolateColorByValueRange(normalMax, rich, normal,
                    value, maxValue - normalMax, normalMax);
        }

        return result;
    }

    private static Color interpolateColorByValueRange(float value, Color target, Color base,
                                                      float baseValue, float valueSpan, float targetValue) {
        float blendFactor = 1.0f - (baseValue - value) / (valueSpan);
        int r = (int) (base.getRed() * blendFactor + target.getRed() * (1.0f - blendFactor));
        int g = (int) (base.getGreen() * blendFactor + target.getGreen() * (1.0f - blendFactor));
        int b = (int) (base.getBlue() * blendFactor + target.getBlue() * (1.0f - blendFactor));
        return new Color(r, g, b);
    }

}
