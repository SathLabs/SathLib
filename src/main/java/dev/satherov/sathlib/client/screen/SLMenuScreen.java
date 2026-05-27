package dev.satherov.sathlib.client.screen;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.node.SLTooltipProvider;
import dev.satherov.sathlib.client.screen.node.UIContainerNode;
import dev.satherov.sathlib.client.screen.node.UINode;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.slot.SLResolvedSlot;
import dev.satherov.sathlib.client.screen.slot.SLSlotLayoutNode;
import dev.satherov.sathlib.client.screen.style.DefaultTheme;
import dev.satherov.sathlib.client.screen.style.UITheme;
import dev.satherov.sathlib.common.menu.SLMenu;
import dev.satherov.sathlib.common.menu.slot.SLDisplaySlot;
import dev.satherov.sathlib.common.menu.slot.SLSlotRenderData;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.joml.Vector2i;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

///
/// Base retained-mode menu screen for all menus.
///
/// Subclasses implement {@link #create()} and can override
/// {@link #createTheme()} or {@link #createViewport()} when a different layout
/// region is required.
///
/// @param <M> backing menu type
///
public abstract class SLMenuScreen<M extends SLMenu> extends AbstractContainerScreen<M> {
    
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");
    private static final int QUICKDROP_DELAY = 500;
    private static final float SNAPBACK_SPEED = 100.0F;
    
    private final UIRoot root = new UIRoot();
    private final Map<Slot, SLResolvedSlot> resolvedSlots = new IdentityHashMap<>();
    private final List<ItemSlotMouseAction> itemSlotMouseActions = new ArrayList<>();
    
    private @Nullable Slot clickedSlot;
    private @Nullable Slot quickdropSlot;
    private @Nullable Slot lastClickSlot;
    private @Nullable SnapbackData snapbackData;
    
    private boolean isSplittingStack;
    private ItemStack draggingItem = ItemStack.EMPTY;
    private long quickdropTime;
    private int quickCraftingType;
    private int quickCraftingButton;
    private boolean skipNextRelease = true;
    private int quickCraftingRemainder;
    private boolean doubleClick;
    private ItemStack lastQuickMoved = ItemStack.EMPTY;
    
    ///
    /// Creates a menu screen with vanilla-sized bounds.
    ///
    /// @param menu      backing menu
    /// @param inventory player inventory
    /// @param title     screen title
    ///
    protected SLMenuScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
    
    ///
    /// Creates a menu screen with explicit background dimensions.
    ///
    /// @param menu        backing menu
    /// @param inventory   player inventory
    /// @param title       screen title
    /// @param imageWidth  background width
    /// @param imageHeight background height
    ///
    protected SLMenuScreen(M menu, Inventory inventory, Component title, int imageWidth, int imageHeight) {
        super(menu, inventory, title, imageWidth, imageHeight);
    }
    
    ///
    /// Returns the owned UI root for advanced screens.
    ///
    /// @return retained-mode UI root
    ///
    protected final UIRoot root() {
        return this.root;
    }
    
    ///
    /// Builds the retained-mode node tree for this menu screen.
    ///
    /// @return new root node
    ///
    protected abstract UINode<?> create();
    
    ///
    /// Returns the viewport used to lay out the UI tree.
    ///
    /// @return menu-area viewport
    ///
    protected SLBounds createViewport() {
        return new SLBounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }
    
    ///
    /// Returns the theme used by built-in retained-mode widgets and slot frames.
    ///
    /// @return active theme
    ///
    protected UITheme createTheme() {
        return DefaultTheme.INSTANCE;
    }
    
    @Override
    protected void addItemSlotMouseAction(@NonNull ItemSlotMouseAction action) {
        super.addItemSlotMouseAction(action);
        this.itemSlotMouseActions.add(action);
    }
    
    @Override
    protected void init() {
        this.itemSlotMouseActions.clear();
        super.init();
        this.rebuild();
    }
    
    @Override
    protected void containerTick() {
        super.containerTick();
        this.root.tick();
    }
    
    @Override
    public void removed() {
        super.removed();
        this.root.setContent(null);
        this.resolvedSlots.clear();
    }
    
    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractContents(graphics, mouseX, mouseY, partialTick);
        this.extractCarriedItem(graphics, mouseX, mouseY);
        this.extractSnapbackItem(graphics);
        this.extractTooltip(graphics, mouseX, mouseY);
    }
    
    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        this.prepareLayout();
    }
    
    @Override
    public void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.prepareLayout();
        
        Slot previousHoveredSlot = this.hoveredSlot;
        this.hoveredSlot = this.getHoveredSlot(mouseX, mouseY);
        
        SLRenderContext renderContext = this.createRenderContext(graphics, partialTick, mouseX, mouseY);
        this.extractSlotHighlightBack(renderContext);
        this.extractSlots(renderContext);
        this.extractSlotHighlightFront(renderContext);
        
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.leftPos, this.topPos);
        this.extractLabels(graphics, mouseX, mouseY);
        graphics.pose().popMatrix();
        
        if (previousHoveredSlot != null && previousHoveredSlot != this.hoveredSlot) {
            this.onStopHovering(previousHoveredSlot);
        }
        
        this.root.render(graphics, this.font, mouseX, mouseY, partialTick);
    }
    
    @Override
    public void extractCarriedItem(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        ItemStack carried = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
        if (carried.isEmpty()) return;
        
        int yOffset = this.draggingItem.isEmpty() ? 8 : 16;
        String itemCount = null;
        if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
            carried = carried.copyWithCount(Mth.ceil(carried.getCount() / 2.0F));
        } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
            carried = carried.copyWithCount(this.quickCraftingRemainder);
            if (carried.isEmpty()) {
                itemCount = ChatFormatting.YELLOW + "0";
            }
        }
        
        SLRenderContext renderContext = this.createRenderContext(graphics, 0.0F, mouseX, mouseY);
        renderContext.nextStratum();
        this.extractFloatingItem(renderContext, carried, mouseX - 8, mouseY - yOffset, itemCount);
    }
    
    @Override
    public void extractSnapbackItem(@NonNull GuiGraphicsExtractor graphics) {
        if (this.snapbackData == null) return;
        
        float snapbackProgress = Mth.clamp((Util.getMillis() - this.snapbackData.time) / SLMenuScreen.SNAPBACK_SPEED, 0.0F, 1.0F);
        int deltaX = this.snapbackData.end.x - this.snapbackData.start.x;
        int deltaY = this.snapbackData.end.y - this.snapbackData.start.y;
        int x = this.snapbackData.start.x + (int) (deltaX * snapbackProgress);
        int y = this.snapbackData.start.y + (int) (deltaY * snapbackProgress);
        
        SLRenderContext renderContext = this.createRenderContext(graphics, 0.0F, x, y);
        renderContext.nextStratum();
        this.extractFloatingItem(renderContext, this.snapbackData.item, x, y, null);
        
        if (snapbackProgress >= 1.0F) {
            this.snapbackData = null;
        }
    }
    
    @Override
    protected void extractTooltip(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot == null) {
            this.extractNodeTooltip(graphics, mouseX, mouseY);
            return;
        }
        
        ItemStack tooltipStack = this.createRenderData(this.hoveredSlot, this.hoveredSlot.getItem()).tooltipStack();
        if (tooltipStack == null || tooltipStack.isEmpty()) {
            this.extractNodeTooltip(graphics, mouseX, mouseY);
            return;
        }
        if (!this.menu.getCarried().isEmpty() && !this.showTooltipWithItemInHand(tooltipStack)) return;
        
        graphics.setTooltipForNextFrame(
                this.font,
                this.getTooltipFromContainerItem(tooltipStack),
                tooltipStack.getTooltipImage(),
                mouseX,
                mouseY,
                tooltipStack.get(DataComponents.TOOLTIP_STYLE)
        );
    }
    
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.prepareLayout();
        
        Slot previousHoveredSlot = this.hoveredSlot;
        this.hoveredSlot = this.getHoveredSlot(mouseX, mouseY);
        if (previousHoveredSlot != null && previousHoveredSlot != this.hoveredSlot) {
            this.onStopHovering(previousHoveredSlot);
        }
        
        this.root.mouseMoved(this.font, mouseX, mouseY);
    }
    
    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        this.prepareLayout();
        if (this.root.mouseClicked(this.font, event, doubleClick)) {
            return true;
        }
        
        boolean cloning = this.minecraft.options.keyPickItem.matchesMouse(event) && Objects.requireNonNull(this.minecraft.player).hasInfiniteMaterials();
        Slot slot = this.getHoveredSlot(event.x(), event.y());
        this.hoveredSlot = slot;
        this.doubleClick = this.lastClickSlot == slot && doubleClick;
        this.skipNextRelease = false;
        
        if (event.button() != 0 && event.button() != 1 && !cloning) {
            this.checkHotbarMouseClicked(event);
        } else {
            boolean clickedOutside = this.hasClickedOutside(event.x(), event.y(), this.leftPos, this.topPos);
            int slotId = slot != null ? slot.index : -1;
            if (clickedOutside) {
                slotId = AbstractContainerMenu.SLOT_CLICKED_OUTSIDE;
            }
            
            if (this.minecraft.options.touchscreen().get() && clickedOutside && this.menu.getCarried().isEmpty()) {
                this.onClose();
                return true;
            }
            
            if (slotId != -1) {
                if (this.minecraft.options.touchscreen().get()) {
                    if (slot != null && slot.hasItem()) {
                        this.clickedSlot = slot;
                        this.draggingItem = ItemStack.EMPTY;
                        this.isSplittingStack = event.button() == 1;
                    } else {
                        this.clickedSlot = null;
                    }
                } else if (!this.isQuickCrafting) {
                    if (this.menu.getCarried().isEmpty()) {
                        if (cloning) {
                            this.slotClicked(slot, slotId, event.button(), ContainerInput.CLONE);
                        } else {
                            boolean quickKey = slotId != AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && event.hasShiftDown();
                            ContainerInput containerInput = ContainerInput.PICKUP;
                            if (quickKey) {
                                //noinspection ConstantValue Slots can in fact be null
                                this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                                containerInput = ContainerInput.QUICK_MOVE;
                            } else if (slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE) {
                                containerInput = ContainerInput.THROW;
                            }
                            
                            this.slotClicked(slot, slotId, event.button(), containerInput);
                        }
                        
                        this.skipNextRelease = true;
                    } else {
                        this.isQuickCrafting = true;
                        this.quickCraftingButton = event.button();
                        this.quickCraftSlots.clear();
                        if (event.button() == 0) {
                            this.quickCraftingType = AbstractContainerMenu.QUICKCRAFT_TYPE_CHARITABLE;
                        } else if (event.button() == 1) {
                            this.quickCraftingType = AbstractContainerMenu.QUICKCRAFT_TYPE_GREEDY;
                        } else if (cloning) {
                            this.quickCraftingType = AbstractContainerMenu.QUICKCRAFT_TYPE_CLONE;
                        }
                    }
                }
            }
        }
        
        this.lastClickSlot = slot;
        return true;
    }
    
    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent event, double deltaX, double deltaY) {
        this.prepareLayout();
        if (this.root.mouseDragged(this.font, event, deltaX, deltaY)) return true;
        
        Slot slot = this.getHoveredSlot(event.x(), event.y());
        this.hoveredSlot = slot;
        ItemStack carried = this.menu.getCarried();
        
        if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
            if (event.button() == 0 || event.button() == 1) {
                if (this.draggingItem.isEmpty()) {
                    if (slot != this.clickedSlot && !this.clickedSlot.getItem().isEmpty()) {
                        this.draggingItem = this.clickedSlot.getItem().copy();
                    }
                } else if (this.draggingItem.getCount() > 1 && slot != null && AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false)) {
                    long time = Util.getMillis();
                    if (this.quickdropSlot == slot) {
                        if (time - this.quickdropTime > SLMenuScreen.QUICKDROP_DELAY) {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ContainerInput.PICKUP);
                            this.slotClicked(slot, slot.index, 1, ContainerInput.PICKUP);
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ContainerInput.PICKUP);
                            this.quickdropTime = time + 750L;
                            this.draggingItem.shrink(1);
                        }
                    } else {
                        this.quickdropSlot = slot;
                        this.quickdropTime = time;
                    }
                }
            }
            
            return true;
        }
        
        if (slot != null && this.shouldAddSlotToQuickCraft(slot, carried) && this.quickCraftSlots.add(slot)) {
            this.recalculateQuickCraftRemaining();
            return true;
        }
        
        return slot != null || !this.menu.getCarried().isEmpty();
    }
    
    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        this.prepareLayout();
        if (this.root.mouseReleased(this.font, event)) return true;
        
        Slot slot = this.getHoveredSlot(event.x(), event.y());
        this.hoveredSlot = slot;
        boolean clickedOutside = this.hasClickedOutside(event.x(), event.y(), this.leftPos, this.topPos);
        int slotId = slot != null ? slot.index : -1;
        if (clickedOutside) {
            slotId = AbstractContainerMenu.SLOT_CLICKED_OUTSIDE;
        }
        
        if (this.doubleClick && slot != null && event.button() == 0 && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, slot)) {
            if (event.hasShiftDown()) {
                if (!this.lastQuickMoved.isEmpty()) {
                    for (Slot target : this.menu.slots) {
                        if (target.mayPickup(Objects.requireNonNull(this.minecraft.player)) && target.hasItem() && target.container == slot.container && AbstractContainerMenu.canItemQuickReplace(target, this.lastQuickMoved, true)) {
                            this.slotClicked(target, target.index, event.button(), ContainerInput.QUICK_MOVE);
                        }
                    }
                }
            } else {
                this.slotClicked(slot, slotId, event.button(), ContainerInput.PICKUP_ALL);
            }
            
            this.doubleClick = false;
        } else {
            if (this.isQuickCrafting && this.quickCraftingButton != event.button()) {
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                this.skipNextRelease = true;
                return true;
            }
            
            if (this.skipNextRelease) {
                this.skipNextRelease = false;
                return true;
            }
            
            if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
                if (event.button() == 0 || event.button() == 1) {
                    if (this.draggingItem.isEmpty() && slot != this.clickedSlot) {
                        this.draggingItem = this.clickedSlot.getItem();
                    }
                    
                    boolean canReplace = AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false);
                    if (slotId != -1 && !this.draggingItem.isEmpty() && canReplace) {
                        this.slotClicked(this.clickedSlot, this.clickedSlot.index, event.button(), ContainerInput.PICKUP);
                        this.slotClicked(slot, slotId, 0, ContainerInput.PICKUP);
                        if (this.menu.getCarried().isEmpty()) {
                            this.snapbackData = null;
                        } else {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, event.button(), ContainerInput.PICKUP);
                            this.snapbackData = new SnapbackData(
                                    this.draggingItem,
                                    new Vector2i((int) event.x(), (int) event.y()),
                                    this.getSnapbackTarget(this.clickedSlot),
                                    Util.getMillis()
                            );
                        }
                    } else if (!this.draggingItem.isEmpty()) {
                        this.snapbackData = new SnapbackData(
                                this.draggingItem,
                                new Vector2i((int) event.x(), (int) event.y()),
                                this.getSnapbackTarget(this.clickedSlot),
                                Util.getMillis()
                        );
                    }
                    
                    this.clearDraggingState();
                }
            } else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
                this.quickCraftToSlots();
            } else if (!this.menu.getCarried().isEmpty()) {
                if (this.minecraft.options.keyPickItem.matchesMouse(event)) {
                    this.slotClicked(slot, slotId, event.button(), ContainerInput.CLONE);
                } else {
                    boolean quickKey = slotId != AbstractContainerMenu.SLOT_CLICKED_OUTSIDE && event.hasShiftDown();
                    if (quickKey) {
                        this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                    }
                    
                    this.slotClicked(slot, slotId, event.button(), quickKey ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP);
                }
            }
        }
        
        this.isQuickCrafting = false;
        this.quickCraftSlots.clear();
        return true;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.prepareLayout();
        if (this.root.mouseScrolled(this.font, mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        
        this.hoveredSlot = this.getHoveredSlot(mouseX, mouseY);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (this.root.keyPressed(event)) return true;
        return super.keyPressed(event);
    }
    
    @Override
    public boolean keyReleased(@NonNull KeyEvent event) {
        if (this.root.keyReleased(event)) return true;
        return super.keyReleased(event);
    }
    
    @Override
    public boolean charTyped(@NonNull CharacterEvent event) {
        if (this.root.charTyped(event)) return true;
        return super.charTyped(event);
    }
    
    @Override
    public void clearDraggingState() {
        this.draggingItem = ItemStack.EMPTY;
        this.clickedSlot = null;
    }
    
    @Override
    protected void slotClicked(@Nullable Slot slot, int slotId, int buttonNum, @NonNull ContainerInput input) {
        //noinspection DataFlowIssue Slots are allowed to be null here
        super.slotClicked(slot, slotId, buttonNum, input);
    }
    
    private void rebuild() {
        this.root.setTheme(this.createTheme());
        this.root.setViewport(this.createViewport());
        this.root.setContent(this.create());
    }
    
    private void prepareLayout() {
        this.root.setViewport(this.createViewport());
        this.root.resolveLayout(this.font);
        this.rebuildSlotBounds();
    }
    
    private void rebuildSlotBounds() {
        this.resolvedSlots.clear();
        UINode<?> content = this.root.getContent();
        if (content == null) return;
        this.collectSlotBounds(content);
    }
    
    private void collectSlotBounds(UINode<?> node) {
        if (!node.isVisible()) return;
        
        if (node instanceof SLSlotLayoutNode slotLayoutNode) {
            slotLayoutNode.collectResolvedSlots(this.resolvedSlots);
        }
        
        if (node instanceof UIContainerNode<?> containerNode) {
            for (UINode<?> child : containerNode.getChildren()) {
                this.collectSlotBounds(child);
            }
        }
    }
    
    private SLRenderContext createRenderContext(GuiGraphicsExtractor graphics, float partialTick, int mouseX, int mouseY) {
        return new SLRenderContext(graphics, this.font, this.root.getTheme(), partialTick, mouseX, mouseY);
    }
    
    private void extractSlotHighlightBack(SLRenderContext context) {
        if (this.hoveredSlot == null || !this.hoveredSlot.isHighlightable()) return;
        
        SLBounds bounds = this.getSlotContentBounds(this.hoveredSlot);
        if (bounds == null) return;
        
        context.blitSprite(SLMenuScreen.SLOT_HIGHLIGHT_BACK_SPRITE, bounds.x() - 4, bounds.y() - 4, 24, 24);
    }
    
    private void extractSlotHighlightFront(SLRenderContext context) {
        if (this.hoveredSlot == null || !this.hoveredSlot.isHighlightable()) return;
        
        SLBounds bounds = this.getSlotContentBounds(this.hoveredSlot);
        if (bounds == null) return;
        
        context.blitSprite(SLMenuScreen.SLOT_HIGHLIGHT_FRONT_SPRITE, bounds.x() - 4, bounds.y() - 4, 24, 24);
    }
    
    private void extractSlots(SLRenderContext context) {
        for (Slot slot : this.menu.slots) {
            if (!slot.isActive() || !this.resolvedSlots.containsKey(slot)) continue;
            this.extractSlot(context, slot);
        }
    }
    
    private void extractSlot(SLRenderContext context, Slot slot) {
        SLResolvedSlot resolvedSlot = this.resolvedSlots.get(slot);
        if (resolvedSlot == null) {
            return;
        }
        
        SLBounds frameBounds = resolvedSlot.frameBounds();
        SLBounds contentBounds = resolvedSlot.contentBounds();
        
        ItemStack itemStack = slot.getItem();
        boolean quickCraftStack = false;
        boolean done = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack carried = this.menu.getCarried();
        String itemCount = null;
        
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !carried.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) return;
            
            if (AbstractContainerMenu.canItemQuickReplace(slot, carried, true) && this.menu.canDragTo(slot)) {
                quickCraftStack = true;
                int maxSize = Math.min(carried.getMaxStackSize(), slot.getMaxStackSize(carried));
                int carry = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                int newCount = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots.size(), this.quickCraftingType, carried) + carry;
                if (newCount > maxSize) {
                    newCount = maxSize;
                    itemCount = ChatFormatting.YELLOW + String.valueOf(maxSize);
                }
                
                itemStack = carried.copyWithCount(newCount);
            } else {
                this.quickCraftSlots.remove(slot);
                this.recalculateQuickCraftRemaining();
            }
        }
        
        SLSlotRenderData renderData = this.createRenderData(slot, itemStack);
        context.theme().renderSlotFrame(context, frameBounds, resolvedSlot.chrome(), slot == this.hoveredSlot, slot.isActive());
        
        if (renderData.displayStack().isEmpty() && slot.isActive()) {
            Identifier icon = renderData.emptyIcon();
            if (icon != null) {
                context.blitSprite(icon, contentBounds.x(), contentBounds.y(), contentBounds.width(), contentBounds.height());
                done = true;
            }
        }
        
        if (done) return;
        if (quickCraftStack) context.fill(contentBounds, 0x80FFFFFF);
        
        int seed = (contentBounds.x() - this.leftPos) + ((contentBounds.y() - this.topPos) * this.imageWidth);
        if (renderData.fakeStack()) {
            context.fakeItem(renderData.displayStack(), contentBounds.x(), contentBounds.y(), seed);
        } else {
            context.item(renderData.displayStack(), contentBounds.x(), contentBounds.y(), seed);
        }
        
        String displayCount = itemCount != null ? itemCount : renderData.itemCountText();
        context.itemDecorations(renderData.displayStack(), contentBounds.x(), contentBounds.y(), displayCount);
    }
    
    private SLSlotRenderData createRenderData(Slot slot, ItemStack stack) {
        if (slot instanceof SLDisplaySlot displaySlot) {
            return displaySlot.createRenderData(stack);
        }
        
        ItemStack tooltipStack = stack.isEmpty() ? ItemStack.EMPTY : stack;
        return new SLSlotRenderData(
                stack,
                tooltipStack.isEmpty() ? null : tooltipStack,
                null,
                slot.isFake(),
                stack.isEmpty() ? slot.getNoItemIcon() : null
        );
    }
    
    ///
    /// Extracts a retained-node tooltip when no slot tooltip is active.
    ///
    private void extractNodeTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        UINode<?> hoveredNode = this.root.getHoveredNode();
        if (!(hoveredNode instanceof SLTooltipProvider tooltipProvider)) return;
        
        List<Component> tooltipLines = tooltipProvider.getTooltipLines();
        if (tooltipLines == null || tooltipLines.isEmpty()) return;
        
        graphics.setTooltipForNextFrame(this.font, tooltipLines, Optional.empty(), mouseX, mouseY, null);
    }
    
    private void extractFloatingItem(SLRenderContext context, ItemStack carried, int x, int y, @Nullable String itemCount) {
        context.graphics().item(carried, x, y);
        context.itemDecorations(carried, x, y - (this.draggingItem.isEmpty() ? 0 : 8), itemCount);
    }
    
    private boolean showTooltipWithItemInHand(ItemStack itemStack) {
        return itemStack.getTooltipImage()
                .map(ClientTooltipComponent::create)
                .map(ClientTooltipComponent::showTooltipWithItemInHand)
                .orElse(false);
    }
    
    private @Nullable Slot getHoveredSlot(double mouseX, double mouseY) {
        for (Slot slot : this.menu.slots) {
            if (slot.isActive() && this.isHovering(slot, mouseX, mouseY)) {
                return slot;
            }
        }
        
        return null;
    }
    
    private boolean isHovering(Slot slot, double mouseX, double mouseY) {
        SLResolvedSlot resolvedSlot = this.resolvedSlots.get(slot);
        if (resolvedSlot == null) return false;
        return resolvedSlot.frameBounds().contains(mouseX, mouseY);
    }
    
    private @Nullable SLBounds getSlotContentBounds(Slot slot) {
        SLResolvedSlot resolvedSlot = this.resolvedSlots.get(slot);
        if (resolvedSlot == null) return null;
        return resolvedSlot.contentBounds();
    }
    
    private Vector2i getSnapbackTarget(Slot slot) {
        SLBounds contentBounds = this.getSlotContentBounds(slot);
        if (contentBounds == null) return new Vector2i(this.leftPos, this.topPos);
        return new Vector2i(contentBounds.x(), contentBounds.y());
    }
    
    private void recalculateQuickCraftRemaining() {
        ItemStack carried = this.menu.getCarried();
        if (carried.isEmpty() || !this.isQuickCrafting) return;
        
        if (this.quickCraftingType == AbstractContainerMenu.QUICKCRAFT_TYPE_CLONE) {
            this.quickCraftingRemainder = carried.getMaxStackSize();
            return;
        }
        
        this.quickCraftingRemainder = carried.getCount();
        for (Slot slot : this.quickCraftSlots) {
            ItemStack slotItemStack = slot.getItem();
            int carry = slotItemStack.isEmpty() ? 0 : slotItemStack.getCount();
            int maxSize = Math.min(carried.getMaxStackSize(), slot.getMaxStackSize(carried));
            int newCount = Math.min(
                    AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots.size(), this.quickCraftingType, carried) + carry,
                    maxSize
            );
            this.quickCraftingRemainder -= newCount - carry;
        }
    }
    
    private void onStopHovering(Slot slot) {
        if (!slot.hasItem()) return;
        
        for (ItemSlotMouseAction itemSlotMouseAction : this.itemSlotMouseActions) {
            if (itemSlotMouseAction.matches(slot)) {
                itemSlotMouseAction.onStopHovering(slot);
            }
        }
    }
    
    private void checkHotbarMouseClicked(MouseButtonEvent event) {
        if (this.hoveredSlot == null || !this.menu.getCarried().isEmpty()) return;
        
        if (this.minecraft.options.keySwapOffhand.matchesMouse(event)) {
            this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ContainerInput.SWAP);
            return;
        }
        
        for (int hotbarIndex = 0; hotbarIndex < 9; hotbarIndex++) {
            if (this.minecraft.options.keyHotbarSlots[hotbarIndex].matchesMouse(event)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, hotbarIndex, ContainerInput.SWAP);
            }
        }
    }
    
    private boolean shouldAddSlotToQuickCraft(Slot slot, ItemStack carried) {
        return this.isQuickCrafting
                && !carried.isEmpty()
                && (carried.getCount() > this.quickCraftSlots.size() || this.quickCraftingType == AbstractContainerMenu.QUICKCRAFT_TYPE_CLONE)
                && AbstractContainerMenu.canItemQuickReplace(slot, carried, true)
                && slot.mayPlace(carried)
                && this.menu.canDragTo(slot);
    }
    
    private void quickCraftToSlots() {
        this.slotClicked(null, AbstractContainerMenu.SLOT_CLICKED_OUTSIDE, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ContainerInput.QUICK_CRAFT);
        for (Slot slot : this.quickCraftSlots) {
            this.slotClicked(slot, slot.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ContainerInput.QUICK_CRAFT);
        }
        this.slotClicked(null, AbstractContainerMenu.SLOT_CLICKED_OUTSIDE, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ContainerInput.QUICK_CRAFT);
    }
    
    ///
    /// Simple snapback animation state for touch interactions.
    ///
    /// @param item  animated stack
    /// @param start animation start position
    /// @param end   animation end position
    /// @param time  animation start time in milliseconds
    ///
    private record SnapbackData(ItemStack item, Vector2i start, Vector2i end, long time) { }
}
