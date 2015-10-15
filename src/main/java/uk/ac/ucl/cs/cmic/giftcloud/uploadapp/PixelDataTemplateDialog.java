/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.display.*;
import com.pixelmed.display.event.FrameSelectionChangeEvent;
import com.pixelmed.display.event.GraphicDisplayChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.event.EventContext;
import com.pixelmed.event.SelfRegisteringListener;
import com.pixelmed.utils.FileUtilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Optional;
import java.util.Vector;

/**
 * <p>This class displays images and allows the user to black out burned-in annotation, and save the result.</p>
 *
 * <p>A main method is provided, which can be supplied with a list of file names or pop up a file chooser dialog.</p>
 *
 * @author	dclunie
 */
public class PixelDataTemplateDialog extends JFrame {
    private BlackoutCurrentImage blackoutCurrentImage;
    private BlackoutShapeDefinition blackoutShapeDefinition;
    private BlackoutDicomFiles blackoutDicomFiles;
    private GiftCloudPropertiesFromApplication giftCloudProperties;
    private final GiftCloudDialogs giftCloudDialogs;

    public PixelDataTemplateDialog(final Component owner, final String title, final GiftCloudPropertiesFromApplication giftCloudProperties, final GiftCloudDialogs giftCloudDialogs) {
        super(title);
        this.giftCloudProperties = giftCloudProperties;
        this.giftCloudDialogs = giftCloudDialogs;
        setLocationRelativeTo(owner);	// without this, appears at TLHC rather then center of parent or screen

        blackoutShapeDefinition = null;
        blackoutDicomFiles = null;
        blackoutCurrentImage = new BlackoutCurrentImage();
        this.burnedinflag = BurnedInAnnotationFlagAction.ADD_AS_NO_IF_SAVED;
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
            updateDisplayedFileNumber();
            loadDicomFileOrDirectory(blackoutShapeDefinition);
        }

    }


    private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/DicomImageBlackout.java,v 1.45 2014/12/06 16:51:43 dclunie Exp $";

    protected String ourAETitle = "OURAETITLE";        // sub-classes might set this to something meaningful if they are active on the network, e.g., DicomCleaner

    //private static final String helpText = "Buttons: left windows; middle scrolls frames; right drag draws box; right click selects box; delete key discards selection";
    private static final String helpText = "Buttons: left windows; right drag draws box; right click selects box; delete key discards selection";

    private static final Dimension maximumMultiPanelDimension = new Dimension(800, 600);
    //private static final Dimension maximumMultiPanelDimension = Toolkit.getDefaultToolkit().getScreenSize();
    //private static final int heightWantedForButtons = 50;
    private static final double splitPaneResizeWeight = 0.9;

    protected Box mainPanel;
    protected JPanel multiPanel;

    protected SingleImagePanel imagePanel;


    protected BlackoutShapeDefinition recordStateOfDrawingShapesForFileChange() {
        BlackoutShapeDefinition shapeDefinition = new BlackoutShapeDefinition(blackoutCurrentImage.getSourceImage(), imagePanel.getPersistentDrawingShapes());
        blackoutShapeDefinition = shapeDefinition;
        return blackoutShapeDefinition;
    }

    protected JPanel cineSliderControlsPanel;
    protected CineSliderChangeListener cineSliderChangeListener;
    protected JSlider cineSlider;

    protected JLabel imagesRemainingLabel;

    protected EventContext ourEventContext;

    protected boolean burnInOverlays;

    protected boolean useZeroBlackoutValue;
    protected boolean usePixelPaddingBlackoutValue;

    protected JCheckBox useZeroBlackoutValueCheckBox;
    protected JCheckBox usePixelPaddingBlackoutValueCheckBox;

    // implement FrameSelectionChangeListener ...

    protected OurFrameSelectionChangeListener ourFrameSelectionChangeListener;

    class OurFrameSelectionChangeListener extends SelfRegisteringListener {

        public OurFrameSelectionChangeListener(EventContext eventContext) {
            super("com.pixelmed.display.event.FrameSelectionChangeEvent", eventContext);
        }

        /**
         * @param    e
         */
        public void changed(com.pixelmed.event.Event e) {
            FrameSelectionChangeEvent fse = (FrameSelectionChangeEvent) e;
            cineSlider.setValue(fse.getIndex() + 1);
        }
    }

    /***/
    protected class CineSliderChangeListener implements ChangeListener {

        public CineSliderChangeListener() {
        }    // so that sub-classes of DicomImageBlackout can cnostruct instances of this inner class

        /**
         * @param    e
         */
        public void stateChanged(ChangeEvent e) {
            ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new FrameSelectionChangeEvent(ourEventContext, cineSlider.getValue() - 1));
        }
    }

    /**
     * @param    min        minimum frame number, starting from 1
     * @param    max        number of frames
     * @param    value    frame number, starting from 1
     */
    protected void createCineSliderIfNecessary(int min, int max, int value) {
        if (cineSlider == null || min != cineSlider.getMinimum() || max != cineSlider.getMaximum()) {
            cineSliderControlsPanel.removeAll();
            if (max > min) {
                cineSlider = new JSlider(min, max, value);                                // don't leave to default, which is 50 and may be outside range
                cineSlider.setLabelTable(cineSlider.createStandardLabels(max - 1, min));    // just label the ends
                cineSlider.setPaintLabels(true);
                //cineSliderControlsPanel.add(new JLabel("Frame index:"));
                cineSliderControlsPanel.add(cineSlider);
                cineSlider.addChangeListener(cineSliderChangeListener);
            } else {
                cineSlider = null;    // else single frame so no slider
            }
        }
        if (cineSlider != null) {
            cineSlider.setValue(value);
        }
    }

    protected void updateDisplayedFileNumber() {
//		if (imagesRemainingLabel != null) {
//			int current = blackoutDicomFiles.getCurrentFileNumber();
//			int total = blackoutDicomFiles.getNumberOfFiles();
//			imagesRemainingLabel.setText(Integer.toString(current + 1) + " of " + Integer.toString(total));
//		}
    }

    protected int burnedinflag;

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
            createCineSliderIfNecessary(1, blackoutCurrentImage.getNumberOfImages(), 1);
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

    public void save() {
        recordStateOfDrawingShapesForFileChange();
        SingleImagePanel.deconstructAllSingleImagePanelsInContainer(multiPanel);
        multiPanel.removeAll();
        multiPanel.revalidate();        // needed because contents have changed
        multiPanel.repaint();            // because if one dimension of the size does not change but the other shrinks, then the old image is left underneath, not overwritten by background (000446)
        SafeCursorChanger cursorChanger = new SafeCursorChanger(this);
        cursorChanger.setWaitCursor();

        try {
            blackoutCurrentImage.save(blackoutDicomFiles.getCurrentFileName(), burnInOverlays, ourAETitle, burnedinflag);
            SourceImage sImg = blackoutCurrentImage.getSourceImage();
            imagePanel = new SingleImagePanelWithRegionDrawing(sImg, ourEventContext);
            imagePanel.setShowOverlays(burnInOverlays);
            imagePanel.setApplyShutter(false);    // we do not want to "hide" from view any identification information hidden behind shutters (000607)
            addSingleImagePanelToMultiPanelAndEstablishLayout(sImg);
            createCineSliderIfNecessary(1, blackoutCurrentImage.getNumberOfImages(), 1);
            cursorChanger.restoreCursor();    // needs to be here and not later, else interferes with cursor in repaint() of  SingleImagePanel
            showUIComponents();                // will pack, revalidate, etc, perhaps for the first time

            if (blackoutShapeDefinition!= null) {
                if (blackoutShapeDefinition.getPreviousRows() == sImg.getHeight() && blackoutShapeDefinition.getPreviousColumns() == sImg.getWidth()) {
                    imagePanel.setPersistentDrawingShapes(blackoutShapeDefinition.getPreviousPersistentDrawingShapes());
                } else {
                    blackoutShapeDefinition = null;
                }
            }

        } catch (Exception e) {
            cursorChanger.restoreCursor();
            dispose();
        }
    }


    private void apply(Vector shapes, boolean burnInOverlays) {
        recordStateOfDrawingShapesForFileChange();
        try {
            blackoutCurrentImage.apply(shapes, burnInOverlays, usePixelPaddingBlackoutValue, useZeroBlackoutValue);
        } catch (Exception ex) {
            dispose();
        }
    }

    protected class ApplyActionListener implements ActionListener {
        PixelDataTemplateDialog application;
        SafeCursorChanger cursorChanger;

        public ApplyActionListener(PixelDataTemplateDialog application) {
            this.application = application;
            cursorChanger = new SafeCursorChanger(application);
        }

        public void actionPerformed(ActionEvent event) {
            cursorChanger.setWaitCursor();
            if (imagePanel != null) {
                apply(imagePanel.getPersistentDrawingShapes(), burnInOverlays);
                imagePanel.dirty(blackoutCurrentImage.getSourceImage());
                imagePanel.repaint();
            }

            cursorChanger.restoreCursor();
        }
    }



    protected ApplyActionListener applyActionListener;
    protected SaveActionListener saveActionListener;
    protected NextActionListener nextActionListener;
    protected PreviousActionListener previousActionListener;

    protected JButton blackoutApplyButton;
    protected JButton blackoutSaveButton;
    protected JButton blackoutNextButton;
    protected JButton blackoutPreviousButton;

    protected class ApplySaveAllActionListener implements ActionListener {
        PixelDataTemplateDialog application;

        public ApplySaveAllActionListener(PixelDataTemplateDialog application) {
            this.application = application;
        }

        public void actionPerformed(ActionEvent event) {
            applyAll();
        }
    }

    private void applyAll() {
        if (imagePanel != null) {
            Vector persistentDrawingShapes = imagePanel.getPersistentDrawingShapes();
            do {
                loadAndApplyAndSave(persistentDrawingShapes, burnInOverlays);
                goToNext();
            }
            while (blackoutDicomFiles.filesExist() && blackoutDicomFiles.getCurrentFileNumber() < blackoutDicomFiles.getNumberOfFiles());
            imagePanel.dirty(blackoutCurrentImage.getSourceImage());
            imagePanel.repaint();
        }
    }

    private void loadAndApplyAndSave(Vector persistentDrawingShapes, boolean burnInOverlays) {
        SafeCursorChanger cursorChanger = new SafeCursorChanger(this);
        cursorChanger.setWaitCursor();
        recordStateOfDrawingShapesForFileChange();

        SingleImagePanel.deconstructAllSingleImagePanelsInContainer(multiPanel);
        multiPanel.removeAll();
        multiPanel.revalidate();        // needed because contents have changed
        multiPanel.repaint();            // because if one dimension of the size does not change but the other shrinks, then the old image is left underneath, not overwritten by background (000446)

        try {
            File inputFile = FileUtilities.getFileFromNameInsensitiveToCaseIfNecessary(blackoutDicomFiles.getCurrentFileName());
            File outputFile = new File(inputFile.getAbsolutePath() + ".new");
            blackoutCurrentImage.loadAndApplyAndSave(inputFile, outputFile, persistentDrawingShapes, burnInOverlays, usePixelPaddingBlackoutValue, useZeroBlackoutValue, ourAETitle);

            SourceImage sImg = blackoutCurrentImage.getSourceImage();
            imagePanel = new SingleImagePanelWithRegionDrawing(sImg, ourEventContext);
            imagePanel.setShowOverlays(burnInOverlays);
            imagePanel.setApplyShutter(false);    // we do not want to "hide" from view any identification information hidden behind shutters (000607)
            addSingleImagePanelToMultiPanelAndEstablishLayout(sImg);
            createCineSliderIfNecessary(1, blackoutCurrentImage.getNumberOfImages(), 1);

            if (blackoutShapeDefinition!= null) {
                if (blackoutShapeDefinition.getPreviousRows() == sImg.getHeight() && blackoutShapeDefinition.getPreviousColumns() == sImg.getWidth()) {
                    imagePanel.setPersistentDrawingShapes(blackoutShapeDefinition.getPreviousPersistentDrawingShapes());
                } else {
                    blackoutShapeDefinition = null;
                }
            }

            cursorChanger.restoreCursor();
            showUIComponents();                // will pack, revalidate, etc, perhaps for the first time
        } catch (Exception e) {
            cursorChanger.restoreCursor();
            dispose();
        }
    }

    protected class CancelActionListener implements ActionListener {
        PixelDataTemplateDialog application;

        public CancelActionListener(PixelDataTemplateDialog application) {
            this.application = application;
        }

        public void actionPerformed(ActionEvent event) {
            // Cancelled
            application.dispose();
        }
    }

    protected class OverlaysChangeListener implements ChangeListener {
        PixelDataTemplateDialog application;
        EventContext eventContext;

        public OverlaysChangeListener(PixelDataTemplateDialog application, EventContext eventContext) {
            this.application = application;
            this.eventContext = eventContext;
        }

        public void stateChanged(ChangeEvent e) {
            if (e != null && e.getSource() instanceof JCheckBox) {
                application.burnInOverlays = ((JCheckBox) (e.getSource())).isSelected();
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new GraphicDisplayChangeEvent(eventContext, application.burnInOverlays));
            }
        }
    }

    protected class ZeroBlackoutValueChangeListener implements ChangeListener {
        PixelDataTemplateDialog application;
        EventContext eventContext;

        public ZeroBlackoutValueChangeListener(PixelDataTemplateDialog application, EventContext eventContext) {
            this.application = application;
            this.eventContext = eventContext;
        }

        public void stateChanged(ChangeEvent e) {
            if (e != null && e.getSource() instanceof JCheckBox) {
                application.useZeroBlackoutValue = ((JCheckBox) (e.getSource())).isSelected();
                if (application.useZeroBlackoutValue) {
                    application.usePixelPaddingBlackoutValue = false;
                    application.usePixelPaddingBlackoutValueCheckBox.setSelected(application.usePixelPaddingBlackoutValue);
                }
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new GraphicDisplayChangeEvent(eventContext, application.useZeroBlackoutValue));
            }
        }
    }

    protected class PixelPaddingBlackoutValueChangeListener implements ChangeListener {
        PixelDataTemplateDialog application;
        EventContext eventContext;

        public PixelPaddingBlackoutValueChangeListener(PixelDataTemplateDialog application, EventContext eventContext) {
            this.application = application;
            this.eventContext = eventContext;
        }

        public void stateChanged(ChangeEvent e) {
            if (e != null && e.getSource() instanceof JCheckBox) {
                application.usePixelPaddingBlackoutValue = ((JCheckBox) (e.getSource())).isSelected();
                if (application.usePixelPaddingBlackoutValue) {
                    application.useZeroBlackoutValue = false;
                    application.useZeroBlackoutValueCheckBox.setSelected(application.useZeroBlackoutValue);
                }
                ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new GraphicDisplayChangeEvent(eventContext, application.usePixelPaddingBlackoutValue));
            }
        }
    }

    protected double getScaleFactorToFitInMaximumAvailable(double useWidth, double useHeight, double maxWidth, double maxHeight) {
        double sx = maxWidth / useWidth;
        double sy = maxHeight / useHeight;
        // always choose smallest, regardless of whether scaling up or down
        double useScaleFactor = sx < sy ? sx : sy;
        return useScaleFactor;
    }

    protected Dimension changeDimensionToFitInMaximumAvailable(Dimension useDimension, Dimension maxDimension, boolean onlySmaller) {
        double useWidth = useDimension.getWidth();
        double useHeight = useDimension.getHeight();
        double maxWidth = maxDimension.getWidth();
        double maxHeight = maxDimension.getHeight();
        double useScaleFactor = getScaleFactorToFitInMaximumAvailable(useWidth, useHeight, maxWidth, maxHeight);
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

    protected class CenterMaximumAfterInitialSizeLayout implements LayoutManager {
        public CenterMaximumAfterInitialSizeLayout() {
        }

        public void addLayoutComponent(String name, Component comp) {
        }

        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int componentCount = parent.getComponentCount();
                Dimension parentSize = parent.getSize();

                int sumOfComponentWidths = 0;
                int sumOfComponentHeights = 0;
                for (int c = 0; c < componentCount; ++c) {
                    Component component = parent.getComponent(c);
                    Dimension componentSize = component.getPreferredSize();
                    sumOfComponentWidths += componentSize.getWidth();
                    sumOfComponentHeights += componentSize.getHeight();
                }

                int availableWidth = parentSize.width - (insets.left + insets.right);
                int availableHeight = parentSize.height - (insets.top + insets.bottom);

                int leftOffset = 0;
                int topOffset = 0;

                boolean useScale = false;
                double useScaleFactor = 1;
                if (sumOfComponentWidths == availableWidth && sumOfComponentHeights <= availableHeight
                        || sumOfComponentWidths <= availableWidth && sumOfComponentHeights == availableHeight) {
                    // First time, the sum of either the widths or the heights will equal what
                    // is available, since the parent size was derived from calls to minimumLayoutSize()
                    // and preferredLayoutSize(), hence no scaling is required or should be performed ...
                    leftOffset = (availableWidth - sumOfComponentWidths) / 2;
                    topOffset = (availableHeight - sumOfComponentHeights) / 2;
                } else {
                    // Subsequently, if a resize on the parent has been performed, we should ALWAYS pay
                    // attention to it ...
                    useScale = true;
                    useScaleFactor = getScaleFactorToFitInMaximumAvailable(sumOfComponentWidths, sumOfComponentHeights, availableWidth, availableHeight);
                    leftOffset = (int) ((availableWidth - sumOfComponentWidths * useScaleFactor) / 2);
                    topOffset = (int) ((availableHeight - sumOfComponentHeights * useScaleFactor) / 2);
                }
                for (int c = 0; c < componentCount; ++c) {
                    Component component = parent.getComponent(c);
                    Dimension componentSize = component.getPreferredSize();
                    int w = componentSize.width;
                    int h = componentSize.height;
                    if (useScale) {
                        w = (int) (w * useScaleFactor);
                        h = (int) (h * useScaleFactor);
                    }
                    component.setBounds(leftOffset, topOffset, w, h);
                    leftOffset += w;
                    topOffset += h;
                }
            }
        }

        public Dimension minimumLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int componentCount = parent.getComponentCount();
                int w = insets.left + insets.right;
                int h = insets.top + insets.bottom;
                for (int c = 0; c < componentCount; ++c) {
                    Component component = parent.getComponent(c);
                    Dimension componentSize = component.getMinimumSize();
                    w += componentSize.getWidth();
                    h += componentSize.getHeight();
                }
                return new Dimension(w, h);
            }
        }

        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int componentCount = parent.getComponentCount();
                int w = insets.left + insets.right;
                int h = insets.top + insets.bottom;
                for (int c = 0; c < componentCount; ++c) {
                    Component component = parent.getComponent(c);
                    Dimension componentSize = component.getPreferredSize();
                    w += componentSize.getWidth();
                    h += componentSize.getHeight();
                }
                return new Dimension(w, h);
            }
        }

        public void removeLayoutComponent(Component comp) {
        }
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
        keepOverlaysCheckBox.addChangeListener(new OverlaysChangeListener(this, ourEventContext));

        // application scope not local, since change listener needs access to make mutually exclusive with useZeroBlackoutValueCheckBox
        usePixelPaddingBlackoutValueCheckBox = new JCheckBox("Use Padding", usePixelPaddingBlackoutValue);
        usePixelPaddingBlackoutValueCheckBox.setToolTipText("Toggle whether or not to use the pixel padding value for blackout pixels, rather than the default minimum possible pixel value based on signedness and bit depth");
        usePixelPaddingBlackoutValueCheckBox.setMnemonic(KeyEvent.VK_P);
        blackoutButtonsPanel.add(usePixelPaddingBlackoutValueCheckBox);
        usePixelPaddingBlackoutValueCheckBox.addChangeListener(new PixelPaddingBlackoutValueChangeListener(this, ourEventContext));

        // application scope not local, since change listener needs access to make mutually exclusive with usePixelPaddingBlackoutValueCheckBox
        useZeroBlackoutValueCheckBox = new JCheckBox("Use Zero", useZeroBlackoutValue);
        useZeroBlackoutValueCheckBox.setToolTipText("Toggle whether or not to use a zero value for blackout pixels, rather than the pixel padding value or default minimum possible pixel value based on signedness and bit depth");
        useZeroBlackoutValueCheckBox.setMnemonic(KeyEvent.VK_Z);
        blackoutButtonsPanel.add(useZeroBlackoutValueCheckBox);
        useZeroBlackoutValueCheckBox.addChangeListener(new ZeroBlackoutValueChangeListener(this, ourEventContext));

        blackoutPreviousButton = new JButton("Previous");
        blackoutPreviousButton.setToolTipText("Move to the previous, skipping this image, if not already saved");
        blackoutButtonsPanel.add(blackoutPreviousButton);
        previousActionListener = new PreviousActionListener(this);
        blackoutPreviousButton.addActionListener(previousActionListener);

        blackoutApplyButton = new JButton("Apply");
        blackoutApplyButton.setToolTipText("Blackout the regions");
        blackoutButtonsPanel.add(blackoutApplyButton);
        applyActionListener = new ApplyActionListener(this);
        blackoutApplyButton.addActionListener(applyActionListener);

        blackoutSaveButton = new JButton("Save");
        blackoutSaveButton.setToolTipText("Save the blacked-out image");
        blackoutButtonsPanel.add(blackoutSaveButton);
        saveActionListener = new SaveActionListener(this);
        blackoutSaveButton.addActionListener(saveActionListener);

        blackoutNextButton = new JButton("Next");
        blackoutNextButton.setToolTipText("Move to the next, skipping this image, if not already saved");
        blackoutButtonsPanel.add(blackoutNextButton);
        nextActionListener = new NextActionListener(this);
        blackoutNextButton.addActionListener(nextActionListener);

        JButton blackoutApplySaveAllButton = new JButton("Apply All & Save");
        blackoutApplySaveAllButton.setToolTipText("Blackout the regions and save the blacked-out image for this and all remaining selected images");
        blackoutButtonsPanel.add(blackoutApplySaveAllButton);
        blackoutApplySaveAllButton.addActionListener(new ApplySaveAllActionListener(this));

        imagesRemainingLabel = new JLabel("0 of 0");
        blackoutButtonsPanel.add(imagesRemainingLabel);

        JButton blackoutCancelButton = new JButton("Cancel");
        blackoutCancelButton.setToolTipText("Cancel work on this image, if not already saved, and skip all remaining images");
        blackoutButtonsPanel.add(blackoutCancelButton);
        blackoutCancelButton.addActionListener(new CancelActionListener(this));

        cineSliderControlsPanel = new JPanel();
        blackoutButtonsPanel.add(cineSliderControlsPanel);
        cineSliderChangeListener = new CineSliderChangeListener();

        ourFrameSelectionChangeListener = new OurFrameSelectionChangeListener(ourEventContext);    // context needs to match SingleImagePanel to link events

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, multiPanel, blackoutButtonsPanel);
        splitPane.setOneTouchExpandable(false);
        splitPane.setResizeWeight(splitPaneResizeWeight);

        JLabel helpBar = new JLabel(helpText);

        mainPanel = new Box(BoxLayout.Y_AXIS);
        mainPanel.add(splitPane);
        mainPanel.add(helpBar);
    }


    public void deconstruct() {
        // avoid "listener leak"
        if (ourFrameSelectionChangeListener != null) {
            ApplicationEventDispatcher.getApplicationEventDispatcher().removeListener(ourFrameSelectionChangeListener);
            ourFrameSelectionChangeListener = null;
        }
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

    protected class PreviousActionListener implements ActionListener {
        PixelDataTemplateDialog application;

        public PreviousActionListener(PixelDataTemplateDialog application) {
            this.application = application;
        }

        public void actionPerformed(ActionEvent event) {
            goToPrevious();
        }
    }


    protected class NextActionListener implements ActionListener {
        PixelDataTemplateDialog application;

        public NextActionListener(PixelDataTemplateDialog application) {
            this.application = application;
        }

        public void actionPerformed(ActionEvent event) {
            goToNext();
        }
    }

    private void goToNext() {
        recordStateOfDrawingShapesForFileChange();
        if (blackoutCurrentImage.UnsavedChanges()) {
            // Changes were made to the dicom file [currentFileNumber] but were discarded and not saved
        }

        if (blackoutDicomFiles.goToNext()) {
            updateDisplayedFileNumber();
            loadDicomFileOrDirectory(blackoutShapeDefinition);
        } else {
            // Normal completion
            dispose();
        }
    }

    private void goToPrevious() {
        recordStateOfDrawingShapesForFileChange();
        if (blackoutCurrentImage.UnsavedChanges()) {
            // Changes were made to the dicom file [currentFileNumber] but were discarded and not saved
        }

        if (blackoutDicomFiles.goToPrevious()) {
            updateDisplayedFileNumber();
            loadDicomFileOrDirectory(blackoutShapeDefinition);
        } else {
            // Normal completion
            dispose();
        }
    }

    protected class SaveActionListener implements ActionListener {
        PixelDataTemplateDialog application;
        SafeCursorChanger cursorChanger;

        public SaveActionListener(PixelDataTemplateDialog application) {
            this.application = application;
            cursorChanger = new SafeCursorChanger(application);
        }

        public void actionPerformed(ActionEvent event) {
            cursorChanger.setWaitCursor();
            save();
            cursorChanger.restoreCursor();
        }
    }


}

