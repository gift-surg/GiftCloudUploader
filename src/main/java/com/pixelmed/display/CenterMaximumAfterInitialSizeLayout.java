package com.pixelmed.display;

import java.awt.*;

public class CenterMaximumAfterInitialSizeLayout implements LayoutManager {

    public CenterMaximumAfterInitialSizeLayout() {}

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

    public static double getScaleFactorToFitInMaximumAvailable(double useWidth, double useHeight, double maxWidth, double maxHeight) {
        double sx = maxWidth / useWidth;
        double sy = maxHeight / useHeight;
        // always choose smallest, regardless of whether scaling up or down
        double useScaleFactor = sx < sy ? sx : sy;
        return useScaleFactor;
    }

}
