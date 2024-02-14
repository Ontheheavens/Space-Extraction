package spacextra.abilities.accidents;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import spacextra.abilities.calculations.ExtractionSource;
import spacextra.utility.Common;

import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 14.02.2024
 */
public class AsteroidCollision extends AbstractAccident {

    @Override
    public void dispatchAccidentReport(TextPanelAPI textPanel, CampaignFleetAPI fleet) {
        textPanel.setFontInsignia();

        String circumstance = "";
        String cause = "";
        textPanel.addParagraph(circumstance);
        textPanel.addParagraph(cause);

        this.dispatchLosses(textPanel, fleet);

        FleetDataAPI fleetData = fleet.getFleetData();
        List<FleetMemberAPI> membersListCopy = fleetData.getMembersListCopy();
        FleetMemberAPI member = membersListCopy.get(0);

        float damageMult = 1.0f;
        Misc.applyDamage(member, null, damageMult, true, "asteroid_impact",
                "Asteroid impact", false, textPanel,
                member.getShipName() + " suffers damage from an asteroid impact");

        SoundType soundType;
        if (Common.randomFloat() < 0.5) {
            soundType = SoundType.ASTEROID_COLLISION;
        } else {
            soundType = SoundType.HIT_HEAVY;
        }
        AbstractAccident.playAccidentSound(soundType);
    }

    @Override
    protected void dispatchAdditionalLosses() {

    }

    @Override
    public boolean canHappenHere(Iterable<CampaignTerrainAPI> targetTerrains, Map<ExtractionSource, Float> sources) {
        boolean insideAsteroidField = Common.isInsideSpecificSource(ExtractionSource.ASTEROID_FIElD);
        boolean insideAsteroidBelt = Common.isInsideSpecificSource(ExtractionSource.ASTEROID_BELT);
        return insideAsteroidField || insideAsteroidBelt;
    }

    @Override
    public Pair<Float, Float> getMaximumLossRatio() {
        return new Pair<>(0.035f, 0.015f);
    }

}
