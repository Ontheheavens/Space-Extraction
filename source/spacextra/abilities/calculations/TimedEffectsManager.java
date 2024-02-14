package spacextra.abilities.calculations;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.SectorAPI;

import java.util.Map;

/**
 * @author Ontheheavens
 * @since 13.02.2024
 */
public final class TimedEffectsManager {

    public static final float MINIMUM_READINESS = 0.4f;
    private static final String SPACEXTRA_READINESS = "spacextra_readiness";

    private static Float readiness;

    /**
     * In sector days.
     */
    private static float readinessIncrementor = 1.0f;


    /**
     * In sector months.
     */
    private static float exhaustionIncrementor = 1.0f;

    private TimedEffectsManager() {
    }

    public static float getReadiness() {
        TimedEffectsManager.ensureReadiness();
        return readiness;
    }

    @SuppressWarnings("NonThreadSafeLazyInitialization")
    private static void ensureReadiness() {
        if (readiness == null) {
            SectorAPI sector = Global.getSector();
            Map<String, Object> persistentData = sector.getPersistentData();
            readiness = (Float) persistentData.get(SPACEXTRA_READINESS);
            if (readiness == null) {
                readiness = 1.0f;
                persistentData.put(SPACEXTRA_READINESS, readiness);
            }
        }
    }

    private static void savePersistently(Object value) {
        SectorAPI sector = Global.getSector();
        Map<String, Object> persistentData = sector.getPersistentData();
        persistentData.put(TimedEffectsManager.SPACEXTRA_READINESS, value);
    }

    public static void decrementReadiness() {
        TimedEffectsManager.ensureReadiness();
        readiness *= 0.6f;
        if (readiness < 0.0f) {
            readiness = 0.0f;
        }
        TimedEffectsManager.savePersistently(readiness);
    }

    public static void advance(float amount) {
        SectorAPI sector = Global.getSector();
        CampaignClockAPI clock = sector.getClock();
        float days = clock.convertToDays(amount);

        TimedEffectsManager.advanceReadiness(amount, days);

        float months = clock.convertToMonths(amount);
        exhaustionIncrementor -= months;
        if (exhaustionIncrementor <= 0.0f) {
            SourceDataManager.restoreExhaustionGlobally();
            exhaustionIncrementor = 1.0f;
        }
    }

    private static void advanceReadiness(float amount, float days) {
        TimedEffectsManager.ensureReadiness();
        if (readiness >= 1.0f) {
            readiness = 1.0f;
            TimedEffectsManager.savePersistently(readiness);
            return;
        }
        readinessIncrementor -= days;
        if (readinessIncrementor <= 0.0f) {
            readiness += 0.1f;
            readinessIncrementor = 1.0f;
            if (readiness >= 1.0f) {
                readiness = 1.0f;
            }
        }
        TimedEffectsManager.savePersistently(readiness);
    }

}
