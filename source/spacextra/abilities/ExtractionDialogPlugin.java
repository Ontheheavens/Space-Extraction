package spacextra.abilities;

import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spacextra.utility.Common;
import spacextra.utility.DialogUtilities;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 10.02.2024
 */
public class ExtractionDialogPlugin implements InteractionDialogPlugin {

    private InteractionDialogAPI dialog;
    private TextPanelAPI textPanel;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;

    private static final float RESOURCE_WIDGET_HEIGHT = 67;;
    private static final float YIELD_MULTIPLIER = 1.0f;

    private enum OptionId {
        BEGIN,
        LEAVE
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;

        textPanel = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        visual = dialog.getVisualPanel();

        visual.showImageVisual(new InteractionDialogImageVisual("illustrations",
                "free_orbit", 480, 300));
        SectorEntityToken target = dialog.getInteractionTarget();

        List<CampaignTerrainAPI> targetTerrains = Common.getTerrainsWithPlayerFleet();

        ExtractionSource primaryTargetSource = DialogUtilities.getPrimaryTargetTerrain(targetTerrains);
        String primaryTargetName = DialogUtilities.getPrimaryTargetName(primaryTargetSource);

        textPanel.addParagraph("Your fleet assumes a stable orbit relative to the " + primaryTargetName + ".");

        String operationName = "extraction";
        if (primaryTargetSource == ExtractionSource.ASTEROID_BELT || primaryTargetSource == ExtractionSource.ASTEROID_FIElD) {
            operationName = "mining";
        }

        textPanel.addParagraph("After a short delay, exploration crew officer submits a " +
                "preliminary assessment of a potential " + operationName + " operation.");

        Color negativeHighlightColor = Misc.getNegativeHighlightColor();
        Color highlight = Misc.getHighlightColor();

        this.addCapabilitiesOverview();

        int valuePercent = 100;
        String valueString = valuePercent + "%";

        TooltipMakerAPI info = textPanel.beginTooltip();
        info.setParaSmallInsignia();
        info.addPara("Resource " + operationName + " effectiveness: %s",
                0.0f, highlight, valueString);

        textPanel.addTooltip();

        this.addAvailableSources();
        this.addYieldProspects();

        options.addOption("Begin " + operationName + " operation", OptionId.BEGIN);
        options.addOption("Leave", OptionId.LEAVE);
    }

    private void addAvailableSources() {
        List<CampaignTerrainAPI> targetTerrains = Common.getTerrainsWithPlayerFleet();
        Map<ExtractionSource, Float> allAvailableSources = DialogUtilities.getAllAvailableSources(targetTerrains);
        Color highlight = Misc.getHighlightColor();

        textPanel.addParagraph("Yield sources richness:");
        TooltipMakerAPI allTerrainsTooltip = textPanel.beginTooltip();
        allTerrainsTooltip.beginGridFlipped(240.0f, 1, highlight,80.0f, 8.0f);
        int terrainCount = 0;

        for (Map.Entry<ExtractionSource, Float> entry : allAvailableSources.entrySet()) {
            ExtractionSource terrainSource = entry.getKey();
            float richness = entry.getValue();
            int valuePercent = (int) (richness * 100);
            String valueString = valuePercent + "%";

            Color richnessColor = RichnessManager.mapRichnessToColor(richness);
            allTerrainsTooltip.addToGrid(0, terrainCount, terrainSource.getDisplayName(),
                    valueString, richnessColor);
            terrainCount++;
        }
        allTerrainsTooltip.addGrid(4.0f);
        textPanel.addTooltip();
    }

    private void addYieldProspects() {
        SectorAPI sector = Global.getSector();
        FactionAPI playerFaction = sector.getPlayerFaction();
        CampaignFleetAPI fleet = sector.getPlayerFleet();
        CargoAPI cargo = fleet.getCargo();

        Color highlight = Misc.getHighlightColor();
        Color playerFactionColor = playerFaction.getColor();

        ResourceCostPanelAPI cost = textPanel.addCostPanel("Potential yield: minimum - maximum",
                RESOURCE_WIDGET_HEIGHT, playerFactionColor, playerFactionColor);
        cost.setNumberOnlyMode(true);
        cost.setWithBorder(false);
        cost.setAlignment(Alignment.LMID);

        List<CampaignTerrainAPI> targetTerrains = Common.getTerrainsWithPlayerFleet();
        Map<ExtractionSource, Float> sources = DialogUtilities.getAllAvailableSources(targetTerrains);

        Map<String, Float> totalYield = ExtractionDialogPlugin.getTotalYield(sources);

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

    private void addCapabilitiesOverview() {
        SectorAPI sector = Global.getSector();
        FactionAPI playerFaction = sector.getPlayerFaction();
        CampaignFleetAPI fleet = sector.getPlayerFleet();
        CargoAPI cargo = fleet.getCargo();

        Color mainPlayerColor = playerFaction.getColor();
        Color highlight = Misc.getHighlightColor();
        Color negativeHighlightColor = Misc.getNegativeHighlightColor();

        ResourceCostPanelAPI cost = textPanel.addCostPanel("Crew & machinery: required (available)",
                RESOURCE_WIDGET_HEIGHT, mainPlayerColor, playerFaction.getDarkUIColor());
        cost.setNumberOnlyMode(true);
        cost.setWithBorder(false);
        cost.setAlignment(Alignment.LMID);

        float machinery = cargo.getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        float crew = cargo.getCommodityQuantity(Commodities.CREW);

        float neededCrew = ExtractionCapability.getNeededCrew(cargo);
        float crewedMachinery = ExtractionCapability.getCrewedMachinery(cargo);

        Color crewColor = mainPlayerColor;
        if (neededCrew > crew) {
            crewColor = negativeHighlightColor;
        }
        Color machineryColor = mainPlayerColor;
        if (crewedMachinery < machinery) {
            machineryColor = negativeHighlightColor;
        }

        String crewString = Common.getRoundedToWhole(crew);
        String neededCrewString = Common.getRoundedToWhole(neededCrew);
        String machineryString = Common.getRoundedToWhole(machinery);
        String crewedMachineryString = Common.getRoundedToWhole(crewedMachinery);

        cost.addCost(Commodities.CREW,
                neededCrewString + " (" + crewString + ")", crewColor);
        cost.addCost(Commodities.HEAVY_MACHINERY,
                machineryString + " (" + crewedMachineryString + ")", machineryColor);

        cost.update();
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData == null) return;

        OptionId option = (OptionId) optionData;

        if (optionText != null) {
            dialog.addOptionSelectedText(option);
        }

        switch (option) {
            case BEGIN:
                FactoryAPI factory = Global.getFactory();
                CargoAPI result = factory.createCargo(true);

                List<CampaignTerrainAPI> targetTerrains = Common.getTerrainsWithPlayerFleet();

                Map<ExtractionSource, Float> sources = DialogUtilities.getAllAvailableSources(targetTerrains);

                if (sources.isEmpty()) {
                    throw new RuntimeException("Space Extraction: No target terrain found");
                }

                Map<String, Float> totalYield = ExtractionDialogPlugin.getTotalYield(sources);

                for (Map.Entry<String, Float> entry : totalYield.entrySet()) {
                    float amount = entry.getValue();
                    float randomized = ExtractionCapability.getRandomized(amount);
                    result.addCommodity(entry.getKey(), Math.round(randomized));
                }

                visual.showLoot("Extracted", result,
                        false, true,
                        true, new CoreInteractionListener() {
                    public void coreUIDismissed() {
                        ExtractionDialogPlugin.this.dialog.dismiss();
                        ExtractionDialogPlugin.this.dialog.hideTextPanel();
                        ExtractionDialogPlugin.this.dialog.hideVisualPanel();
                    }
                });
                options.clearOptions();
                dialog.setPromptText("");
                break;
            case LEAVE:
                dialog.dismiss();
                break;
        }
    }

    /**
     * @param sources map with extraction sources and their richness.
     * @return map with commodity IDs and their yield amounts.
     */
    private static Map<String, Float> getTotalYield(Map<ExtractionSource, Float> sources) {
        Map<String, Float> totalYield = new HashMap<>();

        float crewedMachinery = ExtractionCapability.getPlayerCrewedMachinery();
        for (Map.Entry<ExtractionSource, Float> source : sources.entrySet()) {
            ExtractionSource extractionSource = source.getKey();
            Map<String, Float> resources = extractionSource.getResources();

            for (Map.Entry<String, Float> entry : resources.entrySet()) {
                float amount = (entry.getValue() * crewedMachinery);
                float richnessModified = amount * source.getValue();
                float finalAmount = richnessModified * YIELD_MULTIPLIER;

                Float existingAmount = totalYield.get(entry.getKey());
                if (existingAmount == null) {
                    totalYield.put(entry.getKey(), finalAmount);
                } else {
                    totalYield.put(entry.getKey(), existingAmount + finalAmount);
                }
            }
        }
        return totalYield;
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {

    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {

    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }

}
