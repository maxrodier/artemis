package ca.artemis.vulkan.rendering.scene.elements;

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
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.memory.VulkanBuffer;
import ca.artemis.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.vulkan.api.pipeline.GraphicsPipeline;
import ca.artemis.vulkan.rendering.mesh.Quad;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;

public class UIElement extends RenderableNode {

    private final VulkanBuffer buffer;
    private final Quad quad;

    public UIElement(VulkanContext context, SceneRenderer sceneRenderer, int x, int y, int width, int height) {
        super(context.getDevice(), sceneRenderer.getSimpleShaderProgram(), sceneRenderer.getCommandPool());

        this.buffer = new VulkanBuffer.Builder()
            .setBufferUsage(VK11.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
            .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
            .setLength(Matrix4f.SIZE)
            .setSize(Matrix4f.BYTES)
            .build(context.getMemoryAllocator());

        this.quad = new Quad(context, x, y, width, height, new Vector3f(1,0,1));

        this.updateDescriptorSets(context.getDevice());

        recordDrawCommandBuffer(context.getDevice(), this.drawCommandBuffer, this.shaderProgram.getGraphicsPipeline(), sceneRenderer.getSceneFramebufferObject().getRenderPass(), sceneRenderer.getSceneFramebufferObject().getFramebuffer(), this.descriptorSets, this.quad);
    }

    public void destroy(VulkanContext context, SceneRenderer sceneRenderer) {
        super.destroy(context.getDevice(), sceneRenderer.getCommandPool());
        this.quad.destroy(context.getMemoryAllocator());
        this.buffer.destroy(context.getMemoryAllocator());
    }

    @Override
    public void update(VulkanContext context, MemoryStack stack) {
        PointerBuffer ppData = stack.callocPointer(1);
        Vma.vmaMapMemory(context.getMemoryAllocator().getHandle(), buffer.getAllocationHandle(), ppData);
        FloatBuffer data = ppData.getFloatBuffer(0, Matrix4f.SIZE);
        data.put(0, getTransformation().getMemoryLayout());
        Vma.vmaUnmapMemory(context.getMemoryAllocator().getHandle(), buffer.getAllocationHandle());
    }
    
    @Override
    public void updateDescriptorSets(VulkanDevice device) {
        descriptorSets[0].updateDescriptorBuffer(device, buffer, VK11.VK_WHOLE_SIZE, 0, 0, VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
    }

    @Override
    public void populateDrawCommandBuffers(List<SecondaryCommandBuffer> drawCommandBuffers) {
        drawCommandBuffers.add(drawCommandBuffer);
        super.populateDrawCommandBuffers(drawCommandBuffers);
    }

    public static void recordDrawCommandBuffer(VulkanDevice device, SecondaryCommandBuffer drawCommandBuffer, GraphicsPipeline graphicsPipeline, RenderPass renderPass, VulkanFramebuffer framebuffer, DescriptorSet[] descriptorSets, Quad quad) {
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
