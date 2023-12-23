package me.jellysquid.mods.sodium.client.render.chunk.terrain;

import net.minecraft.client.render.RenderLayer;

public class TerrainRenderPass {
    @Deprecated(forRemoval = true)
    private final RenderLayer layer;

    private final boolean useReverseOrder;
    private final boolean fragmentDiscard;

    public TerrainRenderPass(RenderLayer layer, boolean useReverseOrder, boolean allowFragmentDiscard) {
        this.layer = layer;

        this.useReverseOrder = useReverseOrder;
        this.fragmentDiscard = allowFragmentDiscard;
    }

    public boolean isReverseOrder() {
        return this.useReverseOrder;
    }

    @Deprecated
    public void startDrawing() {
        this.layer.startDrawing();
    }

    @Deprecated
    public void endDrawing() {
        this.layer.endDrawing();
    }

    public boolean supportsFragmentDiscard() {
        return this.fragmentDiscard;
    }
}
        // Define the pipeline rasterization state
        VkPipelineRasterizationStateCreateInfo rasterizationStateInfo = new VkPipelineRasterizationStateCreateInfo();
        rasterizationStateInfo.sType = VK13.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
        rasterizationStateInfo.depthClampEnable = false;
        rasterizationStateInfo.rasterizerDiscardEnable = false;
        rasterizationStateInfo.polygonMode = VK13.VK_POLYGON_MODE_FILL;
        rasterizationStateInfo.lineWidth = 1.0f;
        rasterizationStateInfo.cullMode = VK13.VK_CULL_MODE_BACK_BIT;
        rasterizationStateInfo.frontFace = VK13.VK_FRONT_FACE_CLOCKWISE;
        rasterizationStateInfo.depthBiasEnable = false
