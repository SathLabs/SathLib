package dev.satherov.sathlib.network.chat;

import dev.satherov.sathlib.client.lang.SLTranslatable;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.fml.loading.FMLEnvironment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

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
    public static SLComponent builder() {
        return new SLComponent(Component.empty());
    }
    
    ///
    /// Creates a new component with the given root component
    ///
    /// @return component with the given root
    ///
    public static SLComponent of(MutableComponent component) {
        return new SLComponent(component);
    }
    
    ///
    /// Appends a {@link Component} to the root
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
    /// @return self
    ///
    public SLComponent append(SLTranslatable translatable) {
        return this.append(translatable.translate());
    }
    
    ///
    /// Appends a literal string to the root
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
    /// @return self
    ///
    public SLComponent translateable(String translationKey, Object... args) {
        this.component.append(SLComponent.identify(translationKey, args));
        return this;
    }
    
    ///
    /// Sets the style with the given {@link ChatFormatting}
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
    /// @return self
    ///
    public SLComponent style(ChatFormatting... formats) {
        this.component.withStyle(formats);
        return this;
    }
    
    ///
    /// Sets the style with the given {@link SLStyle} operator
    ///
    /// @return self
    ///
    public SLComponent style(UnaryOperator<SLStyle> operator) {
        this.component.withStyle(operator.apply(new SLStyle()).create());
        return this;
    }
    
    ///
    /// Creates a {@link MutableComponent} from the given translation key and the given translation components
    ///
    /// @return created component
    ///
    public static SLComponent identify(String translationKey, Object... args) {
        List<ChatFormatting> formatting = new ArrayList<>();
        List<Object> arguments = new ArrayList<>();
        
        for (Object arg : args) {
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
        if (formatting.isEmpty()) component.withStyle(formatting.toArray(ChatFormatting[]::new));
        return SLComponent.of(component);
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
