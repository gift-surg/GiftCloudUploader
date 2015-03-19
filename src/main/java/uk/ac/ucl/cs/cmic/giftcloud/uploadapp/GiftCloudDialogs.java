package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.display.SafeFileChooser;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Optional;

public class GiftCloudDialogs {

    private final MainFrame mainFrame;
    private final ImageIcon icon;

    public GiftCloudDialogs(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // Set the default background colour to white
        UIManager UI =new UIManager();
        UI.put("OptionPane.background", Color.white);
        UI.put("Panel.background", Color.white);

        // Get the GIFT-Cloud icon - this will return null if not found
        icon = new ImageIcon(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/GiftCloud.png"));
    }

    public void showMessage(final String message) throws HeadlessException {

        final JPanel messagePanel = new JPanel(new GridBagLayout());
        messagePanel.add(new JLabel(message, SwingConstants.CENTER));


        JOptionPane.showMessageDialog(mainFrame.getContainer(), messagePanel, "GIFT-Cloud", JOptionPane.INFORMATION_MESSAGE, icon);
    }

    public void showError(final String message) throws HeadlessException {

        final JPanel messagePanel = new JPanel(new GridBagLayout());
        messagePanel.add(new JLabel(message, SwingConstants.CENTER));

        JOptionPane.showMessageDialog(mainFrame.getContainer(), messagePanel, "GIFT-Cloud", JOptionPane.ERROR_MESSAGE, icon);
    }




    public String getSelection(String message, String title, String[] valueArray, String defaultSelection) {
        return (String)JOptionPane.showInputDialog(mainFrame.getContainer(), message, title, JOptionPane.QUESTION_MESSAGE, icon, valueArray, defaultSelection);
    }

    Optional<String> selectDirectory(final Optional<String> initialPathInput) throws IOException {
        String initialPath;
        if (initialPathInput.isPresent() && StringUtils.isNotBlank(initialPathInput.get())) {
            initialPath = initialPathInput.get();
        } else {
            initialPath = "/";
        }

        SafeFileChooser chooser = new SafeFileChooser(initialPath);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(mainFrame.getContainer()) == JFileChooser.APPROVE_OPTION) {
            return Optional.of(chooser.getSelectedFile().getCanonicalPath());
        } else {
            return Optional.empty();
        }
    }

    Optional<SelectedPathAndFile> selectFileOrDirectory(final Optional<String> initialPathInput) {

        String initialPath;
        if (initialPathInput.isPresent() && StringUtils.isNotBlank(initialPathInput.get())) {
            initialPath = initialPathInput.get();
        } else {
            initialPath = "/";
        }

        // need to do the file choosing on the main event thread, since Swing is not thread safe, so do it here, instead of delegating to MediaImporter in ImportWorker
        SafeFileChooser chooser = new SafeFileChooser(initialPath);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        if (chooser.showOpenDialog(mainFrame.getContainer()) == JFileChooser.APPROVE_OPTION) {
            return Optional.of(new SelectedPathAndFile(chooser.getCurrentDirectory().getAbsolutePath(), chooser.getSelectedFile().getAbsolutePath()));
        } else {
            return Optional.empty();
        }

    }

    class SelectedPathAndFile {
        private String selectedPath;
        private String selectedFile;

        SelectedPathAndFile(final String selectedPath, final String selectedFile) {
            this.selectedPath = selectedPath;
            this.selectedFile = selectedFile;
        }

        public String getSelectedPath() {
            return selectedPath;
        }

        public String getSelectedFile() {
            return selectedFile;
        }
    }


}
