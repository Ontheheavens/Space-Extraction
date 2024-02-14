package spacextra.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import spacextra.abilities.calculations.ExtractionCapability;
import spacextra.abilities.calculations.ExtractionSource;
import spacextra.abilities.calculations.TimedEffectsManager;
import spacextra.abilities.interaction.ExtractionDialogPlugin;
import spacextra.utility.Common;
import spacextra.utility.DialogUtilities;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 29.01.2023
 */

@SuppressWarnings("ParameterHidesMemberVariable")
public class SpaceExtractionAbility extends BaseDurationAbility {

    public static final String TARGET_EXTRACTION_SOURCE = "extraction_source_terrain";

    private static final float LONG_COOLDOWN = 1.0f;

    private boolean extractionDone;

    public void notifyExtractionDone() {
        this.extractionDone = true;
        cooldownLeft = LONG_COOLDOWN;
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        TimedEffectsManager.advance(amount);

        if (extractionDone) {
            if (cooldownLeft <= 0.0f) {
                extractionDone = false;
            }
        }
    }

    @Override
    public float getCooldownDays() {
        if (extractionDone) return LONG_COOLDOWN;
        return super.getCooldownDays();
    }

    @Override
    protected void activateImpl() {
        CampaignFleetAPI fleet = this.getFleet();
        LocationAPI containingLocation = fleet.getContainingLocation();
        Vector2f fleetPosition = fleet.getLocation();

        SectorEntityToken target = containingLocation.addCustomEntity(
                null, null, Entities.DEBRIS_FIELD_SHARED, Factions.NEUTRAL);
        containingLocation.removeEntity(target);

        SectorAPI sector = Global.getSector();
        CampaignUIAPI campaignUI = sector.getCampaignUI();
        campaignUI.showInteractionDialog(new ExtractionDialogPlugin(), target);
    }

    @Override
    public boolean isUsable() {
        boolean hasMachinery = ExtractionCapability.hasMinimumCrewedMachinery();
        boolean isInExtractionSource = Common.isInsideExtractionSource();
        if (!isInExtractionSource || !hasMachinery) return false;
        return super.isUsable();
    }

    @Override
    protected void applyEffect(float amount, float level) {}

    @Override
    protected void deactivateImpl() {}

    @Override
    protected void cleanupImpl() {}

    @Override
    public float getTooltipWidth() {
        return 300.0f;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        AbilitySpecAPI abilitySpec = this.getSpec();

        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();

        tooltip.addTitle(spec.getName());

        float pad = 10.0f;

        tooltip.addPara("Exploit space objects for valuable raw resources.", pad);

        CampaignFleetAPI fleet = this.getFleet();
        LocationAPI containingLocation = fleet.getContainingLocation();

        List<CampaignTerrainAPI> terrains = Common.getTerrainsWithPlayerFleet();
        Map<ExtractionSource, Float> allAvailableSources = DialogUtilities.getAllAvailableSources(terrains);

        if (terrains != null && !terrains.isEmpty()) {
            tooltip.addPara("Space terrain available for extraction:", 4.0f);
            tooltip.addSpacer(8.0f);

            DialogUtilities.addTerrainSources(tooltip, highlight, allAvailableSources);
        }

        boolean hasMachinery = ExtractionCapability.hasMinimumCrewedMachinery();
        boolean isInExtractionSource = Common.isInsideExtractionSource();

        if (!isInExtractionSource) {
            tooltip.addPara("Your fleet is not currently inside any exploitable terrain.",
                    Misc.getNegativeHighlightColor(), pad);
        } else if (!hasMachinery) {
            tooltip.addPara("Your fleet does not have enough machinery.",
                    Misc.getNegativeHighlightColor(), pad);
        }

        addIncompatibleToTooltip(tooltip, expanded);
    }

    private void addOrbitInfo(TooltipMakerAPI tooltip, boolean expanded) {
        Color highlight = Misc.getHighlightColor();
        CampaignFleetAPI fleet = this.getFleet();
        SectorEntityToken focus = fleet.getOrbitFocus();

        float pad = 4.0f;
        if (focus instanceof CampaignTerrainAPI) {
            focus = focus.getOrbitFocus();
        }
        float orbitHeight = fleet.getCircularOrbitRadius() - focus.getRadius();
        if (orbitHeight < 0) {
            orbitHeight = 0;
        }
        String distance = Misc.getRoundedValueMaxOneAfterDecimal(orbitHeight);
        tooltip.addPara("Orbiting %s with distance %s.", pad, highlight, focus.getName(), distance);
        MarketAPI market = focus.getMarket();
        if (market != null) {
            MarketAPI.SurveyLevel surveyLevel = market.getSurveyLevel();
            if (surveyLevel == MarketAPI.SurveyLevel.FULL) {
                List<MarketConditionAPI> focusConditions = market.getConditions();
                tooltip.addPara("Orbited body features:", pad);
                for (MarketConditionAPI condition : focusConditions) {
                    tooltip.addPara("   - %s", pad, highlight, condition.getName());
                }
            } else {
                tooltip.addPara("Body not surveyed: features unknown", pad);
            }
        }
    }

}
