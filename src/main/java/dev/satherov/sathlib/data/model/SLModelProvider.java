package dev.satherov.sathlib.data.model;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

///
/// Custom implementation of {@link ModelProvider}
///
@NothingNull
@SuppressWarnings("DataFlowIssue")
public abstract class SLModelProvider implements DataProvider {
    
    public final String modId;
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider itemInfoPathProvider;
    private final PackOutput.PathProvider modelPathProvider;
    
    public SLModelProvider(PackOutput output, String modId) {
        this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.itemInfoPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        this.modId = modId;
    }
    
    ///
    /// Registers the models to generate
    ///
    /// @param blockModels block models to register
    /// @param itemModels  item models to register
    ///
    protected abstract void registerModels(SLBlockModelGenerators blockModels, ItemModelGenerators itemModels);
    
    ///
    /// All known blocks to validate for. Defaults to all blocks in the mod namespace.
    ///
    /// @return all known blocks
    ///
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.listElements().filter(holder -> holder.getKey().identifier().getNamespace().equals(this.modId));
    }
    
    ///
    /// All known items to validate for. Defaults to all items in the mod namespace.
    ///
    /// @return all known items
    ///
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return BuiltInRegistries.ITEM.listElements().filter(holder -> holder.getKey().identifier().getNamespace().equals(this.modId));
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        ItemInfoCollector itemModels = new ItemInfoCollector(this::getKnownItems);
        BlockStateGeneratorCollector blockStateGenerators = new BlockStateGeneratorCollector(this::getKnownBlocks);
        SimpleModelCollector simpleModels = new SimpleModelCollector();
        this.registerModels(new SLBlockModelGenerators(blockStateGenerators, itemModels, simpleModels), new SLItemModelGenerators(itemModels, simpleModels));
        blockStateGenerators.validate();
        itemModels.finalizeAndValidate();
        return CompletableFuture.allOf(
                blockStateGenerators.save(cache, this.blockStatePathProvider),
                simpleModels.save(cache, this.modelPathProvider),
                itemModels.save(cache, this.itemInfoPathProvider)
        );
    }
    
    @Override
    public String getName() {
        return "Model Definitions {" + this.modId + "}";
    }
    
    @SuppressWarnings("deprecation")
    private static class BlockStateGeneratorCollector implements Consumer<BlockModelDefinitionGenerator> {
        private final Map<Block, BlockModelDefinitionGenerator> generators = new HashMap<>();
        private final Supplier<Stream<? extends Holder<Block>>> knownBlocks;
        
        public BlockStateGeneratorCollector(Supplier<Stream<? extends Holder<Block>>> knownBlocks) {
            this.knownBlocks = knownBlocks;
        }
        
        public void accept(BlockModelDefinitionGenerator generator) {
            Block block = generator.block();
            BlockModelDefinitionGenerator prev = this.generators.put(block, generator);
            if (prev != null) throw new IllegalStateException("Duplicate blockstate definition for " + block);
        }
        
        public void validate() {
            var holders = this.knownBlocks.get();
            List<Identifier> missingDefinitions = holders.filter(e -> !this.generators.containsKey(e.value())).map(e -> e.unwrapKey().orElseThrow().identifier()).toList();
            if (!missingDefinitions.isEmpty()) throw new IllegalStateException("Missing blockstate definitions for: " + missingDefinitions);
        }
        
        public CompletableFuture<?> save(CachedOutput cache, PackOutput.PathProvider pathProvider) {
            Map<Block, BlockStateModelDispatcher> definitions = Maps.transformValues(this.generators, BlockModelDefinitionGenerator::create);
            Function<Block, Path> pathGetter = block -> pathProvider.json(block.builtInRegistryHolder().key().identifier());
            return DataProvider.saveAll(cache, BlockStateModelDispatcher.CODEC, pathGetter, definitions);
        }
    }
    
    @SuppressWarnings("deprecation")
    private static class ItemInfoCollector implements ItemModelOutput {
        private final Map<Item, ClientItem> itemInfos = new HashMap<>();
        private final Map<Item, Item> copies = new HashMap<>();
        private final Supplier<Stream<? extends Holder<Item>>> knownItems;
        private final Map<Identifier, ClientItem> infos = new HashMap<>();
        
        public ItemInfoCollector(Supplier<Stream<? extends Holder<Item>>> known) {
            this.knownItems = known;
        }
        
        @Override
        public void accept(Item item, ItemModel.Unbaked model, ClientItem.Properties properties) {
            this.register(item, new ClientItem(model, properties));
        }
        
        public void register(Item item, ClientItem itemInfo) {
            ClientItem prev = this.itemInfos.put(item, itemInfo);
            if (prev != null) throw new IllegalStateException("Duplicate item model definition for " + item);
        }
        
        @Override
        public void register(Identifier identifier, ClientItem clientItem) {
            ClientItem existing = this.infos.putIfAbsent(identifier, clientItem);
            if (existing != null) throw new IllegalStateException("Duplicate item model definition for " + identifier);
        }
        
        @Override
        public void copy(Item donor, Item acceptor) {
            this.copies.put(acceptor, donor);
        }
        
        public void finalizeAndValidate() {
            this.knownItems.get().map(Holder::value).forEach(item -> {
                if (!this.copies.containsKey(item)) {
                    if (item instanceof BlockItem blockItem && !this.itemInfos.containsKey(blockItem)) {
                        Identifier targetModel = ModelLocationUtils.getModelLocation(blockItem.getBlock());
                        this.accept(blockItem, ItemModelUtils.plainModel(targetModel));
                    }
                }
            });
            
            this.copies.forEach((acceptor, donor) -> {
                ClientItem donorInfo = this.itemInfos.get(donor);
                if (donorInfo == null) throw new IllegalStateException("Missing donor: " + donor + " -> " + acceptor);
                else this.register(acceptor, donorInfo);
            });
            
            List<Identifier> missing = this.knownItems.get()
                    .filter(e -> !this.itemInfos.containsKey(e.value()))
                    .map(e -> e.unwrapKey().orElseThrow().identifier())
                    .toList();
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing item model definitions for: " + missing);
            }
        }
        
        public CompletableFuture<?> save(CachedOutput cache, PackOutput.PathProvider pathProvider) {
            return CompletableFuture.allOf(
                    DataProvider.saveAll(cache, ClientItem.CODEC, item -> pathProvider.json(item.builtInRegistryHolder().key().identifier()), this.itemInfos),
                    DataProvider.saveAll(cache, ClientItem.CODEC, pathProvider::json, this.infos));
        }
    }
    
    private static class SimpleModelCollector implements BiConsumer<Identifier, ModelInstance> {
        private final Map<Identifier, ModelInstance> models = new HashMap<>();
        
        public void accept(Identifier id, ModelInstance contents) {
            Supplier<JsonElement> prev = this.models.put(id, contents);
            if (prev != null) throw new IllegalStateException("Duplicate model definition for " + id);
        }
        
        public CompletableFuture<?> save(CachedOutput cache, PackOutput.PathProvider pathProvider) {
            return DataProvider.saveAll(cache, Supplier::get, pathProvider::json, this.models);
        }
    }
}
