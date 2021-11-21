package ca.artemis.vulkan.api.pipeline;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;

import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.vulkan.api.descriptor.PushConstantRange;
import ca.artemis.vulkan.api.framebuffer.RenderPass;

public class GraphicsPipeline {

    private final long handle;
    private final long pipelineLayout;
    private final ShaderModule[] shaderModules;

    private GraphicsPipeline(long handle, long pipelineLayout, ShaderModule[] shaderModules) {
        this.handle = handle;
        this.pipelineLayout = pipelineLayout;
        this.shaderModules = shaderModules;
    }

    public void destroy() {
        for(ShaderModule shaderModule : shaderModules)
            shaderModule.destroy();
        VK11.vkDestroyPipelineLayout(VulkanContext.getContext().getDevice().getHandle(), pipelineLayout, null);
        VK11.vkDestroyPipeline(VulkanContext.getContext().getDevice().getHandle(), handle, null);
    }

    public long getHandle() {
        return handle;
    }

    public long getPipelineLayout() {
        return pipelineLayout;
    }

    public static class Builder {
        
        private List<ShaderModule> shaderModules = new ArrayList<>();
        private VertexInputState vertexInputState = new VertexInputState();
        private InputAssemblyState inputAssemblyState = new InputAssemblyState();
        private TessellationState tessellationState = new TessellationState();
        private ViewportState viewportState = new ViewportState();
        private RasterizationState rasterizationState = new RasterizationState();
        private MultisampleState multisampleState = new MultisampleState();
        private DepthStencilState depthStencilState = new DepthStencilState();
        private ColorBlendState colorBlendState = new ColorBlendState();
        private List<Integer> dynamicStates = new ArrayList<>();
        private DescriptorSetLayout[] descriptorSetLayouts = {};
        private PushConstantRange[] pushConstantRanges = {};
        private RenderPass renderPass;
        private int subpass;

        private VkPipelineShaderStageCreateInfo.Buffer buildShaderStageCreateInfo(MemoryStack stack) {
            ByteBuffer pMain = stack.UTF8("main");
            VkPipelineShaderStageCreateInfo.Buffer pShaderStages = shaderModules.size() == 0 ? null : VkPipelineShaderStageCreateInfo.callocStack(shaderModules.size(), stack);
            for(int i = 0; i < shaderModules.size(); i++) {
                ShaderModule shaderModule = shaderModules.get(i);
                pShaderStages.get(i)
                    .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(shaderModule.getSpirv().getShaderStageKind().getStage())
                    .module(shaderModule.getHandle())
                    .pName(pMain);
            }

            return pShaderStages;
        }

        public VkPipelineDynamicStateCreateInfo buildDynamicStateCreateInfo(MemoryStack stack) {
            IntBuffer pDynamicStates = dynamicStates.size() == 0 ? null : stack.callocInt(dynamicStates.size());
            for(int i = 0; i < dynamicStates.size(); i++) {
                pDynamicStates.put(i, dynamicStates.get(i));
            }

            return VkPipelineDynamicStateCreateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                .pDynamicStates(pDynamicStates);
        }

        public long createPipelineLayout(MemoryStack stack) {
            LongBuffer pSetLayouts = descriptorSetLayouts.length == 0 ? null : stack.callocLong(descriptorSetLayouts.length);
            for(int i = 0; i < descriptorSetLayouts.length; i++) {
                pSetLayouts.put(i, descriptorSetLayouts[i].getHandle());
            }

            VkPushConstantRange.Buffer pPushConstantRanges = pushConstantRanges.length == 0 ? null : VkPushConstantRange.callocStack(pushConstantRanges.length);
            for(int i = 0; i < pushConstantRanges.length; i++) {
                VkPushConstantRange vkPushConstantRange = VkPushConstantRange.callocStack(stack);
                vkPushConstantRange.stageFlags(pushConstantRanges[i].getStageFlags());
                vkPushConstantRange.offset(pushConstantRanges[i].getOffset());
                vkPushConstantRange.size(pushConstantRanges[i].getSize());
                pPushConstantRanges.put(i, vkPushConstantRange);
            }

            VkPipelineLayoutCreateInfo pPipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                .pSetLayouts(pSetLayouts)
                .pPushConstantRanges(pPushConstantRanges);
    
            LongBuffer pPipelineLayout = stack.callocLong(1);
            int error = VK11.vkCreatePipelineLayout(VulkanContext.getContext().getDevice().getHandle(), pPipelineLayoutCreateInfo, null, pPipelineLayout);
            if(error != VK11.VK_SUCCESS) {
                throw new AssertionError("Failed to create pipeline layout");
            } 
            
            return pPipelineLayout.get(0); 
        }

        public GraphicsPipeline build() {
            try(MemoryStack stack = MemoryStack.stackPush()) {

                long pipelineLayout = createPipelineLayout(stack);
            
                VkGraphicsPipelineCreateInfo.Buffer pCreateInfos = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
                pCreateInfos.get(0)
                    .sType(VK11.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                    .pStages(buildShaderStageCreateInfo(stack))
                    .pVertexInputState(vertexInputState.buildVertexInputStateCreateInfo(stack))
                    .pInputAssemblyState(inputAssemblyState.buildInputAssemblyStateCreateInfo(stack))
                    .pTessellationState(tessellationState.buildTessellationStateCreateInfo(stack))
                    .pViewportState(viewportState.buildViewportStateCreateInfo(stack))
                    .pRasterizationState(rasterizationState.buildRasterizationStateCreateInfo(stack))
                    .pMultisampleState(multisampleState.buildMultisampleStateCreateInfo(stack))
                    .pDepthStencilState(depthStencilState.buildDepthStencilStateCreateInfo(stack))
                    .pColorBlendState(colorBlendState.buildColorBlendStateCreateInfo(stack))
                    .pDynamicState(buildDynamicStateCreateInfo(stack))
                    .layout(pipelineLayout)
                    .renderPass(renderPass.getHandle())
                    .subpass(subpass);

                LongBuffer pPipelines = stack.callocLong(1);
                int error = VK11.vkCreateGraphicsPipelines(VulkanContext.getContext().getDevice().getHandle(), VK11.VK_NULL_HANDLE, pCreateInfos, null, pPipelines);
                if(error != VK11.VK_SUCCESS)
                    throw new AssertionError("Failed to create graphics pipeline");

                return new GraphicsPipeline(pPipelines.get(0), pipelineLayout, shaderModules.toArray(new ShaderModule[shaderModules.size()]));
            }
        }

        public Builder addShaderModule(ShaderModule shaderModule) {
            this.shaderModules.add(shaderModule);
            return this;
        }

        public Builder setVertexInputState(VertexInputState vertexInputState) {
            this.vertexInputState = vertexInputState;
            return this;
        }
        
        public Builder setInputAssemblyState(InputAssemblyState inputAssemblyState) {
            this.inputAssemblyState = inputAssemblyState;
            return this;
        }

        public Builder setTessellationState(TessellationState tessellationState) {
            this.tessellationState = tessellationState;
            return this;
        }

        public Builder setViewportState(ViewportState viewportState) {
            this.viewportState = viewportState;
            return this;
        }

        public Builder setRasterizationState(RasterizationState rasterizationState) {
            this.rasterizationState = rasterizationState;
            return this;
        }

        public Builder setDepthStencilState(DepthStencilState depthStencilState) {
            this.depthStencilState = depthStencilState;
            return this;
        }

        public Builder setColorBlendState(ColorBlendState colorBlendState) {
            this.colorBlendState = colorBlendState;
            return this;
        }

        public Builder addDynamicState(int dynamicState) {
            this.dynamicStates.add(dynamicState);
            return this;
        }

        public Builder setDescriptorSetLayouts(DescriptorSetLayout[] descriptorSetLayouts) {
            this.descriptorSetLayouts = descriptorSetLayouts;
            return this;
        }

        public Builder setPushConstantRange(PushConstantRange[] pushConstantRanges) {
            this.pushConstantRanges = pushConstantRanges;
            return this;
        }

        public Builder setRenderPass(RenderPass renderPass) {
            this.renderPass = renderPass;
            return this;
        }

        public Builder setSubpass(int subpass) {
            this.subpass = subpass;
            return this;
        }
    }
}