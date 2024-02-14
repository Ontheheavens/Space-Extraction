package spacextra.abilities.accidents;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
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

    private List<String> getMalfunctionCauses() {
        return Arrays.asList(
                "wear and tear of equipment",
                "a certain network software error",
                "unforeseen environmental factors",
                "some sudden technical issues"
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
    public void dispatchAccidentReport(TextPanelAPI textPanel, CampaignFleetAPI fleet) {
        textPanel.setFontInsignia();
        String circumstance;
        String cause;
        if (Common.randomFloat() < 0.5) {
            circumstance = AbstractAccident.getRandomMiddleOperationBeginning() + " the operation " +
                    "your flagship's comms erupt with chatter as " + AbstractAccident.getRandomMessage() +
                    " is sent from the salvage crew.";
            cause = "Apparently, " + getRandomMalfunctionCause() + " led to a critical malfunction " +
                    "of extraction equipment, " + getRandomMalfunctionResult() + ".";
        } else {
            circumstance = AbstractAccident.getRandomInterruptedLeisureBeginning() + " when a sudden noise " +
                    "from your comm set interrupts the leisure. Rushing to the terminal, you see "
                    + AbstractAccident.getRandomMessage() + " flashing on the screen.";
            cause = "You query attendant duty officer, who reports that " + getRandomMalfunctionCause() +
                    " caused a disastrous malfunction of extraction hardware, " + getRandomMalfunctionResult() + ".";
        }
        textPanel.addParagraph(circumstance);
        textPanel.addParagraph(cause);

        this.dispatchLosses(textPanel, fleet);

        AbstractAccident.playAccidentSound(SoundType.EXPLOSION);
    }

    @Override
    protected void dispatchAdditionalLosses() {}

    @Override
    public Pair<Float, Float> getMaximumLossRatio() {
        return new Pair<>(0.015f, 0.035f);
    }

    @Override
    public boolean canHappenHere(Iterable<CampaignTerrainAPI> targetTerrains, Map<ExtractionSource, Float> sources) {
        return true;
    }

}
