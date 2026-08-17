package arsenide.relix.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import arsenide.relix.Relix;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class RelixRenderPipelines {

    public static final BindGroupLayout DEPTH_SAMPLER = BindGroupLayout.builder()
        .withSampler("DepthSampler")
        .build();
    
    public static final RenderPipeline PHARAOH_SKY =
        RenderPipeline.builder(
            // Start from the globals...
            new RenderPipeline.Snippet[] {
                RenderPipelines.MATRICES_FOG_SNIPPET
            }
        )
        // Add location
        .withLocation(Identifier.fromNamespaceAndPath(Relix.MODID, "pipeline/pharaoh_sky"))
        .withVertexShader(Identifier.fromNamespaceAndPath(Relix.MODID, "pharaoh_sky"))
        .withFragmentShader(Identifier.fromNamespaceAndPath(Relix.MODID, "pharaoh_sky"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withCull(false)
        .build();

    // Second pass pipeline for the lines on the edges
    public static final RenderPipeline PHARAOH_SKY_LINES =
        RenderPipeline.builder(
            new RenderPipeline.Snippet[] {
                RenderPipelines.MATRICES_FOG_SNIPPET
            }
        )
        .withLocation(Identifier.fromNamespaceAndPath(Relix.MODID, "pipeline/pharaoh_sky_lines"))
        .withVertexShader(Identifier.fromNamespaceAndPath(Relix.MODID, "pharaoh_sky_lines"))
        .withFragmentShader(Identifier.fromNamespaceAndPath(Relix.MODID, "pharaoh_sky_lines"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
        .withPrimitiveTopology(PrimitiveTopology.LINES)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withCull(false)
        .build();

    public static final RenderPipeline SANDSTORM =
        RenderPipeline.builder(
            new RenderPipeline.Snippet[] {
                RenderPipelines.MATRICES_FOG_SNIPPET
            }
        )
        .withLocation(Identifier.fromNamespaceAndPath(Relix.MODID, "pipeline/sandstorm"))
        .withVertexShader("core/screenquad")
        .withFragmentShader(Identifier.fromNamespaceAndPath(Relix.MODID, "sandstorm"))
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withBindGroupLayout(DEPTH_SAMPLER)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .build();

    public static final RenderPipeline PHARAOH_DARKEN =
        RenderPipeline.builder(
            new RenderPipeline.Snippet[] {
                RenderPipelines.GLOBALS_SNIPPET
            }
        )
        .withLocation(Identifier.fromNamespaceAndPath(Relix.MODID, "pipeline/pharaoh_darken"))
        .withVertexShader("core/screenquad")
        .withFragmentShader(Identifier.fromNamespaceAndPath(Relix.MODID, "darken_overlay"))
        .withBindGroupLayout(BindGroupLayouts.IN_SAMPLER)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .build();
}
