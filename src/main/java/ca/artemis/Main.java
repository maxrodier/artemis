package ca.artemis;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import ca.artemis.Vertex.VertexKind;
import ca.artemis.core.ResourceManager;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.context.VulkanPhysicalDevice;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.memory.VulkanBuffer;
import ca.artemis.vulkan.api.pipeline.ShaderModule;
import ca.artemis.vulkan.api.pipeline.SharderUtils.ShaderStageKind;
import ca.artemis.vulkan.rendering.RenderingEngine;
import ca.artemis.vulkan.rendering.programs.SwapchainShaderProgram;
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

        private VulkanContext context;
        private RenderingEngine renderingEngine;
        private Mesh model;
        private CommandPool commandPool;
        private List<VulkanBuffer> uniformBuffers; //One per frame in flight
        private List<DescriptorSet> descriptorSets; //One per frame in flight
        private List<SecondaryCommandBuffer> secondaryCommandBuffers;

        private SwapchainShaderProgram swapchainShaderProgram;

        public void run() {
            initVulkan();
            mainLoop();
            cleanup();
        }

        private void setFramebufferSizeCallback() {
            renderingEngine.setFramebufferSizeCallback(context);
        }

        private void initVulkan() {
            createContext();
            renderingEngine = RenderingEngine.createInstance(context);

            ResourceManager.addShaderModule("swapchainVertShaderModule", new ShaderModule(context.getDevice(), "shaders/swapchain.vert", ShaderStageKind.VERTEX_SHADER));
            ResourceManager.addShaderModule("swapchainFragShaderModule", new ShaderModule(context.getDevice(), "shaders/swapchain.frag", ShaderStageKind.FRAGMENT_SHADER));
    
            ResourceManager.addShaderProgram("swapchainShaderProgram", new SwapchainShaderProgram(context.getDevice(), renderingEngine.getSwapchainRenderer()));
            swapchainShaderProgram = ResourceManager.getShaderProgram("swapchainShaderProgram", SwapchainShaderProgram.class);

            setFramebufferSizeCallback();

            createCommandPool();
            createModel();
            createUniformBuffers();
            createDescriptorSets();
            allocateSecondaryCommandBuffers();
        }

        private void createContext() {
            context = VulkanContext.create();
        }

        public void createCommandPool() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                List<VulkanPhysicalDevice.QueueFamily> queueFamilies = context.getPhysicalDevice().getQueueFamilies();
    
                commandPool = new CommandPool(context.getDevice(), queueFamilies.get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
            }
        }

        public void createModel() {
            model = new Mesh(context.getDevice(), context.getMemoryAllocator(), context.getDevice().getGraphicsQueue(), commandPool, vertices, indices, VertexKind.POS_COLOUR);
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
                    .build(context.getMemoryAllocator());

                uniformBuffers.add(indexBuffer);
            }
        }

        public void createDescriptorSets() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                descriptorSets = new ArrayList<>();
                for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {

                    DescriptorSet descriptorSet = new DescriptorSet(context.getDevice(), swapchainShaderProgram.getDescriptorPool(), swapchainShaderProgram.getDescriptorSetLayouts()[0]);

                    descriptorSets.add(descriptorSet);

                    VkDescriptorBufferInfo.Buffer pBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
                    VkDescriptorBufferInfo bufferInfo = pBufferInfo.get(0);
                    bufferInfo.buffer(uniformBuffers.get(i).getHandle());
                    bufferInfo.offset(0);
                    bufferInfo.range(UniformBufferObject.BYTES);
        
                    VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(1, stack);
                    VkWriteDescriptorSet descriptorWrite;

                    descriptorWrite = descriptorWrites.get(0);
                    descriptorWrite.sType(VK11.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                    descriptorWrite.dstSet(descriptorSets.get(i).getHandle());
                    descriptorWrite.dstBinding(0);
                    descriptorWrite.dstArrayElement(0);
                    descriptorWrite.descriptorType(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                    descriptorWrite.descriptorCount(1);
                    descriptorWrite.pBufferInfo(pBufferInfo);
        
                    VK11.vkUpdateDescriptorSets(context.getDevice().getHandle(), descriptorWrites, null);
                }
            }
        }

        public void allocateSecondaryCommandBuffers() {
            secondaryCommandBuffers = renderingEngine.getSwapchainRenderer().allocateSecondaryCommandBuffers(context.getDevice(), "TestRender1");
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
                IntBuffer pImageIndex = renderingEngine.prepareRender(stack, context);
                if(pImageIndex == null) {
                    return;
                }

                updateUniformBuffer(renderingEngine.getCurrentFrame(), stack);

                recordSecondaryCommandBuffer(secondaryCommandBuffers.get(renderingEngine.getCurrentFrame()),pImageIndex.get(0), stack, model, descriptorSets.get(renderingEngine.getCurrentFrame()));

                renderingEngine.render(stack, context, pImageIndex);
            }
        }

        public void updateUniformBuffer(int imageIndex, MemoryStack stack) {
            Mat4 model = new Mat4(1.0f).rotate((float)Math.toRadians(0.0f), new Vec3(0.0f, 0.0f, 1.0f));
            Mat4 view = new Mat4(1.0f).lookAt(new Vec3(2.0f, 2.0f, 2.0f), new Vec3(0.0f, 0.0f, 0.0f), new Vec3(0.0f, 0.0f, 1.0f));
            Mat4 proj = new Mat4(1.0f).perspective((float) Math.toRadians(45.0f), renderingEngine.getSwapchainRenderer().getSurfaceSupportDetails().getSurfaceExtent().width() / (float) renderingEngine.getSwapchainRenderer().getSurfaceSupportDetails().getSurfaceExtent().height(), 0.1f, 10.0f);
            proj.set(1, 1, proj.m11 * -1);

            PointerBuffer ppData = stack.callocPointer(1);
            Vma.vmaMapMemory(context.getMemoryAllocator().getHandle(), uniformBuffers.get(imageIndex).getAllocationHandle(), ppData);
            FloatBuffer data = ppData.getFloatBuffer(0, 48);
            model.toDfb(data, 0);
            view.toDfb(data, 16);
            proj.toDfb(data, 32);
            Vma.vmaUnmapMemory(context.getMemoryAllocator().getHandle(), uniformBuffers.get(imageIndex).getAllocationHandle());
        }

        public void recordSecondaryCommandBuffer(SecondaryCommandBuffer secondaryCommandBuffer, int imageIndex, MemoryStack stack, Mesh model, DescriptorSet descriptorSet) {
            SwapchainRenderer swapchainRenderer = renderingEngine.getSwapchainRenderer();
            
            //Record Secondary fram buffer
            VkViewport.Buffer viewports = VkViewport.callocStack(1, stack);
            VkViewport viewport = viewports.get(0);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width((float) swapchainRenderer.getSurfaceSupportDetails().getSurfaceExtent().width());
            viewport.height((float) swapchainRenderer.getSurfaceSupportDetails().getSurfaceExtent().height());
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);
    
            VkOffset2D offset = VkOffset2D.callocStack(stack);
            offset.x(0);
            offset.y(0);
    
            VkRect2D.Buffer scissors = VkRect2D.callocStack(1, stack);
            VkRect2D scissor = scissors.get(0);
            scissor.offset(offset);
            scissor.extent(swapchainRenderer.getSurfaceSupportDetails().getSurfaceExtent());
    
            VK11.vkResetCommandBuffer(secondaryCommandBuffer.getCommandBuffer(), 0);        
            secondaryCommandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, swapchainRenderer.getRenderPass(), swapchainRenderer.getSwapchain().getFramebuffer(imageIndex));
            secondaryCommandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchainShaderProgram.getGraphicsPipeline());
            secondaryCommandBuffer.setViewportCmd(viewports);
            secondaryCommandBuffer.setScissorCmd(scissors);
            secondaryCommandBuffer.bindVertexBufferCmd(stack, model.getVertexBuffer());
            secondaryCommandBuffer.bindIndexBufferCmd(model.getIndexBuffer());
            secondaryCommandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchainShaderProgram.getGraphicsPipeline().getPipelineLayout(), descriptorSet);
            secondaryCommandBuffer.drawIndexedCmd(model.getIndexBuffer().getLenght(), 1);
            secondaryCommandBuffer.endRecording();
    
            //Add object to execution list if not culled
            swapchainRenderer.addToExecutionList("TestRender1", renderingEngine.getCurrentFrame());
        }

        private void cleanup() {
            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                uniformBuffers.get(i).destroy(context.getMemoryAllocator());
            }
            model.destroy(context.getMemoryAllocator());
            VK11.vkDestroyCommandPool(context.getDevice().getHandle(), commandPool.getHandle(), null);

            ResourceManager.cleanup(context);

            renderingEngine.destroy(context);
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