package ca.artemis.engine.rendering.entity;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import ca.artemis.UniformBufferObject;
import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.rendering.RenderData;
import ca.artemis.engine.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.engine.vulkan.api.memory.VulkanBuffer;
import ca.artemis.engine.vulkan.core.mesh.Quad;
import ca.artemis.engine.vulkan.core.mesh.Vertex.VertexKind;
import ca.artemis.game.rendering.LowPolyRenderingEngine;
import glm.mat._4.Mat4;

public class EntityRenderData extends RenderData {

    private Quad quad;
    private List<VulkanBuffer> uniformBuffers; //One per frame in flight
    private List<DescriptorSet> descriptorSets; //One per frame in flight
    private List<SecondaryCommandBuffer> secondaryCommandBuffers; //One per frame in flight

    public EntityRenderData() {
        VulkanContext context = LowPolyEngine.instance().getContext();

        this.quad = new Quad(context, VertexKind.POS_COLOUR);
        createUniformBuffers(context);
        createDescriptorSets(context, LowPolyRenderingEngine.instance().getEntityRenderer().getShaderProgram());
        allocateSecondaryCommandBuffers(LowPolyRenderingEngine.instance());
    }

    private void createUniformBuffers(VulkanContext context) {
        int bufferSize = UniformBufferObject.BYTES;

        uniformBuffers = new ArrayList<>();

        for(int i = 0; i < LowPolyRenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            VulkanBuffer indexBuffer = new VulkanBuffer.Builder()
                .setLength(1)
                .setSize(bufferSize)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
                .build(context.getMemoryAllocator());

            uniformBuffers.add(indexBuffer);
        }
    }

    private void createDescriptorSets(VulkanContext context, EntityShaderProgram entityShaderProgram) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            descriptorSets = new ArrayList<>();
            for (int i = 0; i < LowPolyRenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {

                DescriptorSet descriptorSet = new DescriptorSet(context.getDevice(), entityShaderProgram.getDescriptorPool(), entityShaderProgram.getDescriptorSetLayouts()[0]);

                descriptorSets.add(descriptorSet);

                VkDescriptorBufferInfo.Buffer pBufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
                VkDescriptorBufferInfo bufferInfo = pBufferInfo.get(0);
                bufferInfo.buffer(uniformBuffers.get(i).getHandle());
                bufferInfo.offset(0);
                bufferInfo.range(UniformBufferObject.BYTES);
    
                VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.calloc(1, stack);
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

    private void allocateSecondaryCommandBuffers(LowPolyRenderingEngine lowPolyRenderingEngine) {
        VulkanContext context = LowPolyEngine.instance().getContext();
        secondaryCommandBuffers = lowPolyRenderingEngine.getEntityRenderer().allocateSecondaryCommandBuffers(context.getDevice(), "TestRender1");
    }


    private static void updateUniformBuffer(MemoryStack stack, VulkanContext context, VulkanBuffer uniformBuffer) {
        //Mat4 model = new Mat4(1.0f).rotate((float)Math.toRadians(0.0f), new Vec3(0.0f, 0.0f, 1.0f));
        //Mat4 view = new Mat4(1.0f).lookAt(new Vec3(2.0f, 2.0f, 2.0f), new Vec3(0.0f, 0.0f, 0.0f), new Vec3(0.0f, 0.0f, 1.0f));
        //Mat4 proj = new Mat4(1.0f).perspective((float) Math.toRadians(45.0f), context.getSurfaceSupportDetails().getSurfaceExtent().width() / (float) context.getSurfaceSupportDetails().getSurfaceExtent().height(), 0.1f, 10.0f);
        //proj.set(1, 1, proj.m11 * -1);

        Mat4 model = new Mat4().identity();
        Mat4 view = new Mat4().identity();
        Mat4 proj = new Mat4().identity();

        PointerBuffer ppData = stack.callocPointer(1);
        Vma.vmaMapMemory(context.getMemoryAllocator().getHandle(), uniformBuffer.getAllocationHandle(), ppData);
        FloatBuffer data = ppData.getFloatBuffer(0, 48);
        model.toDfb(data, 0);
        view.toDfb(data, 16);
        proj.toDfb(data, 32);
        Vma.vmaUnmapMemory(context.getMemoryAllocator().getHandle(), uniformBuffer.getAllocationHandle());
    }

    private static void recordSecondaryCommandBuffer(MemoryStack stack, VulkanContext context, EntityRenderer entityRenderer, EntityShaderProgram  entityShaderProgram, SecondaryCommandBuffer secondaryCommandBuffer, DescriptorSet descriptorSet, Quad quad, EntityRenderData entityRenderData) {
        //Record Secondary frame buffer
        VK11.vkResetCommandBuffer(secondaryCommandBuffer.getCommandBuffer(), 0);        
        secondaryCommandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, entityRenderer.getRenderPass(), entityRenderer.getFramebufferObject().getFramebuffer(entityRenderData.getFrameIndex()));
        secondaryCommandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, entityShaderProgram.getGraphicsPipeline());
        secondaryCommandBuffer.bindVertexBufferCmd(stack, quad.getVertexBuffer());
        secondaryCommandBuffer.bindIndexBufferCmd(quad.getIndexBuffer());
        secondaryCommandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, entityShaderProgram.getGraphicsPipeline().getPipelineLayout(), descriptorSet);
        secondaryCommandBuffer.drawIndexedCmd(quad.getIndexBuffer().getLenght(), 1);
        secondaryCommandBuffer.endRecording();

        //Add object to execution list if not culled
        entityRenderer.addToExecutionList("TestRender1", entityRenderData.getFrameIndex());
    }


    //EndTempStuff
    @Override
    public void update(MemoryStack stack) {
        //TempStuff
        VulkanContext context = LowPolyEngine.instance().getContext();
        EntityRenderer entityRenderer = LowPolyRenderingEngine.instance().getEntityRenderer(); 
        EntityShaderProgram entityShaderProgram = entityRenderer.getShaderProgram();

        entityRenderer.getinFlightFences().get(this.getFrameIndex()).waitFor(stack, context.getDevice());
        entityRenderer.getinFlightFences().get(this.getFrameIndex()).reset(stack, context.getDevice());

        updateUniformBuffer(stack, context, uniformBuffers.get(this.getFrameIndex()));
        recordSecondaryCommandBuffer(stack, context, entityRenderer, entityShaderProgram, secondaryCommandBuffers.get(this.getFrameIndex()), descriptorSets.get(this.getFrameIndex()), quad, this);
    }

    @Override
    public void close() throws Exception {
        VulkanContext context = LowPolyEngine.instance().getContext();
        for(int i = 0; i < LowPolyRenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            uniformBuffers.get(i).destroy(context.getMemoryAllocator());
        }
        quad.destroy(context.getMemoryAllocator());
    }
}
