package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLAxis;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLLength;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.layout.SLScalar;

import net.minecraft.client.gui.Font;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

///
/// Shared base class for ordered flow containers such as rows and columns.
///
/// Flow containers measure children in order, then assign final bounds during
/// layout using the configured axis, gap, and main-axis alignment.
///
/// @param <S> concrete flow subtype used for fluent setters
///
public abstract class SLFlowNode<S extends SLFlowNode<S>> extends UIContainerNode<S> {
    
    private final SLAxis axis;
    private SLScalar gap;
    private SLAlignment mainAxisAlignment;
    
    ///
    /// Creates a flow container with defaults.
    ///
    /// @param axis layout axis used to order children
    ///
    protected SLFlowNode(SLAxis axis) {
        this(SLModifier.none(), axis, List.of(), SLScalar.zero(), SLAlignment.START);
    }
    
    ///
    /// Creates a flow container with explicit configuration.
    ///
    /// @param modifier          node modifier
    /// @param axis              layout axis used to order children
    /// @param children          initial child list
    /// @param gap               semantic gap between children
    /// @param mainAxisAlignment alignment for unused main-axis space
    ///
    protected SLFlowNode(
            @Nullable SLModifier modifier,
            SLAxis axis,
            List<UINode<?>> children,
            @Nullable SLScalar gap,
            @Nullable SLAlignment mainAxisAlignment
    ) {
        super(modifier, children);
        this.axis = Objects.requireNonNull(axis);
        this.gap = Objects.requireNonNullElse(gap, SLScalar.zero());
        this.mainAxisAlignment = Objects.requireNonNullElse(mainAxisAlignment, SLAlignment.START);
    }
    
    ///
    /// Returns the ordered layout axis.
    ///
    /// @return flow axis
    ///
    public final SLAxis getAxis() {
        return this.axis;
    }
    
    ///
    /// Returns the semantic gap between visible children.
    ///
    /// @return child gap
    ///
    public final SLScalar getGap() {
        return this.gap;
    }
    
    ///
    /// Returns the main-axis alignment for unused space.
    ///
    /// @return flow alignment
    ///
    public final SLAlignment getMainAxisAlignment() {
        return this.mainAxisAlignment;
    }
    
    ///
    /// Sets the gap between visible children.
    ///
    /// @param gap semantic gap value
    ///
    /// @return this flow node for fluent runtime setup
    ///
    public final S gap(SLScalar gap) {
        SLScalar normalizedGap = Objects.requireNonNullElse(gap, SLScalar.zero());
        if (this.gap.equals(normalizedGap)) {
            return this.self();
        }
        
        this.gap = normalizedGap;
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets the main-axis alignment used when the flow is smaller than the
    /// available content area.
    ///
    /// @param alignment main-axis alignment
    ///
    /// @return this flow node for fluent runtime setup
    ///
    public final S mainAxisAlignment(SLAlignment alignment) {
        SLAlignment normalizedAlignment = Objects.requireNonNullElse(alignment, SLAlignment.START);
        if (this.mainAxisAlignment == normalizedAlignment) {
            return this.self();
        }
        
        this.mainAxisAlignment = normalizedAlignment;
        this.invalidateLayout();
        return this.self();
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        int totalMain = 0;
        int maxCross = 0;
        boolean firstVisibleChild = true;
        int gapPixels = this.gap.resolve(this.axis == SLAxis.HORIZONTAL ? availableWidth : availableHeight);
        
        for (UINode<?> child : this.getChildren()) {
            if (!child.isVisible()) {
                continue;
            }
            
            SLInsets margin = child.getModifier().margin();
            int childAvailableWidth = Math.max(0, availableWidth - margin.horizontal(availableWidth));
            int childAvailableHeight = Math.max(0, availableHeight - margin.vertical(availableHeight));
            SLMeasuredSize childSize = child.measure(font, childAvailableWidth, childAvailableHeight);
            
            int outerMain = this.axis == SLAxis.HORIZONTAL
                    ? childSize.width() + margin.horizontal(availableWidth)
                    : childSize.height() + margin.vertical(availableHeight);
            int outerCross = this.axis == SLAxis.HORIZONTAL
                    ? childSize.height() + margin.vertical(availableHeight)
                    : childSize.width() + margin.horizontal(availableWidth);
            
            if (!firstVisibleChild) {
                totalMain += gapPixels;
            }
            
            totalMain += outerMain;
            maxCross = Math.max(maxCross, outerCross);
            firstVisibleChild = false;
        }
        
        if (this.axis == SLAxis.HORIZONTAL) {
            return new SLMeasuredSize(totalMain, maxCross);
        }
        
        return new SLMeasuredSize(maxCross, totalMain);
    }
    
    @Override
    protected void onLayout(Font font, SLBounds contentBounds) {
        List<UINode<?>> visibleChildren = new ArrayList<>();
        for (UINode<?> child : this.getChildren()) {
            if (child.isVisible()) {
                visibleChildren.add(child);
            }
        }
        
        if (visibleChildren.isEmpty()) {
            return;
        }
        
        int availableMain = this.axis == SLAxis.HORIZONTAL ? contentBounds.width() : contentBounds.height();
        int availableCross = this.axis == SLAxis.HORIZONTAL ? contentBounds.height() : contentBounds.width();
        int gapPixels = this.gap.resolve(availableMain);
        
        int preferredMain = 0;
        float totalFillWeight = 0.0F;
        
        for (int childIndex = 0; childIndex < visibleChildren.size(); childIndex++) {
            UINode<?> child = visibleChildren.get(childIndex);
            SLInsets margin = child.getModifier().margin();
            SLMeasuredSize childSize = child.getMeasuredSize();
            
            if (childIndex > 0) {
                preferredMain += gapPixels;
            }
            
            preferredMain += this.axis == SLAxis.HORIZONTAL
                    ? childSize.width() + margin.horizontal(contentBounds.width())
                    : childSize.height() + margin.vertical(contentBounds.height());
            
            SLLength mainLength = this.axis == SLAxis.HORIZONTAL
                    ? child.getModifier().width()
                    : child.getModifier().height();
            totalFillWeight += mainLength.weight();
        }
        
        int extraMain = Math.max(0, availableMain - preferredMain);
        int flowStart = switch (this.mainAxisAlignment) {
            case CENTER -> extraMain / 2;
            case END -> extraMain;
            case START, FILL -> 0;
        };
        
        int cursor = (this.axis == SLAxis.HORIZONTAL ? contentBounds.x() : contentBounds.y()) + flowStart;
        int distributedMain = 0;
        
        for (int childIndex = 0; childIndex < visibleChildren.size(); childIndex++) {
            UINode<?> child = visibleChildren.get(childIndex);
            SLModifier childModifier = child.getModifier();
            SLInsets margin = childModifier.margin();
            SLMeasuredSize childSize = child.getMeasuredSize();
            
            int marginStart = this.axis == SLAxis.HORIZONTAL ? margin.left(contentBounds.width()) : margin.top(contentBounds.height());
            int marginEnd = this.axis == SLAxis.HORIZONTAL ? margin.right(contentBounds.width()) : margin.bottom(contentBounds.height());
            int marginCrossStart = this.axis == SLAxis.HORIZONTAL ? margin.top(contentBounds.height()) : margin.left(contentBounds.width());
            int marginCrossEnd = this.axis == SLAxis.HORIZONTAL ? margin.bottom(contentBounds.height()) : margin.right(contentBounds.width());
            
            SLLength mainLength = this.axis == SLAxis.HORIZONTAL ? childModifier.width() : childModifier.height();
            SLLength crossLength = this.axis == SLAxis.HORIZONTAL ? childModifier.height() : childModifier.width();
            
            int additionalMain = 0;
            if (mainLength.isFill() && totalFillWeight > 0.0F) {
                if (childIndex == visibleChildren.size() - 1) {
                    additionalMain = Math.max(0, extraMain - distributedMain);
                } else {
                    additionalMain = Math.round(extraMain * (mainLength.weight() / totalFillWeight));
                    distributedMain += additionalMain;
                }
            }
            
            int preferredChildMain = this.axis == SLAxis.HORIZONTAL ? childSize.width() : childSize.height();
            int resolvedChildMain = mainLength.resolveFinal(preferredChildMain + additionalMain, preferredChildMain);
            
            int crossAvailable = Math.max(0, availableCross - marginCrossStart - marginCrossEnd);
            SLAlignment crossAlignment = this.axis == SLAxis.HORIZONTAL
                    ? childModifier.verticalAlignment()
                    : childModifier.horizontalAlignment();
            int preferredChildCross = this.axis == SLAxis.HORIZONTAL ? childSize.height() : childSize.width();
            int resolvedChildCross = crossLength.isFill()
                    ? crossAvailable
                    : crossAlignment.resolveSize(crossAvailable, crossLength.resolveFinal(crossAvailable, preferredChildCross));
            
            int childMainStart = cursor + marginStart;
            int crossRegionStart = (this.axis == SLAxis.HORIZONTAL ? contentBounds.y() : contentBounds.x()) + marginCrossStart;
            int crossRegionAvailable = Math.max(0, availableCross - marginCrossStart - marginCrossEnd);
            int childCrossStart = crossAlignment.resolvePosition(crossRegionStart, crossRegionAvailable, resolvedChildCross);
            
            int offsetX = childModifier.offsetX().resolve(contentBounds.width());
            int offsetY = childModifier.offsetY().resolve(contentBounds.height());
            
            SLBounds childBounds = this.axis == SLAxis.HORIZONTAL
                    ? new SLBounds(childMainStart + offsetX, childCrossStart + offsetY, resolvedChildMain, resolvedChildCross)
                    : new SLBounds(childCrossStart + offsetX, childMainStart + offsetY, resolvedChildCross, resolvedChildMain);
            
            child.layout(childBounds, font);
            
            cursor += marginStart + resolvedChildMain + marginEnd;
            if (childIndex < visibleChildren.size() - 1) {
                cursor += gapPixels;
            }
        }
    }
}
