package spacextra.abilities.accidents;

import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.util.vector.Vector2f;
import spacextra.abilities.calculations.ExtractionCapability;
import spacextra.utility.Common;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Ontheheavens
 * @since 13.02.2024
 */
@SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
abstract class AbstractAccident implements ExtractionAccident {

    private final TextPanelAPI textPanel;

    private final OptionPanelAPI options;

    private final CampaignFleetAPI fleet;

    AbstractAccident(TextPanelAPI textPanel, OptionPanelAPI options, CampaignFleetAPI fleet) {
        this.textPanel = textPanel;
        this.options = options;
        this.fleet = fleet;
    }

    public TextPanelAPI getTextPanel() {
        return textPanel;
    }

    public OptionPanelAPI getOptions() {
        return options;
    }

    public CampaignFleetAPI getFleet() {
        return fleet;
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    enum SoundId {
        EXPLOSION,
        HIT_SOLID,
        ASTEROID_COLLISION
    }

    private static List<String> getBeginnings() {
        return Arrays.asList(
                "In the midst of",
                "While conducting",
                "In the course of",
                "During",
                "Right in the middle of",
                "In the thick of"
        );
    }

    private static List<String> getMessages() {
        return Arrays.asList(
                "a frantic series of messages",
                "a panicked report",
                "an emergency transmission"
        );
    }

    static String getRandomMessage() {
        return Common.chooseRandom(AbstractAccident.getMessages());
    }

    static String getRandomBeginning() {
        return Common.chooseRandom(AbstractAccident.getBeginnings());
    }

    @Override
    public CargoAPI getLosses(CampaignFleetAPI fleet) {
        CargoAPI fleetCargo = fleet.getCargo();

        float crew = fleetCargo.getCrew();
        float machinery = fleetCargo.getCommodityQuantity(Commodities.HEAVY_MACHINERY);

        Pair<Float, Float> lossRatio = getMaximumLossRatio();

        float crewLossRatio = lossRatio.one;
        float machineryLossRatio = lossRatio.two;

        FactoryAPI factory = Global.getFactory();
        CargoAPI losses = factory.createCargo(true);

        WeightedRandomPicker<String> lossPicker = new WeightedRandomPicker<>(new Random());

        float crewedMachinery = ExtractionCapability.getCrewedMachinery(fleetCargo);
        float manpowerRatio = crewedMachinery / machinery;

        if (manpowerRatio < 0.1f) {
            manpowerRatio = 0.1f;
        }
        if (manpowerRatio > 0.9f) {
            manpowerRatio = 0.9f;
        }

        float crewWeight = 10.0f + 100.0f * (1.0f - manpowerRatio);
        float machineryWeight = 10.0f + 100.0f * manpowerRatio;

        lossPicker.add(Commodities.CREW, crewWeight * crewLossRatio);
        lossPicker.add(Commodities.HEAVY_MACHINERY, machineryWeight * machineryLossRatio);

        SectorAPI sector = Global.getSector();
        EconomyAPI economy = sector.getEconomy();

        CommoditySpecAPI machinerySpec = economy.getCommoditySpec(Commodities.HEAVY_MACHINERY);
        float totalMachineryPrice = crewedMachinery * machinerySpec.getBasePrice();

        CommoditySpecAPI crewSpec = economy.getCommoditySpec(Commodities.CREW);
        float totalCrewPrice = ExtractionCapability.getAssignedCrew() * crewSpec.getBasePrice();

        float totalLossValue = (totalCrewPrice * crewLossRatio) + (totalMachineryPrice * machineryLossRatio);

        float loss = 0;
        while (loss < totalLossValue) {
            String id = lossPicker.pick();
            CommoditySpecAPI spec = economy.getCommoditySpec(id);
            loss += spec.getBasePrice();
            losses.addCommodity(id, 1.0f);
        }
        losses.sort();

        int crewLost = losses.getCrew();
        if (crewLost > 0) {
            losses.removeCrew(crewLost);
            MutableFleetStatsAPI fleetStats = fleet.getStats();
            DynamicStatsAPI statsDynamic = fleetStats.getDynamic();
            crewLost = (int) Math.floor(crewLost * statsDynamic.getValue(Stats.NON_COMBAT_CREW_LOSS_MULT));
            if (crewLost < 1) crewLost = 1;
            losses.addCrew(crewLost);
        }

        return losses;
    }

    public static void playAccidentSound(SoundId soundId) {
        SoundPlayerAPI soundPlayer = Global.getSoundPlayer();
        Vector2f position = soundPlayer.getListenerPos();
        Vector2f velocity = new Vector2f();

        String soundName = null;
        switch (soundId) {
            case EXPLOSION:
                soundName = "explosion_from_damage";
                break;
            case HIT_SOLID:
                soundName = "hit_solid";
                break;
            case ASTEROID_COLLISION:
                soundName = "collision_asteroid_ship";
                break;
        }
        soundPlayer.playSound(soundName, 1, 1, position, velocity);
    }

}
