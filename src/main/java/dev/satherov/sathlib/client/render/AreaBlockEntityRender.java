package dev.satherov.sathlib.client.render;

import lombok.extern.slf4j.Slf4j;

import dev.satherov.sathlib.client.render.state.AreaRenderState;
import dev.satherov.sathlib.common.blockentity.AreaBlockEntity;
import dev.satherov.sathlib.core.annotations.NothingNull;
import dev.satherov.sathlib.util.STRenderUtil;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;

@Slf4j
@NothingNull
public class AreaBlockEntityRender<T extends AreaBlockEntity<T>> implements BlockEntityRenderer<T, AreaRenderState> {
    
    private static final int OUTER_BOX_COLOR = 0xFF000030;
    private static final int OUTER_LINE_COLOR = 0x00FF00FF;
    private static final int INNER_BOX_COLOR = 0x0000FF30;
    private static final int INNER_LINE_COLOR = 0xDDDDDDFF;
    
    public AreaBlockEntityRender(BlockEntityRendererProvider.Context context) { }
    
    @Override
    public AreaRenderState createRenderState() {
        return new AreaRenderState();
    }
    
    @Override
    public AABB getRenderBoundingBox(T entity) {
        return AABB.of(entity.getWorldArea(entity.getBlockPos()));
    }
    
    @Override
    public void extractRenderState(
            T entity,
            AreaRenderState state,
            float partialTicks,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay progress
    ) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, camera, progress);
        state.setVisible(entity.isVisible());
        state.setArea(entity.getArea());
    }
    
    @Override
    public void submit(AreaRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.getVisible()) return;
        
        final BoundingBox box = state.getArea();
        final AABB center = new AABB(BlockPos.ZERO);
        
        collector.submitCustomGeometry(stack, RenderTypes.debugFilledBox(), (pose, buffer) -> STRenderUtil.drawFilledBox(buffer, pose.pose(), box, AreaBlockEntityRender.OUTER_BOX_COLOR));
        collector.submitCustomGeometry(stack, RenderTypes.lines(), (pose, buffer) -> STRenderUtil.drawLineBox(buffer, pose.pose(), box, AreaBlockEntityRender.OUTER_LINE_COLOR, 3f));
        
        if (!box.getLength().equals(Vec3i.ZERO)) {
            collector.submitCustomGeometry(stack, RenderTypes.debugFilledBox(), (pose, buffer) -> STRenderUtil.drawFilledBox(buffer, pose.pose(), center, AreaBlockEntityRender.INNER_BOX_COLOR));
            collector.submitCustomGeometry(stack, RenderTypes.lines(), (pose, buffer) -> STRenderUtil.drawLineBox(buffer, pose.pose(), center, AreaBlockEntityRender.INNER_LINE_COLOR, 3f));
        }
    }
}
