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
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.util.vector.Vector2f;
import spacextra.abilities.calculations.ExtractionCapability;
import spacextra.utility.Common;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Ontheheavens
 * @since 13.02.2024
 */
abstract class AbstractAccident implements ExtractionAccident {

    @SuppressWarnings("PackageVisibleInnerClass")
    enum SoundType {
        EXPLOSION("explosion_from_damage"),
        HIT_SOLID("hit_solid"),
        ASTEROID_COLLISION("collision_asteroid_ship");

        private final String soundID;

        SoundType(String sound) {
            this.soundID = sound;
        }

        String getSoundId() {
            return soundID;
        }

    }

    private static List<String> getMiddleOperationBeginnings() {
        return Arrays.asList(
                "In the midst of",
                "While conducting",
                "In the course of",
                "During",
                "Right in the middle of"
        );
    }

    private static List<String> getMessages() {
        return Arrays.asList(
                "a frantic series of messages",
                "a panicked report",
                "an emergency transmission"
        );
    }

    private static List<String> getInterruptedLeisureBeginnings() {
        return Arrays.asList(
                "Very comfortable, you are snuggling in your quarters after lunch",
                "Eyes closed, you are dozing off in your captain's chair",
                "With your mouth full, you savor the early meal"
        );
    }

    static String getRandomInterruptedLeisureBeginning() {
        return Common.chooseRandom(AbstractAccident.getInterruptedLeisureBeginnings());
    }

    static String getRandomMessage() {
        return Common.chooseRandom(AbstractAccident.getMessages());
    }

    static String getRandomMiddleOperationBeginning() {
        return Common.chooseRandom(AbstractAccident.getMiddleOperationBeginnings());
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

    void dispatchLosses(TextPanelAPI textPanel, CampaignFleetAPI fleet) {
        CargoAPI losses = getLosses(fleet);

        int crewLost = losses.getCrew();
        int machineryLost = (int) losses.getCommodityQuantity(Commodities.HEAVY_MACHINERY);

        String outcome = "After the incident is resolved, head of the emergency response team reports " +
                "that a total of ";
        textPanel.addParagraph(outcome);

        Color highlight = Misc.getHighlightColor();

        String machineryLossString = " units of heavy machinery have been lost.";
        if (crewLost <= 0) {
            textPanel.appendToLastParagraph(machineryLost + machineryLossString);
            textPanel.highlightInLastPara(highlight, "" + machineryLost);
        } else if (machineryLost <= 0) {
            textPanel.appendToLastParagraph(crewLost + " crew members have been lost.");
            textPanel.highlightInLastPara(highlight, "" + crewLost);
        } else {
            textPanel.appendToLastParagraph(crewLost + " crew members " +
                    "and " + machineryLost + machineryLossString);
            textPanel.highlightInLastPara(highlight, "" + crewLost, "" + machineryLost);
        }

        CargoAPI fleetCargo = fleet.getCargo();
        for (CargoStackAPI stack : losses.getStacksCopy()) {
            float stackSize = stack.getSize();
            String commodityId = stack.getCommodityId();

            fleetCargo.removeCommodity(commodityId, stackSize);
            AddRemoveCommodity.addCommodityLossText(commodityId, (int) stackSize, textPanel);
        }
    }

    static void playAccidentSound(SoundType sound) {
        SoundPlayerAPI soundPlayer = Global.getSoundPlayer();
        Vector2f position = soundPlayer.getListenerPos();
        Vector2f velocity = new Vector2f();
        soundPlayer.playSound(sound.getSoundId(), 1, 1, position, velocity);
    }

}
