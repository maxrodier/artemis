package ca.artemis.vulkan.rendering.renderer;

import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;

import ca.artemis.Configuration;
import ca.artemis.engine.scene.SceneGraph;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.commands.SubmitInfo;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.framebuffer.FramebufferObject.Attachment;
import ca.artemis.vulkan.api.framebuffer.SceneFramebufferObject;
import ca.artemis.vulkan.api.memory.VulkanImageView;
import ca.artemis.vulkan.api.synchronization.VulkanFence;
import ca.artemis.vulkan.rendering.programs.FontShaderProgram;
import ca.artemis.vulkan.rendering.programs.SimpleShaderProgram;
import ca.artemis.vulkan.rendering.programs.SpriteShaderProgram;
import ca.artemis.vulkan.rendering.programs.SpriteSheetShaderProgram;

public class SceneRenderer extends Renderer {

    private final SceneFramebufferObject sceneFramebufferObject;

    private final SimpleShaderProgram simpleShaderProgram;
    private final SpriteShaderProgram spriteShaderProgram;
    private final SpriteSheetShaderProgram spriteSheetShaderProgram;
    private final FontShaderProgram fontShaderProgram;

    private final CommandPool commandPool;

    private final PrimaryCommandBuffer primaryCommandBuffer;

    private final VulkanFence renderFence;

    private final SubmitInfo submitInfo;

    public SceneRenderer(VulkanContext context) {
        super(context.getDevice(), null);

        this.sceneFramebufferObject = new SceneFramebufferObject(context);

        this.simpleShaderProgram = new SimpleShaderProgram(context.getDevice(), this.sceneFramebufferObject.getRenderPass());
        this.spriteShaderProgram = new SpriteShaderProgram(context.getDevice(), this.sceneFramebufferObject.getRenderPass());
        this.spriteSheetShaderProgram = new SpriteSheetShaderProgram(context.getDevice(), this.sceneFramebufferObject.getRenderPass());
        this.fontShaderProgram = new FontShaderProgram(context.getDevice(), this.sceneFramebufferObject.getRenderPass());

        this.commandPool = new CommandPool(context.getDevice(), context.getPhysicalDevice().getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        this.primaryCommandBuffer = new PrimaryCommandBuffer(context.getDevice(), this.commandPool);

        this.renderFence = new VulkanFence(context.getDevice());

        this.submitInfo = new SubmitInfo(this.renderFence)
            .setCommandBuffers(primaryCommandBuffer)
            .setSignalSemaphores(this.signalSemaphore);
    }

    public void destroy(VulkanContext context) {
        this.submitInfo.destroy();
        this.renderFence.destroy(context.getDevice());
        this.primaryCommandBuffer.destroy(context.getDevice(), this.commandPool);

        this.commandPool.destroy(context.getDevice());

        this.spriteSheetShaderProgram.destroy(context.getDevice());
        this.spriteShaderProgram.destroy(context.getDevice());
        this.simpleShaderProgram.destroy(context.getDevice());
        this.fontShaderProgram.destroy(context.getDevice());

        this.sceneFramebufferObject.destroy(context);
        super.destroy(context.getDevice());
    }

    public void update(SceneGraph sceneGraph) {
        recordCommandBuffer(this.primaryCommandBuffer, sceneGraph.getDrawCommandBuffers(), this.sceneFramebufferObject);
    }

    private static void recordCommandBuffer(PrimaryCommandBuffer primaryCommandBuffer, List<SecondaryCommandBuffer> secondaryCommandBuffers, SceneFramebufferObject sceneFramebufferObject) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            
            VkClearValue.Buffer pClearValues = VkClearValue.callocStack(1);
            pClearValues.get(0).color()
                .float32(0, 36f/255f)
                .float32(1, 10f/255f)
                .float32(2, 48f/255f)
                .float32(3, 1);

            primaryCommandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
            primaryCommandBuffer.beginRenderPassCmd(stack, sceneFramebufferObject.getRenderPass().getHandle(), sceneFramebufferObject.getFramebuffer().getHandle(), Configuration.windowWidth, Configuration.windowHeight, pClearValues, VK11.VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

            PointerBuffer pCommandBuffers = stack.callocPointer(secondaryCommandBuffers.size());
            for(int i = 0; i < secondaryCommandBuffers.size(); i++) {
                pCommandBuffers.put(i, secondaryCommandBuffers.get(i).getHandle());
            }
            VK11.vkCmdExecuteCommands(primaryCommandBuffer.getCommandBuffer(), pCommandBuffers);

            primaryCommandBuffer.endRenderPassCmd();
            primaryCommandBuffer.endRecording();
        }
    }

    @Override
    public void draw(VulkanDevice device, MemoryStack stack) {
        submitInfo.submit(device, device.getGraphicsQueue());
    }

    public VulkanFence getRenderFence() {
        return renderFence;
    }

    public VulkanImageView getDisplayImage() {
        return sceneFramebufferObject.getAttachment(Attachment.COLOR).getImageView();
    }

    public SceneFramebufferObject getSceneFramebufferObject() {
        return sceneFramebufferObject;
    }

    public CommandPool getCommandPool() {
        return commandPool;
    }

    public SimpleShaderProgram getSimpleShaderProgram() {
        return simpleShaderProgram;
    }

    public SpriteShaderProgram getSpriteShaderProgram() {
        return spriteShaderProgram;
    }

    public SpriteSheetShaderProgram getSpriteSheetShaderProgram() {
        return spriteSheetShaderProgram;
    }

    public FontShaderProgram getFontShaderProgram() {
        return fontShaderProgram;
    }
}
