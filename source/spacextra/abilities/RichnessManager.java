package spacextra.abilities;

import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import spacextra.utility.Common;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 11.02.2024
 */
@SuppressWarnings("StaticCollection")
public final class RichnessManager {

    private static final Map<String, Map<String, Float>> RICHNESS_MAP = new HashMap<>();

    private static final float MIN_RICHNESS = 0.2f;
    private static final float MAX_RICHNESS = 1.8f;

    private RichnessManager() {}

    public static float getRichness(LocationAPI location, CampaignTerrainAPI terrain) {
        String locationID = location.getId();
        String terrainID = terrain.getId();
        if (!RICHNESS_MAP.containsKey(locationID)) {
            RICHNESS_MAP.put(locationID, new HashMap<String, Float>());
        }
        Map<String, Float> locationTerrains = RICHNESS_MAP.get(locationID);
        if (!locationTerrains.containsKey(terrainID)) {
            locationTerrains.put(terrainID, RichnessManager.generateRichness());
        }
        return locationTerrains.get(terrainID);
    }

    private static float generateRichness() {
        return Common.getRandomizedGaussian(1.0f, MIN_RICHNESS, MAX_RICHNESS, 0.6f);
    }

    @SuppressWarnings("IfStatementWithTooManyBranches")
    static Color mapRichnessToColor(float richness) {
        Color veryScarce = new Color(200,100,100);
        Color scarce = new Color(200,150,100);
        Color poor = new Color(210,210,120);
        Color normal = new Color(200,200,200);
        Color moderate = new Color(100,200,110);
        Color abundant = new Color(75,125,255);
        Color rich = new Color(150,100,200);
        Color ultrarich = new Color(255,100,255);

        Color result;
        if (richness <= 0.4f) {
            result = veryScarce;
        } else if (richness <= 0.6f) {
            result = scarce;
        } else if (richness <= 0.8f) {
            result = poor;
        } else if (richness <= 1.0f) {
            result = normal;
        } else if (richness <= 1.2f) {
            result = moderate;
        } else if (richness <= 1.4f) {
            result = abundant;
        } else if (richness <= 1.6f) {
            result = rich;
        } else {
            result = ultrarich;
        }

        return result;
    }

}
