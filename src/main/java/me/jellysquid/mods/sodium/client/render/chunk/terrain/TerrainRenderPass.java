package me.jellysquid.mods.sodium.client.render.chunk.terrain;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK13;

import me.jellysquid.mods.sodium.client.render.shader.ShaderModule;

public class TerrainRenderPass {
    private final VkDevice device;
    private final VkPipeline pipeline;
    private final VkPipelineLayout layout;

    public TerrainRenderPass(VkDevice device) {
        this.device = device;

        this.pipeline = createPipeline(device);
        this.layout = createPipelineLayout(device, pipeline);
    }

    private static VkPipeline createPipeline(VkDevice device) {
        // Define the vertex shader stage
        VkPipelineShaderStageCreateInfo vertexShaderStageInfo = new VkPipelineShaderStageCreateInfo();
        vertexShaderStageInfo.sType = VK13.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
        vertexShaderStageInfo.stage = VK13.VK_SHADER_STAGE_VERTEX_BIT;
        vertexShaderStageInfo.module = ShaderModule.load(device, "shaders/terrain.vert.spv");
        vertexShaderStageInfo.pName = "main";

        // Define the fragment shader stage
        VkPipelineShaderStageCreateInfo fragmentShaderStageInfo = new VkPipelineShaderStageCreateInfo();
        fragmentShaderStageInfo.sType = VK13.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
        fragmentShaderStageInfo.stage = VK13.VK_SHADER_STAGE_FRAGMENT_BIT;
        fragmentShaderStageInfo.module = ShaderModule.load(device, "shaders/terrain.frag.spv");
        fragmentShaderStageInfo.pName = "main";

        // Define the pipeline vertex input state
        VkPipelineVertexInputStateCreateInfo vertexInputStateInfo = new VkPipelineVertexInputStateCreateInfo();
        vertexInputStateInfo.sType = VK13.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
        vertexInputStateInfo.vertexBindingDescriptionCount = 1;
        vertexInputStateInfo.pVertexBindingDescriptions = VertexInputBindingDescription.asByteBuffer().asIntBuffer().get();
        vertexInputStateInfo.vertexAttributeDescriptionCount = VertexAttributeDescriptions.size();
        vertexInputStateInfo.pVertexAttributeDescriptions = VertexAttributeDescriptions.stream()
                .mapToInt(VertexAttributeDescription::toVkVertexInputAttributeDescription)
                .toArray();

        // Define the pipeline input assembly state
        VkPipelineInputAssemblyStateCreateInfo inputAssemblyStateInfo = new VkPipelineInputAssemblyStateCreateInfo();
        inputAssemblyStateInfo.sType = VK13.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
        inputAssemblyStateInfo.topology = VK13.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;

        // Define the pipeline viewport state
        VkPipelineViewportStateCreateInfo viewportStateInfo = new VkPipelineViewportStateCreateInfo();
        viewportStateInfo.sType = VK13.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
        viewportStateInfo.viewportCount = 1;
        viewportStateInfo.pViewports = Viewport.asByteBuffer().asFloatBuffer().get();
        viewportStateInfo.scissorCount = 1;
        viewportStateInfo.pScissors = Scissor.asByteBuffer().asIntBuffer().get();

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
