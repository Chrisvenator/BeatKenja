package UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.DropDowns;

import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.AbstractDropDownMenu;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.CreateAdvancedMapButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

public class CreateAdvancedMapDropDownMenuGenres extends AbstractDropDownMenu {
    public CreateAdvancedMapDropDownMenuGenres(MyButton parent, int genreIndex) {
        super(genreIndex == 1 ? ElementTypes.ADVANCED_MAP_CREATOR_SET_GENRES : ElementTypes.ADVANCED_MAP_CREATOR_SET_GENRES_2,
                parent, genreIndex - 1, Parameters.MUSIC_GENRES, CreateAdvancedMapButton.genres);
    }
}
