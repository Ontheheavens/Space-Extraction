package spacextra;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;

/**
 * @author Ontheheavens
 * @since 29.01.2023
 */

public class SpaceExtraModPlugin extends BaseModPlugin {

    @SuppressWarnings("WeakerAccess")
    public static final String SPACE_EXTRACTION_ABILITY = "space_extraction";

    @Override
    public void onGameLoad(boolean newGame) {
        SectorAPI sector = Global.getSector();
        CampaignFleetAPI playerFleet = sector.getPlayerFleet();
        if (playerFleet == null || !playerFleet.isValidPlayerFleet()) {
            return;
        }
        if (!playerFleet.hasAbility(SPACE_EXTRACTION_ABILITY)) {
            CharacterDataAPI characterData = sector.getCharacterData();
            characterData.addAbility(SPACE_EXTRACTION_ABILITY);
        }
    }
}
