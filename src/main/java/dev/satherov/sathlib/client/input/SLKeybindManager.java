package dev.satherov.sathlib.client.input;

import lombok.RequiredArgsConstructor;

import dev.satherov.sathlib.client.lang.SLTranslatable;
import dev.satherov.sathlib.util.SLResourceUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

import net.minecraft.client.KeyMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor(staticName = "create")
public class SLKeybindManager {
    
    private final String namespace;
    private final KeyMapping.Category category;
    private final Map<KeyMapping, Consumer<InputEvent.Key>> mappings = new HashMap<>();
    
    public static SLKeybindManager create(String namespace, SLTranslatable category) {
        return new SLKeybindManager(namespace, new KeyMapping.Category(SLResourceUtils.id(namespace, category.key())));
    }
    
    public KeyMapping add(SLTranslatable name, int key, Consumer<InputEvent.Key> callback) {
        KeyMapping mapping = new KeyMapping(name.key(), key, this.category);
        this.mappings.put(mapping, callback);
        return mapping;
    }
    
    public KeyMapping add(Function<KeyMapping.Category, KeyMapping> factory, Consumer<InputEvent.Key> callback) {
        KeyMapping mapping = factory.apply(this.category);
        this.mappings.put(mapping, callback);
        return mapping;
    }
    
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
