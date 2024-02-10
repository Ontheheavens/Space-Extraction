package spacextra.utility;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import spacextra.abilities.ExtractionSource;

import java.util.List;
import java.util.Set;

/**
 * @author Ontheheavens
 * @since 10.02.2024
 */
public final class Accessors {

    private Accessors() {}

    public static boolean isInsideExtractionSource() {
        List<CampaignTerrainAPI> terrains = Accessors.getTerrainsWithPlayerFleet();
        if (terrains == null) return false;
        for (CampaignTerrainAPI terrain : terrains) {
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();
            Set<String> allSourceIDs = ExtractionSource.getAllSourceIDs();
            if (allSourceIDs.contains(terrainPlugin.getTerrainId())) {
                return true;
            }
        }
        return false;
    }

    public static List<CampaignTerrainAPI> getTerrainsWithPlayerFleet() {
        SectorAPI sector = Global.getSector();
        CampaignFleetAPI playerFleet = sector.getPlayerFleet();

        if (playerFleet == null) {
            return null;
        }

        LocationAPI containingLocation = playerFleet.getContainingLocation();
        List<CampaignTerrainAPI> allTerrains = containingLocation.getTerrainCopy();

        List<CampaignTerrainAPI> result = Caches.getEmptyTerrainCollection();
        for (CampaignTerrainAPI terrain : allTerrains) {
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();
            if (terrainPlugin.containsEntity(playerFleet)) {
                result.add(terrain);
            }
        }

        return result;
    }

}
