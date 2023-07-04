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
import ca.artemis.engine.core.Camera;
import ca.artemis.engine.maths.Matrix4f;
import ca.artemis.engine.rendering.RenderDataComponent;
import ca.artemis.engine.rendering.RenderingEngine;
import ca.artemis.engine.scenes.GameObject;
import ca.artemis.engine.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.engine.vulkan.api.memory.VulkanBuffer;
import ca.artemis.engine.vulkan.core.mesh.Mesh;
import ca.artemis.engine.vulkan.core.mesh.MeshComponent;
import ca.artemis.engine.vulkan.core.mesh.TransformComponent;
import ca.artemis.game.rendering.LowPolyRenderingEngine;
import glm.mat._4.Mat4;

public class EntityRenderDataComponent extends RenderDataComponent {

    public static Camera camera;

    private List<SecondaryCommandBuffer> secondaryCommandBuffers; //One per frame in flight
    private List<VulkanBuffer> uniformBuffers; //One per frame in flight
    private List<DescriptorSet> descriptorSets; //One per frame in flight

    //During the init phase of the EntityRenderDataComponent we create all vulkan objects needed by the class
    @Override
    public void init() { 
        VulkanContext context = LowPolyEngine.instance().getContext();

        if(camera == null) {
            camera = new Camera();
        }

        this.secondaryCommandBuffers = allocateSecondaryCommandBuffers(LowPolyRenderingEngine.instance());
        
        createUniformBuffers(context);
        createDescriptorSets(context, LowPolyRenderingEngine.instance().getEntityRenderer().getShaderProgram());
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

    private List<SecondaryCommandBuffer> allocateSecondaryCommandBuffers(LowPolyRenderingEngine lowPolyRenderingEngine) {
        VulkanContext context = LowPolyEngine.instance().getContext();
        List<SecondaryCommandBuffer> secondaryCommandBuffers = new ArrayList<>();
        for(int i = 0; i < RenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            secondaryCommandBuffers.add(new SecondaryCommandBuffer(context.getDevice(), lowPolyRenderingEngine.getEntityRenderer().getCommandPool(i)));
        }
        return secondaryCommandBuffers;
    }


    private static void updateUniformBuffer(MemoryStack stack, VulkanContext context, VulkanBuffer uniformBuffer, TransformComponent transformComponent) {
        Matrix4f model = transformComponent.getTransformationMatrix();
        Mat4 _model = new Mat4(model.getFloats());

        Matrix4f projection = camera.getProjectionMatrix();
        Mat4 _projection = new Mat4(projection.getFloats());

        _projection.print();

        Matrix4f view = camera.getViewMatrix();
        Mat4 _view = new Mat4(view.getFloats());

        PointerBuffer ppData = stack.callocPointer(1);
        Vma.vmaMapMemory(context.getMemoryAllocator().getHandle(), uniformBuffer.getAllocationHandle(), ppData);
        FloatBuffer data = ppData.getFloatBuffer(0, 48);
        _model.toDfb(data, 0);
        _view.toDfb(data, 16);
        _projection.toDfb(data, 32);
        Vma.vmaUnmapMemory(context.getMemoryAllocator().getHandle(), uniformBuffer.getAllocationHandle());
    }

    private static void recordSecondaryCommandBuffer(MemoryStack stack, VulkanContext context, EntityRenderer entityRenderer, EntityShaderProgram  entityShaderProgram, SecondaryCommandBuffer secondaryCommandBuffer, DescriptorSet descriptorSet, Mesh mesh, int frameIndex) {
        //Record Secondary frame buffer
        VK11.vkResetCommandBuffer(secondaryCommandBuffer.getCommandBuffer(), 0);        
        secondaryCommandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, entityRenderer.getRenderPass(), entityRenderer.getFramebufferObject().getFramebuffer(frameIndex));
        secondaryCommandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, entityShaderProgram.getGraphicsPipeline());
        secondaryCommandBuffer.bindVertexBufferCmd(stack, mesh.getVertexBuffer());
        secondaryCommandBuffer.bindIndexBufferCmd(mesh.getIndexBuffer());
        secondaryCommandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, entityShaderProgram.getGraphicsPipeline().getPipelineLayout(), descriptorSet);
        secondaryCommandBuffer.drawIndexedCmd(mesh.getIndexBuffer().getLenght(), 1);
        secondaryCommandBuffer.endRecording();
    }


    //EndTempStuff
    @Override
    public void update(MemoryStack stack, int frameIndex) {
        //TempStuff
        VulkanContext context = LowPolyEngine.instance().getContext();
        EntityRenderer entityRenderer = LowPolyRenderingEngine.instance().getEntityRenderer(); 
        EntityShaderProgram entityShaderProgram = entityRenderer.getShaderProgram();

        entityRenderer.getinFlightFences().get(frameIndex).waitFor(context.getDevice());

        GameObject parent = super.getParent();
        MeshComponent meshComponent = parent.getComponent(MeshComponent.class);
        TransformComponent transformComponent = parent.getComponent(TransformComponent.class);

        updateUniformBuffer(stack, context, uniformBuffers.get(frameIndex), transformComponent);
        recordSecondaryCommandBuffer(stack, context, entityRenderer, entityShaderProgram, secondaryCommandBuffers.get(frameIndex), descriptorSets.get(frameIndex), meshComponent.getMesh(), frameIndex);
    }

    @Override
    public void close() {
        VulkanContext context = LowPolyEngine.instance().getContext();
        for(int i = 0; i < LowPolyRenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            uniformBuffers.get(i).destroy(context.getMemoryAllocator());
        }
    }

    public SecondaryCommandBuffer getSecondaryCommandBuffer(int index) {
        return secondaryCommandBuffers.get(index);
    }
}
