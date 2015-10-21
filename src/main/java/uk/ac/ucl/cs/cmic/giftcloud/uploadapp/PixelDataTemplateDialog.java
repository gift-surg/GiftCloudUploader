
package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.display.*;
import com.pixelmed.event.EventContext;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PixelDataTemplateDialog extends JFrame {
    private BlackoutCurrentImage blackoutCurrentImage;
    private BlackoutDicomFiles blackoutDicomFiles;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private final GiftCloudDialogs giftCloudDialogs;
    private final GiftCloudReporter reporter;
    private SaveTemplateActionListener saveTemplateActionListener;
    private LoadImageActionListener loadImageActionListener;

    private JButton saveTemplateButton;
    private JButton loadImageButton;

    private static final String helpText = "Left button: drag to draw box. Left click: select or unselect box. Delete: delete selected boxes. Right mouse: adjust window.";
    private static final Dimension maximumMultiPanelDimension = new Dimension(800, 600);
    private static final double splitPaneResizeWeight = 0.9;
    private Box mainPanel;
    private JPanel multiPanel;
    private SingleImagePanel imagePanel;
    private EventContext ourEventContext;

    public PixelDataTemplateDialog(final Component owner, final String title, final GiftCloudPropertiesFromApplication giftCloudProperties, final GiftCloudDialogs giftCloudDialogs, final GiftCloudReporter reporter) {
        super(title);
        this.giftCloudProperties = giftCloudProperties;
        this.giftCloudDialogs = giftCloudDialogs;
        this.reporter = reporter;
        setLocationRelativeTo(owner);	// without this, appears at TLHC rather then center of parent or screen

        blackoutDicomFiles = null;
        blackoutCurrentImage = new BlackoutCurrentImage();

        //No need to setBackground(Color.lightGray) .. we set this via L&F UIManager properties for the application that uses this class
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // Window closed
                dispose();
            }
        });

        buildUIComponents();

        selectAndLoadImages();
    }

    private void selectAndLoadImages() {
        Optional<GiftCloudDialogs.SelectedPathAndFile> selectFileOrDirectory = giftCloudDialogs.selectFileOrDirectory(giftCloudProperties.getLastTemplateImageSourceDirectory());

        if (selectFileOrDirectory.isPresent()) {
            giftCloudProperties.setLastTemplateImageSourceDirectory(selectFileOrDirectory.get().getParentPath());
            giftCloudProperties.save();
            String filePath = selectFileOrDirectory.get().getSelectedFile();
            String[] fileNames = {filePath};
            String dicomFileNames[] = fileNames;
            blackoutDicomFiles = new BlackoutDicomFiles(dicomFileNames);

            loadDicomFileOrDirectory();
        }
    }

    /**
     * <p>Load the named DICOM file and display it in the image panel.</p>
     *
     */
    protected void loadDicomFileOrDirectory() {
        SingleImagePanel.deconstructAllSingleImagePanelsInContainer(multiPanel);
        multiPanel.removeAll();
        multiPanel.revalidate();        // needed because contents have changed
        multiPanel.repaint();            // because if one dimension of the size does not change but the other shrinks, then the old image is left underneath, not overwritten by background (000446)
        SafeCursorChanger cursorChanger = new SafeCursorChanger(this);
        cursorChanger.setWaitCursor();

        try {
            blackoutCurrentImage.loadDicomFileOrDirectory(blackoutDicomFiles.getCurrentFileName());
            SourceImage sImg = blackoutCurrentImage.getSourceImage();
            imagePanel = new SingleImagePanelWithRegionDrawing(sImg, ourEventContext);
            imagePanel.setShowOverlays(giftCloudProperties.getBurnInOverlays());
            imagePanel.setApplyShutter(false);    // we do not want to "hide" from view any identification information hidden behind shutters (000607)
            addSingleImagePanelToMultiPanelAndEstablishLayout(sImg);
            cursorChanger.restoreCursor();    // needs to be here and not later, else interferes with cursor in repaint() of  SingleImagePanel
            showUIComponents();                // will pack, revalidate, etc, perhaps for the first time

        } catch (Exception e) {
            // Read failed
            cursorChanger.restoreCursor();
            dispose();
        }
    }





    protected Dimension changeDimensionToFitInMaximumAvailable(Dimension useDimension, Dimension maxDimension, boolean onlySmaller) {
        double useWidth = useDimension.getWidth();
        double useHeight = useDimension.getHeight();
        double maxWidth = maxDimension.getWidth();
        double maxHeight = maxDimension.getHeight();
        double useScaleFactor = CenterMaximumAfterInitialSizeLayout.getScaleFactorToFitInMaximumAvailable(useWidth, useHeight, maxWidth, maxHeight);
        if (useScaleFactor < 1 || !onlySmaller) {
            useWidth = useWidth * useScaleFactor;
            useHeight = useHeight * useScaleFactor;
        }
        useDimension = new Dimension((int) useWidth, (int) useHeight);
        return useDimension;
    }

    protected Dimension reduceDimensionToFitInMaximumAvailable(Dimension useDimension) {
        return changeDimensionToFitInMaximumAvailable(useDimension, maximumMultiPanelDimension, true);
    }

    protected void addSingleImagePanelToMultiPanelAndEstablishLayout(SourceImage sImg) {
        // Need to have some kind of layout manager, else imagePanel does not resize when frame is resized by user
        addSingleImagePanelToMultiPanelAndEstablishLayoutWithCenterMaximumAfterInitialSizeLayout(sImg);
    }

    protected void addSingleImagePanelToMultiPanelAndEstablishLayoutWithCenterMaximumAfterInitialSizeLayout(SourceImage sImg) {
        Dimension useDimension = reduceDimensionToFitInMaximumAvailable(sImg.getDimension());

        imagePanel.setPreferredSize(useDimension);
        imagePanel.setMinimumSize(useDimension);    // this is needed to force initial size to be large enough; will be reset to null later to allow resize to change

        multiPanel.setPreferredSize(useDimension);    // this seems to be needed as well
        multiPanel.setMinimumSize(useDimension);    // this seems to be needed as well

        CenterMaximumAfterInitialSizeLayout layout = new CenterMaximumAfterInitialSizeLayout();
        multiPanel.setLayout(layout);
        multiPanel.setBackground(Color.black);

        multiPanel.add(imagePanel);
    }


    protected void showUIComponents() {
        remove(mainPanel);                    // in case not the first time
        add(mainPanel);
        pack();
        //multiPanel.revalidate();
        validate();
        setVisible(true);
        imagePanel.setMinimumSize(null);    // this is needed to prevent later resize being limited to initial size ...
        multiPanel.setMinimumSize(null);    // this is needed to prevent later resize being limited to initial size ...
    }

    protected void buildUIComponents() {
        ourEventContext = new EventContext("Blackout Panel");

        multiPanel = new JPanel();

        JPanel blackoutButtonsPanel = new JPanel();
        blackoutButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        saveTemplateButton = new JButton("Save template");
        saveTemplateButton.setToolTipText("Save an image template for the defined shapes");
        blackoutButtonsPanel.add(saveTemplateButton);
        saveTemplateActionListener = new SaveTemplateActionListener(this);
        saveTemplateButton.addActionListener(saveTemplateActionListener);

        loadImageButton = new JButton("Select new image");
        loadImageButton.setToolTipText("Save an image template for the defined shapes");
        blackoutButtonsPanel.add(loadImageButton);
        loadImageActionListener = new LoadImageActionListener(this);
        loadImageButton.addActionListener(loadImageActionListener);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, multiPanel, blackoutButtonsPanel);
        splitPane.setOneTouchExpandable(false);
        splitPane.setResizeWeight(splitPaneResizeWeight);

        JLabel helpBar = new JLabel(helpText);

        mainPanel = new Box(BoxLayout.Y_AXIS);
        mainPanel.add(splitPane);
        mainPanel.add(helpBar);
    }


    public void deconstruct() {
        if (multiPanel != null) {
            SingleImagePanel.deconstructAllSingleImagePanelsInContainer(multiPanel);
        }
    }

    public void dispose() {
        deconstruct();        // just in case wasn't already called, and garbage collection occurs
        super.dispose();
    }

    protected void finalize() throws Throwable {
        deconstruct();        // just in case wasn't already called, and garbage collection occurs
        super.finalize();
    }

    private void saveTemplate() throws DicomException, IOException {
        final String filterName = giftCloudDialogs.showTextInputDialog(this, "Please enter a name for this template:", Optional.<String>empty());

        final PixelDataAnonymiseFilter filter = createFilter(filterName);
        final String filterPath = giftCloudProperties.getFilterDirectory().getAbsolutePath();
        final File filterFile = new File(filterPath, filterName + ".gcfilter");
        try {
            PixelDataAnonymiserFilterJsonWriter.writeJsonfile(filterFile, filter, reporter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PixelDataAnonymiseFilter createFilter(final String filterName) throws DicomException {
        return new PixelDataAnonymiseFilter(filterName, getRequiredTagsFromAttributes(blackoutCurrentImage.getDicomAttributes()), imagePanel.getPersistentDrawingShapes());
    }

    private static List<PixelDataAnonymiseFilterRequiredTag> getRequiredTagsFromAttributes(final AttributeList dicomAttributes) throws DicomException {
        final java.util.List<PixelDataAnonymiseFilterRequiredTag> requiredTags = new ArrayList<PixelDataAnonymiseFilterRequiredTag>();

        addStringIfPresent(requiredTags, dicomAttributes, TagFromName.TransferSyntaxUID);
        addStringIfPresent(requiredTags, dicomAttributes, TagFromName.SOPClassUID);
        addStringIfPresent(requiredTags, dicomAttributes, TagFromName.Modality);
        addStringIfPresent(requiredTags, dicomAttributes, TagFromName.Manufacturer);
        addStringIfPresent(requiredTags, dicomAttributes, TagFromName.ManufacturerModelName);
        addIntIfPresent(requiredTags, dicomAttributes, TagFromName.Rows);
        addIntIfPresent(requiredTags, dicomAttributes, TagFromName.Columns);

        return requiredTags;
    }

    private static void addIntIfPresent(final java.util.List<PixelDataAnonymiseFilterRequiredTag> requiredTags, final AttributeList dicomAttributes, final AttributeTag tag) throws DicomException {
        if (dicomAttributes.containsKey(tag)) {
            requiredTags.add(new IntFilterTag(dicomAttributes.get(tag)));
        }
    }

    private static void addStringIfPresent(final java.util.List<PixelDataAnonymiseFilterRequiredTag> requiredTags, final AttributeList dicomAttributes, final AttributeTag tag) throws DicomException {
        if (dicomAttributes.containsKey(tag)) {
            requiredTags.add(new StringFilterTag(dicomAttributes.get(tag)));
        }
    }

    protected class SaveTemplateActionListener implements ActionListener {
        PixelDataTemplateDialog application;
        SafeCursorChanger cursorChanger;

        public SaveTemplateActionListener(PixelDataTemplateDialog application) {
            this.application = application;
            cursorChanger = new SafeCursorChanger(application);
        }

        public void actionPerformed(ActionEvent event) {
            cursorChanger.setWaitCursor();
            try {
                saveTemplate();
            } catch (Exception e) {
                reporter.reportErrorToUser("Saving the template failed due to the following error: " + e.getLocalizedMessage(), e);
            }
            cursorChanger.restoreCursor();
        }
    }

    protected class LoadImageActionListener implements ActionListener {
        PixelDataTemplateDialog application;
        SafeCursorChanger cursorChanger;

        public LoadImageActionListener(PixelDataTemplateDialog application) {
            this.application = application;
            cursorChanger = new SafeCursorChanger(application);
        }

        public void actionPerformed(ActionEvent event) {
            cursorChanger.setWaitCursor();
            selectAndLoadImages();
            cursorChanger.restoreCursor();
        }
    }
}

