package spacextra.abilities.calculations;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.RepairGantry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import spacextra.utility.Common;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Ontheheavens
 * @since 11.02.2024
 */
public final class ExtractionCapability {

    private static final float MINIMUM_MACHINERY = 30.0f;
    private static final float MINIMUM_CREW = 90.0f;
    private static final float CREW_PER_MACHINERY = 3.0f;

    private static final float SUPPLY_USE_PER_MACHINERY = 0.075f;

    private static final float YIELD_MULTIPLIER = 1.0f;

    public static final float MINIMUM_ACCIDENT_CHANCE = 0.075f;

    public static final float MAXIMUM_ACCIDENT_CHANCE = 0.85f;

    private ExtractionCapability() {}

    public static float getNeededCrew(CargoAPI cargo) {
        float machinery = cargo.getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        return machinery * CREW_PER_MACHINERY;
    }

    public static float getAssignedCrew() {
        return ExtractionCapability.getPlayerCrewedMachinery() * CREW_PER_MACHINERY;
    }

    static float getPlayerCrewedMachinery() {
        SectorAPI sector = Global.getSector();
        FactionAPI playerFaction = sector.getPlayerFaction();
        CampaignFleetAPI fleet = sector.getPlayerFleet();
        CargoAPI cargo = fleet.getCargo();
        return ExtractionCapability.getCrewedMachinery(cargo);
    }

    public static boolean hasMinimumCrewedMachinery() {
        return ExtractionCapability.getPlayerCrewedMachinery() >= MINIMUM_MACHINERY;
    }

    public static float getCrewedMachinery(CargoAPI cargo) {
        float machinery = cargo.getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        float crew = cargo.getCommodityQuantity(Commodities.CREW);

        float mannedMachinery = 0;

        // Calculate the number of manned machinery based on crew availability.
        if (crew >= MINIMUM_CREW) {
            float potentialMannedMachinery = (crew / CREW_PER_MACHINERY);
            mannedMachinery = Math.min(machinery, potentialMannedMachinery);
        }

        return mannedMachinery;
    }

    public static boolean hasRequiredSupplies(CargoAPI cargo) {
        float availableSupplies = cargo.getCommodityQuantity(Commodities.SUPPLIES);
        float requiredSupplies = ExtractionCapability.getRequiredSupplies(cargo);
        return availableSupplies >= requiredSupplies;
    }

    public static float getRequiredSupplies(CargoAPI cargo) {
        float crewedMachinery = ExtractionCapability.getCrewedMachinery(cargo);
        return crewedMachinery * SUPPLY_USE_PER_MACHINERY;
    }

    private static float getMaximumResultRange(float value) {
        return value * 0.2f;
    }

    public static float getMinimumRandomized(float value) {
        return value - ExtractionCapability.getMaximumResultRange(value);
    }

    public static float getMaximumRandomized(float value) {
        return value + ExtractionCapability.getMaximumResultRange(value);
    }

    public static float getRandomized(float value) {
        Random random = new Random();
        float range = random.nextFloat() * ExtractionCapability.getMaximumResultRange(value);
        return Common.getRandomized(value, range, range);
    }

    private static float getFleetwideSalvagingModifier() {
        SectorAPI sector = Global.getSector();
        CampaignFleetAPI playerFleet = sector.getPlayerFleet();
        return RepairGantry.getAdjustedGantryModifier(playerFleet, null, 0);
    }

    public static MutableStat getExtractionEfficiency() {
        MutableStat efficiency = new MutableStat(1.0f);

        efficiency.modifyPercent("base", -100.0f);
        efficiency.modifyPercent("base_positive", 100,
                "Base effectiveness");

        float salvagingModifier = ExtractionCapability.getFleetwideSalvagingModifier();
        efficiency.modifyPercentAlways("salvaging_modifier", Math.round(salvagingModifier * 100.0f),
                "Fleetwide salvaging capability");

        float readiness = TimedEffectsManager.getReadiness();
        efficiency.modifyMultAlways("extraction_readiness", readiness,
                "Recent extraction operations");

        return efficiency;
    }

    /**
     * @param sources map with extraction sources and their richness.
     * @return map with commodity IDs and their yield amounts.
     */
    public static Map<String, Float> getTotalYield(Map<ExtractionSource, Float> sources) {
        Map<String, Float> totalYield = new HashMap<>();

        float crewedMachinery = ExtractionCapability.getPlayerCrewedMachinery();
        for (Map.Entry<ExtractionSource, Float> source : sources.entrySet()) {
            ExtractionSource extractionSource = source.getKey();
            Map<String, Float> resources = extractionSource.getResources();

            MutableStat extractionEfficiency = ExtractionCapability.getExtractionEfficiency();
            float efficiencyComputed = extractionEfficiency.getModifiedValue();

            float terrainRichness = source.getValue();
            for (Map.Entry<String, Float> entry : resources.entrySet()) {
                float amount = (entry.getValue() * crewedMachinery);
                String commodityID = entry.getKey();

                float richnessInfluence = ExtractionSource.getRichnessInfluence(commodityID);
                double richnessModified = amount * Math.pow(terrainRichness, richnessInfluence);
                float efficiencyModified = (float) (richnessModified * efficiencyComputed);

                float finalAmount = efficiencyModified * YIELD_MULTIPLIER;

                Float existingAmount = totalYield.get(commodityID);
                if (existingAmount == null) {
                    totalYield.put(commodityID, finalAmount);
                } else {
                    totalYield.put(commodityID, existingAmount + finalAmount);
                }
            }
        }
        return totalYield;
    }

}
