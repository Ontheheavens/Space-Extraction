package spacextra.utility;

import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spacextra.abilities.calculations.ExtractionSource;
import spacextra.abilities.calculations.SourceDataManager;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 10.02.2024
 */
public final class DialogUtilities {

    private DialogUtilities() {}

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

    /**
     * @return map of all available extraction sources with richness values.
     */
    public static Map<ExtractionSource, Float> getAllAvailableSources(Iterable<CampaignTerrainAPI> targetTerrains) {
        Map<ExtractionSource, Float> result = new EnumMap<>(ExtractionSource.class);
        for (ExtractionSource source : ExtractionSource.values()) {
            CampaignTerrainAPI sourceTerrain = DialogUtilities.getSurroundingSource(targetTerrains, source);
            if (sourceTerrain != null) {
                LocationAPI location = sourceTerrain.getContainingLocation();
                float richness = SourceDataManager.getRichness(location, sourceTerrain);
                result.put(source, richness);
            }
        }
        return result;
    }

    public static ExtractionSource getPrimaryTargetTerrain(Iterable<CampaignTerrainAPI> targetTerrains) {
        for (ExtractionSource source : ExtractionSource.values()) {
            if (DialogUtilities.getSurroundingSource(targetTerrains, source) != null) {
                return source;
            }
        }
        return null;
    }

    /**
     * @return terrain instance if matches source, null otherwise.
     */
    private static CampaignTerrainAPI getSurroundingSource(Iterable<CampaignTerrainAPI> targetTerrains,
                                                           ExtractionSource targetSource) {
        for (CampaignTerrainAPI terrain : targetTerrains) {
            CampaignTerrainPlugin plugin = terrain.getPlugin();
            String terrainId = plugin.getTerrainId();
            if (terrainId.equals(targetSource.getTerrainID())) {
                return terrain;
            }
        }
        return null;
    }

    public static TooltipMakerAPI.StatModValueGetter getEfficiencyModPrinter() {
        return new EfficiencyValueGetter();
    }

    private static class EfficiencyValueGetter implements TooltipMakerAPI.StatModValueGetter {
        boolean percent;
        public String getPercentValue(MutableStat.StatMod mod) {
            percent = true;
            if (mod.desc == null || mod.desc.isEmpty()) return "";

            String prefix = mod.getValue() >= 0 ? "+" : "";
            return prefix + (int)(mod.getValue()) + "%";
        }
        public String getMultValue(MutableStat.StatMod mod) {percent = false; return null;}
        public String getFlatValue(MutableStat.StatMod mod) {percent = false; return null;}
        public Color getModColor(MutableStat.StatMod mod) {
            if ((!percent && mod.getValue() < 1.0f) || mod.getValue() < 0) return Misc.getNegativeHighlightColor();
            return null;
        }
    }

}
