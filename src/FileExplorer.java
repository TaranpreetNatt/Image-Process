import javax.swing.*;
import java.io.File;

public class FileExplorer {

    public File openFile() {
        File file = null;
        int response;
        JFileChooser chooser = new JFileChooser("../images");

        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        response = chooser.showOpenDialog(null);

        if (response == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
        }
        return file;
    }

}
