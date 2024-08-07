package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.Common;

import lombok.Getter;
import lombok.Setter;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FilenameFilter;

@Getter @Setter
public class DifficultyFileNameExtensionFilter extends FileFilter implements FilenameFilter {
    private final String[] extensions;
    private final String description;
    private final String[] excludedFiles;

    public DifficultyFileNameExtensionFilter(String description, String[] extensions, String[] excludedFiles) {
        this.description = description;
        this.extensions = extensions;
        this.excludedFiles = excludedFiles;
    }

    /**
     * Decides if a File should be accepted based on the Rules
     *
     * @param file the File to test
     * @return boolean
     */
    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        String fileName = file.getName().toLowerCase();
        return checkFile(fileName);
    }

    /**
     * Decides which Files in a folder "dir" should be accepted based on the Rules
     *
     * @param dir the File to test
     * @return boolean
     */

    @Override
    public boolean accept(File dir, String name) {
        File file = new File(dir, name);
        if (file.isDirectory()) {
            return false;
        }

        String fileName = name.toLowerCase();
        return checkFile(fileName);
    }

    /**
     * Decides, if the file should be accepted based on the filename
     *
     * @param fileName filename
     * @return true, if accepts
     */
    private boolean checkFile(String fileName) {
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
