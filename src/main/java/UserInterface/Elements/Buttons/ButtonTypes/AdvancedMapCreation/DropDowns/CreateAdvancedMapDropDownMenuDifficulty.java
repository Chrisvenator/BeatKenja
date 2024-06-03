package UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.DropDowns;

import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.AbstractDropDownMenu;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.CreateAdvancedMapButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateAdvancedMapDropDownMenuDifficulty extends AbstractDropDownMenu {
    public CreateAdvancedMapDropDownMenuDifficulty(MyButton parent) {
        super(ElementTypes.ADVANCED_MAP_CREATOR_SET_Difficulty, parent, 0, getReversedDifficulties(), CreateAdvancedMapButton.difficulties);
    }

    private static List<String> getReversedDifficulties() {
        List<String> reversedDifficulties = new ArrayList<>(Parameters.DIFFICULTIES);
        Collections.reverse(reversedDifficulties);
        return reversedDifficulties;
    }
}
