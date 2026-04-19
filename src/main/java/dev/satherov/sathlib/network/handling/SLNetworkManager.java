package dev.satherov.sathlib.network.handling;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashSet;
import java.util.Set;

///
/// Network manager for registering payloads
///
public class SLNetworkManager {
    
    private final String namespace;
    private final Set<PayloadProvider<?>> providers = new HashSet<>();
    
    private SLNetworkManager(String namespace) {
        this.namespace = namespace;
    }
    
    ///
    /// Creates a network manager for one payload namespace.
    ///
    /// @param namespace payload namespace
    ///
    /// @return new network manager
    ///
    public static SLNetworkManager create(String namespace) {
        return new SLNetworkManager(namespace);
    }
    
    ///
    /// Adds a payload provider to the network manager.
    ///
    /// @param provider Payload provider to add
    ///
    /// @return This network manager instance for chaining
    ///
    public SLNetworkManager add(PayloadProvider<?> provider) {
        if (!this.providers.add(provider)) {
            throw new IllegalArgumentException("Payload provider already registered");
        }
        return this;
    }
    
    ///
    /// Registers all providers to the given {@link IEventBus}.
    ///
    /// @param bus Event bus to register providers to
    ///
    public void register(final IEventBus bus) {
        bus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            final PayloadRegistrar registrar = event.registrar("1");
            this.providers.forEach(provider -> provider.register(registrar));
            this.providers.clear();
        });
    }
}
