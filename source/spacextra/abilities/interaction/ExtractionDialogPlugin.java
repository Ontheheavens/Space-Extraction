package spacextra.abilities.interaction;

import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import org.lwjgl.input.Keyboard;
import spacextra.SpaceExtraModPlugin;
import spacextra.abilities.SpaceExtractionAbility;
import spacextra.abilities.accidents.ExtractionAccident;
import spacextra.abilities.calculations.ExtractionCapability;
import spacextra.abilities.calculations.ExtractionSource;
import spacextra.abilities.calculations.SourceDataManager;
import spacextra.abilities.calculations.TimedEffectsManager;
import spacextra.utility.Common;
import spacextra.utility.DialogUtilities;

import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 10.02.2024
 */
public class ExtractionDialogPlugin implements InteractionDialogPlugin {

    private static final String LEAVE_OPTION_TEXT = "Leave";
    private InteractionDialogAPI dialog;
    private TextPanelAPI textPanel;
    private OptionPanelAPI options;
    private VisualPanelAPI visual;

    private enum OptionId {
        BEGIN,
        CONTINUE,
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

        TerrainExtractionInfo.addCapabilitiesOverview(textPanel);
        TerrainExtractionInfo.addEfficiencyOverview(textPanel, operationName);
        TerrainExtractionInfo.addAvailableSources(textPanel);
        TerrainExtractionInfo.addYieldProspects(textPanel);
        TerrainExtractionInfo.addAccidentChanceHint(textPanel);

        options.addOption("Begin " + operationName + " operation", OptionId.BEGIN);
        if (!ExtractionCapability.hasRequiredSupplies(Common.getPlayerCargo())) {
            options.setEnabled(OptionId.BEGIN, false);
            options.setTooltip(OptionId.BEGIN, "Insufficient supplies");
        }

        this.addLeaveOption();
    }

    private void addLeaveOption() {
        options.addOption(LEAVE_OPTION_TEXT, OptionId.LEAVE);
        options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData == null) return;

        OptionId option = (OptionId) optionData;

        if (optionText != null) {
            dialog.addOptionSelectedText(option);
        }

        List<CampaignTerrainAPI> targetTerrains = Common.getTerrainsWithPlayerFleet();

        Map<ExtractionSource, Float> sources = DialogUtilities.getAllAvailableSources(targetTerrains);

        switch (option) {
            case BEGIN:
                if (Common.randomFloat() < DialogUtilities.getAccidentProbability()) {
                    this.createAccident();
                } else {
                    this.commenceExtraction(targetTerrains, sources);
                }
                break;
            case CONTINUE:
                this.commenceExtraction(targetTerrains, sources);
                break;
            case LEAVE:
                dialog.dismiss();
                break;
        }
    }

    private void createAccident() {
        ExtractionAccident accident = DialogUtilities.createRandomAccident();
        SectorAPI sector = Global.getSector();
        accident.dispatchAccidentReport(textPanel, sector.getPlayerFleet());

        options.clearOptions();
        options.addOption("Continue", OptionId.CONTINUE);
    }

    private void commenceExtraction(Iterable<CampaignTerrainAPI> targetTerrains,
                                    Map<ExtractionSource, Float> sources) {
        if (sources.isEmpty() || targetTerrains == null) {
            throw new RuntimeException("Space Extraction: No target terrain found");
        }

        Map<String, Float> totalYield = ExtractionCapability.getTotalYield(sources);

        CargoAPI cargo = Common.getPlayerCargo();
        float requiredSupplies = ExtractionCapability.getRequiredSupplies(cargo);
        cargo.removeCommodity(Commodities.SUPPLIES, requiredSupplies);

        TimedEffectsManager.decrementReadiness();

        SectorAPI sector = Global.getSector();
        CampaignFleetAPI playerFleet = sector.getPlayerFleet();
        AbilityPlugin ability = playerFleet.getAbility(SpaceExtraModPlugin.SPACE_EXTRACTION_ABILITY);
        if (ability instanceof SpaceExtractionAbility) {
            ((SpaceExtractionAbility) ability).notifyExtractionDone();
        }

        MutableFleetStatsAPI stats = playerFleet.getStats();
        stats.addTemporaryModFlat(0.25f, "salvage_ops",
                "Recent extraction operation", SalvageEntity.SALVAGE_DETECTION_MOD_FLAT,
                stats.getDetectedRangeMod());
        sector.addPing(playerFleet, "noticed_player");

        SourceDataManager.increaseExhaustion(targetTerrains);

        this.addYieldsAsLoot(totalYield);
    }

    private void addYieldsAsLoot(Map<String, Float> totalYield) {
        FactoryAPI factory = Global.getFactory();
        CargoAPI result = factory.createCargo(true);

        for (Map.Entry<String, Float> entry : totalYield.entrySet()) {
            float amount = entry.getValue();
            float randomized = ExtractionCapability.getRandomized(amount);
            int rounded = Math.round(randomized);
            result.addCommodity(entry.getKey(), rounded);
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
