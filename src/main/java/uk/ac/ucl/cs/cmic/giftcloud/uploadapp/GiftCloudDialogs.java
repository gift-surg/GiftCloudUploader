package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.display.SafeFileChooser;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CancellationException;

public class GiftCloudDialogs {

    private final MainFrame mainFrame;
    private final ImageIcon icon;
    private final String applicationName;

    public GiftCloudDialogs(final GiftCloudUploaderAppConfiguration application, final MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.applicationName = application.getApplicationTitle();

        // Set the default background colour to white
        UIManager UI = new UIManager();
        UI.put("OptionPane.background", Color.white);
        UI.put("Panel.background", Color.white);

        // Get the GIFT-Cloud icon - this will return null if not found
        icon = new ImageIcon(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/GiftCloud.png"));
    }

    public void showMessage(final String message) throws HeadlessException {

        final JPanel messagePanel = new JPanel(new GridBagLayout());
        final JEditorPane textField = new JEditorPane();
        textField.setContentType("text/html");
        textField.setText(message);
        textField.setEditable(false);
        textField.setBackground(null);
        textField.setBorder(null);
        textField.setEditable(false);
        textField.setForeground(UIManager.getColor("Label.foreground"));
        textField.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        textField.setFont(UIManager.getFont("Label.font"));

        messagePanel.add(textField);
        textField.setAlignmentX(SwingConstants.CENTER);

        JOptionPane.showMessageDialog(mainFrame.getContainer(), messagePanel, applicationName, JOptionPane.INFORMATION_MESSAGE, icon);
    }

    public void showError(final String message, final Optional<String> additionalText) throws HeadlessException {
        final JPanel messagePanel = new JPanel(new GridBagLayout());
        final StringBuilder stringMessage = new StringBuilder();
        stringMessage.append("<html>");
        stringMessage.append(message);
        if (additionalText.isPresent()) {
            stringMessage.append("<br>");
            stringMessage.append(additionalText.get());
        }
        stringMessage.append("</html>");
        messagePanel.add(new JLabel(stringMessage.toString(), SwingConstants.CENTER));

        JOptionPane.showMessageDialog(mainFrame.getContainer(), messagePanel, applicationName, JOptionPane.ERROR_MESSAGE, icon);
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
            return Optional.of(new SelectedPathAndFile(chooser));
        } else {
            return Optional.empty();
        }
    }

    Optional<SelectedPathAndFiles> selectMultipleFilesOrDirectors(final Optional<String> initialPathInput) {

        String initialPath;
        if (initialPathInput.isPresent() && StringUtils.isNotBlank(initialPathInput.get())) {
            initialPath = initialPathInput.get();
        } else {
            initialPath = "/";
        }

        // need to do the file choosing on the main event thread, since Swing is not thread safe, so do it here, instead of delegating to MediaImporter in ImportWorker
        SafeFileChooser chooser = new SafeFileChooser(initialPath);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);

        if (chooser.showOpenDialog(mainFrame.getContainer()) == JFileChooser.APPROVE_OPTION) {
            return Optional.of(new SelectedPathAndFiles(chooser));
        } else {
            return Optional.empty();
        }
    }


    public String showInputDialogToSelectProject(final java.util.List<String> projectMap, final Component component, final Optional<String> lastProject) throws IOException {
        final String lastProjectName = lastProject.isPresent() ? lastProject.get() : "";

        if (projectMap.size() < 1) {
            return null;
        }

        String[] projectStringArray = projectMap.toArray(new String[0]);

        final String defaultSelection = projectMap.contains(lastProjectName) ? lastProjectName : null;

        final Object returnValue = JOptionPane.showInputDialog(component, "Please select a project to which data will be uploaded.", applicationName, JOptionPane.QUESTION_MESSAGE, null, projectStringArray, defaultSelection);

        if (returnValue == null) {
            throw new CancellationException("User cancelled project selection during upload");
        }

        if (!(returnValue instanceof String)) {
            throw new RuntimeException("Bad return type");
        }
        return (String)returnValue;
    }

    public String showTextInputDialog(final Component component, final String message, final Optional<String> defaultName) throws IOException {
        final String initialName = defaultName.isPresent() ? defaultName.get() : "";

        String returnString = "";

        while (returnString.length() < 1) {
            final Object returnValue = JOptionPane.showInputDialog(component, message, applicationName, JOptionPane.PLAIN_MESSAGE, icon, null, initialName);
            if (returnValue == null) {
                throw new CancellationException("User cancelled template saving");
            }
            if (!(returnValue instanceof String)) {
                throw new RuntimeException("Bad return type");
            }
            returnString = (String)returnValue;
        }
        return returnString;
    }

    /**
     * A helper class used to represent a file or directory selection
     */
    class SelectedPathAndFile {
        private final String parentPath;
        private final String selectedPath;
        private final String selectedFile;

        SelectedPathAndFile(final SafeFileChooser chooser) {
            this.parentPath = chooser.getCurrentDirectory().getAbsolutePath();
            this.selectedFile = chooser.getSelectedFile().getAbsolutePath();

            final File selectedFile = chooser.getSelectedFile();
            if (selectedFile.isDirectory()) {
                this.selectedPath = chooser.getSelectedFile().getAbsolutePath();

            } else {
                this.selectedPath = chooser.getCurrentDirectory().getAbsolutePath();
            }
        }

        /**
         * @return the currently visible path from the file dialog. If the selected file is actually a directory, getParentPath() will return the parent directory, whereas getSelectedPath() will return the selected directory. If the selected file is not a path, these will be the same
         */
        public String getParentPath() {
            return parentPath;
        }

        /**
         * @return the path containing the file that has been selected, or the selected directory.
         */
        public String getSelectedPath() {
            return selectedPath;
        }

        /**
         * @return the file or directory that has been selected
         */
        public String getSelectedFile() {
            return selectedFile;
        }
    }
    /**
     * A helper class used to represent a file or directory selection
     */
    class SelectedPathAndFiles {
        private final String parentPath;
        private final String selectedPath;
        private final java.util.List<File> selectedFiles;
        private final java.util.List<String> selectedFileStrings;

        SelectedPathAndFiles(final SafeFileChooser chooser) {
            parentPath = chooser.getCurrentDirectory().getAbsolutePath();
            selectedFileStrings = new ArrayList<String>();
            selectedFiles = Arrays.asList(chooser.getSelectedFiles());
            for (final File file : selectedFiles) {
                selectedFileStrings.add(file.getAbsolutePath());
            }

            if (selectedFiles.size() == 1 && selectedFiles.get(0).isDirectory()) {
                this.selectedPath = selectedFiles.get(0).getAbsolutePath();

            } else {
                this.selectedPath = chooser.getCurrentDirectory().getAbsolutePath();
            }
        }

        /**
         * @return the currently visible path from the file dialog. If the selected file is actually a directory, getParentPath() will return the parent directory, whereas getSelectedPath() will return the selected directory. If the selected file is not a path, these will be the same
         */
        public String getParentPath() {
            return parentPath;
        }

        /**
         * @return the path containing the file that has been selected, or the selected directory.
         */
        public String getSelectedPath() {
            return selectedPath;
        }

        /**
         * @return the file or directory that has been selected
         */
        public java.util.List<File> getSelectedFiles() {
            return selectedFiles;
        }
    }


}
