
package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.display.*;
import com.pixelmed.display.event.GraphicDisplayChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.event.EventContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

public class PixelDataTemplateDialog extends JFrame {
    private BlackoutCurrentImage blackoutCurrentImage;
    private BlackoutShapeDefinition blackoutShapeDefinition;
    private BlackoutDicomFiles blackoutDicomFiles;
    private GiftCloudPropertiesFromApplication giftCloudProperties;
    private final GiftCloudDialogs giftCloudDialogs;
    protected SaveTemplateActionListener saveTemplateActionListener;

    protected JButton saveTemplateButton;
    protected JCheckBox useZeroBlackoutValueCheckBox;
    protected JCheckBox usePixelPaddingBlackoutValueCheckBox;

    private static final String helpText = "Left button: drag to draw box. Left click: select or unselect box. Delete: delete selected boxes. Right mouse: adjust window.";
    private static final Dimension maximumMultiPanelDimension = new Dimension(800, 600);
    private static final double splitPaneResizeWeight = 0.9;
    protected Box mainPanel;
    protected JPanel multiPanel;
    protected SingleImagePanel imagePanel;
    protected EventContext ourEventContext;
    protected boolean burnInOverlays;
    protected boolean useZeroBlackoutValue;
    protected boolean usePixelPaddingBlackoutValue;

    public PixelDataTemplateDialog(final Component owner, final String title, final GiftCloudPropertiesFromApplication giftCloudProperties, final GiftCloudDialogs giftCloudDialogs) {
        super(title);
        this.giftCloudProperties = giftCloudProperties;
        this.giftCloudDialogs = giftCloudDialogs;
        setLocationRelativeTo(owner);	// without this, appears at TLHC rather then center of parent or screen

        blackoutShapeDefinition = null;
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
            giftCloudProperties.setLastTemplateImageSourceDirectory(selectFileOrDirectory.get().getSelectedPath());
            giftCloudProperties.save();
            String filePath = selectFileOrDirectory.get().getSelectedFile();
            String[] fileNames = {filePath};
            String dicomFileNames[] = fileNames;
            blackoutDicomFiles = new BlackoutDicomFiles(dicomFileNames);

            loadDicomFileOrDirectory(blackoutShapeDefinition);
        }
    }

    /**
     * <p>Load the named DICOM file and display it in the image panel.</p>
     *
     */
    protected void loadDicomFileOrDirectory(BlackoutShapeDefinition shapeDefinition) {
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
            imagePanel.setShowOverlays(burnInOverlays);
            imagePanel.setApplyShutter(false);    // we do not want to "hide" from view any identification information hidden behind shutters (000607)
            addSingleImagePanelToMultiPanelAndEstablishLayout(sImg);
            cursorChanger.restoreCursor();    // needs to be here and not later, else interferes with cursor in repaint() of  SingleImagePanel
            showUIComponents();                // will pack, revalidate, etc, perhaps for the first time

            if (shapeDefinition != null) {
                if (shapeDefinition.getPreviousRows() == sImg.getHeight() && shapeDefinition.getPreviousColumns() == sImg.getWidth()) {
                    imagePanel.setPersistentDrawingShapes(shapeDefinition.getPreviousPersistentDrawingShapes());
                } else {
//					shapeDefinition = null;
                }
            }

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
        // don't set button panel height, else interacts with validate during showUIComponents() needed for no initial image resizing, and cuts off button panel
        //blackoutButtonsPanel.setPreferredSize(new Dimension((int)multiPanel.getPreferredSize().getWidth(),heightWantedForButtons));

        blackoutButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        burnInOverlays = false;
        useZeroBlackoutValue = false;
        usePixelPaddingBlackoutValue = true;

        JCheckBox keepOverlaysCheckBox = new JCheckBox("Overlays", burnInOverlays);
        keepOverlaysCheckBox.setToolTipText("Toggle whether or not to display overlays, and if displayed burn them into the image and remove them from the header");
        keepOverlaysCheckBox.setMnemonic(KeyEvent.VK_O);
        blackoutButtonsPanel.add(keepOverlaysCheckBox);
        keepOverlaysCheckBox.addChangeListener(new OverlaysChangeListener(ourEventContext));

        // application scope not local, since change listener needs access to make mutually exclusive with useZeroBlackoutValueCheckBox
        usePixelPaddingBlackoutValueCheckBox = new JCheckBox("Use Padding", usePixelPaddingBlackoutValue);
        usePixelPaddingBlackoutValueCheckBox.setToolTipText("Toggle whether or not to use the pixel padding value for blackout pixels, rather than the default minimum possible pixel value based on signedness and bit depth");
        usePixelPaddingBlackoutValueCheckBox.setMnemonic(KeyEvent.VK_P);
        blackoutButtonsPanel.add(usePixelPaddingBlackoutValueCheckBox);
        usePixelPaddingBlackoutValueCheckBox.addChangeListener(new PixelPaddingBlackoutValueChangeListener(ourEventContext));

        // application scope not local, since change listener needs access to make mutually exclusive with usePixelPaddingBlackoutValueCheckBox
        useZeroBlackoutValueCheckBox = new JCheckBox("Use Zero", useZeroBlackoutValue);
        useZeroBlackoutValueCheckBox.setToolTipText("Toggle whether or not to use a zero value for blackout pixels, rather than the pixel padding value or default minimum possible pixel value based on signedness and bit depth");
        useZeroBlackoutValueCheckBox.setMnemonic(KeyEvent.VK_Z);
        blackoutButtonsPanel.add(useZeroBlackoutValueCheckBox);
        useZeroBlackoutValueCheckBox.addChangeListener(new ZeroBlackoutValueChangeListener(ourEventContext));

        saveTemplateButton = new JButton("Save template");
        saveTemplateButton.setToolTipText("Save an image template for the defined shapes");
        blackoutButtonsPanel.add(saveTemplateButton);
        saveTemplateActionListener = new SaveTemplateActionListener(this);
        saveTemplateButton.addActionListener(saveTemplateActionListener);

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

    private void saveTemplate() {

    }



    protected class ZeroBlackoutValueChangeListener implements ChangeListener {
        EventContext eventContext;

        public ZeroBlackoutValueChangeListener(EventContext eventContext) {
            this.eventContext = eventContext;
        }

        public void stateChanged(ChangeEvent e) {
            if (e != null && e.getSource() instanceof JCheckBox) {
                useZeroBlackoutValue = ((JCheckBox) (e.getSource())).isSelected();
                if (useZeroBlackoutValue) {
                    usePixelPaddingBlackoutValue = false;
                    usePixelPaddingBlackoutValueCheckBox.setSelected(usePixelPaddingBlackoutValue);
                }
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new GraphicDisplayChangeEvent(eventContext, useZeroBlackoutValue));
            }
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
            saveTemplate();
            cursorChanger.restoreCursor();
        }
    }

    protected class OverlaysChangeListener implements ChangeListener {
        EventContext eventContext;

        public OverlaysChangeListener(EventContext eventContext) {
            this.eventContext = eventContext;
        }

        public void stateChanged(ChangeEvent e) {
            if (e != null && e.getSource() instanceof JCheckBox) {
                burnInOverlays = ((JCheckBox) (e.getSource())).isSelected();
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new GraphicDisplayChangeEvent(eventContext, burnInOverlays));
            }
        }
    }

    protected class PixelPaddingBlackoutValueChangeListener implements ChangeListener {
        EventContext eventContext;

        public PixelPaddingBlackoutValueChangeListener(EventContext eventContext) {
            this.eventContext = eventContext;
        }

        public void stateChanged(ChangeEvent e) {
            if (e != null && e.getSource() instanceof JCheckBox) {
                usePixelPaddingBlackoutValue = ((JCheckBox) (e.getSource())).isSelected();
                if (usePixelPaddingBlackoutValue) {
                    useZeroBlackoutValue = false;
                    useZeroBlackoutValueCheckBox.setSelected(useZeroBlackoutValue);
                }
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new GraphicDisplayChangeEvent(eventContext, usePixelPaddingBlackoutValue));
            }
        }
    }

}

