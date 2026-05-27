package dev.satherov.sathlib.client.screen.view;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

///
/// Read-only client-facing view of one fluid tank.
///
/// Screen nodes use this interface so they can bind either to NeoForge fluid
/// handlers directly or to plain synced menu values exposed through suppliers.
///
public interface SLFluidTankView {
    
    ///
    /// Creates a view backed by a {@link ResourceHandler} tank index.
    ///
    /// @param fluidHandler backing fluid handler
    /// @param tankIndex    backing tank index
    ///
    /// @return new fluid-tank view
    ///
    static SLFluidTankView of(ResourceHandler<FluidResource> fluidHandler, int tankIndex) {
        Objects.requireNonNull(fluidHandler);
        return new SLFluidTankView() {
            @Override
            public FluidStack stack() {
                return FluidUtil.getStack(fluidHandler, tankIndex);
            }
            
            @Override
            public long capacity() {
                FluidResource resource = fluidHandler.getResource(tankIndex);
                FluidResource capacityResource = resource.isEmpty() ? FluidResource.EMPTY : resource;
                return fluidHandler.getCapacityAsLong(tankIndex, capacityResource);
            }
        };
    }
    
    ///
    /// Creates a view backed by arbitrary stack and capacity suppliers.
    ///
    /// @param stackSupplier    current-stack supplier
    /// @param capacitySupplier capacity supplier
    ///
    /// @return new fluid-tank view
    ///
    static SLFluidTankView of(Supplier<FluidStack> stackSupplier, LongSupplier capacitySupplier) {
        Objects.requireNonNull(stackSupplier);
        Objects.requireNonNull(capacitySupplier);
        return new SLFluidTankView() {
            @Override
            public FluidStack stack() {
                FluidStack stack = stackSupplier.get();
                return stack == null ? FluidStack.EMPTY : stack.copy();
            }
            
            @Override
            public long capacity() {
                return capacitySupplier.getAsLong();
            }
        };
    }
    
    ///
    /// Returns the fluid contents currently stored in the tank.
    ///
    /// Implementations should return a defensive copy when the backing API
    /// exposes mutable stacks.
    ///
    /// @return current tank contents
    ///
    FluidStack stack();
    
    ///
    /// Returns the total tank capacity.
    ///
    /// @return tank capacity
    ///
    long capacity();
    
    ///
    /// Returns the currently stored amount.
    ///
    /// @return stored fluid amount
    ///
    default long amount() {
        return this.stack().getAmount();
    }
    
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
