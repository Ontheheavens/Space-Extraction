package spacextra.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import spacextra.utility.Accessors;

import java.awt.*;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 29.01.2023
 */

@SuppressWarnings("ParameterHidesMemberVariable")
public class SpaceExtractionAbility extends BaseDurationAbility {

    public static final String TARGET_EXTRACTION_SOURCE = "extraction_source_terrain";

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
        if (!Accessors.isInsideExtractionSource()) return false;
        return super.isUsable();
    }

    @Override
    protected void applyEffect(float amount, float level) {}

    @Override
    protected void deactivateImpl() {}

    @Override
    protected void cleanupImpl() {}

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        Color highlight = Misc.getHighlightColor();
        AbilitySpecAPI abilitySpec = this.getSpec();
        tooltip.addTitle(abilitySpec.getName());
        CampaignFleetAPI fleet = this.getFleet();
        LocationAPI containingLocation = fleet.getContainingLocation();
        List<CampaignTerrainAPI> terrains = Accessors.getTerrainsWithPlayerFleet();
        if (terrains != null && !terrains.isEmpty()) {
            tooltip.addPara("Fleet is in:", 4.0f);
            tooltip.addSpacer(4.0f);
            for (CampaignTerrainAPI terrain : terrains) {
                CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();
                String terrainName = terrainPlugin.getTerrainName();
                tooltip.addPara("   - %s (Instance: %s)", 2.0f, highlight,
                        terrainName, terrain.toString());
            }
        }
        if (fleet.getOrbit() != null) {
            addOrbitInfo(tooltip, expanded);
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
