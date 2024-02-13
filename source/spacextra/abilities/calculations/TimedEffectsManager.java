package spacextra.abilities.calculations;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.SectorAPI;

/**
 * @author Ontheheavens
 * @since 13.02.2024
 */
public final class TimedEffectsManager {

    public static final float MINIMUM_READINESS = 0.4f;
    private static float readiness = 1.0f;
    private static float readinessIncrementor = 1.0f;
    private static float exhaustionIncrementor = 1.0f;

    private TimedEffectsManager() {
    }

    static float getReadiness() {
        return readiness;
    }

    public static void decrementReadiness() {
        readiness *= 0.6f;
        if (readiness < 0.0f) {
            readiness = 0.0f;
        }
    }

    public static void advance(float amount) {
        if (readiness >= 1.0f) {
            readiness = 1.0f;
            return;
        }
        SectorAPI sector = Global.getSector();
        CampaignClockAPI clock = sector.getClock();
        float days = clock.convertToDays(amount);
        readinessIncrementor -= days;
        if (readinessIncrementor <= 0.0f) {
            readiness += 0.1f;
            readinessIncrementor = 1.0f;
            if (readiness >= 1.0f) {
                readiness = 1.0f;
            }
        }

        float months = clock.convertToMonths(amount);
        exhaustionIncrementor -= months;
        if (exhaustionIncrementor <= 0.0f) {
            SourceDataManager.restoreExhaustionGlobally();
            exhaustionIncrementor = 1.0f;
        }
    }

}
