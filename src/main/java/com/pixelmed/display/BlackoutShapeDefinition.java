package com.pixelmed.display;

import java.awt.geom.Rectangle2D;
import java.util.Vector;

public class BlackoutShapeDefinition {
    private int previousRows;
    private int previousColumns;
    private Vector previousPersistentDrawingShapes;

    public BlackoutShapeDefinition(SourceImage sImg, Vector<Rectangle2D.Double> persistentDrawingShapes) {
        previousRows = sImg.getHeight();
        previousColumns = sImg.getWidth();
        previousPersistentDrawingShapes = persistentDrawingShapes;
    }

    public int getPreviousRows() {
        return previousRows;
    }

    public int getPreviousColumns() {
        return previousColumns;
    }

    public Vector getPreviousPersistentDrawingShapes() {
        return previousPersistentDrawingShapes;
    }
}
