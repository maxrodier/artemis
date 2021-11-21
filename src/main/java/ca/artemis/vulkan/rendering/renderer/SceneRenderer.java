package ca.artemis.vulkan.rendering.renderer;

import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;

import ca.artemis.Configuration;
import ca.artemis.engine.scene.SceneGraph;
import ca.artemis.math.Vector3f;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.commands.SubmitInfo;
import ca.artemis.vulkan.api.context.VulkanContext;
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

    private final VkClearValue.Buffer pClearValues;

    public SceneRenderer(Vector3f clearColour) {
        super(null);

        this.sceneFramebufferObject = new SceneFramebufferObject();

        this.simpleShaderProgram = new SimpleShaderProgram(this.sceneFramebufferObject.getRenderPass());
        this.spriteShaderProgram = new SpriteShaderProgram(this.sceneFramebufferObject.getRenderPass());
        this.spriteSheetShaderProgram = new SpriteSheetShaderProgram(this.sceneFramebufferObject.getRenderPass());
        this.fontShaderProgram = new FontShaderProgram(this.sceneFramebufferObject.getRenderPass());

        this.commandPool = new CommandPool(VulkanContext.getContext().getDevice(), VulkanContext.getContext().getPhysicalDevice().getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        this.primaryCommandBuffer = new PrimaryCommandBuffer(this.commandPool);

        this.renderFence = new VulkanFence();

        this.submitInfo = new SubmitInfo(this.renderFence)
            .setCommandBuffers(primaryCommandBuffer)
            .setSignalSemaphores(this.signalSemaphore);

        this.pClearValues = VkClearValue.calloc(1);
        this.pClearValues.get(0).color()
            .float32(0, clearColour.getX())
            .float32(1, clearColour.getY())
            .float32(2, clearColour.getZ())
            .float32(3, 1);
    }

    public void destroy() {
        MemoryUtil.memFree(pClearValues);
        this.submitInfo.destroy();
        this.renderFence.destroy();
        this.primaryCommandBuffer.destroy(this.commandPool);

        this.commandPool.destroy(VulkanContext.getContext().getDevice());

        this.spriteSheetShaderProgram.destroy();
        this.spriteShaderProgram.destroy();
        this.simpleShaderProgram.destroy();
        this.fontShaderProgram.destroy();

        this.sceneFramebufferObject.destroy();
        super.destroy();
    }

    public void update(SceneGraph sceneGraph) {
        recordCommandBuffer(this.primaryCommandBuffer, sceneGraph.getDrawCommandBuffers(), this.sceneFramebufferObject);
    }

    private void recordCommandBuffer(PrimaryCommandBuffer primaryCommandBuffer, List<SecondaryCommandBuffer> secondaryCommandBuffers, SceneFramebufferObject sceneFramebufferObject) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
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
    public void draw(MemoryStack stack) {
        submitInfo.submit(VulkanContext.getContext().getDevice().getGraphicsQueue());
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
