package spacextra.abilities.accidents;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.util.Pair;
import spacextra.abilities.calculations.ExtractionSource;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 13.02.2024
 */
public interface ExtractionAccident {

    void dispatchAccidentReport(TextPanelAPI textPanel, CampaignFleetAPI fleet);

    CargoAPI getLosses(CampaignFleetAPI fleet);

    boolean canHappenHere(Iterable<CampaignTerrainAPI> targetTerrains, Map<ExtractionSource, Float> sources);

    /**
     * @return pair of maximum crew loss ratio and machinery loss ratio.
     */
    Pair<Float, Float> getMaximumLossRatio();

}
