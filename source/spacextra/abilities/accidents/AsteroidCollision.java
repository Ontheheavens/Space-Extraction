package spacextra.abilities.accidents;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.util.Pair;
import spacextra.abilities.calculations.ExtractionSource;
import spacextra.utility.Common;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 14.02.2024
 */
public class AsteroidCollision extends AbstractAccident {

    @Override
    public void dispatchAccidentReport(TextPanelAPI textPanel, CampaignFleetAPI fleet) {
        textPanel.setFontInsignia();

        String circumstance;
        String cause;
        if (Common.randomFloat() < 0.5) {

        } else {

        }
//        textPanel.addParagraph(circumstance);
//        textPanel.addParagraph(cause);

        this.dispatchLosses(textPanel, fleet);

        AbstractAccident.playAccidentSound(SoundType.ASTEROID_COLLISION);
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
