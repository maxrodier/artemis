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
import ca.artemis.vulkan.api.memory.SpriteSheet;
import ca.artemis.vulkan.api.memory.VulkanBuffer;
import ca.artemis.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.vulkan.api.pipeline.GraphicsPipeline;
import ca.artemis.vulkan.rendering.mesh.Quad;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;

public class SpriteSheetNode extends RenderableNode {

    private static final int UBO_LENGTH = 4 + Matrix4f.SIZE;
    private static final int UBO_SIZE = Integer.BYTES * 4 + Matrix4f.BYTES;

    private final SpriteSheet spriteSheet;

    private final VulkanBuffer buffer;
    private final Quad quad;
    
    private int spriteX = 0;
    private int spriteY = 0;

    public SpriteSheetNode(SceneRenderer sceneRenderer, int x, int y, int width, int height, SpriteSheet spriteSheet) {
        super(sceneRenderer.getSpriteSheetShaderProgram(), sceneRenderer.getCommandPool());

        this.spriteSheet = spriteSheet;

        this.buffer = new VulkanBuffer.Builder()
            .setBufferUsage(VK11.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
            .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
            .setLength(UBO_LENGTH)
            .setSize(UBO_SIZE)
            .build();

        this.quad = new Quad(x, y, width, height, new Vector3f(1,0,1), spriteSheet.getU(), spriteSheet.getV());

        updateDescriptorSets();
        recordDrawCommandBuffer(this.drawCommandBuffer, this.shaderProgram.getGraphicsPipeline(), sceneRenderer.getSceneFramebufferObject().getRenderPass(), sceneRenderer.getSceneFramebufferObject().getFramebuffer(), this.descriptorSets, this.quad);
    }

    public void destroy() {
        super.destroy();
        this.quad.destroy();
        this.buffer.destroy();
    }

    public void setSpriteIndex(int spriteX, int spriteY) {
        this.spriteX = spriteX;  
        this.spriteY = spriteY;  
    }

    @Override
    public void updateDescriptorSets() {
        descriptorSets[0].updateDescriptorBuffer(buffer, VK11.VK_WHOLE_SIZE, 0, 0, VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        descriptorSets[0].updateDescriptorImageBuffer(spriteSheet.getTexture().getImageBundle().getImageView(), spriteSheet.getTexture().getSampler(), VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 1, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    }

    @Override
    public void update(MemoryStack stack) {
        VulkanMemoryAllocator memoryAllocator = VulkanContext.getContext().getMemoryAllocator();
        PointerBuffer ppData = stack.callocPointer(1);
        Vma.vmaMapMemory(memoryAllocator.getHandle(), buffer.getAllocationHandle(), ppData);
        FloatBuffer data = ppData.getFloatBuffer(0, UBO_LENGTH);
        data.put(0, getTransformation().getMemoryLayout());
        data.put(16, spriteX);
        data.put(17, spriteY);
        data.put(18, spriteSheet.getNormalizedSpriteWidth());
        data.put(19, spriteSheet.getNormalizedSpriteHeight());
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
