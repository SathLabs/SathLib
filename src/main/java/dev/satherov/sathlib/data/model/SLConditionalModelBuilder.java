package dev.satherov.sathlib.data.model;

import dev.satherov.sathlib.client.model.conditional.ConditionalBlockModel;
import dev.satherov.sathlib.client.model.conditional.ConditionalPredicate;
import dev.satherov.sathlib.client.model.conditional.ConditionalRules;
import dev.satherov.sathlib.client.model.data.SLModelProperty;
import dev.satherov.sathlib.client.model.data.SLModelPropertyValue;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;

///
/// Builder for conditional models that dispatch to child blockstate models based on predicates.
///
@NothingNull
public final class SLConditionalModelBuilder extends CustomBlockStateModelBuilder {
    
    private final BlockStateModel.Unbaked fallback;
    private final List<ConditionalBlockModel.UnbakedCase> cases;
    
    private SLConditionalModelBuilder(final BlockStateModel.Unbaked fallback, final List<ConditionalBlockModel.UnbakedCase> cases) {
        this.fallback = fallback;
        this.cases = List.copyOf(cases);
    }
    
    ///
    /// Starts a new conditional model builder with a fallback model variant.
    ///
    /// @param variant fallback model variant
    ///
    /// @return conditional model builder
    ///
    public static SLConditionalModelBuilder conditional(final Variant variant) {
        return new SLConditionalModelBuilder(new SingleVariant.Unbaked(variant), List.of());
    }
    
    ///
    /// Starts a new conditional model builder with a fallback model from an identifier.
    ///
    /// @param model fallback model identifier
    ///
    /// @return conditional model builder
    ///
    public static SLConditionalModelBuilder conditional(final Identifier model) {
        return SLConditionalModelBuilder.conditional(new Variant(model));
    }
    
    ///
    /// Starts a new conditional model builder with a fallback model from an unbaked blockstate model.
    ///
    /// @param fallback fallback model
    ///
    /// @return conditional model builder
    ///
    public static SLConditionalModelBuilder conditional(final BlockStateModel.Unbaked fallback) {
        return new SLConditionalModelBuilder(fallback, List.of());
    }
    
    ///
    /// Starts a new conditional model builder with a fallback model from a custom blockstate model builder.
    ///
    /// @param fallback fallback model builder
    ///
    /// @return conditional model builder
    ///
    public static SLConditionalModelBuilder conditional(final CustomBlockStateModelBuilder fallback) {
        return SLConditionalModelBuilder.conditional(fallback.toUnbaked());
    }
    
    ///
    /// Mutates a variant using a variant mutator.
    ///
    /// @param model          variant to mutate
    /// @param variantMutator variant mutator
    ///
    /// @return mutated variant
    ///
    private static BlockStateModel.Unbaked mutateVariant(final BlockStateModel.Unbaked model, final VariantMutator variantMutator) {
        if (model instanceof SingleVariant.Unbaked(Variant variant)) {
            return new SingleVariant.Unbaked(variant.with(variantMutator));
        }
        
        return model;
    }
    
    ///
    /// Adds a conditional case to the builder with an identifier for the model the user if the given predicate matches.
    ///
    /// @param predicate predicate to match
    /// @param model     model identifier
    ///
    /// @return updated builder
    ///
    public SLConditionalModelBuilder when(final ConditionalPredicate predicate, final Identifier model) {
        return this.when(predicate, new Variant(model));
    }
    
    ///
    /// Adds a conditional case to the builder with a variant for the model the user if the given predicate matches.
    ///
    /// @param predicate predicate to match
    /// @param model     model variant
    ///
    /// @return updated builder
    ///
    public SLConditionalModelBuilder when(final ConditionalPredicate predicate, final Variant model) {
        return this.when(predicate, new SingleVariant.Unbaked(model));
    }
    
    ///
    /// Adds a conditional case to the builder with a custom blockstate model builder for the model the user if the given predicate matches.
    ///
    /// @param predicate predicate to match
    /// @param model     model builder
    ///
    /// @return updated builder
    ///
    public SLConditionalModelBuilder when(final ConditionalPredicate predicate, final CustomBlockStateModelBuilder model) {
        return this.when(predicate, model.toUnbaked());
    }
    
    ///
    /// Adds a conditional case to the builder with an unbaked blockstate model for the model the user if the given predicate matches.
    ///
    /// @param predicate predicate to match
    /// @param model     model
    ///
    /// @return updated builder
    ///
    public SLConditionalModelBuilder when(final ConditionalPredicate predicate, final BlockStateModel.Unbaked model) {
        final ArrayList<ConditionalBlockModel.UnbakedCase> updatedCases = new ArrayList<>(this.cases);
        updatedCases.add(new ConditionalBlockModel.UnbakedCase(predicate, model));
        return new SLConditionalModelBuilder(this.fallback, updatedCases);
    }
    
    ///
    /// Adds a conditional case to the builder where the given block state properties must match with the model identifier to use in that case.
    ///
    /// @param property property to match
    /// @param value    property value to match
    /// @param model    model identifier
    ///
    /// @return updated builder
    ///
    public <T extends Comparable<T>> SLConditionalModelBuilder whenState(final Property<T> property, final T value, final Identifier model) {
        return this.when(ConditionalRules.state(property, value), model);
    }
    
    ///
    /// Adds a conditional case to the builder where the given block state properties must match with the model variant to use in that case.
    ///
    /// @param property property to match
    /// @param value    property value to match
    /// @param model    model variant
    ///
    /// @return updated builder
    ///
    public <T extends Comparable<T>> SLConditionalModelBuilder whenState(final Property<T> property, final T value, final Variant model) {
        return this.when(ConditionalRules.state(property, value), model);
    }
    
    ///
    /// Adds a conditional case to the builder where the given block state properties must match with the model builder to use in that case.
    ///
    /// @param property property to match
    /// @param value    property value to match
    /// @param model    model builder
    ///
    /// @return updated builder
    ///
    public <T extends Comparable<T>> SLConditionalModelBuilder whenState(final Property<T> property, final T value, final CustomBlockStateModelBuilder model) {
        return this.when(ConditionalRules.state(property, value), model);
    }
    
    ///
    /// Adds a conditional case to the builder where the given block state properties must match with the model to use in that case.
    ///
    /// @param property property to match
    /// @param value    property value to match
    /// @param model    model
    ///
    /// @return updated builder
    ///
    public <T extends Comparable<T>> SLConditionalModelBuilder whenState(final Property<T> property, final T value, final BlockStateModel.Unbaked model) {
        return this.when(ConditionalRules.state(property, value), model);
    }
    
    ///
    /// Adds a conditional case to the builder where the given model property must match with the model identifier to use in that case.
    ///
    /// @param property property to match
    /// @param value    property value to match
    /// @param model    model identifier
    ///
    /// @return updated builder
    ///
    public <T extends SLModelPropertyValue> SLConditionalModelBuilder whenModelProperty(final SLModelProperty<T> property, final T value, final Identifier model) {
        return this.when(ConditionalRules.modelProperty(property, value), model);
    }
    
    ///
    /// Adds a conditional case to the builder where the given model property must match with the model variant to use in that case.
    ///
    /// @param property property to match
    /// @param value    property value to match
    /// @param model    model variant
    ///
    /// @return updated builder
    ///
    public <T extends SLModelPropertyValue> SLConditionalModelBuilder whenModelProperty(final SLModelProperty<T> property, final T value, final Variant model) {
        return this.when(ConditionalRules.modelProperty(property, value), model);
    }
    
    ///
    /// Adds a conditional case to the builder where the given model property must match with the model builder to use in that case.
    ///
    /// @param property property to match
    /// @param value    property value to match
    /// @param model    model builder
    ///
    /// @return updated builder
    ///
    public <T extends SLModelPropertyValue> SLConditionalModelBuilder whenModelProperty(final SLModelProperty<T> property, final T value, final CustomBlockStateModelBuilder model) {
        return this.when(ConditionalRules.modelProperty(property, value), model);
    }
    
    ///
    /// Adds a conditional case to the builder where the given model property must match with the model to use in that case.
    ///
    /// @param property property to match
    /// @param value    property value to match
    /// @param model    model
    ///
    /// @return updated builder
    ///
    public <T extends SLModelPropertyValue> SLConditionalModelBuilder whenModelProperty(final SLModelProperty<T> property, final T value, final BlockStateModel.Unbaked model) {
        return this.when(ConditionalRules.modelProperty(property, value), model);
    }
    
    @Override
    public CustomBlockStateModelBuilder with(final VariantMutator variantMutator) {
        return new SLConditionalModelBuilder(
                SLConditionalModelBuilder.mutateVariant(this.fallback, variantMutator),
                this.cases.stream()
                        .map(candidate -> new ConditionalBlockModel.UnbakedCase(
                                candidate.predicate(),
                                SLConditionalModelBuilder.mutateVariant(candidate.model(), variantMutator)
                        ))
                        .toList()
        );
    }
    
    @Override
    public CustomBlockStateModelBuilder with(final UnbakedMutator variantMutator) {
        return new SLConditionalModelBuilder(
                variantMutator.apply(this.fallback),
                this.cases.stream()
                        .map(candidate -> new ConditionalBlockModel.UnbakedCase(
                                candidate.predicate(),
                                variantMutator.apply(candidate.model())
                        ))
                        .toList()
        );
    }
    
    @Override
    public CustomUnbakedBlockStateModel toUnbaked() {
        return new ConditionalBlockModel.Unbaked(this.fallback, this.cases);
    }
}
