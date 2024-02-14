package spacextra.abilities.interaction;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spacextra.abilities.calculations.ExtractionCapability;
import spacextra.abilities.calculations.ExtractionSource;
import spacextra.utility.Common;
import spacextra.utility.DialogUtilities;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 13.02.2024
 */
final class TerrainExtractionInfo {

    private static final float RESOURCE_WIDGET_HEIGHT = 67;

    private TerrainExtractionInfo() {
    }

    static void addEfficiencyOverview(TextPanelAPI textPanel, String operationName) {
        Color negativeHighlightColor = Misc.getNegativeHighlightColor();
        Color highlight = Misc.getHighlightColor();

        MutableStat efficiency = ExtractionCapability.getExtractionEfficiency();

        int valuePercent = Math.round(efficiency.getModifiedValue() * 100.0f);
        if (valuePercent < 0) valuePercent = 0;
        String valueString = valuePercent + "%";
        Color valueColor = highlight;

        if (valuePercent < 100) {
            valueColor = negativeHighlightColor;
        }

        TooltipMakerAPI info = textPanel.beginTooltip();
        info.setParaSmallInsignia();
        info.addPara("Resource recovery effectiveness: %s", 0.0f, valueColor, valueString);
        if (!efficiency.isUnmodified()) {
            info.addStatModGrid(300, 50, 10.0f, 5.0f, efficiency,
                    true, DialogUtilities.getEfficiencyModPrinter());
        }
        textPanel.addTooltip();
    }

    static void addAvailableSources(TextPanelAPI textPanel) {
        List<CampaignTerrainAPI> targetTerrains = Common.getTerrainsWithPlayerFleet();
        Map<ExtractionSource, Float> allAvailableSources = DialogUtilities.getAllAvailableSources(targetTerrains);
        Color highlight = Misc.getHighlightColor();

        textPanel.addParagraph("Resource richness:");
        TooltipMakerAPI allTerrainsTooltip = textPanel.beginTooltip();
        DialogUtilities.addTerrainSources(allTerrainsTooltip, highlight, allAvailableSources);
        textPanel.addTooltip();
    }

    static void addYieldProspects(TextPanelAPI textPanel) {
        SectorAPI sector = Global.getSector();
        FactionAPI playerFaction = sector.getPlayerFaction();
        CampaignFleetAPI fleet = sector.getPlayerFleet();
        CargoAPI cargo = fleet.getCargo();

        Color highlight = Misc.getHighlightColor();
        Color color = playerFaction.getBrightUIColor();

        TooltipMakerAPI spacer = textPanel.beginTooltip();
        spacer.addSpacer(-4.0f);
        textPanel.addTooltip();

        ResourceCostPanelAPI cost = textPanel.addCostPanel("Potential resource yield: minimum - maximum",
                RESOURCE_WIDGET_HEIGHT, color, color);
        cost.setNumberOnlyMode(true);
        cost.setWithBorder(false);
        cost.setAlignment(Alignment.LMID);

        List<CampaignTerrainAPI> targetTerrains = Common.getTerrainsWithPlayerFleet();
        Map<ExtractionSource, Float> sources = DialogUtilities.getAllAvailableSources(targetTerrains);

        Map<String, Float> totalYield = ExtractionCapability.getTotalYield(sources);

        for (Map.Entry<String, Float> entry : totalYield.entrySet()) {
            float amount = entry.getValue();
            float minimum = ExtractionCapability.getMinimumRandomized(amount);
            float maximum = ExtractionCapability.getMaximumRandomized(amount);

            String minimumString = Common.getRoundedToWhole(minimum);
            String maximumString = Common.getRoundedToWhole(maximum);

            cost.addCost(entry.getKey(), minimumString + " - " + maximumString, highlight);
        }

        cost.update();
    }

    static void addCapabilitiesOverview(TextPanelAPI textPanel) {
        SectorAPI sector = Global.getSector();
        FactionAPI playerFaction = sector.getPlayerFaction();
        CampaignFleetAPI fleet = sector.getPlayerFleet();
        CargoAPI cargo = fleet.getCargo();

        Color playerColor = playerFaction.getBrightUIColor();
        Color highlight = Misc.getHighlightColor();
        Color negativeHighlightColor = Misc.getNegativeHighlightColor();

        ResourceCostPanelAPI cost = textPanel.addCostPanel("Crew, machinery & supplies: required (available)",
                RESOURCE_WIDGET_HEIGHT, playerColor, playerFaction.getDarkUIColor());
        cost.setNumberOnlyMode(true);
        cost.setWithBorder(false);
        cost.setAlignment(Alignment.LMID);

        float machinery = cargo.getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        float crew = cargo.getCommodityQuantity(Commodities.CREW);

        float neededCrew = ExtractionCapability.getNeededCrew(cargo);
        float crewedMachinery = ExtractionCapability.getCrewedMachinery(cargo);

        float availableSupplies = cargo.getCommodityQuantity(Commodities.SUPPLIES);
        float requiredSupplies = ExtractionCapability.getRequiredSupplies(cargo);

        Color crewColor = playerColor;
        if (neededCrew > crew) {
            crewColor = negativeHighlightColor;
        }
        Color machineryColor = playerColor;
        if (crewedMachinery < machinery) {
            machineryColor = negativeHighlightColor;
        }
        Color suppliesColor = playerColor;
        if (availableSupplies < requiredSupplies) {
            suppliesColor = negativeHighlightColor;
        }

        String crewString = Common.getRoundedToWhole(crew);
        String neededCrewString = Common.getRoundedToWhole(neededCrew);
        String machineryString = Common.getRoundedToWhole(machinery);
        String crewedMachineryString = Common.getRoundedToWhole(crewedMachinery);

        String suppliesString = Common.getRoundedToWhole(availableSupplies);
        String requiredSuppliesString = Common.getRoundedToWhole(requiredSupplies);

        cost.addCost(Commodities.CREW,
                neededCrewString + " (" + crewString + ")", crewColor);
        cost.addCost(Commodities.HEAVY_MACHINERY,
                crewedMachineryString + " (" + machineryString + ")", machineryColor);
        cost.addCost(Commodities.SUPPLIES,
                requiredSuppliesString + " (" + suppliesString + ")", suppliesColor);
        cost.setLastCostConsumed(true);

        cost.update();
    }

    static void addAccidentChanceHint(TextPanelAPI textPanel) {
        float accidentProbability = DialogUtilities.getAccidentProbability();
        if (accidentProbability <= ExtractionCapability.MINIMUM_ACCIDENT_CHANCE) {
            String lowRisk = "Salvage crews are fully prepared and ready to go, " +
                    "risk of an accident during extraction operation is %s.";
            textPanel.addPara(lowRisk, Misc.getPositiveHighlightColor(), "minimal");
        } else if (accidentProbability < 0.5f) {
            String significantRisk = "Extraction equipment is in need of maintenance, " +
                    "lack of preparation can pose a %s risk in an upcoming operation.";
            textPanel.addPara(significantRisk, Misc.getHighlightColor(), "significant");
        } else {
            String highRisk = "Both hardware and crewmen are in a state of disarray, " +
                    "there is a %s chance of possible accidents.";
            textPanel.addPara(highRisk, Misc.getNegativeHighlightColor(), "high");
        }
    }

}
