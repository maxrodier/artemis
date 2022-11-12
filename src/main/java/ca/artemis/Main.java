package ca.artemis;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import ca.artemis.Vertex.VertexKind;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.rendering.renderers.SwapchainRenderer;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;

public class Main {

    private static class HelloTriangleApplication {

        private static final int MAX_FRAMES_IN_FLIGHT = 2;

        private static final Vertex[] vertices = {
            new Vertex(new Vector3f(-0.5f, -0.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f)),
            new Vertex(new Vector3f(0.5f, -0.5f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f)),
            new Vertex(new Vector3f(0.5f, 0.5f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f)),
            new Vertex(new Vector3f(-0.5f, 0.5f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f)),
        };

        private static final Integer[] indices = {
            0, 1, 2, 2, 3, 0
        };

        private static final int msaaSamples = VK11.VK_SAMPLE_COUNT_1_BIT;

        private VulkanContext context;
        private SwapchainRenderer swapchainRenderer;
        private Mesh model;
        private long commandPool;
        private List<VulkanBuffer> uniformBuffers; //One per frame in flight
        private long descriptorPool;
        private List<Long> descriptorSets; //One per frame in flight
        private List<CommandBuffer> commandBuffers; //One per frame in flight
        private List<Long> imageAvailableSemaphores; //One per frame in flight
        private List<Long> renderFinishedSemaphores; //One per frame in flight
        private List<Long> inFlightFences; //One per frame in flight

        private int currentFrame = 0;
        private boolean framebufferResized = false;

        public void run() {
            initVulkan();
            mainLoop();
            cleanup();
        }

        private void setFramebufferSizeCallback() {
            GLFW.glfwSetFramebufferSizeCallback(context.getWindow().getHandle(), new GLFWFramebufferSizeCallbackI() {
                @Override
                public void invoke(long window, int width, int height) {
                    framebufferResized = true;
                }
            });
        }

        private void initVulkan() {
            createContext();
            setFramebufferSizeCallback();
            createSwapchainRenderer();

            createCommandPool();
            createModel();
            createUniformBuffers();
            createDescriptorPool();
            createDescriptorSets();
            createCommandBuffers();
            createSyncObjects();
        }

        private void createContext() {
            context = VulkanContext.create();
        }

        private void createSwapchainRenderer() {
            swapchainRenderer = new SwapchainRenderer(context);
        }

        public void createCommandPool() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                List<QueueFamily> queueFamilies = QueueFamily.getQueueFamilies(context.getSurface().getHandle(), context.getPhysicalDevice().getHandle());
        
                VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
                poolInfo.sType(VK11.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
                poolInfo.flags(VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
                poolInfo.queueFamilyIndex(queueFamilies.get(0).getIndex());
        
                LongBuffer pCommandPool = stack.callocLong(1);
                if(VK11.vkCreateCommandPool(context.getDevice().getHandle(), poolInfo, null, pCommandPool) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create graphics command pool!");
                }
                commandPool = pCommandPool.get(0);
            }
        }

        public void createModel() {
            model = new Mesh(context.getDevice().getHandle(), context.getMemoryAllocator().getHandle(), context.getDevice().getGraphicsQueue(), commandPool, vertices, indices, VertexKind.POS_COLOUR);
        }

        public void createUniformBuffers() {
            int bufferSize = UniformBufferObject.BYTES;
    
            uniformBuffers = new ArrayList<>();
    
            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                VulkanBuffer indexBuffer = new VulkanBuffer.Builder()
                    .setLength(1)
                    .setSize(bufferSize)
                    .setBufferUsage(VK11.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
                    .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
                    .build(context.getMemoryAllocator().getHandle());

                uniformBuffers.add(indexBuffer);
            }
        }

        public void createDescriptorPool() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.callocStack(1, stack);
                VkDescriptorPoolSize poolSize;

                poolSize = poolSizes.get(0);
                poolSize.type(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                poolSize.descriptorCount(MAX_FRAMES_IN_FLIGHT);
        
                VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
                poolInfo.sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
                poolInfo.pPoolSizes(poolSizes);
                poolInfo.maxSets(MAX_FRAMES_IN_FLIGHT);
        
                LongBuffer pDescriptorPool = stack.callocLong(1);
                if(VK11.vkCreateDescriptorPool(context.getDevice().getHandle(), poolInfo, null, pDescriptorPool) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create descriptor pool!");
                }
                descriptorPool = pDescriptorPool.get(0);
            }
        }

        public void createDescriptorSets() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer layouts = stack.callocLong(MAX_FRAMES_IN_FLIGHT);
                for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                    layouts.put(i, swapchainRenderer.getSwapchainShaderProgram().getDescriptorSetLayout().getHandle());
                }

                VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
                allocInfo.sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
                allocInfo.descriptorPool(descriptorPool);
                allocInfo.pSetLayouts(layouts);

                LongBuffer pDescriptorSets = stack.callocLong(MAX_FRAMES_IN_FLIGHT);
                if(VK11.vkAllocateDescriptorSets(context.getDevice().getHandle(), allocInfo, pDescriptorSets) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to allocate descriptor sets!");
                }
                descriptorSets = new ArrayList<>();
                for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                    descriptorSets.add(pDescriptorSets.get(i));

                    VkDescriptorBufferInfo.Buffer pBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
                    VkDescriptorBufferInfo bufferInfo = pBufferInfo.get(0);
                    bufferInfo.buffer(uniformBuffers.get(i).getHandle());
                    bufferInfo.offset(0);
                    bufferInfo.range(UniformBufferObject.BYTES);
        
                    VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(1, stack);
                    VkWriteDescriptorSet descriptorWrite;

                    descriptorWrite = descriptorWrites.get(0);
                    descriptorWrite.sType(VK11.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                    descriptorWrite.dstSet(descriptorSets.get(i));
                    descriptorWrite.dstBinding(0);
                    descriptorWrite.dstArrayElement(0);
                    descriptorWrite.descriptorType(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                    descriptorWrite.descriptorCount(1);
                    descriptorWrite.pBufferInfo(pBufferInfo);
        
                    VK11.vkUpdateDescriptorSets(context.getDevice().getHandle(), descriptorWrites, null);
                }
            }
        }

        public void createCommandBuffers() {
            commandBuffers = new ArrayList<>();
            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                commandBuffers.add(new PrimaryCommandBuffer(context.getDevice().getHandle(), commandPool));
            }
        }

        public void createSyncObjects() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
                semaphoreInfo.sType(VK11.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
        
                VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
                fenceInfo.sType(VK11.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
                fenceInfo.flags(VK11.VK_FENCE_CREATE_SIGNALED_BIT);

                imageAvailableSemaphores = new ArrayList<>();
                renderFinishedSemaphores = new ArrayList<>();
                inFlightFences = new ArrayList<>();

                for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                    boolean error = false;
                    
                    LongBuffer pImageAvailableSemaphores = stack.callocLong(1);
                    error = (VK11.vkCreateSemaphore(context.getDevice().getHandle(), semaphoreInfo, null, pImageAvailableSemaphores) != VK11.VK_SUCCESS) && error;
                    imageAvailableSemaphores.add(pImageAvailableSemaphores.get(0));

                    LongBuffer pRenderFinishedSemaphores = stack.callocLong(1);
                    error = (VK11.vkCreateSemaphore(context.getDevice().getHandle(), semaphoreInfo, null, pRenderFinishedSemaphores) != VK11.VK_SUCCESS) && error;
                    renderFinishedSemaphores.add(pRenderFinishedSemaphores.get(0));

                    LongBuffer pInFlightFences = stack.callocLong(1);
                    error = (VK11.vkCreateFence(context.getDevice().getHandle(), fenceInfo, null, pInFlightFences) != VK11.VK_SUCCESS) && error;
                    inFlightFences.add(pInFlightFences.get(0));
                    
                    if(error) {
                        throw new RuntimeException("Failed to create synchronization objects for a frame!");
                    }
                }
            }
        }

        private void mainLoop() {
            while(!GLFW.glfwWindowShouldClose(context.getWindow().getHandle())) {
                GLFW.glfwPollEvents();
                drawFrame();
            }
            VK11.vkDeviceWaitIdle(context.getDevice().getHandle());
        }

        private void drawFrame() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer pFence = stack.callocLong(1);
                pFence.put(0, inFlightFences.get(currentFrame));
                VK11.vkWaitForFences(context.getDevice().getHandle(), pFence, true, Long.MAX_VALUE);

                IntBuffer pImageIndex = stack.callocInt(1);
                int result = KHRSwapchain.vkAcquireNextImageKHR(context.getDevice().getHandle(), swapchainRenderer.getSwapchain().getHandle(), Long.MAX_VALUE, imageAvailableSemaphores.get(currentFrame), VK11.VK_NULL_HANDLE, pImageIndex);
                if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                    recreateSwapchain();
                    return;
                } else if(result != VK11.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                    throw new RuntimeException("Failed to acquire swapchain image!");
                }
                int imageIndex = pImageIndex.get(0);

                updateUniformBuffer(currentFrame, stack);

                VK11.vkResetFences(context.getDevice().getHandle(), pFence);

                VK11.vkResetCommandBuffer(commandBuffers.get(currentFrame).commandBuffer, 0);
                recordCommandBuffer(commandBuffers.get(currentFrame).commandBuffer, imageIndex, stack);

                VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
                submitInfo.sType(VK11.VK_STRUCTURE_TYPE_SUBMIT_INFO);

                LongBuffer pWaitSemaphores = stack.callocLong(1);
                pWaitSemaphores.put(0, imageAvailableSemaphores.get(currentFrame));

                IntBuffer pWaitDstStageMask = stack.callocInt(1);
                pWaitDstStageMask.put(0, VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

                submitInfo.waitSemaphoreCount(1);
                submitInfo.pWaitSemaphores(pWaitSemaphores);
                submitInfo.pWaitDstStageMask(pWaitDstStageMask);

                PointerBuffer pCommandBuffers = stack.callocPointer(1);
                pCommandBuffers.put(0, commandBuffers.get(currentFrame).commandBuffer);

                submitInfo.pCommandBuffers(pCommandBuffers);

                LongBuffer pSignalSemaphores = stack.callocLong(1);
                pSignalSemaphores.put(0, renderFinishedSemaphores.get(currentFrame));
        
                submitInfo.pSignalSemaphores(pSignalSemaphores);

                if(VK11.vkQueueSubmit(context.getDevice().getGraphicsQueue(), submitInfo, inFlightFences.get(currentFrame)) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("Failed to submit draw command buffer!");
                }

                VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
                presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
                presentInfo.pWaitSemaphores(pSignalSemaphores);

                LongBuffer pSwapChains = stack.callocLong(1);
                pSwapChains.put(0, swapchainRenderer.getSwapchain().getHandle());

                presentInfo.swapchainCount(1);
                presentInfo.pSwapchains(pSwapChains);
                presentInfo.pImageIndices(pImageIndex);

                result = KHRSwapchain.vkQueuePresentKHR(context.getDevice().getGraphicsQueue(), presentInfo);
                if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR || framebufferResized) {
                    framebufferResized = false;
                    recreateSwapchain();
                } else if (result != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to present swap chain image!");
                }

                currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
            }
        }

        public void updateUniformBuffer(int imageIndex, MemoryStack stack) {
            Mat4 model = new Mat4(1.0f).rotate((float)Math.toRadians(0.0f), new Vec3(0.0f, 0.0f, 1.0f));
            Mat4 view = new Mat4(1.0f).lookAt(new Vec3(2.0f, 2.0f, 2.0f), new Vec3(0.0f, 0.0f, 0.0f), new Vec3(0.0f, 0.0f, 1.0f));
            Mat4 proj = new Mat4(1.0f).perspective((float) Math.toRadians(45.0f), swapchainRenderer.getSurfaceSupportDetails().getSurfaceExtent().width() / (float) swapchainRenderer.getSurfaceSupportDetails().getSurfaceExtent().height(), 0.1f, 10.0f);
            proj.set(1, 1, proj.m11 * -1);

            PointerBuffer ppData = stack.callocPointer(1);
            Vma.vmaMapMemory(context.getMemoryAllocator().getHandle(), uniformBuffers.get(imageIndex).getAllocationHandle(), ppData);
            FloatBuffer data = ppData.getFloatBuffer(0, 48);
            model.toDfb(data, 0);
            view.toDfb(data, 16);
            proj.toDfb(data, 32);
            Vma.vmaUnmapMemory(context.getMemoryAllocator().getHandle(), uniformBuffers.get(imageIndex).getAllocationHandle());
        }

        public void recordCommandBuffer(VkCommandBuffer commandBuffer, int imageIndex, MemoryStack stack) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
    
            if(VK11.vkBeginCommandBuffer(commandBuffer, beginInfo) != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to begin recording command buffer!");
            }
    
            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK11.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(swapchainRenderer.getRenderPass().getHandle());
            renderPassInfo.framebuffer(swapchainRenderer.getSwapchain().getFramebuffer(imageIndex).getHandle());
            renderPassInfo.renderArea().offset().x(0);
            renderPassInfo.renderArea().offset().y(0);
            renderPassInfo.renderArea().extent(swapchainRenderer.getSurfaceSupportDetails().getSurfaceExtent());
    
            VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
            clearValues.get(0).color().float32(0, 0.0f);
            clearValues.get(0).color().float32(1, 0.0f);
            clearValues.get(0).color().float32(2, 0.0f);
            clearValues.get(0).color().float32(3, 1.0f);
            
            renderPassInfo.pClearValues(clearValues);
    
            VK11.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK11.VK_SUBPASS_CONTENTS_INLINE);
    
                VK11.vkCmdBindPipeline(commandBuffer, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchainRenderer.getSwapchainShaderProgram().getGraphicsPipeline().getHandle());
    
                VkViewport.Buffer viewports = VkViewport.callocStack(1, stack);
                VkViewport viewport = viewports.get(0);
                viewport.x(0.0f);
                viewport.y(0.0f);
                viewport.width((float) swapchainRenderer.getSurfaceSupportDetails().getSurfaceExtent().width());
                viewport.height((float) swapchainRenderer.getSurfaceSupportDetails().getSurfaceExtent().height());
                viewport.minDepth(0.0f);
                viewport.maxDepth(1.0f);
                VK11.vkCmdSetViewport(commandBuffer, 0, viewports);
    
                VkOffset2D offset = VkOffset2D.callocStack(stack);
                offset.x(0);
                offset.y(0);

                VkRect2D.Buffer scissors = VkRect2D.callocStack(1, stack);
                VkRect2D scissor = scissors.get(0);
                scissor.offset(offset);
                scissor.extent(swapchainRenderer.getSurfaceSupportDetails().getSurfaceExtent());
                VK11.vkCmdSetScissor(commandBuffer, 0, scissors);

                LongBuffer pBuffers = stack.callocLong(1);
                pBuffers.put(0, model.getVertexBuffer().getHandle());
                LongBuffer pOffsets = stack.callocLong(1);
                pOffsets.put(0, 0L);
                VK11.vkCmdBindVertexBuffers(commandBuffer, 0, pBuffers, pOffsets);

                VK11.vkCmdBindIndexBuffer(commandBuffer, model.getIndexBuffer().getHandle(), 0, VK11.VK_INDEX_TYPE_UINT32);

                LongBuffer pDescriptorSets = stack.callocLong(1);
                pDescriptorSets.put(0, descriptorSets.get(currentFrame));
                VK11.vkCmdBindDescriptorSets(commandBuffer, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchainRenderer.getSwapchainShaderProgram().getGraphicsPipeline().getPipelineLayout(), 0, pDescriptorSets, null);

                VK11.vkCmdDrawIndexed(commandBuffer, indices.length, 1, 0, 0, 0);
    
            VK11.vkCmdEndRenderPass(commandBuffer);
    
            if(VK11.vkEndCommandBuffer(commandBuffer) != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to record command buffer!");
            }
        }

        public void recreateSwapchain() {
            //Code to wait recreation of the swapchain while minimized //Can this pause a game also? //This has not been tested
            try(MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer width = stack.callocInt(1), height = stack.callocInt(1);
                GLFW.glfwGetFramebufferSize(context.getWindow().getHandle(), width, height);
                while (width.get(0) == 0 || height.get(0) == 0) {
                    System.out.println("Is minimized");
                    width.clear(); height.clear();
                    GLFW.glfwGetFramebufferSize(context.getWindow().getHandle(), width, height);
                    GLFW.glfwWaitEvents();
                }
            }

            VK11.vkDeviceWaitIdle(context.getDevice().getHandle());

            swapchainRenderer.regenerateRenderer(context);
        }

        private void cleanup() {
            swapchainRenderer.destroy(context.getDevice());
            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                VK11.vkDestroySemaphore(context.getDevice().getHandle(), imageAvailableSemaphores.get(i), null);
                VK11.vkDestroySemaphore(context.getDevice().getHandle(), renderFinishedSemaphores.get(i), null);
                VK11.vkDestroyFence(context.getDevice().getHandle(), inFlightFences.get(i), null);
                uniformBuffers.get(i).destroy(context.getMemoryAllocator().getHandle());
            }
            model.destroy(context.getMemoryAllocator().getHandle());
            VK11.vkDestroyDescriptorPool(context.getDevice().getHandle(), descriptorPool, null);
            VK11.vkDestroyCommandPool(context.getDevice().getHandle(), commandPool, null);
            context.destroy();
        }
    }

    public static void main(String[] args) {
        HelloTriangleApplication app = new HelloTriangleApplication();

        try {
            app.run();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}