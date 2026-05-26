package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;
import lombok.Singular;

import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLLength;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;

import net.minecraft.client.gui.Font;

import java.util.List;
import java.util.Objects;

///
/// Overlay container that places children on top of each other.
///
/// Each child is measured independently, then aligned inside the stack content
/// box during layout.
///
public class SLStackNode extends UIContainerNode<SLStackNode> {
    
    ///
    /// Creates an empty stack container.
    ///
    public SLStackNode() {
        super();
    }
    
    ///
    /// Creates a fully configured stack.
    ///
    /// @param modifier node modifier
    /// @param children initial child list
    ///
    protected SLStackNode(SLModifier modifier, List<UINode<?>> children) {
        super(modifier, children);
    }
    
    ///
    /// Creates a builder-backed stack while normalizing omitted values to the
    /// framework defaults.
    ///
    /// @param modifier node modifier
    /// @param children initial child list
    ///
    /// @return configured stack node
    ///
    @Builder(builderMethodName = "builder")
    public static SLStackNode of(SLModifier modifier, @Singular("child") List<UINode<?>> children) {
        return new SLStackNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(children, List.of())
        );
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        int maxWidth = 0;
        int maxHeight = 0;
        
        for (UINode<?> child : this.getChildren()) {
            if (!child.isVisible()) {
                continue;
            }
            
            SLInsets margin = child.getModifier().margin();
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
            if (!child.isVisible()) {
                continue;
            }
            
            SLModifier childModifier = child.getModifier();
            SLInsets margin = childModifier.margin();
            int leftMargin = margin.left(contentBounds.width());
            int rightMargin = margin.right(contentBounds.width());
            int topMargin = margin.top(contentBounds.height());
            int bottomMargin = margin.bottom(contentBounds.height());
            
            int regionWidth = Math.max(0, contentBounds.width() - leftMargin - rightMargin);
            int regionHeight = Math.max(0, contentBounds.height() - topMargin - bottomMargin);
            int regionX = contentBounds.x() + leftMargin;
            int regionY = contentBounds.y() + topMargin;
            
            SLMeasuredSize childSize = child.getMeasuredSize();
            SLLength widthLength = childModifier.width();
            SLLength heightLength = childModifier.height();
            SLAlignment horizontalAlignment = childModifier.horizontalAlignment();
            SLAlignment verticalAlignment = childModifier.verticalAlignment();
            
            int childWidth = widthLength.isFill()
                    ? regionWidth
                    : horizontalAlignment.resolveSize(regionWidth, widthLength.resolveFinal(regionWidth, childSize.width()));
            int childHeight = heightLength.isFill()
                    ? regionHeight
                    : verticalAlignment.resolveSize(regionHeight, heightLength.resolveFinal(regionHeight, childSize.height()));
            
            int childX = horizontalAlignment.resolvePosition(regionX, regionWidth, childWidth)
                    + childModifier.offsetX().resolve(contentBounds.width());
            int childY = verticalAlignment.resolvePosition(regionY, regionHeight, childHeight)
                    + childModifier.offsetY().resolve(contentBounds.height());
            
            child.layout(new SLBounds(childX, childY, childWidth, childHeight), font);
        }
    }
}
