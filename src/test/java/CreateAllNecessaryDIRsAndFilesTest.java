import DataManager.CreateAllNecessaryDIRsAndFiles;
import DataManager.FileManager;

import static DataManager.Parameters.*;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class CreateAllNecessaryDIRsAndFilesTest {

    @Test
    void createAllNecessaryDIRsAndFiles() {
        for (String s : CreateAllNecessaryDIRsAndFiles.directories) {
            File f = new File(s);
            if (f.exists()) {
                assertTrue(f.isDirectory());
            }
        }

        for (String s : CreateAllNecessaryDIRsAndFiles.preMadePatternsFilesToCopy) {
            File f = new File(s);
            if (f.exists()) {
                assertTrue(f.isFile());
            }
        }
    }

    @Test
    void createDirectories() {
        for (String s : CreateAllNecessaryDIRsAndFiles.directories) {
            File f = new File(s);
            if (f.exists()) {
                assertTrue(f.isDirectory());
            }
        }
    }

    @Test
    void isFFMpegInstalled() {
        assertTrue(CreateAllNecessaryDIRsAndFiles.isFFMpegInstalled());
    }

    @Test
    void isPythonInstalled() {
        assertTrue(CreateAllNecessaryDIRsAndFiles.isPythonInstalled());
    }

    @Test
    void isPipInstalled() {
        assertTrue(CreateAllNecessaryDIRsAndFiles.isPipInstalled());
    }
}