package ca.artemis.engine.core.scenes;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

import ca.artemis.engine.core.Camera;
import ca.artemis.engine.core.GameObject;
import ca.artemis.engine.core.ResourceManager;
import ca.artemis.engine.core.math.Vector2f;
import ca.artemis.engine.vulkan.api.commands.CommandBufferUtils;
import ca.artemis.engine.vulkan.api.commands.CommandPool;
import ca.artemis.engine.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.engine.vulkan.api.descriptor.UniformBufferObject;
import ca.artemis.engine.vulkan.api.memory.VulkanBuffer;
import ca.artemis.engine.vulkan.api.memory.VulkanTexture;
import ca.artemis.engine.vulkan.api.pipeline.ShaderModule;
import ca.artemis.engine.vulkan.api.pipeline.SharderUtils.ShaderStageKind;
import ca.artemis.engine.vulkan.programs.DefaultShaderProgram;
import ca.artemis.engine.vulkan.programs.ShaderProgram;
import ca.artemis.engine.vulkan.rendering.RenderingEngine;
import ca.artemis.engine.vulkan.rendering.SwapchainRenderer;

public class LevelEditorScene extends Scene {

    private DefaultShaderProgram defaultShaderProgram;
    private UniformBufferObject uniformBufferObject;
    private VulkanTexture texture;

    private List<DescriptorSet> descriptorSets; //One per frame in flight
    private List<SecondaryCommandBuffer> commandBuffers; //One per frame in flight

    private static final float[] vertexArray = {
        100.0f, 0.0f, 0.0f,      1.0f, 0.0f, 0.0f, 1.0f,        1.0f, 0.0f,    //Bottom right
        0.0f, 100.0f, 0.0f,      0.0f, 1.0f, 0.0f, 1.0f,        0.0f, 1.0f,    //Top left
        100.0f, 100.0f, 0.0f,    1.0f, 0.0f, 1.0f, 1.0f,        1.0f, 1.0f,    //Top right
        0.0f, 0.0f, 0.0f,        1.0f, 1.0f, 0.0f, 1.0f,        0.0f, 0.0f     //Bottom left
    };

    // IMPORTANT: Must be in counter-clockwise order
    private static final int[] elementArray = {
        2, 1, 0,    //Top right triangle 
        0, 1, 3     //Bottom left triangle
    };

    VulkanBuffer vbo;
    VulkanBuffer ebo;

    GameObject testGameObject;

    public LevelEditorScene() {
        this.camera = new Camera(new Vector2f(0, 0));
    }

    @Override
    public void init() {
        VulkanContext context = VulkanContext.getContext();
        RenderingEngine renderingEngine = RenderingEngine.getInstance();

        ResourceManager.addShaderModule("defaultVertShaderModule", new ShaderModule(context.getDevice(), "shaders/default.vert.glsl", ShaderStageKind.VERTEX_SHADER));
        ResourceManager.addShaderModule("defaultFragShaderModule", new ShaderModule(context.getDevice(), "shaders/default.frag.glsl", ShaderStageKind.FRAGMENT_SHADER));

        ResourceManager.addShaderProgram("defaultShaderProgram", new DefaultShaderProgram(context.getDevice(), renderingEngine.getSwapchainRenderer()));

        defaultShaderProgram = ResourceManager.getShaderProgram("defaultShaderProgram", DefaultShaderProgram.class);
        vbo = createVertexBufferObject(context.getDevice(), context.getMemoryAllocator(), context.getDevice().getGraphicsQueue(), context.getCommandPool(), vertexArray);
        ebo = createElementBufferObject(context.getDevice(), context.getMemoryAllocator(), context.getDevice().getGraphicsQueue(), context.getCommandPool(), elementArray);
        uniformBufferObject = new UniformBufferObject(context.getDevice(), context.getMemoryAllocator(), defaultShaderProgram, 32);
        texture = new VulkanTexture(context, "textures/viking.png", false);

        createDescriptorSets(context.getDevice(), defaultShaderProgram, 32 * Float.BYTES);
        commandBuffers = renderingEngine.getSwapchainRenderer().allocateSecondaryCommandBuffers(context.getDevice(), "object1");
    }

    private void createDescriptorSets(VulkanDevice device, ShaderProgram shaderProgram, int bufferSize) { //TODO: IDEA Only have the update Descriptor here create descriptor sets elsewhere with textures
        try(MemoryStack stack = MemoryStack.stackPush()) {
            descriptorSets = new ArrayList<>();
            for (int i = 0; i < RenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
                DescriptorSet descriptorSet = new DescriptorSet(device, shaderProgram.getDescriptorPool(), shaderProgram.getDescriptorSetLayouts()[0]);

                descriptorSet.updateDescriptorBuffer(device, uniformBufferObject.getUniformBuffer(i) , VK11.VK_WHOLE_SIZE, 0, 0, VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                descriptorSet.updateDescriptorImageBuffer(device, texture.getImageBundle().getImageView(), texture.getSampler(), VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 1, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);

                descriptorSets.add(descriptorSet);
            }
        }
    }

    @Override
    public void update(float dt, int frameIndex) {
        for(GameObject gameObject : gameObjects) {
            gameObject.update(dt);
        }

        try(MemoryStack stack = MemoryStack.stackPush()) {
            
            //Update Uniform Buffer
            if(dt >= 0) {
                camera.position.x -= dt * 50.0f;
                camera.position.y -= dt * 20.0f;
            }
            uniformBufferObject.updateBuffer(stack, VulkanContext.getContext().getMemoryAllocator(), camera.getProjectionMatrix(), camera.getViewMatrix(), frameIndex);


            //Record Secondary fram buffer
            DescriptorSet descriptorSet = descriptorSets.get(frameIndex);
            SecondaryCommandBuffer secondaryCommandBuffer = commandBuffers.get(frameIndex);
            SwapchainRenderer swapchainRenderer = RenderingEngine.getInstance().getSwapchainRenderer();

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

            secondaryCommandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, swapchainRenderer.getRenderPass(), swapchainRenderer.getSwapchain().getFramebuffer(frameIndex));
            secondaryCommandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, defaultShaderProgram.getGraphicsPipeline());
            secondaryCommandBuffer.setViewportCmd(viewports);
            secondaryCommandBuffer.setScissorCmd(scissors);
            secondaryCommandBuffer.bindVertexBufferCmd(stack, vbo);
            secondaryCommandBuffer.bindIndexBufferCmd(ebo);
            secondaryCommandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, defaultShaderProgram.getGraphicsPipeline().getPipelineLayout(), descriptorSet);
            secondaryCommandBuffer.drawIndexedCmd(ebo.getLenght(), 1);
            secondaryCommandBuffer.endRecording();
        
            //Add object to execution list if not culled
            swapchainRenderer.render("object1", frameIndex);
        }
    }



    // TEMP CODE

    private static VulkanBuffer createVertexBufferObject(VulkanDevice device, VulkanMemoryAllocator allocator, VkQueue graphicsQueue, CommandPool commandPool, float[] vertexArray) {
		int bufferLength = vertexArray.length;
    	int bufferSize = Float.BYTES;
    	
		VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
			.setLength(bufferLength)
			.setSize(bufferSize)
			.setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
			.setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
			.build(allocator);
    	
        PointerBuffer ppData = MemoryUtil.memAllocPointer(1); //TODO: Change this for stack allocation
        Vma.vmaMapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle(), ppData);
    	FloatBuffer data = ppData.getFloatBuffer(0, bufferLength);
    	
        for(int i = 0; i < vertexArray.length; i++) {
            data.put(vertexArray[i]);
    	}

        Vma.vmaUnmapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle());
		
		VulkanBuffer vertexBuffer = new VulkanBuffer.Builder()
			.setLength(bufferLength)
			.setSize(bufferSize)
			.setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK11.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
			.setMemoryUsage(Vma.VMA_MEMORY_USAGE_GPU_ONLY)
			.build(allocator);

		CommandBufferUtils.copyBuffer(device, graphicsQueue, commandPool, stagingBuffer, vertexBuffer, bufferLength * bufferSize);
        stagingBuffer.destroy(allocator);

        return vertexBuffer;
    }

    private static VulkanBuffer createElementBufferObject(VulkanDevice device, VulkanMemoryAllocator allocator, VkQueue graphicsQueue, CommandPool commandPool, int[] indices) {
		int bufferLength = indices.length;
    	int bufferSize = Integer.BYTES;
        
        VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
            .setLength(bufferLength)
            .setSize(bufferSize)
            .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
            .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
            .build(allocator);

    	PointerBuffer ppData = MemoryUtil.memAllocPointer(1);
        Vma.vmaMapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle(), ppData);
    	IntBuffer data = ppData.getIntBuffer(0, indices.length);

    	for(int i = 0; i < indices.length; i++) {
    		data.put(i, indices[i]);
    	}
    	
        Vma.vmaUnmapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle());
		
		VulkanBuffer indexBuffer = new VulkanBuffer.Builder()
			.setLength(bufferLength)
			.setSize(bufferSize)
			.setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK11.VK_BUFFER_USAGE_INDEX_BUFFER_BIT)
			.setMemoryUsage(Vma.VMA_MEMORY_USAGE_GPU_ONLY)
			.build(allocator);

		CommandBufferUtils.copyBuffer(device, graphicsQueue, commandPool,stagingBuffer, indexBuffer, bufferLength * bufferSize);
        stagingBuffer.destroy(allocator);
        
        return indexBuffer;
    }
}
