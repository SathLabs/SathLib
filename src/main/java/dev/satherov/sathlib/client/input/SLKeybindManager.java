package dev.satherov.sathlib.client.input;

import dev.satherov.sathlib.client.lang.SLTranslatable;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

///
/// Manager for all keybinds under a specific category
///
public class SLKeybindManager {
    
    private final String namespace;
    private final KeyMapping.Category category;
    private final Map<KeyMapping, Consumer<InputEvent.Key>> mappings = new HashMap<>();
    
    private SLKeybindManager(String namespace, KeyMapping.Category category) {
        this.namespace = namespace;
        this.category = category;
    }
    
    ///
    /// Creates a Keybind manager under the given namespace and with a category of the given name
    ///
    /// @param namespace namespace of this manager and namespace under which the category is created
    /// @param category  the identifier of this category. The translation key is used for the category identifier
    ///
    /// @return the created Keybind manager
    ///
    public static SLKeybindManager create(String namespace, Identifier category) {
        return new SLKeybindManager(namespace, new KeyMapping.Category(category));
    }
    
    ///
    /// Adds a new keybind mapping under the given translation entry and with the configured input even callback
    ///
    /// @param name     the translatable entry under which this mapping is created
    /// @param key      the GLFW key code used as the default key of this mapping
    /// @param callback the input event callback used when this key is pressed
    ///
    /// @return the created key mapping
    ///
    public KeyMapping add(SLTranslatable name, int key, Consumer<InputEvent.Key> callback) {
        KeyMapping mapping = new KeyMapping(name.key(), key, this.category);
        this.mappings.put(mapping, callback);
        return mapping;
    }
    
    ///
    /// Adds a new keybind mapping created with the given factory and with the configured input even callback
    ///
    /// @param factory  the factory for this key mapping, supplied with the category of this manager
    /// @param callback the input event callback used when this key is pressed
    ///
    /// @return the created key mapping
    ///
    public KeyMapping add(Function<KeyMapping.Category, KeyMapping> factory, Consumer<InputEvent.Key> callback) {
        KeyMapping mapping = factory.apply(this.category);
        this.mappings.put(mapping, callback);
        return mapping;
    }
    
    ///
    /// Registers this manager to the give mod event bus by registering
    /// the category and all mappings and adds the listener for the input event callbacks
    ///
    /// @param bus the mod event bus
    ///
    public void register(final IEventBus bus) {
        bus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.registerCategory(this.category);
            for (KeyMapping mapping : this.mappings.keySet()) event.register(mapping);
        });
        NeoForge.EVENT_BUS.addListener(InputEvent.Key.class, event -> {
            for (Map.Entry<KeyMapping, Consumer<InputEvent.Key>> entry : this.mappings.entrySet()) {
                KeyMapping mapping = entry.getKey();
                Consumer<InputEvent.Key> consumer = entry.getValue();
                if (mapping.matches(event.getKeyEvent())) consumer.accept(event);
            }
        });
    }
}
