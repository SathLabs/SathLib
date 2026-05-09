package dev.satherov.sathlib.client.render;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.SathLib;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import net.minecraft.client.renderer.RenderPipelines;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

///
/// Render pipelines register
///
@UtilityClass
public class SLRenderPipelines {
    
    ///
    /// Vertex-only GUI pipeline used by custom retained-mode rendering.
    ///
    public static final RenderPipeline VERTEX_GUI = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(SathLib.id("render_pipeline/vertex_gui"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .build();
    
    ///
    /// Registers all render pipelines to the mod event bus
    ///
    /// @param bus the mod event bus
    ///
    public static void register(final IEventBus bus) {
        bus.addListener(RegisterRenderPipelinesEvent.class, event -> {
            event.registerPipeline(SLRenderPipelines.VERTEX_GUI);
        });
    }
}
