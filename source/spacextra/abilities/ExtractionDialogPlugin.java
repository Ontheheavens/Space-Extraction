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
import spacextra.utility.Accessors;
import spacextra.utility.DialogUtilities;

import java.awt.*;
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

        List<CampaignTerrainAPI> targetTerrains = Accessors.getTerrainsWithPlayerFleet();

        ExtractionSource source = DialogUtilities.getPrimaryTargetTerrain(targetTerrains);
        String primaryTargetName = DialogUtilities.getPrimaryTargetName(source);

        textPanel.addParagraph("Your fleet assumes a stable orbit relative to the " + primaryTargetName + ".");

        String operationName = "extraction";
        if (source == ExtractionSource.ASTEROID_BELT || source == ExtractionSource.ASTEROID_FIElD) {
            operationName = "mining";
        }

        textPanel.addParagraph("After a short delay, exploration crew officer submits a " +
                "preliminary assessment of a potential " + operationName + " operation.");

        SectorAPI sector = Global.getSector();
        FactionAPI playerFaction = sector.getPlayerFaction();
        CampaignFleetAPI fleet = sector.getPlayerFleet();
        CargoAPI cargo = fleet.getCargo();

        Color color = playerFaction.getColor();
        Color negativeHighlightColor = Misc.getNegativeHighlightColor();
        Color highlight = Misc.getHighlightColor();
        float costHeight = 67;

        ResourceCostPanelAPI cost = textPanel.addCostPanel("Crew & machinery available:", costHeight,
                color, playerFaction.getDarkUIColor());
        cost.setNumberOnlyMode(true);
        cost.setWithBorder(false);
        cost.setAlignment(Alignment.LMID);

        Map<String, Integer> requiredRes = DialogUtilities.getExtractionCapabilities();

        for (Map.Entry<String, Integer> entry : requiredRes.entrySet()) {
            String commodityId = entry.getKey();
            int required = entry.getValue();
            int available = (int) cargo.getCommodityQuantity(commodityId);
            Color curr = color;
            if (required > cargo.getQuantity(CargoAPI.CargoItemType.RESOURCES, commodityId)) {
                curr = negativeHighlightColor;
            }
            cost.addCost(commodityId, required + " (" + available + ")", curr);
        }
        cost.update();

        int valuePercent = 100;
        if (valuePercent < 0) valuePercent = 0;
        String valueString = valuePercent + "%";
        Color valueColor = highlight;

        if (valuePercent < 100) {
            valueColor = negativeHighlightColor;
        }

        TooltipMakerAPI info = textPanel.beginTooltip();
        info.setParaSmallInsignia();
        info.addPara("Resource " + operationName + " effectiveness: %s", 0.0f, valueColor, valueString);

        textPanel.addTooltip();

        options.addOption("Begin extraction operation", OptionId.BEGIN);
        options.addOption("Leave", OptionId.LEAVE);
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

                result.addCommodity(Commodities.ORE, 10);

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
