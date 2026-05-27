package dev.satherov.sathlib.common.menu.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

///
/// Declarative quick-move routing for a menu.
///
/// A plan maps one source slot key to one or more destination keys in order.
/// The menu base uses that route table to implement shared shift-click
/// transfers without exposing raw slot indices.
///
public final class SLQuickMovePlan {
    
    private final Map<SLSlotKey, List<RouteTarget>> targetsBySource;
    
    private SLQuickMovePlan(Map<SLSlotKey, List<RouteTarget>> targetsBySource) {
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
    /// Returns the ordered destination targets for the given source key.
    ///
    /// @param source source slot key
    ///
    /// @return immutable ordered route target list
    ///
    public List<RouteTarget> targetsFor(SLSlotKey source) {
        return this.targetsBySource.getOrDefault(source, List.of());
    }
    
    ///
    /// One destination entry in a quick-move route.
    ///
    /// @param target  destination slot key
    /// @param reverse whether destination slots should be tried in reverse order
    ///
    public record RouteTarget(SLSlotKey target, boolean reverse) { }
    
    ///
    /// Builder for quick-move plans.
    ///
    public static final class Builder {
        
        private final Map<SLSlotKey, List<RouteTarget>> targetsBySource = new LinkedHashMap<>();
        
        private Builder() { }
        
        ///
        /// Adds forward-order source-to-target pairs.
        ///
        /// @param source  source slot key
        /// @param targets ordered destination keys
        ///
        /// @return this builder
        ///
        public Builder fromTo(SLSlotKey source, SLSlotKey... targets) {
            return this.fromTo(source, false, targets);
        }
        
        ///
        /// Adds reverse-order source-to-target pairs.
        ///
        /// @param source  source slot key
        /// @param targets ordered destination keys
        ///
        /// @return this builder
        ///
        public Builder fromToReverse(SLSlotKey source, SLSlotKey... targets) {
            return this.fromTo(source, true, targets);
        }
        
        ///
        /// Starts a route chain for one source key.
        ///
        /// @param source source slot key
        ///
        /// @return new route builder
        ///
        public RouteBuilder from(SLSlotKey source) {
            return new RouteBuilder(this, source);
        }
        
        ///
        /// Builds the immutable quick-move plan.
        ///
        /// @return built quick-move plan
        ///
        public SLQuickMovePlan build() {
            Map<SLSlotKey, List<RouteTarget>> frozenTargets = new LinkedHashMap<>();
            for (Map.Entry<SLSlotKey, List<RouteTarget>> entry : this.targetsBySource.entrySet()) {
                frozenTargets.put(entry.getKey(), List.copyOf(entry.getValue()));
            }
            return new SLQuickMovePlan(Map.copyOf(frozenTargets));
        }
        
        ///
        /// Adds one route target for one source key.
        ///
        /// @param source      source slot key
        /// @param destination destination slot key
        /// @param reverse     whether the destination order should be reversed
        ///
        /// @return this builder
        ///
        private Builder addTarget(SLSlotKey source, SLSlotKey destination, boolean reverse) {
            Objects.requireNonNull(source);
            Objects.requireNonNull(destination);
            this.targetsBySource.computeIfAbsent(source, ignored -> new ArrayList<>()).add(new RouteTarget(destination, reverse));
            return this;
        }
        
        ///
        /// Adds a batch of source-to-target pairs that all share one direction.
        ///
        /// @param source  source slot key
        /// @param reverse whether every destination should use reverse order
        /// @param targets ordered destination keys
        ///
        /// @return this builder
        ///
        private Builder fromTo(SLSlotKey source, boolean reverse, SLSlotKey... targets) {
            Objects.requireNonNull(source);
            Objects.requireNonNull(targets);
            for (SLSlotKey target : targets) {
                this.addTarget(source, target, reverse);
            }
            return this;
        }
    }
    
    ///
    /// Builder for one source route chain.
    ///
    public static final class RouteBuilder {
        
        private final Builder parent;
        private final SLSlotKey source;
        
        private RouteBuilder(Builder parent, SLSlotKey source) {
            this.parent = parent;
            this.source = Objects.requireNonNull(source);
        }
        
        ///
        /// Adds one forward-order destination.
        ///
        /// @param destination destination slot key
        ///
        /// @return this route builder
        ///
        public RouteBuilder to(SLSlotKey destination) {
            this.parent.addTarget(this.source, destination, false);
            return this;
        }
        
        ///
        /// Adds one reverse-order destination.
        ///
        /// @param destination destination slot key
        ///
        /// @return this route builder
        ///
        public RouteBuilder toReverse(SLSlotKey destination) {
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
