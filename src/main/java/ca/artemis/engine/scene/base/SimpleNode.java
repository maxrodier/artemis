package ca.artemis.engine.scene.base;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.scene.RenderableNode;
import ca.artemis.math.Matrix4f;
import ca.artemis.math.Vector3f;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.memory.VulkanBuffer;
import ca.artemis.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.vulkan.api.pipeline.GraphicsPipeline;
import ca.artemis.vulkan.rendering.mesh.Quad;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;

public class SimpleNode extends RenderableNode {
    
    private final VulkanBuffer buffer;
    private final Quad quad;

    public SimpleNode(SceneRenderer sceneRenderer, int width, int height, Vector3f colour) {
        super(sceneRenderer.getSimpleShaderProgram(), sceneRenderer.getCommandPool());

        this.buffer = new VulkanBuffer.Builder()
            .setBufferUsage(VK11.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
            .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
            .setLength(Matrix4f.SIZE)
            .setSize(Matrix4f.BYTES)
            .build();

        this.quad = new Quad(0, 0, width, height, colour);

        updateDescriptorSets();
        recordDrawCommandBuffer(this.drawCommandBuffer, this.shaderProgram.getGraphicsPipeline(), sceneRenderer.getSceneFramebufferObject().getRenderPass(), sceneRenderer.getSceneFramebufferObject().getFramebuffer(), this.descriptorSets, this.quad);
    }

    public void destroy(SceneRenderer sceneRenderer) {
        super.destroy(sceneRenderer.getCommandPool());
        this.quad.destroy();
        this.buffer.destroy();
    }


    @Override
    public void updateDescriptorSets() {
        descriptorSets[0].updateDescriptorBuffer(buffer, VK11.VK_WHOLE_SIZE, 0, 0, VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
    }

    @Override
    public void update(MemoryStack stack) {
        VulkanMemoryAllocator memoryAllocator = VulkanContext.getContext().getMemoryAllocator();
        PointerBuffer ppData = stack.callocPointer(1);
        Vma.vmaMapMemory(memoryAllocator.getHandle(), buffer.getAllocationHandle(), ppData);
        FloatBuffer data = ppData.getFloatBuffer(0, Matrix4f.SIZE);
        data.put(0, getTransformation().getMemoryLayout());
        Vma.vmaUnmapMemory(memoryAllocator.getHandle(), buffer.getAllocationHandle());
    }

    @Override
    public void populateDrawCommandBuffers(List<SecondaryCommandBuffer> drawCommandBuffers) {
        drawCommandBuffers.add(drawCommandBuffer);
        super.populateDrawCommandBuffers(drawCommandBuffers);
    }

    public static void recordDrawCommandBuffer(SecondaryCommandBuffer drawCommandBuffer, GraphicsPipeline graphicsPipeline, RenderPass renderPass, VulkanFramebuffer framebuffer, DescriptorSet[] descriptorSets, Quad quad) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            drawCommandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, renderPass, framebuffer);
            drawCommandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
            drawCommandBuffer.bindVertexBufferCmd(stack, quad.getVertexBuffer());
            drawCommandBuffer.bindIndexBufferCmd(quad.getIndexBuffer());
            drawCommandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.getPipelineLayout(), descriptorSets);
            drawCommandBuffer.drawIndexedCmd(quad.getIndexBuffer().getLenght(), 1);
            drawCommandBuffer.endRecording();
        }
    }
}
