package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.Common;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class DifficultyFileNameExtensionFilter extends FileFilter {
    private final String[] extensions;
    private final String description;
    private final String[] excludedFiles;


    public DifficultyFileNameExtensionFilter(String description, String[] extensions, String[] excludedFiles) {
        this.description = description;
        this.extensions = extensions;
        this.excludedFiles = excludedFiles;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        String fileName = file.getName().toLowerCase();
        for (String excludedFile : excludedFiles) {
            if (fileName.equals(excludedFile.toLowerCase())) {
                return false;
            }
        }

        for (String extension : extensions) {
            if (fileName.endsWith("." + extension)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
