package UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.DropDowns;

import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.AbstractDropDownMenu;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.CreateAdvancedMapButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateAdvancedMapDropDownMenuTags extends AbstractDropDownMenu {
    public CreateAdvancedMapDropDownMenuTags(MyButton parent, int tagIndex) {
        super(tagIndex == 1 ? ElementTypes.ADVANCED_MAP_CREATOR_SET_TAGS : ElementTypes.ADVANCED_MAP_CREATOR_SET_TAGS_2,
                parent, tagIndex - 1, getReversedTags(), CreateAdvancedMapButton.tags);
    }

    private static List<String> getReversedTags() {
        List<String> reversedTags = new ArrayList<>(Parameters.MAP_TAGS);
        Collections.reverse(reversedTags);
        return reversedTags;
    }
}
