package dev.satherov.sathlib.client.screen.view;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;

import java.util.Objects;
import java.util.function.LongSupplier;

///
/// Read-only client-facing view of an energy container.
///
/// Screen nodes use this interface so they can bind either to NeoForge energy
/// handlers directly or to plain synced menu values exposed through suppliers.
///
public interface SLEnergyView {
    
    ///
    /// Creates a view backed by a {@link EnergyHandler}.
    ///
    /// @param energyHandler backing energy handler
    ///
    /// @return new energy view
    ///
    static SLEnergyView of(EnergyHandler energyHandler) {
        Objects.requireNonNull(energyHandler);
        return new SLEnergyView() {
            @Override
            public long amount() {
                return energyHandler.getAmountAsLong();
            }
            
            @Override
            public long capacity() {
                return energyHandler.getCapacityAsLong();
            }
        };
    }
    
    ///
    /// Creates a view backed by arbitrary amount and capacity suppliers.
    ///
    /// @param amountSupplier   current-energy supplier
    /// @param capacitySupplier capacity supplier
    ///
    /// @return new energy view
    ///
    static SLEnergyView of(LongSupplier amountSupplier, LongSupplier capacitySupplier) {
        Objects.requireNonNull(amountSupplier);
        Objects.requireNonNull(capacitySupplier);
        return new SLEnergyView() {
            @Override
            public long amount() {
                return amountSupplier.getAsLong();
            }
            
            @Override
            public long capacity() {
                return capacitySupplier.getAsLong();
            }
        };
    }
    
    ///
    /// Returns the currently stored energy amount.
    ///
    /// @return stored energy amount
    ///
    long amount();
    
    ///
    /// Returns the maximum energy capacity.
    ///
    /// @return total energy capacity
    ///
    long capacity();
    
    ///
    /// Returns the normalized fill ratio.
    ///
    /// @return fill ratio in {@code [0, 1]}
    ///
    default float fillRatio() {
        long capacity = this.capacity();
        if (capacity <= 0L) return 0.0F;
        return Math.clamp((float) this.amount() / (float) capacity, 0.0F, 1.0F);
    }
}
