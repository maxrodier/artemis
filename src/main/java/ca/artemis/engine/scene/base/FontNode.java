package ca.artemis.engine.scene.base;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.scene.RenderableNode;
import ca.artemis.engine.text.Character;
import ca.artemis.engine.text.Font;
import ca.artemis.math.Matrix4f;
import ca.artemis.math.Vector2f;
import ca.artemis.math.Vector3f;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.memory.VulkanBuffer;
import ca.artemis.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.vulkan.api.pipeline.GraphicsPipeline;
import ca.artemis.vulkan.rendering.mesh.Mesh;
import ca.artemis.vulkan.rendering.mesh.Vertex;
import ca.artemis.vulkan.rendering.mesh.Vertex.VertexKind;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;

public class FontNode extends RenderableNode {
  
    private final Font font;

    private final VulkanBuffer buffer;
    private final Mesh mesh;

    public FontNode(SceneRenderer sceneRenderer, String text, int size, Vector3f colour, Font font) {
        super(sceneRenderer.getFontShaderProgram(), sceneRenderer.getCommandPool());

        this.font = font;

        this.buffer = new VulkanBuffer.Builder()
            .setBufferUsage(VK11.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
            .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
            .setLength(Matrix4f.SIZE)
            .setSize(Matrix4f.BYTES)
            .build();

        this.mesh = createMesh(text, size, colour, font);

        updateDescriptorSets();
        recordDrawCommandBuffer(this.drawCommandBuffer, this.shaderProgram.getGraphicsPipeline(), sceneRenderer.getSceneFramebufferObject().getRenderPass(), sceneRenderer.getSceneFramebufferObject().getFramebuffer(), this.descriptorSets, this.mesh);
    }

    public void destroy(SceneRenderer sceneRenderer) {
        super.destroy(sceneRenderer.getCommandPool());
        this.mesh.destroy();
        this.buffer.destroy();
    }

    @Override
    public void updateDescriptorSets() {
        descriptorSets[0].updateDescriptorBuffer(buffer, VK11.VK_WHOLE_SIZE, 0, 0, VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        descriptorSets[0].updateDescriptorImageBuffer(font.getTexture().getImageBundle().getImageView(), font.getTexture().getSampler(), VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 1, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
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

    public static void recordDrawCommandBuffer(SecondaryCommandBuffer drawCommandBuffer, GraphicsPipeline graphicsPipeline, RenderPass renderPass, VulkanFramebuffer framebuffer, DescriptorSet[] descriptorSets, Mesh mesh) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            drawCommandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, renderPass, framebuffer);
            drawCommandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
            drawCommandBuffer.bindVertexBufferCmd(stack, mesh.getVertexBuffer());
            drawCommandBuffer.bindIndexBufferCmd(mesh.getIndexBuffer());
            drawCommandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.getPipelineLayout(), descriptorSets);
            drawCommandBuffer.drawIndexedCmd(mesh.getIndexBuffer().getLenght(), 1);
            drawCommandBuffer.endRecording();
        }
    }

    private static Mesh createMesh(String text, int size, Vector3f colour, Font font) {
        List<Vertex> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        Vector2f cursor = new Vector2f(0.f, 0.f);
        int index = 0;

        for(char c : text.toCharArray()) {
            Character character = font.characters.get((int)c);
            if(character == null) {
                continue;
            }

            vertices.add(new Vertex(new Vector3f((cursor.getX() + character.xOffset) * size, (cursor.getY() + character.yOffset) * size, 0.0f), colour, new Vector2f(character.xMinTexCoord, character.yMinTexCoord)));
            vertices.add(new Vertex(new Vector3f((cursor.getX() + character.xOffset + character.width) * size, (cursor.getY() + character.yOffset) * size, 0.0f), colour, new Vector2f(character.xMaxTexCoord, character.yMinTexCoord)));
            vertices.add(new Vertex(new Vector3f((cursor.getX() + character.xOffset + character.width) * size, (cursor.getY() + character.yOffset + character.height) * size, 0.0f), colour,  new Vector2f(character.xMaxTexCoord, character.yMaxTexCoord)));
            vertices.add(new Vertex(new Vector3f((cursor.getX() + character.xOffset) * size, (cursor.getY() + character.yOffset + character.height) * size, 0.0f), colour, new Vector2f(character.xMinTexCoord, character.yMaxTexCoord)));

            indices.add(4 * index + 0);
            indices.add(4 * index + 1);
            indices.add(4 * index + 2);
            indices.add(4 * index + 0);
            indices.add(4 * index + 2);
            indices.add(4 * index + 3);

            cursor.setX(cursor.getX() + character.xAdvance);
            index++;
        }

        return new Mesh(vertices.toArray(new Vertex[vertices.size()]), indices.toArray(new Integer[indices.size()]), VertexKind.POS_COLOUR_UV);
    }
}
