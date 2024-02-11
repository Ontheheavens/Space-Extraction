package spacextra.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import spacextra.utility.Common;

import java.util.Random;

/**
 * @author Ontheheavens
 * @since 11.02.2024
 */
final class ExtractionCapability {

    private static final float MINIMUM_MACHINERY = 10.0f;
    private static final float MINIMUM_CREW = 30.0f;
    private static final float CREW_PER_MACHINERY = 3.0f;

    private ExtractionCapability() {}

    static float getNeededCrew(CargoAPI cargo) {
        float machinery = cargo.getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        return machinery * CREW_PER_MACHINERY;
    }

    static float getPlayerCrewedMachinery() {
        SectorAPI sector = Global.getSector();
        FactionAPI playerFaction = sector.getPlayerFaction();
        CampaignFleetAPI fleet = sector.getPlayerFleet();
        CargoAPI cargo = fleet.getCargo();
        return ExtractionCapability.getCrewedMachinery(cargo);
    }

    static boolean hasMinimumCrewedMachinery() {
        return ExtractionCapability.getPlayerCrewedMachinery() >= MINIMUM_MACHINERY;
    }

    static float getCrewedMachinery(CargoAPI cargo) {
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

    private static float getMaximumResultRange(float value) {
        return value * 0.2f;
    }

    static float getMinimumRandomized(float value) {
        return value - ExtractionCapability.getMaximumResultRange(value);
    }

    static float getMaximumRandomized(float value) {
        return value + ExtractionCapability.getMaximumResultRange(value);
    }

    static float getRandomized(float value) {
        Random random = new Random();
        float range = random.nextFloat() * ExtractionCapability.getMaximumResultRange(value);
        return Common.getRandomized(value, range, range);
    }

}
