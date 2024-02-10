package spacextra.utility;

import com.fs.starfarer.api.campaign.CampaignTerrainAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 10.02.2024
 */
@SuppressWarnings("StaticCollection")
final class Caches {

    private static final List<CampaignTerrainAPI> cachedTerrainNames = new ArrayList<>();

    private Caches() {}

    /**
     * Clears map instance returned by previous call of this method. This is to avoid memory allocation
     * for methods called every frame.
     * @return recycled map instance.
     */
    static List<CampaignTerrainAPI> getEmptyTerrainCollection() {
        cachedTerrainNames.clear();
        return cachedTerrainNames;
    }

}
