package dev.satherov.sathlib.network.chat;

import dev.satherov.sathlib.client.lang.FormattingLang;
import dev.satherov.sathlib.client.lang.GenericLang;
import dev.satherov.sathlib.client.lang.SLTranslatable;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.fml.loading.FMLEnvironment;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import com.mojang.blaze3d.platform.InputConstants;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

///
/// Wrapper class for building components
///
@NothingNull
public class SLComponent implements Component {
    
    private final MutableComponent component;
    
    private SLComponent(MutableComponent component) {
        this.component = component;
    }
    
    ///
    /// Creates a new empty component to start building from
    ///
    /// @return Empty SLComponent
    ///
    public static SLComponent empty() {
        return new SLComponent(Component.empty());
    }
    
    ///
    /// Creates a new component with the given root component
    ///
    /// @param component root component to wrap
    ///
    /// @return component with the given root
    ///
    public static SLComponent of(MutableComponent component) {
        return new SLComponent(component);
    }
    
    ///
    /// Appends a {@link Component} to the root
    ///
    /// @param component component to append
    ///
    /// @return self
    ///
    public SLComponent append(Component component) {
        this.component.append(component);
        return this;
    }
    
    ///
    /// Appends a {@link SLTranslatable} to the root
    ///
    /// @param translatable translation entry to append
    ///
    /// @return self
    ///
    public SLComponent append(SLTranslatable translatable) {
        return this.append(translatable.translate());
    }
    
    ///
    /// Appends a literal string to the root
    ///
    /// @param text literal text to append
    ///
    /// @return self
    ///
    public SLComponent literal(String text) {
        this.component.append(Component.literal(text));
        return this;
    }
    
    ///
    /// Appends a translateable component to the root, using the given translation key
    ///
    /// @param translationKey translation key to append
    ///
    /// @return self
    ///
    public SLComponent translateable(String translationKey) {
        this.component.append(Component.translatable(translationKey));
        return this;
    }
    
    ///
    /// Appends a translateable component to the root, using the given translation key and
    /// translation argument to be inferred by {@link SLComponent#identify(String, Object...)}
    ///
    /// @param translationKey translation key to append
    /// @param args           translation arguments
    ///
    /// @return self
    ///
    public SLComponent translateable(String translationKey, Object... args) {
        this.component.append(SLComponent.identify(translationKey, args));
        return this;
    }
    
    ///
    /// Sets the style with the given {@link ChatFormatting}
    ///
    /// @param format style format to apply
    ///
    /// @return self
    ///
    public SLComponent style(ChatFormatting format) {
        this.component.withStyle(format);
        return this;
    }
    
    ///
    /// Sets the style with the given {@link ChatFormatting}s
    ///
    /// @param formats style formats to apply
    ///
    /// @return self
    ///
    public SLComponent style(ChatFormatting... formats) {
        this.component.withStyle(formats);
        return this;
    }
    
    ///
    /// Sets the style with the given {@link SLStyle} operator
    ///
    /// @param operator style mutator
    ///
    /// @return self
    ///
    public SLComponent style(UnaryOperator<SLStyle> operator) {
        this.component.withStyle(operator.apply(new SLStyle()).create());
        return this;
    }
    
    ///
    /// Creates a {@link SLComponent} from the given translation key and the given translation components
    ///
    /// @param translationKey translation key to resolve
    /// @param args           translation arguments and optional formatting markers
    ///
    /// @return created component
    ///
    public static SLComponent identify(String translationKey, @Nullable Object... args) {
        List<ChatFormatting> formatting = new ArrayList<>();
        List<Object> arguments = new ArrayList<>();
        
        for (@Nullable Object arg : args) {
            switch (arg) {
                case ChatFormatting format -> formatting.add(format);
                case Component _, Number _, Boolean _, String _ -> arguments.add(arg);
                case null -> { }
                default -> {
                    if (!FMLEnvironment.isProduction())
                        throw new IllegalArgumentException("Translation Argument must either ChatFormatting or a Component, Number, Boolean or String, given for " + translationKey + " was " + arg);
                }
            }
        }
        
        MutableComponent component = arguments.isEmpty() ? Component.translatable(translationKey) : Component.translatable(translationKey, arguments.toArray());
        if (!formatting.isEmpty()) component.withStyle(formatting.toArray(ChatFormatting[]::new));
        return SLComponent.of(component);
    }
    
    ///
    /// Returns a component for the given key
    ///
    /// @param key the input constant key
    ///
    /// @return component of the key's display name
    ///
    public static SLComponent key(InputConstants.Key key) {
        return SLComponent.of(key.getDisplayName().copy());
    }
    
    ///
    /// Returns a component for the given block position
    ///
    /// @param pos the block position
    ///
    /// @return component of the formatted block position
    ///
    public static SLComponent pos(BlockPos pos) {
        return SLComponent.of(Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()));
    }
    
    ///
    /// Returns the correct enabled / disabled state for the given boolean
    ///
    /// @param enabled decides which text to return
    ///
    /// @return {@link GenericLang#ENABLED} if `true`, {@link GenericLang#DISABLED} if `false`
    ///
    public static SLComponent enabledDisabled(boolean enabled) {
        return enabled ? GenericLang.ENABLED.translate(ChatFormatting.DARK_GREEN) : GenericLang.DISABLED.translate(ChatFormatting.DARK_RED);
    }
    
    ///
    /// Returns the correct on / off state for the given boolean
    ///
    /// @param on decides which text to return
    ///
    /// @return {@link GenericLang#ON} if `true`, {@link GenericLang#OFF} if `false`
    ///
    public static SLComponent onOff(boolean on) {
        return on ? GenericLang.ON.translate(ChatFormatting.DARK_GREEN) : GenericLang.OFF.translate(ChatFormatting.DARK_RED);
    }
    
    ///
    /// Returns the correct allowed / deny state for the given boolean
    ///
    /// @param allowed decides which text to return
    ///
    /// @return {@link GenericLang#ALLOW} if `true`, {@link GenericLang#DENY} if `false`
    ///
    public static SLComponent allowedDenied(boolean allowed) {
        return allowed ? GenericLang.ALLOW.translate(ChatFormatting.DARK_GREEN) : GenericLang.DENY.translate(ChatFormatting.DARK_RED);
    }
    
    ///
    /// Wraps the given component into two rounded brackets
    ///
    /// "Example" -> "\(Example\)"
    ///
    /// @param component the component to wrap
    ///
    /// @return the component wrapped in rounded brackets
    ///
    public static SLComponent roundBrackets(Component component) {
        return FormattingLang.ROUND_BRACKETS.translate(component);
    }
    
    ///
    /// Wraps the given component into two square brackets
    ///
    /// "Example" -> "\[Example\]"
    ///
    /// @param component the component to wrap
    ///
    /// @return the component wrapped in square brackets
    ///
    public static SLComponent squareBrackets(Component component) {
        return FormattingLang.SQUARE_BRACKETS.translate(component);
    }
    
    ///
    /// Wraps the given component into two curly brackets
    ///
    /// "Example" -> "\{Example\}"
    ///
    /// @param component the component to wrap
    ///
    /// @return the component wrapped in curly brackets
    ///
    public static SLComponent curlyBrackets(Component component) {
        return FormattingLang.CURLY_BRACKETS.translate(component);
    }
    
    
    @Override
    public Style getStyle() {
        return this.component.getStyle();
    }
    
    @Override
    public ComponentContents getContents() {
        return this.component.getContents();
    }
    
    @Override
    public List<Component> getSiblings() {
        return this.component.getSiblings();
    }
    
    @Override
    public FormattedCharSequence getVisualOrderText() {
        return this.component.getVisualOrderText();
    }
}
