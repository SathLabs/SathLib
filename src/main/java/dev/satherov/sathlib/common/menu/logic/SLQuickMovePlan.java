package dev.satherov.sathlib.common.menu.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

///
/// Declarative quick-move routing for a menu.
///
/// Plans map one source semantic to one or more destination semantics in order.
/// The menu base uses that route table to perform shift-click transfer without
/// exposing raw slot indices.
///
/// - describe quick-move routes by semantic role
/// - keep transfer logic readable
/// - allow menus to opt into the shared base implementation
///
public final class SLQuickMovePlan {
    
    private final Map<SLSlotSemantic, List<RouteTarget>> targetsBySource;
    
    private SLQuickMovePlan(Map<SLSlotSemantic, List<RouteTarget>> targetsBySource) {
        this.targetsBySource = targetsBySource;
    }
    
    ///
    /// Creates a new quick-move plan builder.
    ///
    /// @return new builder
    ///
    public static Builder builder() {
        return new Builder();
    }
    
    ///
    /// Returns the configured route targets for one source semantic.
    ///
    /// @param source source semantic
    ///
    /// @return ordered route targets
    ///
    public List<RouteTarget> targetsFor(SLSlotSemantic source) {
        return this.targetsBySource.getOrDefault(source, List.of());
    }
    
    ///
    /// One destination entry in a quick-move route.
    ///
    /// @param semantic destination semantic
    /// @param reverse  whether the destination slots should be tried backwards
    ///
    public record RouteTarget(SLSlotSemantic semantic, boolean reverse) { }
    
    ///
    /// Builder for quick-move plans.
    ///
    /// Use `from(...).to(...).end()` chains to describe the route order for
    /// each source semantic.
    ///
    public static final class Builder {
        
        private final Map<SLSlotSemantic, List<RouteTarget>> targetsBySource = new LinkedHashMap<>();
        
        private Builder() { }
        
        ///
        /// Starts a route definition for one source semantic.
        ///
        /// @param source source semantic
        ///
        /// @return route builder
        ///
        public RouteBuilder from(SLSlotSemantic source) {
            return new RouteBuilder(this, source);
        }
        
        ///
        /// Builds the immutable quick-move plan.
        ///
        /// @return new quick-move plan
        ///
        public SLQuickMovePlan build() {
            Map<SLSlotSemantic, List<RouteTarget>> frozenTargets = new LinkedHashMap<>();
            for (Map.Entry<SLSlotSemantic, List<RouteTarget>> entry : this.targetsBySource.entrySet()) {
                frozenTargets.put(entry.getKey(), List.copyOf(entry.getValue()));
            }
            return new SLQuickMovePlan(Map.copyOf(frozenTargets));
        }
        
        private Builder addTarget(SLSlotSemantic source, SLSlotSemantic destination, boolean reverse) {
            Objects.requireNonNull(source);
            Objects.requireNonNull(destination);
            this.targetsBySource.computeIfAbsent(source, ignored -> new ArrayList<>()).add(new RouteTarget(destination, reverse));
            return this;
        }
    }
    
    ///
    /// Builder for a single source route.
    ///
    public static final class RouteBuilder {
        
        private final Builder parent;
        private final SLSlotSemantic source;
        
        private RouteBuilder(Builder parent, SLSlotSemantic source) {
            this.parent = parent;
            this.source = source;
        }
        
        ///
        /// Adds a forward-order destination.
        ///
        /// @param destination destination semantic
        ///
        /// @return this route builder
        ///
        public RouteBuilder to(SLSlotSemantic destination) {
            this.parent.addTarget(this.source, destination, false);
            return this;
        }
        
        ///
        /// Adds a reverse-order destination.
        ///
        /// @param destination destination semantic
        ///
        /// @return this route builder
        ///
        public RouteBuilder toReverse(SLSlotSemantic destination) {
            this.parent.addTarget(this.source, destination, true);
            return this;
        }
        
        ///
        /// Returns to the parent plan builder.
        ///
        /// @return parent builder
        ///
        public Builder end() {
            return this.parent;
        }
    }
}
