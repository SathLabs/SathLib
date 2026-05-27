package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.view.SLEnergyView;
import dev.satherov.sathlib.util.SLColorUtils;
import dev.satherov.sathlib.util.SLStringUtils;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

///
/// Built-in retained-mode energy bar node.
///
/// The node renders a live fill meter from any {@link SLEnergyView} and exposes
/// a default hover tooltip showing the current amount and capacity.
///
public class SLEnergyBarNode extends UILeafNode<SLEnergyBarNode> implements SLTooltipProvider {
    
    private static final int ENERGY_RED_COLOR = 0xFF0000;
    private static final int DEFAULT_HORIZONTAL_WIDTH = 96;
    private static final int DEFAULT_HORIZONTAL_HEIGHT = 14;
    private static final int DEFAULT_VERTICAL_WIDTH = 14;
    private static final int DEFAULT_VERTICAL_HEIGHT = 54;
    
    private SLEnergyView energyView;
    private boolean vertical;
    private @Nullable Function<SLEnergyView, Component> overlayFactory;
    private @Nullable Function<SLEnergyView, List<Component>> tooltipFactory;
    
    ///
    /// Creates an energy bar with default settings.
    ///
    public SLEnergyBarNode() {
        this(SLModifier.none(), SLEnergyView.of(() -> 0L, () -> 0L), true, null, null);
    }
    
    ///
    /// Creates a fully configured energy bar.
    ///
    /// @param modifier       node modifier
    /// @param energyView     backing energy view
    /// @param vertical       whether the bar should fill vertically
    /// @param overlayFactory optional overlay-text factory
    /// @param tooltipFactory optional tooltip factory
    ///
    protected SLEnergyBarNode(
            SLModifier modifier,
            SLEnergyView energyView,
            boolean vertical,
            @Nullable Function<SLEnergyView, Component> overlayFactory,
            @Nullable Function<SLEnergyView, List<Component>> tooltipFactory
    ) {
        super(modifier);
        this.energyView = Objects.requireNonNull(energyView);
        this.vertical = vertical;
        this.overlayFactory = overlayFactory;
        this.tooltipFactory = tooltipFactory;
    }
    
    ///
    /// Creates a builder-backed energy bar while normalizing omitted values to
    /// framework defaults.
    ///
    /// @param modifier       node modifier
    /// @param energyView     backing energy view
    /// @param vertical       whether the bar should fill vertically
    /// @param overlayFactory optional overlay-text factory
    /// @param tooltipFactory optional tooltip factory
    ///
    /// @return configured energy bar
    ///
    @Builder
    public static SLEnergyBarNode of(
            SLModifier modifier,
            SLEnergyView energyView,
            Boolean vertical,
            Function<SLEnergyView, Component> overlayFactory,
            Function<SLEnergyView, List<Component>> tooltipFactory
    ) {
        return new SLEnergyBarNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElseGet(energyView, () -> SLEnergyView.of(() -> 0L, () -> 0L)),
                Objects.requireNonNullElse(vertical, true),
                overlayFactory,
                tooltipFactory
        );
    }
    
    ///
    /// Replaces the backing energy view.
    ///
    /// @param energyView new energy view
    ///
    /// @return this node
    ///
    public SLEnergyBarNode energy(SLEnergyView energyView) {
        this.energyView = Objects.requireNonNull(energyView);
        return this.self();
    }
    
    ///
    /// Sets whether the meter should fill vertically.
    ///
    /// @param vertical new fill direction flag
    ///
    /// @return this node
    ///
    public SLEnergyBarNode vertical(boolean vertical) {
        this.vertical = vertical;
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets the overlay-text factory.
    ///
    /// @param overlayFactory new overlay-text factory, or {@code null}
    ///
    /// @return this node
    ///
    public SLEnergyBarNode overlay(@Nullable Function<SLEnergyView, Component> overlayFactory) {
        this.overlayFactory = overlayFactory;
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets the tooltip factory.
    ///
    /// @param tooltipFactory new tooltip factory, or {@code null}
    ///
    /// @return this node
    ///
    public SLEnergyBarNode tooltip(@Nullable Function<SLEnergyView, List<Component>> tooltipFactory) {
        this.tooltipFactory = tooltipFactory;
        return this.self();
    }
    
    @Override
    public @Nullable List<Component> getTooltipLines() {
        if (this.tooltipFactory != null) return this.tooltipFactory.apply(this.energyView);
        return List.of(Component.literal(String.format(Locale.ROOT, "%s / %s FE", SLStringUtils.displayDecimal(this.energyView.amount()), SLStringUtils.displayDecimal(this.energyView.capacity()))));
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
        if (this.vertical) return new SLMeasuredSize(SLEnergyBarNode.DEFAULT_VERTICAL_WIDTH, SLEnergyBarNode.DEFAULT_VERTICAL_HEIGHT);
        return new SLMeasuredSize(SLEnergyBarNode.DEFAULT_HORIZONTAL_WIDTH, SLEnergyBarNode.DEFAULT_HORIZONTAL_HEIGHT);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        SLBounds cavityBounds = bounds.inset(2);
        int frameColor = this.isHovered() ? context.theme().colors().insetStrong() : context.theme().colors().insetBorder();
        int brightAccent = SLColorUtils.lerp(0.35F, SLEnergyBarNode.ENERGY_RED_COLOR, context.theme().colors().white());
        int darkAccent = SLColorUtils.lerp(0.42F, context.theme().colors().insetFill(), SLEnergyBarNode.ENERGY_RED_COLOR);
        
        context.fill(bounds, context.theme().colors().panelFillBottom());
        context.outline(bounds, frameColor);
        context.fill(cavityBounds, 0xFF04070B);
        
        if (cavityBounds.width() > 2 && cavityBounds.height() > 2) {
            context.fill(new SLBounds(cavityBounds.x(), cavityBounds.y(), cavityBounds.width(), 1), 0x33000000);
            context.fill(new SLBounds(cavityBounds.x(), cavityBounds.bottom() - 1, cavityBounds.width(), 1), 0x22000000);
            context.fill(new SLBounds(cavityBounds.x(), cavityBounds.y(), 1, cavityBounds.height()), 0x1FFFFFFF);
            context.fill(new SLBounds(cavityBounds.right() - 1, cavityBounds.y(), 1, cavityBounds.height()), 0x26000000);
        }
        
        float fillRatio = this.energyView.fillRatio();
        if (fillRatio > 0.0F && cavityBounds.width() > 0 && cavityBounds.height() > 0) {
            if (this.vertical) {
                int fillHeight = Math.max(1, Math.round(cavityBounds.height() * fillRatio));
                SLBounds fillBounds = new SLBounds(cavityBounds.x(), cavityBounds.bottom() - fillHeight, cavityBounds.width(), fillHeight);
                context.fillVerticalGradient(fillBounds, brightAccent, darkAccent);
                context.fill(new SLBounds(fillBounds.x(), fillBounds.y(), fillBounds.width(), 1), 0xBFFFFFFF);
            } else {
                int fillWidth = Math.max(1, Math.round(cavityBounds.width() * fillRatio));
                SLBounds fillBounds = new SLBounds(cavityBounds.x(), cavityBounds.y(), fillWidth, cavityBounds.height());
                context.fillVerticalGradient(fillBounds, brightAccent, darkAccent);
                context.fill(new SLBounds(fillBounds.x(), fillBounds.y(), Math.max(1, fillBounds.width()), 1), 0xA0FFFFFF);
            }
        }
        
        if (this.vertical && cavityBounds.height() >= 12) {
            for (int y = cavityBounds.y() + 10; y < cavityBounds.bottom() - 2; y += 10) {
                context.fill(new SLBounds(cavityBounds.x(), y, cavityBounds.width(), 1), 0x28000000);
            }
        } else if (!this.vertical && cavityBounds.width() >= 18) {
            for (int x = cavityBounds.x() + 14; x < cavityBounds.right() - 2; x += 14) {
                context.fill(new SLBounds(x, cavityBounds.y(), 1, cavityBounds.height()), 0x22000000);
            }
        }
        
        if (this.overlayFactory != null) {
            Component overlay = this.overlayFactory.apply(this.energyView);
            if (overlay != null) {
                context.centeredText(overlay, bounds, context.theme().labelColor(this.isEnabled()), false);
            }
        }
    }
}
