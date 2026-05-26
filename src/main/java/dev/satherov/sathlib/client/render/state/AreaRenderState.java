package dev.satherov.sathlib.client.render.state;

import lombok.Data;
import lombok.EqualsAndHashCode;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

@Data
@EqualsAndHashCode(callSuper = true)
public class AreaRenderState extends BlockEntityRenderState {
    private Boolean visible;
    private BoundingBox area;
}
