package dev.satherov.sathlib.data.model;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SLBlockModelGenerators extends BlockModelGenerators {
    
    public SLBlockModelGenerators(Consumer<BlockModelDefinitionGenerator> blockStateOutput, ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelInstance> modelOutput) {
        super(blockStateOutput, itemModelOutput, modelOutput);
    }
    
    ///
    /// Registers a multi-variant block state generator.
    ///
    /// @param block   block to generate a multi-variant for
    /// @param variant multi-variant to generate
    ///
    public void registerMultiVariant(Supplier<? extends Block> block, MultiVariant variant) {
        this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block.get(), variant));
    }
}
