package ca.artemis.engine.api.vulkan.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

public class ViewportState {

    private int viewportCount = 0;
    private List<Viewport> viewports = new ArrayList<>();
    private int scissorCount = 0;
    private List<Scissor> scissors = new ArrayList<>();

    public VkPipelineViewportStateCreateInfo buildViewportStateCreateInfo(MemoryStack stack) {
        VkViewport.Buffer pViewports = viewports.size() == 0 ? null : VkViewport.calloc(viewports.size(), stack);
        for(int i = 0; i < viewports.size(); i++) {
            pViewports.put(i, viewports.get(i).build(stack));
        }  
        VkRect2D.Buffer pScissors = scissors.size() == 0 ? null : VkRect2D.calloc(scissors.size(), stack);
        for(int i = 0; i < scissors.size(); i++) {
            pScissors.put(i, scissors.get(i).build(stack));
        }

        return VkPipelineViewportStateCreateInfo.calloc(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
            .viewportCount(viewportCount > 0 ? viewportCount : viewports.size())
            .pViewports(pViewports)
            .scissorCount(scissorCount > 0 ? scissorCount : scissors.size())
            .pScissors(pScissors);
    }

    public ViewportState setViewportCount(int viewportCount) {
        this.viewportCount = viewportCount;
        return this;
    }

    public ViewportState addViewport(Viewport viewport) {
        this.viewports.add(viewport);
        return this;
    }

    public ViewportState setScissorCount(int scissorCount) {
        this.scissorCount = scissorCount;
        return this;
    }

    public ViewportState addScissors(Scissor scissor) {
        this.scissors.add(scissor);
        return this;
    }

    public static class Viewport {
        
        private int x;
        private int y;
        private int width;
        private int height;
        private float minDepth;
        private float maxDepth;

        public Viewport(int x, int y, int width, int height, float minDepth, float maxDepth) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.minDepth = minDepth;
            this.maxDepth = maxDepth;
        }

        public VkViewport build(MemoryStack stack) {
            return VkViewport.calloc(stack).set(x, y, width, height, minDepth, maxDepth);
        }
    }

    public static class Scissor {

        private int x;
        private int y;
        private int width;
        private int height;

        public Scissor(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public VkRect2D build(MemoryStack stack) {
            return VkRect2D.calloc(stack).set(VkOffset2D.calloc(stack).set(x, y), VkExtent2D.calloc(stack).set(width, height));
        }
    }
}