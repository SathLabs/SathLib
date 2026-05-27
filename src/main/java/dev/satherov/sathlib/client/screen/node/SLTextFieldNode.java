package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;
import lombok.Getter;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.state.UIState;
import dev.satherov.sathlib.client.screen.style.SLTextFieldRenderState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.UnaryOperator;

///
/// Reusable single-line text field node.
///
/// The field owns its editable value, selection, and clipboard handling while
/// delegating visuals to the active {@link dev.satherov.sathlib.client.screen.style.UITheme}.
///
public class SLTextFieldNode extends UILeafNode<SLTextFieldNode> {
    
    private static final int CURSOR_BLINK_HALF_PERIOD_TICKS = 6;
    private static final int CURSOR_BLINK_PERIOD_TICKS = SLTextFieldNode.CURSOR_BLINK_HALF_PERIOD_TICKS * 2;
    
    private final int maxLength;
    private final UnaryOperator<String> sanitizer;
    private final @Nullable IntPredicate acceptedCodepoint;
    private final @Nullable Consumer<String> onValueChanged;
    @Getter private String value;
    private @Nullable String placeholder;
    private @Nullable Consumer<String> onCommit;
    private @Nullable UIState<String> valueState;
    private @Nullable Runnable unsubscribeValueState;
    
    private int cursor;
    private int selectionAnchor;
    private int cursorBlinkTicks;
    private boolean wasFocused;
    
    ///
    /// Creates an empty text field.
    ///
    public SLTextFieldNode() {
        this(SLModifier.none(), "", null, Integer.MAX_VALUE, UnaryOperator.identity(), null, null, null, null);
    }
    
    ///
    /// Creates a fully configured text field.
    ///
    /// @param modifier          node modifier
    /// @param value             initial text value
    /// @param placeholder       optional placeholder
    /// @param maxLength         maximum stored character count
    /// @param sanitizer         text sanitizer applied after edits
    /// @param acceptedCodepoint optional accepted-codepoint filter
    /// @param onValueChanged    optional live change callback
    /// @param onCommit          optional commit callback
    /// @param valueState        optional external value state
    ///
    protected SLTextFieldNode(
            SLModifier modifier,
            String value,
            @Nullable String placeholder,
            int maxLength,
            UnaryOperator<String> sanitizer,
            @Nullable IntPredicate acceptedCodepoint,
            @Nullable Consumer<String> onValueChanged,
            @Nullable Consumer<String> onCommit,
            @Nullable UIState<String> valueState
    ) {
        super(modifier);
        this.placeholder = placeholder;
        this.maxLength = Math.max(0, maxLength);
        this.sanitizer = Objects.requireNonNull(sanitizer);
        this.acceptedCodepoint = acceptedCodepoint;
        this.onValueChanged = onValueChanged;
        this.onCommit = onCommit;
        this.valueState = valueState;
        this.value = this.normalize(value);
        this.cursor = this.value.length();
        this.selectionAnchor = this.cursor;
        
        if (valueState != null) {
            this.setValueSilently(valueState.get());
        }
    }
    
    ///
    /// Creates a builder-backed text field while normalizing omitted values to
    /// the framework defaults.
    ///
    /// @param modifier          node modifier
    /// @param value             initial text value
    /// @param placeholder       optional placeholder
    /// @param maxLength         maximum stored character count
    /// @param sanitizer         text sanitizer applied after edits
    /// @param acceptedCodepoint optional accepted-codepoint filter
    /// @param onValueChanged    optional live change callback
    /// @param onCommit          optional commit callback
    /// @param valueState        optional external value state
    ///
    /// @return configured text field node
    ///
    @Builder
    public static SLTextFieldNode of(
            SLModifier modifier,
            String value,
            String placeholder,
            Integer maxLength,
            UnaryOperator<String> sanitizer,
            IntPredicate acceptedCodepoint,
            Consumer<String> onValueChanged,
            Consumer<String> onCommit,
            UIState<String> valueState
    ) {
        return new SLTextFieldNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(value, ""),
                placeholder,
                Objects.requireNonNullElse(maxLength, Integer.MAX_VALUE),
                Objects.requireNonNullElse(sanitizer, UnaryOperator.identity()),
                acceptedCodepoint,
                onValueChanged,
                onCommit,
                valueState
        );
    }
    
    ///
    /// Sets the placeholder text shown while the field is empty.
    ///
    /// @param placeholder placeholder text, or {@code null}
    ///
    /// @return this field
    ///
    public SLTextFieldNode placeholder(@Nullable String placeholder) {
        this.placeholder = placeholder;
        return this;
    }
    
    ///
    /// Sets the field value without firing callbacks.
    ///
    /// @param value new field value
    ///
    /// @return this field
    ///
    public SLTextFieldNode setValueSilently(String value) {
        this.value = this.normalize(value);
        this.cursor = this.value.length();
        this.selectionAnchor = this.cursor;
        this.resetCursorBlink();
        return this;
    }
    
    ///
    /// Sets the field value and notifies the live change callback.
    ///
    /// @param value new field value
    ///
    /// @return this field
    ///
    public SLTextFieldNode value(String value) {
        this.setValueSilently(value);
        this.fireValueChanged();
        return this;
    }
    
    ///
    /// Binds the field to an external state used for passive synchronization.
    ///
    /// @param valueState external value state
    ///
    /// @return this field
    ///
    public SLTextFieldNode bindValue(UIState<String> valueState) {
        this.valueState = valueState;
        this.setValueSilently(valueState.get());
        return this;
    }
    
    ///
    /// Sets the commit callback invoked on enter or focus loss.
    ///
    /// @param onCommit commit callback
    ///
    /// @return this field
    ///
    public SLTextFieldNode onCommit(@Nullable Consumer<String> onCommit) {
        this.onCommit = onCommit;
        return this;
    }
    
    @Override
    protected boolean isInputTarget() {
        return true;
    }
    
    @Override
    protected void onAttached(UIRoot root) {
        if (this.valueState != null) {
            this.unsubscribeValueState = this.valueState.listen(value -> {
                if (!this.isFocused()) {
                    this.setValueSilently(value);
                }
            });
        }
    }
    
    @Override
    protected void onDetached() {
        if (this.unsubscribeValueState != null) {
            this.unsubscribeValueState.run();
            this.unsubscribeValueState = null;
        }
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        int measuredTextWidth = Math.max(font.width(this.value), this.placeholder == null ? 0 : font.width(this.placeholder));
        return new SLMeasuredSize(Math.max(80, measuredTextWidth + 8), Math.max(18, font.lineHeight + 6));
    }
    
    @Override
    protected void tick() {
        if (this.wasFocused && !this.isFocused()) {
            this.commitValue();
        }
        if (this.isFocused()) {
            this.cursorBlinkTicks = (this.cursorBlinkTicks + 1) % SLTextFieldNode.CURSOR_BLINK_PERIOD_TICKS;
        } else {
            this.cursorBlinkTicks = 0;
        }
        this.wasFocused = this.isFocused();
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        context.theme().renderTextField(
                context,
                this.getBounds(),
                new SLTextFieldRenderState(
                        this.value,
                        this.placeholder,
                        this.cursor,
                        this.selectionStart(),
                        this.selectionEnd(),
                        this.isFocused(),
                        this.isCursorVisible(),
                        this.isEnabled()
                )
        );
    }
    
    @Override
    public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        if (!this.isEnabled() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        
        if (doubleClick) {
            this.selectAll();
        } else {
            this.moveCursorTo(this.cursorAt(event.x()), event.hasShiftDown());
        }
        this.setPressedState(true);
        return true;
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (!this.isPressed() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        this.moveCursorTo(this.cursorAt(event.x()), true);
        return true;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean wasPressed = this.isPressed();
        this.setPressedState(false);
        return wasPressed && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.isFocused() || !this.isEnabled()) return false;
        
        return switch (event.key()) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                this.commitValue();
                yield true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                if (this.getRoot() != null) {
                    this.getRoot().requestFocus(null);
                }
                yield true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                this.deleteText(-1);
                yield true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                this.deleteText(1);
                yield true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                this.moveCursorBy(-1, event.hasShiftDown());
                yield true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                this.moveCursorBy(1, event.hasShiftDown());
                yield true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                this.moveCursorTo(0, event.hasShiftDown());
                yield true;
            }
            case GLFW.GLFW_KEY_END -> {
                this.moveCursorTo(this.value.length(), event.hasShiftDown());
                yield true;
            }
            default -> {
                if (event.isSelectAll()) {
                    this.selectAll();
                    yield true;
                }
                if (event.isCopy()) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(this.highlightedText());
                    yield true;
                }
                if (event.isCut()) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(this.highlightedText());
                    if (this.hasSelection()) this.insertText("");
                    yield true;
                }
                if (event.isPaste()) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                    yield true;
                }
                yield false;
            }
        };
    }
    
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!this.isFocused() || !this.isEnabled()) return false;
        
        int codepoint = event.codepoint();
        if (this.acceptedCodepoint != null && !this.acceptedCodepoint.test(codepoint)) return false;
        if (Character.isISOControl(codepoint)) return false;
        
        this.insertText(new String(Character.toChars(codepoint)));
        return true;
    }
    
    ///
    /// Commits the current value to the optional callback.
    ///
    private void commitValue() {
        if (this.onCommit != null) {
            this.onCommit.accept(this.value);
        }
    }
    
    ///
    /// Inserts sanitized text at the current selection.
    ///
    /// @param insertedText text to insert
    ///
    private void insertText(String insertedText) {
        int selectionStart = this.selectionStart();
        int selectionEnd = this.selectionEnd();
        String normalizedInsertedText = this.normalizeInsertedText(insertedText);
        String prefix = this.value.substring(0, selectionStart);
        String suffix = this.value.substring(selectionEnd);
        String combinedValue = prefix + normalizedInsertedText + suffix;
        String normalizedValue = this.normalize(combinedValue);
        int targetCursor = this.normalize(prefix + normalizedInsertedText).length();
        this.value = normalizedValue;
        this.cursor = Math.min(targetCursor, this.value.length());
        this.selectionAnchor = this.cursor;
        this.resetCursorBlink();
        this.fireValueChanged();
    }
    
    ///
    /// Deletes text relative to the current cursor.
    ///
    /// @param direction negative for backspace, positive for delete
    ///
    private void deleteText(int direction) {
        if (this.hasSelection()) {
            this.insertText("");
            return;
        }
        if (this.value.isEmpty()) return;
        
        int selectionStart = this.cursor;
        int selectionEnd = this.cursor;
        if (direction < 0 && this.cursor > 0) {
            selectionStart = this.cursor - 1;
        } else if (direction > 0 && this.cursor < this.value.length()) {
            selectionEnd = this.cursor + 1;
        } else {
            return;
        }
        
        this.value = this.normalize(this.value.substring(0, selectionStart) + this.value.substring(selectionEnd));
        this.cursor = Math.min(selectionStart, this.value.length());
        this.selectionAnchor = this.cursor;
        this.resetCursorBlink();
        this.fireValueChanged();
    }
    
    ///
    /// Moves the cursor by a relative delta.
    ///
    /// @param delta         cursor delta
    /// @param keepSelection whether to preserve the selection anchor
    ///
    private void moveCursorBy(int delta, boolean keepSelection) {
        this.moveCursorTo(this.cursor + delta, keepSelection);
    }
    
    ///
    /// Moves the cursor to an absolute position.
    ///
    /// @param position      target cursor position
    /// @param keepSelection whether to preserve the selection anchor
    ///
    private void moveCursorTo(int position, boolean keepSelection) {
        this.cursor = Mth.clamp(position, 0, this.value.length());
        if (!keepSelection) this.selectionAnchor = this.cursor;
        this.resetCursorBlink();
    }
    
    ///
    /// Selects the whole field value.
    ///
    private void selectAll() {
        this.cursor = this.value.length();
        this.selectionAnchor = 0;
        this.resetCursorBlink();
    }
    
    ///
    /// Resolves the closest cursor position for the given mouse x coordinate.
    ///
    /// @param mouseX pointer x position
    ///
    /// @return closest cursor index
    ///
    private int cursorAt(double mouseX) {
        Font font = Minecraft.getInstance().font;
        int textX = this.getBounds().x() + 4;
        int relativeX = (int) Math.round(mouseX) - textX;
        if (relativeX <= 0) return 0;
        
        int closestCursor = this.value.length();
        int closestDistance = Integer.MAX_VALUE;
        for (int cursorIndex = 0; cursorIndex <= this.value.length(); cursorIndex++) {
            int cursorX = font.width(this.value.substring(0, cursorIndex));
            int distance = Math.abs(cursorX - relativeX);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestCursor = cursorIndex;
            }
        }
        return closestCursor;
    }
    
    ///
    /// Returns whether the field currently has a selection range.
    ///
    /// @return {@code true} when a selection exists
    ///
    private boolean hasSelection() {
        return this.cursor != this.selectionAnchor;
    }
    
    ///
    /// Returns the normalized selection start.
    ///
    /// @return selection start
    ///
    private int selectionStart() {
        return Math.min(this.cursor, this.selectionAnchor);
    }
    
    ///
    /// Returns the normalized selection end.
    ///
    /// @return selection end
    ///
    private int selectionEnd() {
        return Math.max(this.cursor, this.selectionAnchor);
    }
    
    ///
    /// Returns the currently highlighted text or the full value when no
    /// selection exists.
    ///
    /// @return highlighted text
    ///
    private String highlightedText() {
        if (!this.hasSelection()) return this.value;
        return this.value.substring(this.selectionStart(), this.selectionEnd());
    }
    
    ///
    /// Normalizes input text through the sanitizer and max-length policy.
    ///
    /// @param value source value
    ///
    /// @return normalized field value
    ///
    private String normalize(String value) {
        String normalized = Objects.requireNonNullElse(value, "");
        normalized = Objects.requireNonNullElse(this.sanitizer.apply(normalized), "");
        if (normalized.length() > this.maxLength) normalized = normalized.substring(0, this.maxLength);
        return normalized;
    }
    
    ///
    /// Filters inserted text through the accepted-codepoint policy before the
    /// full field sanitizer runs.
    ///
    /// @param insertedText source inserted text
    ///
    /// @return filtered inserted text
    ///
    private String normalizeInsertedText(String insertedText) {
        String text = Objects.requireNonNullElse(insertedText, "");
        if (text.isBlank()) return "";
        
        StringBuilder builder = new StringBuilder(text.length());
        text.codePoints().forEach(codepoint -> {
            if (Character.isISOControl(codepoint)) return;
            if (this.acceptedCodepoint == null || this.acceptedCodepoint.test(codepoint)) {
                builder.appendCodePoint(codepoint);
            }
        });
        return builder.toString();
    }
    
    ///
    /// Returns whether the caret should currently be rendered.
    ///
    /// @return {@code true} when the blinking caret is in its visible phase
    ///
    private boolean isCursorVisible() {
        return !this.isFocused() || this.cursorBlinkTicks < SLTextFieldNode.CURSOR_BLINK_HALF_PERIOD_TICKS;
    }
    
    ///
    /// Restarts the caret blink cycle after local interaction.
    ///
    private void resetCursorBlink() {
        this.cursorBlinkTicks = 0;
    }
    
    ///
    /// Notifies the optional live change callback.
    ///
    private void fireValueChanged() {
        if (this.onValueChanged != null) {
            this.onValueChanged.accept(this.value);
        }
    }
}
