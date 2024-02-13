package spacextra.abilities.accidents;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;
import spacextra.abilities.calculations.ExtractionSource;
import spacextra.utility.Common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 13.02.2024
 */
@SuppressWarnings("MethodMayBeStatic")
public class EquipmentMalfunction extends AbstractAccident {

    public EquipmentMalfunction(TextPanelAPI textPanel, OptionPanelAPI options, CampaignFleetAPI fleet) {
        super(textPanel, options, fleet);
    }

    private List<String> getMalfunctionCauses() {
        return Arrays.asList(
                "neglected wear and tear",
                "programming software error",
                "unforeseen environmental factors",
                "sudden technical issues"
        );
    }

    private String getRandomMalfunctionCause() {
        return Common.chooseRandom(getMalfunctionCauses());
    }

    private List<String> getMalfunctionResults() {
        return Arrays.asList(
                "setting off a domino effect of machinery failures",
                "triggering a chain of explosions",
                "culminating in a rapid succession of system breakdowns",
                "sparking a series of volatile reactions"
        );
    }

    private String getRandomMalfunctionResult() {
        return Common.chooseRandom(getMalfunctionResults());
    }

    @Override
    public void dispatchTextReport(TextPanelAPI textPanel, OptionPanelAPI options) {
        SectorAPI sector = Global.getSector();
        CargoAPI losses = getLosses(sector.getPlayerFleet());

        int crewLost = losses.getCrew();
        int machineryLost = (int) losses.getCommodityQuantity(Commodities.HEAVY_MACHINERY);

        String circumstance = AbstractAccident.getRandomBeginning() + " the operation " +
                "your flagship's comms erupt with chatter as " + AbstractAccident.getRandomMessage() +
                " is transmitted from the salvage crew.";
        String cause = "Apparently, some " + getRandomMalfunctionCause() + " led to a critical malfunction " +
                "of crucial extraction equipment, " + getRandomMalfunctionResult() + ".";
        String outcome = "After the incident is resolved, the head of the emergency response team reports " +
                "that a total of " + crewLost + " crew members " +
                "and " + machineryLost + " units of heavy machinery have been lost.";
    }

    @Override
    public Pair<Float, Float> getMaximumLossRatio() {
        return new Pair<>(0.015f, 0.035f);
    }

    @Override
    public void handleFleetLosses(CampaignFleetAPI fleet) {

    }

    @Override
    public boolean canHappenHere(Iterable<CampaignTerrainAPI> targetTerrains, Map<ExtractionSource, Float> sources) {
        return true;
    }

}
