package dev.satherov.sathlib.data.model;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

///
/// Small extension of Mojang's item-model generator helpers.
///
public class SLItemModelGenerators extends ItemModelGenerators {
    
    ///
    /// Creates the generator wrapper.
    ///
    /// @param itemModelOutput item-model output sink
    /// @param modelOutput     model-json output sink
    ///
    public SLItemModelGenerators(ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelInstance> modelOutput) {
        super(itemModelOutput, modelOutput);
    }
}
