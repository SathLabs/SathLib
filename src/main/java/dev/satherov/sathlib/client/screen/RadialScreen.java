package dev.satherov.sathlib.client.screen;

import lombok.Getter;
import lombok.Setter;

import dev.satherov.sathlib.client.render.SLRenderPipelines;
import dev.satherov.sathlib.core.annotations.NothingNull;
import dev.satherov.sathlib.util.SLColorUtils;
import dev.satherov.sathlib.util.SLMathUtils;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

///
/// Base screen implementation for radial menus composed of interactive slices.
///
/// @param <T> concrete screen type
/// @param <S> slice type rendered by the screen
///
@NothingNull
@SuppressWarnings({ "doclint:missing", "UnusedReturnValue", "SameParameterValue" })
public class RadialScreen<T extends RadialScreen<T, S>, S extends RadialScreen.RadialSlice<T, S>> extends Screen {
    
    private static final int DEFAULT_SLICE_COLOR = 0xC0202020;
    private static final int DEFAULT_HOVER_COLOR = 0xD04C4C4C;
    private static final int DEFAULT_LABEL_COLOR = 0xFFF2F2F2;
    
    protected final List<S> slices = new ArrayList<>();
    
    protected double mouseX;
    protected double mouseY;
    
    protected float centerX;
    protected float centerY;
    
    @Getter
    @Nullable
    protected S hovered;
    
    @Getter
    @Setter
    protected float startAngleDegrees = -90.0F;
    
    @Getter
    protected float sliceInnerRadius = 40.0F;
    
    @Getter
    protected float sliceOuterRadius = 110.0F;
    
    @Getter
    protected float hoverExpandDistance = 16.0F;
    
    @Getter
    protected float hoverOutwardOffset = 8.0F;
    
    @Getter
    protected float hoverAnimationSpeed = 0.55F;
    
    @Getter
    protected float sliceSpacingDegrees = 0.0F;
    
    @Getter
    protected float maxDegreesPerCurveSegment = 2.0F;
    
    @Getter
    protected float maxPixelsPerCurveSegment = 2.0F;
    
    ///
    /// Creates an empty radial screen.
    ///
    public RadialScreen() {
        super(Component.empty());
    }
    
    ///
    /// Returns this screen as type T for method chaining.
    ///
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
    
    ///
    /// Initializes pointer tracking for the radial menu.
    ///
    @Override
    protected void init() {
        super.init();
        this.centerX = this.width / 2.0F;
        this.centerY = this.height / 2.0F;
        this.mouseX = this.centerX;
        this.mouseY = this.centerY;
        this.updateHovered(this.mouseX, this.mouseY);
    }
    
    ///
    /// Updates slice state and renders the radial menu.
    ///
    /// @param guiGraphics graphics extractor for the current render pass
    /// @param mouseX      current mouse x position
    /// @param mouseY      current mouse y position
    /// @param partialTick time since the last tick
    ///
    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        this.updateHovered(mouseX, mouseY);
        this.animateSlices();
        this.renderRadialMenu(guiGraphics, mouseX, mouseY, partialTick);
        if (this.hovered != null) this.hovered.onHovered(this.self(), mouseX, mouseY, partialTick);
    }
    
    ///
    /// Renders every slice in the radial menu.
    ///
    /// @param guiGraphics graphics extractor for the current render pass
    /// @param mouseX      current mouse x position
    /// @param mouseY      current mouse y position
    /// @param partialTick time since the last tick
    ///
    protected void renderRadialMenu(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.slices.isEmpty()) return;
        
        List<S> snapshot = List.copyOf(this.slices);
        final S hovered = this.hovered;
        
        for (int index = 0; index < snapshot.size(); index++) {
            final S slice = snapshot.get(index);
            if (slice == hovered) continue;
            
            slice.renderSlice(guiGraphics, this.createContext(slice, index, snapshot.size(), mouseX, mouseY));
        }
        
        if (hovered != null) {
            int idx = snapshot.indexOf(hovered);
            if (idx >= 0) hovered.renderSlice(guiGraphics, this.createContext(hovered, idx, snapshot.size(), mouseX, mouseY));
        }
    }
    
    /// 
    /// If this screen should pause the game in singleplayer and save the game.
    /// 
    /// @return `true` if the game should pause and save
    /// 
    public boolean isPauseScreen() {
        return false;
    }
    
    ///
    /// Updates hovered slice state when the mouse moves.
    ///
    /// @param mouseX current mouse x position
    /// @param mouseY current mouse y position
    ///
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        this.updateHovered(mouseX, mouseY);
    }
    
    ///
    /// Handles mouse click input for the hovered slice.
    ///
    /// @param event       input event
    /// @param doubleClick whether the click is a double click
    ///
    /// @return `true` when the event is handled
    ///
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.updateHovered(event.x(), event.y());
        if (this.hovered != null && this.hovered.mousePressed(this.self(), event, doubleClick)) return true;
        return super.mouseClicked(event, doubleClick);
    }
    
    ///
    /// Handles mouse release input for the hovered slice.
    ///
    /// @param event input event
    ///
    /// @return `true` when the event is handled
    ///
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.updateHovered(event.x(), event.y());
        if (this.hovered != null && this.hovered.mouseReleased(this.self(), event)) return true;
        return super.mouseReleased(event);
    }
    
    ///
    /// Handles mouse drag input for the hovered slice.
    ///
    /// @param event  input event
    /// @param mouseX current mouse x position
    /// @param mouseY current mouse y position
    ///
    /// @return `true` when the event is handled
    ///
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        this.updateHovered(event.x(), event.y());
        if (this.hovered != null && this.hovered.mouseDragged(this.self(), event, mouseX, mouseY)) return true;
        return super.mouseDragged(event, mouseX, mouseY);
    }
    
    ///
    /// Handles scroll input for the hovered slice.
    ///
    /// @param mouseX  current mouse x position
    /// @param mouseY  current mouse y position
    /// @param scrollX horizontal scroll amount
    /// @param scrollY vertical scroll amount
    ///
    /// @return `true` when the event is handled
    ///
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.updateHovered(mouseX, mouseY);
        if (this.hovered != null && this.hovered.mouseScrolled(this.self(), mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    ///
    /// Handles key press input for the hovered slice.
    ///
    /// @param event input event
    ///
    /// @return `true` when the event is handled
    ///
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.hovered != null && this.hovered.keyPressed(this.self(), event)) return true;
        return super.keyPressed(event);
    }
    
    ///
    /// Handles key release input for the hovered slice.
    ///
    /// @param event input event
    ///
    /// @return `true` when the event is handled
    ///
    @Override
    public boolean keyReleased(KeyEvent event) {
        if (this.hovered != null && this.hovered.keyReleased(this.self(), event)) return true;
        return super.keyReleased(event);
    }
    
    ///
    /// Handles character input for the hovered slice.
    ///
    /// @param event input event
    ///
    /// @return `true` when the event is handled
    ///
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.hovered != null && this.hovered.charTyped(this.self(), event)) return true;
        return super.charTyped(event);
    }
    
    ///
    /// Adds a slice to the radial menu.
    ///
    /// @param slice slice to use
    ///
    /// @return the added slice
    ///
    public S addSlice(S slice) {
        this.slices.add(slice);
        this.onSlicesChanged();
        return slice;
    }
    
    ///
    /// Adds a slice to the radial menu at the given index.
    ///
    /// @param index slice index
    /// @param slice slice to use
    ///
    /// @return the added slice
    ///
    public S addSlice(int index, S slice) {
        this.slices.add(index, slice);
        this.onSlicesChanged();
        return slice;
    }
    
    ///
    /// Removes a slice from the radial menu by index.
    ///
    /// @param index slice index
    ///
    /// @return the removed slice
    ///
    public S removeSlice(int index) {
        S removed = this.slices.remove(index);
        this.onSlicesChanged();
        return removed;
    }
    
    ///
    /// Removes a slice from the radial menu by reference.
    ///
    /// @param slice slice to use
    ///
    /// @return whether the operation succeeded
    ///
    public boolean removeSlice(S slice) {
        boolean removed = this.slices.remove(slice);
        if (removed) this.onSlicesChanged();
        return removed;
    }
    
    ///
    /// Clears all slices from the radial menu.
    ///
    public void clear() {
        if (this.slices.isEmpty()) return;
        this.slices.clear();
        this.onSlicesChanged();
    }
    
    ///
    /// Returns the current slices as an immutable list.
    ///
    /// @return an immutable list of slices
    ///
    public List<S> slices() {
        return Collections.unmodifiableList(this.slices);
    }
    
    ///
    /// Returns the index of the currently hovered slice.
    ///
    /// @return the hovered slice index, or `-1`
    ///
    public int getHoverIndex() {
        return this.hovered == null ? -1 : this.slices.indexOf(this.hovered);
    }
    
    ///
    /// Sets the inner radius used for slices.
    ///
    /// @param radius radius value
    ///
    /// @return the result
    ///
    public T setSliceInnerRadius(float radius) {
        this.sliceInnerRadius = Math.max(0.0F, radius);
        return this.self();
    }
    
    ///
    /// Sets the outer radius used for slices.
    ///
    /// @param radius radius value
    ///
    /// @return the result
    ///
    public T setSliceOuterRadius(float radius) {
        this.sliceOuterRadius = Math.max(this.sliceInnerRadius + 1.0F, radius);
        return this.self();
    }
    
    ///
    /// Sets how far hovered slices expand outward.
    ///
    /// @param distance distance value
    ///
    /// @return the result
    ///
    public T setHoverExpandDistance(float distance) {
        this.hoverExpandDistance = Math.max(0.0F, distance);
        return this.self();
    }
    
    ///
    /// Sets how far hovered slices shift away from the center.
    ///
    /// @param distance distance value
    ///
    /// @return the result
    ///
    public T setHoverOutwardOffset(float distance) {
        this.hoverOutwardOffset = Math.max(0.0F, distance);
        return this.self();
    }
    
    ///
    /// Sets the hover animation speed.
    ///
    /// @param speed the `speed` argument
    ///
    /// @return the result
    ///
    public T setHoverAnimationSpeed(float speed) {
        this.hoverAnimationSpeed = Math.max(0.0F, speed);
        return this.self();
    }
    
    ///
    /// Sets the angular spacing between slices.
    ///
    /// @param degrees degree value
    ///
    /// @return the result
    ///
    public T setSliceSpacingDegrees(float degrees) {
        this.sliceSpacingDegrees = Math.max(0.0F, degrees);
        return this.self();
    }
    
    ///
    /// Sets the angular curve tessellation limit.
    ///
    /// @param degrees degree value
    ///
    /// @return the result
    ///
    public T setMaxDegreesPerCurveSegment(float degrees) {
        this.maxDegreesPerCurveSegment = Math.max(0.25F, degrees);
        return this.self();
    }
    
    ///
    /// Sets the pixel-based curve tessellation limit.
    ///
    /// @param pixels pixel value
    ///
    /// @return the result
    ///
    public T setMaxPixelsPerCurveSegment(float pixels) {
        this.maxPixelsPerCurveSegment = Math.max(0.5F, pixels);
        return this.self();
    }
    
    ///
    /// Updates hover state after the slice list changes.
    ///
    protected void onSlicesChanged() {
        if (this.hovered != null && !this.slices.contains(this.hovered)) {
            this.hovered.onHoverEnded(this.self());
            this.hovered = null;
        }
        
        this.updateHovered(this.mouseX, this.mouseY);
    }
    
    ///
    /// Builds the render context for a slice.
    ///
    /// @param slice      slice to use
    /// @param index      slice index
    /// @param sliceCount number of slices
    /// @param mouseX     current mouse x position
    /// @param mouseY     current mouse y position
    ///
    /// @return the slice render context
    ///
    protected SliceRenderContext<T, S> createContext(S slice, int index, int sliceCount, double mouseX, double mouseY) {
        float sweep = this.getSliceSweepDegrees(sliceCount);
        float spacing = this.getEffectiveSliceSpacingDegrees(sweep);
        float startAngle = this.startAngleDegrees + (sweep * index) + (spacing * 0.5F);
        float endAngle = this.startAngleDegrees + (sweep * (index + 1)) - (spacing * 0.5F);
        float middleAngle = startAngle + ((endAngle - startAngle) * 0.5F);
        float hoverProgress = slice.getHoverProgress();
        float hoverOffsetX = SLMathUtils.cos(middleAngle) * this.hoverOutwardOffset * hoverProgress;
        float hoverOffsetY = SLMathUtils.sin(middleAngle) * this.hoverOutwardOffset * hoverProgress;
        
        return new SliceRenderContext<>(
                this.self(), slice,
                index, sliceCount, slice == this.hovered, hoverProgress,
                this.centerX + hoverOffsetX, this.centerY + hoverOffsetY,
                startAngle, middleAngle, endAngle,
                this.sliceInnerRadius, this.sliceOuterRadius + (this.hoverExpandDistance * hoverProgress),
                mouseX, mouseY
        );
    }
    
    ///
    /// Draws the default background for a slice.
    ///
    /// @param guiGraphics graphics extractor for the current render pass
    /// @param context     slice render context
    /// @param baseColor   base slice color
    /// @param hoverColor  hover slice color
    ///
    protected void drawDefaultSliceBackground(GuiGraphicsExtractor guiGraphics, SliceRenderContext<T, S> context, int baseColor, int hoverColor) {
        this.fillSlice(guiGraphics, context, SLColorUtils.lerp(context.progress(), baseColor, hoverColor));
        
        if (context.progress() > 0.0F) {
            int alpha = Mth.clamp((int) (0.08F * context.progress() * 0xFF), 0, 0xFF);
            int overlayColor = (0x00FFFFFF) | (alpha << 24);
            this.fillSlice(guiGraphics, context, overlayColor);
        }
    }
    
    ///
    /// Advances the hover animation state for all slices.
    ///
    private void animateSlices() {
        float deltaTicks = this.minecraft.getDeltaTracker().getRealtimeDeltaTicks();
        float blend = Mth.clamp(deltaTicks * this.hoverAnimationSpeed, 0.0F, 1.0F);
        
        for (S slice : this.slices) {
            float target = slice == this.hovered ? 1.0F : 0.0F;
            slice.setHoverProgress(Mth.lerp(blend, slice.getHoverProgress(), target));
            
            if (Math.abs(slice.getHoverProgress() - target) < 0.001F) {
                slice.setHoverProgress(target);
            }
        }
    }
    
    ///
    /// Updates which slice is currently hovered.
    ///
    /// @param mouseX current mouse x position
    /// @param mouseY current mouse y position
    ///
    private void updateHovered(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        
        S updated = this.findSliceAt(mouseX, mouseY);
        if (updated == this.hovered) return;
        if (this.hovered != null) this.hovered.onHoverEnded(this.self());
        
        this.hovered = updated;
        
        if (this.hovered != null) this.hovered.onHoverStarted(this.self());
    }
    
    ///
    /// Finds the slice under the given mouse position.
    ///
    /// @param mouseX current mouse x position
    /// @param mouseY current mouse y position
    ///
    /// @return the hovered slice, or `null`
    ///
    private @Nullable S findSliceAt(double mouseX, double mouseY) {
        if (this.slices.isEmpty()) return null;
        
        double deltaX = mouseX - this.centerX;
        double deltaY = mouseY - this.centerY;
        double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        double maxRadius = this.sliceOuterRadius + this.hoverExpandDistance + this.hoverOutwardOffset;
        
        if (distance < this.sliceInnerRadius || distance > maxRadius) return null;
        
        float angle = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
        float normalizedAngle = Mth.positiveModulo(angle - this.startAngleDegrees, 360.0F);
        float sweep = this.getSliceSweepDegrees(this.slices.size());
        int index = Mth.clamp((int) (normalizedAngle / sweep), 0, this.slices.size() - 1);
        float localAngle = normalizedAngle - (index * sweep);
        float spacing = this.getEffectiveSliceSpacingDegrees(sweep);
        float halfSpacing = spacing * 0.5F;
        
        if (localAngle < halfSpacing || localAngle > sweep - halfSpacing) {
            return null;
        }
        
        return this.slices.get(index);
    }
    
    ///
    /// Calculates the angular sweep assigned to each slice.
    ///
    /// @param sliceCount number of slices
    ///
    /// @return the slice sweep in degrees
    ///
    private float getSliceSweepDegrees(int sliceCount) {
        return 360.0F / sliceCount;
    }
    
    ///
    /// Clamps the effective slice spacing for the current sweep.
    ///
    /// @param sliceSweepDegrees the `sliceSweepDegrees` argument
    ///
    /// @return the effective spacing in degrees
    ///
    private float getEffectiveSliceSpacingDegrees(float sliceSweepDegrees) {
        return Math.min(this.sliceSpacingDegrees, Math.max(0.0F, sliceSweepDegrees - 0.01F));
    }
    
    ///
    /// Builds the mesh used to fill a slice.
    ///
    /// @param guiGraphics graphics extractor for the current render pass
    /// @param context     slice render context
    /// @param color       color value
    ///
    private void fillSlice(GuiGraphicsExtractor guiGraphics, SliceRenderContext<T, S> context, int color) {
        float sweep = context.endAngle() - context.startAngle();
        float absoluteSweep = Math.abs(sweep);
        float outerArcLength = absoluteSweep * Mth.DEG_TO_RAD * context.outerRadius();
        int countByDegrees = Mth.ceil(absoluteSweep / this.maxDegreesPerCurveSegment);
        int countByArcLength = Mth.ceil(outerArcLength / this.maxPixelsPerCurveSegment);
        int count = Math.max(1, Math.max(countByDegrees, countByArcLength));
        List<Vector2d> outerVertices = new ArrayList<>(count + 1);
        List<Vector2d> innerVertices = new ArrayList<>(count + 1);
        
        for (int step = 0; step <= count; step++) {
            float angle = Mth.lerp((float) step / count, context.startAngle(), context.endAngle());
            outerVertices.add(SLMathUtils.getPointOnCircle(new Vector2d(context.centerX(), context.centerY()), angle, context.outerRadius()));
        }
        
        for (int step = 0; step <= count; step++) {
            float angle = Mth.lerp((float) step / count, context.startAngle(), context.endAngle());
            innerVertices.add(SLMathUtils.getPointOnCircle(new Vector2d(context.centerX(), context.centerY()), angle, context.innerRadius()));
        }
        
        this.submitSliceMesh(guiGraphics, outerVertices, innerVertices, color);
    }
    
    ///
    /// Submits a tessellated slice mesh for rendering.
    ///
    /// @param graphics      graphics extractor for the current render pass
    /// @param outerVertices outer arc vertices
    /// @param innerVertices inner arc vertices
    /// @param color         color value
    ///
    private void submitSliceMesh(GuiGraphicsExtractor graphics, List<Vector2d> outerVertices, List<Vector2d> innerVertices, int color) {
        if (outerVertices.size() < 2 || innerVertices.size() < 2) return;
        
        float[] triangleVertices = new float[(outerVertices.size() - 1) * 12];
        int vertexIndex = 0;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        
        for (int index = 0; index < outerVertices.size() - 1; index++) {
            Vector2d outerStart = outerVertices.get(index);
            Vector2d outerEnd = outerVertices.get(index + 1);
            Vector2d innerStart = innerVertices.get(index);
            Vector2d innerEnd = innerVertices.get(index + 1);
            
            vertexIndex = RadialScreen.putTriangle(triangleVertices, vertexIndex, outerStart, innerStart, outerEnd);
            vertexIndex = RadialScreen.putTriangle(triangleVertices, vertexIndex, outerEnd, innerStart, innerEnd);
            
            minX = Math.min(minX, Math.min(Math.min(outerStart.x(), outerEnd.x()), Math.min(innerStart.x(), innerEnd.x())));
            maxX = Math.max(maxX, Math.max(Math.max(outerStart.x(), outerEnd.x()), Math.max(innerStart.x(), innerEnd.x())));
            minY = Math.min(minY, Math.min(Math.min(outerStart.y(), outerEnd.y()), Math.min(innerStart.y(), innerEnd.y())));
            maxY = Math.max(maxY, Math.max(Math.max(outerStart.y(), outerEnd.y()), Math.max(innerStart.y(), innerEnd.y())));
        }
        
        if (vertexIndex == 0) return;
        
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle rawBounds = new ScreenRectangle(
                Mth.floor(minX),
                Mth.floor(minY),
                Math.max(1, Mth.ceil(maxX) - Mth.floor(minX)),
                Math.max(1, Mth.ceil(maxY) - Mth.floor(minY))
        );
        ScreenRectangle bounds = rawBounds.transformMaxBounds(pose);
        graphics.submitGuiElementRenderState(new RadialSliceRenderState(
                pose,
                triangleVertices,
                color,
                graphics.peekScissorStack(),
                bounds
        ));
    }
    
    ///
    /// Appends triangle vertices to the target buffer.
    ///
    /// @param target target vertex buffer
    /// @param index  slice index
    /// @param a      first vertex
    /// @param b      second vertex
    /// @param c      third vertex
    ///
    /// @return the next write index
    ///
    private static int putTriangle(float[] target, int index, Vector2d a, Vector2d b, Vector2d c) {
        target[index++] = (float) a.x();
        target[index++] = (float) a.y();
        target[index++] = (float) b.x();
        target[index++] = (float) b.y();
        target[index++] = (float) c.x();
        target[index++] = (float) c.y();
        return index;
    }
    
    ///
    /// Immutable context object used while rendering a single slice.
    ///
    /// @param <T>         concrete screen type
    /// @param <S>         slice type rendered by the screen
    /// @param screen      owning radial screen
    /// @param slice       slice being rendered
    /// @param index       zero-based slice index
    /// @param count       total slice count
    /// @param hovered     whether the slice is currently hovered
    /// @param progress    hover animation progress
    /// @param centerX     radial menu center x position
    /// @param centerY     radial menu center y position
    /// @param startAngle  slice start angle in degrees
    /// @param middleAngle slice middle angle in degrees
    /// @param endAngle    slice end angle in degrees
    /// @param innerRadius slice inner radius
    /// @param outerRadius slice outer radius
    /// @param mouseX      current mouse x position
    /// @param mouseY      current mouse y position
    ///
    public record SliceRenderContext<T extends RadialScreen<T, S>, S extends RadialScreen.RadialSlice<T, S>>(
            T screen, S slice,
            int index, int count, boolean hovered, float progress,
            float centerX, float centerY,
            float startAngle, float middleAngle, float endAngle,
            float innerRadius, float outerRadius,
            double mouseX, double mouseY
    ) {
        ///
        /// Calculates the preferred content radius for this slice.
        ///
        /// @return the calculated slice content radius
        ///
        public float radius() {
            double sweepRadians = Math.abs((this.endAngle - this.startAngle) * Mth.DEG_TO_RAD);
            if (sweepRadians <= 1.0E-4D) {
                return (this.innerRadius + this.outerRadius) * 0.5F;
            }
            
            double outerRadiusSquared = this.outerRadius * this.outerRadius;
            double innerRadiusSquared = this.innerRadius * this.innerRadius;
            double radialDenominator = outerRadiusSquared - innerRadiusSquared;
            if (Math.abs(radialDenominator) <= 1.0E-4D) {
                return (this.innerRadius + this.outerRadius) * 0.5F;
            }
            
            double outerRadiusCubed = outerRadiusSquared * this.outerRadius;
            double innerRadiusCubed = innerRadiusSquared * this.innerRadius;
            double angularFactor = (4.0D * Math.sin(sweepRadians * 0.5D)) / (3.0D * sweepRadians);
            double radialFactor = (outerRadiusCubed - innerRadiusCubed) / radialDenominator;
            return (float) Mth.clamp(angularFactor * radialFactor, this.innerRadius, this.outerRadius);
        }
        
        ///
        /// Returns the preferred x position for slice content.
        ///
        /// @return the preferred x position for slice content
        ///
        public float contentX() {
            return this.centerX + (SLMathUtils.cos(this.middleAngle) * this.radius());
        }
        
        ///
        /// Returns the preferred y position for slice content.
        ///
        /// @return the preferred y position for slice content
        ///
        public float contentY() {
            return this.centerY + (SLMathUtils.sin(this.middleAngle) * this.radius());
        }
        
        ///
        /// Returns a point along the middle angle of the slice.
        ///
        /// @param percent interpolation percentage in the `[0, 1]` range
        ///
        /// @return the interpolated position
        ///
        public Vector2f getVectorAlongMiddle(float percent) {
            float clampedPercent = Mth.clamp(percent, 0.0F, 1.0F);
            float radius = Mth.lerp(clampedPercent, this.innerRadius, this.outerRadius);
            
            return new Vector2f(
                    this.centerX + (SLMathUtils.cos(this.middleAngle) * radius),
                    this.centerY + (SLMathUtils.sin(this.middleAngle) * radius)
            );
        }
        
        ///
        /// Interpolates the radius along the middle of the slice.
        ///
        /// @param percent interpolation percentage in the `[0, 1]` range
        ///
        /// @return the interpolated radius
        ///
        public float radiusAlongMiddle(float percent) {
            float clampedPercent = Mth.clamp(percent, 0.0F, 1.0F);
            return Mth.lerp(clampedPercent, this.innerRadius, this.outerRadius);
        }
        
        ///
        /// Returns the x position along the middle of the slice.
        ///
        /// @param percent interpolation percentage in the `[0, 1]` range
        ///
        /// @return the interpolated x position
        ///
        public float xAlongMiddle(float percent) {
            return this.getVectorAlongMiddle(percent).x();
        }
        
        ///
        /// Returns the y position along the middle of the slice.
        ///
        /// @param percent interpolation percentage in the `[0, 1]` range
        ///
        /// @return the interpolated y position
        ///
        public float yAlongMiddle(float percent) {
            return this.getVectorAlongMiddle(percent).y();
        }
    }
    
    ///
    /// Base slice type used by {@link RadialScreen}.
    ///
    /// @param <T> concrete screen type
    /// @param <S> slice type implementation
    ///
    @Getter
    public abstract static class RadialSlice<T extends RadialScreen<T, S>, S extends RadialSlice<T, S>> {
        
        @Setter
        private int baseColor = RadialScreen.DEFAULT_SLICE_COLOR;
        
        @Setter
        private int hoverColor = RadialScreen.DEFAULT_HOVER_COLOR;
        
        @Setter
        private int labelColor = RadialScreen.DEFAULT_LABEL_COLOR;
        
        @Getter
        @Setter
        private float hoverProgress;
        
        ///
        /// Creates an empty radial slice.
        ///
        protected RadialSlice() { }
        
        ///
        /// Renders the background and contents of this slice.
        ///
        /// @param graphics graphics extractor for the current render pass
        /// @param context  slice render context
        ///
        public void renderSlice(GuiGraphicsExtractor graphics, SliceRenderContext<T, S> context) {
            this.renderBackground(graphics, context);
            this.renderContents(graphics, context);
        }
        
        ///
        /// Renders the background for this slice.
        ///
        /// @param graphics graphics extractor for the current render pass
        /// @param context  slice render context
        ///
        protected void renderBackground(GuiGraphicsExtractor graphics, SliceRenderContext<T, S> context) {
            context.screen().drawDefaultSliceBackground(graphics, context, this.baseColor, this.hoverColor);
        }
        
        ///
        /// Renders the custom contents for this slice.
        ///
        /// @param graphics graphics extractor for the current render pass
        /// @param context  slice render context
        ///
        protected abstract void renderContents(GuiGraphicsExtractor graphics, SliceRenderContext<T, S> context);
        
        ///
        /// Runs when the pointer starts hovering this slice.
        ///
        /// @param screen screen that owns this slice
        ///
        public void onHoverStarted(T screen) { }
        
        ///
        /// Runs when the pointer stops hovering this slice.
        ///
        /// @param screen screen that owns this slice
        ///
        public void onHoverEnded(T screen) { }
        
        ///
        /// Runs every frame while this slice remains hovered.
        ///
        /// @param screen      screen that owns this slice
        /// @param mouseX      current mouse x position
        /// @param mouseY      current mouse y position
        /// @param partialTick time since the last tick
        ///
        public void onHovered(T screen, double mouseX, double mouseY, float partialTick) { }
        
        ///
        /// Handles mouse press input for this slice.
        ///
        /// @param screen      screen that owns this slice
        /// @param event       input event
        /// @param doubleClick whether the click is a double click
        ///
        /// @return `true` when the event is handled
        ///
        public boolean mousePressed(T screen, MouseButtonEvent event, boolean doubleClick) {
            return false;
        }
        
        ///
        /// Handles mouse release input for this slice.
        ///
        /// @param screen screen that owns this slice
        /// @param event  input event
        ///
        /// @return `true` when the event is handled
        ///
        public boolean mouseReleased(T screen, MouseButtonEvent event) {
            return false;
        }
        
        ///
        /// Handles mouse drag input for this slice.
        ///
        /// @param screen screen that owns this slice
        /// @param event  input event
        /// @param dragX  horizontal drag delta
        /// @param dragY  vertical drag delta
        ///
        /// @return `true` when the event is handled
        ///
        public boolean mouseDragged(T screen, MouseButtonEvent event, double dragX, double dragY) {
            return false;
        }
        
        ///
        /// Handles scroll input for this slice.
        ///
        /// @param screen  screen that owns this slice
        /// @param mouseX  current mouse x position
        /// @param mouseY  current mouse y position
        /// @param scrollX horizontal scroll amount
        /// @param scrollY vertical scroll amount
        ///
        /// @return `true` when the event is handled
        ///
        public boolean mouseScrolled(T screen, double mouseX, double mouseY, double scrollX, double scrollY) {
            return false;
        }
        
        ///
        /// Handles key press input for this slice.
        ///
        /// @param screen screen that owns this slice
        /// @param event  input event
        ///
        /// @return `true` when the event is handled
        ///
        public boolean keyPressed(T screen, KeyEvent event) {
            return false;
        }
        
        ///
        /// Handles key release input for this slice.
        ///
        /// @param screen screen that owns this slice
        /// @param event  input event
        ///
        /// @return `true` when the event is handled
        ///
        public boolean keyReleased(T screen, KeyEvent event) {
            return false;
        }
        
        ///
        /// Handles character input for this slice.
        ///
        /// @param screen screen that owns this slice
        /// @param event  input event
        ///
        /// @return `true` when the event is handled
        ///
        public boolean charTyped(T screen, CharacterEvent event) {
            return false;
        }
    }
    
    ///
    /// Render-state object that submits a slice mesh to the GUI renderer.
    ///
    private record RadialSliceRenderState(
            Matrix3x2fc pose,
            float[] vertices,
            int color,
            @Nullable ScreenRectangle scissorArea,
            ScreenRectangle bounds
    ) implements GuiElementRenderState {
        
        ///
        /// Builds the vertex data for the slice render state.
        ///
        /// @param consumer vertex consumer
        ///
        @Override
        public void buildVertices(VertexConsumer consumer) {
            for (int index = 0; index < this.vertices.length; index += 2) {
                consumer.addVertexWith2DPose(this.pose, this.vertices[index], this.vertices[index + 1]).setColor(this.color);
            }
        }
        
        ///
        /// Returns the render pipeline used for slice rendering.
        ///
        /// @return the render pipeline
        ///
        @Override
        public RenderPipeline pipeline() {
            return SLRenderPipelines.VERTEX_GUI;
        }
        
        ///
        /// Returns the texture setup used for slice rendering.
        ///
        /// @return the texture setup
        ///
        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }
    }
}
