package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;

import dev.satherov.sathlib.client.lang.GenericLang;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.view.SLFluidTankView;
import dev.satherov.sathlib.util.SLColorUtils;
import dev.satherov.sathlib.util.SLFluidUtils;
import dev.satherov.sathlib.util.SLStringUtils;

import net.neoforged.neoforge.fluids.FluidStack;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

///
/// Built-in retained-mode fluid tank node.
///
/// The node renders a live fill meter from any {@link SLFluidTankView} and
/// exposes a default hover tooltip showing the current contents and capacity.
///
public class SLFluidTankNode extends UILeafNode<SLFluidTankNode> implements SLTooltipProvider {
    
    private static final int DEFAULT_HORIZONTAL_WIDTH = 54;
    private static final int DEFAULT_HORIZONTAL_HEIGHT = 18;
    private static final int DEFAULT_VERTICAL_WIDTH = 18;
    private static final int DEFAULT_VERTICAL_HEIGHT = 54;
    
    private SLFluidTankView fluidTankView;
    private boolean vertical;
    private @Nullable Function<FluidStack, Integer> colorFactory;
    private @Nullable Function<SLFluidTankView, List<Component>> tooltipFactory;
    
    ///
    /// Creates a fluid tank with default settings.
    ///
    public SLFluidTankNode() {
        this(SLModifier.none(), SLFluidTankView.of(() -> FluidStack.EMPTY, () -> 0L), true, null, null);
    }
    
    ///
    /// Creates a fully configured fluid tank.
    ///
    /// @param modifier       node modifier
    /// @param fluidTankView  backing fluid-tank view
    /// @param vertical       whether the tank should fill vertically
    /// @param colorFactory   optional fill-color factory
    /// @param tooltipFactory optional tooltip factory
    ///
    protected SLFluidTankNode(
            SLModifier modifier,
            SLFluidTankView fluidTankView,
            boolean vertical,
            @Nullable Function<FluidStack, Integer> colorFactory,
            @Nullable Function<SLFluidTankView, List<Component>> tooltipFactory
    ) {
        super(modifier);
        this.fluidTankView = Objects.requireNonNull(fluidTankView);
        this.vertical = vertical;
        this.colorFactory = colorFactory;
        this.tooltipFactory = tooltipFactory;
    }
    
    ///
    /// Creates a builder-backed fluid tank while normalizing omitted values to
    /// framework defaults.
    ///
    /// @param modifier       node modifier
    /// @param fluidTankView  backing fluid-tank view
    /// @param vertical       whether the tank should fill vertically
    /// @param colorFactory   optional fill-color factory
    /// @param tooltipFactory optional tooltip factory
    ///
    /// @return configured fluid tank
    ///
    @Builder
    public static SLFluidTankNode of(
            SLModifier modifier,
            SLFluidTankView fluidTankView,
            Boolean vertical,
            Function<FluidStack, Integer> colorFactory,
            Function<SLFluidTankView, List<Component>> tooltipFactory
    ) {
        return new SLFluidTankNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElseGet(fluidTankView, () -> SLFluidTankView.of(() -> FluidStack.EMPTY, () -> 0L)),
                Objects.requireNonNullElse(vertical, true),
                colorFactory,
                tooltipFactory
        );
    }
    
    ///
    /// Replaces the backing fluid-tank view.
    ///
    /// @param fluidTankView new fluid-tank view
    ///
    /// @return this node
    ///
    public SLFluidTankNode tank(SLFluidTankView fluidTankView) {
        this.fluidTankView = Objects.requireNonNull(fluidTankView);
        return this.self();
    }
    
    ///
    /// Sets whether the tank should fill vertically.
    ///
    /// @param vertical new fill direction flag
    ///
    /// @return this node
    ///
    public SLFluidTankNode vertical(boolean vertical) {
        this.vertical = vertical;
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets the fill-color factory.
    ///
    /// @param colorFactory new fill-color factory, or {@code null}
    ///
    /// @return this node
    ///
    public SLFluidTankNode color(@Nullable Function<FluidStack, Integer> colorFactory) {
        this.colorFactory = colorFactory;
        return this.self();
    }
    
    ///
    /// Sets the tooltip factory.
    ///
    /// @param tooltipFactory new tooltip factory, or {@code null}
    ///
    /// @return this node
    ///
    public SLFluidTankNode tooltip(@Nullable Function<SLFluidTankView, List<Component>> tooltipFactory) {
        this.tooltipFactory = tooltipFactory;
        return this.self();
    }
    
    @Override
    public @Nullable List<Component> getTooltipLines() {
        if (this.tooltipFactory != null) return this.tooltipFactory.apply(this.fluidTankView);
        
        FluidStack stack = this.fluidTankView.stack();
        Component fluidName = stack.isEmpty() ? GenericLang.EMPTY.translate() : stack.getHoverName();
        Component amountLine = Component.literal(String.format(Locale.ROOT, "%s / %s mB", SLStringUtils.displayDecimal(this.fluidTankView.amount()), SLStringUtils.displayDecimal(this.fluidTankView.capacity())));
        return List.of(fluidName, amountLine);
    }
    
    @Override
    protected boolean isInputTarget() {
        return true;
    }
    
    @Override
    public boolean isFocusable() {
        return false;
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        if (this.vertical) return new SLMeasuredSize(SLFluidTankNode.DEFAULT_VERTICAL_WIDTH, SLFluidTankNode.DEFAULT_VERTICAL_HEIGHT);
        return new SLMeasuredSize(SLFluidTankNode.DEFAULT_HORIZONTAL_WIDTH, SLFluidTankNode.DEFAULT_HORIZONTAL_HEIGHT);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        SLBounds innerBounds = bounds.inset(2);
        context.fill(new SLBounds(bounds.x() + 1, bounds.y() + 1, bounds.width(), bounds.height()), SLColorUtils.multiplyAlpha(context.theme().colors().panelShadow(), 0.85F));
        context.fillVerticalGradient(
                bounds,
                SLColorUtils.lerp(0.16F, context.theme().colors().insetFill(), context.theme().colors().panelFillTop()),
                context.theme().colors().insetFill()
        );
        context.outline(bounds, this.isHovered() ? context.theme().colors().insetStrong() : context.theme().colors().insetBorder());
        context.fill(innerBounds, 0xFF06090D);
        context.fill(new SLBounds(innerBounds.x(), innerBounds.y(), innerBounds.width(), 1), 0x24000000);
        context.fill(new SLBounds(innerBounds.x(), innerBounds.bottom() - 1, innerBounds.width(), 1), 0x28000000);
        
        FluidStack stack = this.fluidTankView.stack();
        float fillRatio = this.fluidTankView.fillRatio();
        if (stack.isEmpty() || fillRatio <= 0.0F || innerBounds.width() <= 0 || innerBounds.height() <= 0) return;
        
        long capacity = Math.max(1L, this.fluidTankView.capacity());
        int tint = this.colorFactory != null
                ? Objects.requireNonNullElse(this.colorFactory.apply(stack), SLFluidUtils.getTintColor(stack))
                : SLFluidUtils.getTintColor(stack);
        
        SLFluidUtils.getStillFluidSprite(stack).ifPresent(sprite -> {
            final long amount = stack.getAmount();
            if (amount <= 0L) return;
            
            if (this.vertical) {
                int scaledHeight = (int) Math.clamp((amount * innerBounds.height()) / capacity, 1L, innerBounds.height());
                context.blitVerticalSprite(
                        sprite,
                        innerBounds.x(),
                        innerBounds.y(),
                        innerBounds.width(),
                        innerBounds.height(),
                        scaledHeight,
                        tint
                );
                return;
            }
            
            int scaledWidth = (int) Math.clamp((amount * innerBounds.width()) / capacity, 1L, innerBounds.width());
            context.pushClip(new SLBounds(innerBounds.x(), innerBounds.y(), scaledWidth, innerBounds.height()));
            try {
                context.blitVerticalSprite(
                        sprite,
                        innerBounds.x(),
                        innerBounds.y(),
                        innerBounds.width(),
                        innerBounds.height(),
                        innerBounds.height(),
                        tint
                );
            } finally {
                context.popClip();
            }
        });
        
        context.fill(new SLBounds(innerBounds.x(), innerBounds.y(), 1, innerBounds.height()), 0x24FFFFFF);
        context.fill(new SLBounds(innerBounds.right() - 1, innerBounds.y(), 1, innerBounds.height()), 0x18000000);
    }
}
