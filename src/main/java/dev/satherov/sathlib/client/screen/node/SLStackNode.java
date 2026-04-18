package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLLength;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;

import net.minecraft.client.gui.Font;

///
/// Overlay container that places children on top of each other.
///
/// Each child is measured independently, then aligned inside the stack content
/// box during layout.
///
/// - support overlay and anchored layouts
/// - respect per-child alignment, offsets, and fill sizing
///
/// Extend this class when you need a stacking container with additional visuals.
///
public class SLStackNode extends UIContainerNode<SLStackNode> {
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        int maxWidth = 0;
        int maxHeight = 0;
        
        for (UINode<?> child : this.getChildren()) {
            if (!child.isVisible()) continue;
            SLInsets margin = child.getLayoutSpec().margin();
            int childAvailableWidth = Math.max(0, availableWidth - margin.horizontal(availableWidth));
            int childAvailableHeight = Math.max(0, availableHeight - margin.vertical(availableHeight));
            SLMeasuredSize childSize = child.measure(font, childAvailableWidth, childAvailableHeight);
            
            maxWidth = Math.max(maxWidth, childSize.width() + margin.horizontal(availableWidth));
            maxHeight = Math.max(maxHeight, childSize.height() + margin.vertical(availableHeight));
        }
        
        return new SLMeasuredSize(maxWidth, maxHeight);
    }
    
    @Override
    protected void onLayout(Font font, SLBounds contentBounds) {
        for (UINode<?> child : this.getChildren()) {
            if (!child.isVisible()) continue;
            
            SLInsets margin = child.getLayoutSpec().margin();
            int leftMargin = margin.left(contentBounds.width());
            int rightMargin = margin.right(contentBounds.width());
            int topMargin = margin.top(contentBounds.height());
            int bottomMargin = margin.bottom(contentBounds.height());
            
            int regionWidth = Math.max(0, contentBounds.width() - leftMargin - rightMargin);
            int regionHeight = Math.max(0, contentBounds.height() - topMargin - bottomMargin);
            int regionX = contentBounds.x() + leftMargin;
            int regionY = contentBounds.y() + topMargin;
            
            SLMeasuredSize childSize = child.getMeasuredSize();
            SLLength widthLength = child.getLayoutSpec().width();
            SLLength heightLength = child.getLayoutSpec().height();
            SLAlignment horizontalAlignment = child.getLayoutSpec().horizontalAlignment();
            SLAlignment verticalAlignment = child.getLayoutSpec().verticalAlignment();
            
            int childWidth = widthLength.isFill()
                    ? regionWidth
                    : horizontalAlignment.resolveSize(regionWidth, widthLength.resolveFinal(regionWidth, childSize.width()));
            int childHeight = heightLength.isFill()
                    ? regionHeight
                    : verticalAlignment.resolveSize(regionHeight, heightLength.resolveFinal(regionHeight, childSize.height()));
            
            int childX = horizontalAlignment.resolvePosition(regionX, regionWidth, childWidth)
                    + child.getLayoutSpec().offsetX().resolve(contentBounds.width());
            int childY = verticalAlignment.resolvePosition(regionY, regionHeight, childHeight)
                    + child.getLayoutSpec().offsetY().resolve(contentBounds.height());
            
            child.layout(new SLBounds(childX, childY, childWidth, childHeight), font);
        }
    }
}
