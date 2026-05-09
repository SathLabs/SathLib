package dev.satherov.sathlib.data.model;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

public class SLItemModelGenerators extends ItemModelGenerators {
    
    public SLItemModelGenerators(ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelInstance> modelOutput) {
        super(itemModelOutput, modelOutput);
    }
}
