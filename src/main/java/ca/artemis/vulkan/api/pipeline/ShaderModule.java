package ca.artemis.vulkan.api.pipeline;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.pipeline.SharderUtils.ShaderStageKind;
import ca.artemis.vulkan.api.pipeline.SharderUtils.Spirv;

public class ShaderModule {

    private final Spirv spirv;
    private final long handle ;

    public ShaderModule(String path, ShaderStageKind shaderStageKind) {
        this.spirv = SharderUtils.compileShaderFile(path, shaderStageKind);
        this.handle = createHandle(this.spirv);
    }
    
    public void destroy() {
        spirv.destroy();
        VK11.vkDestroyShaderModule(VulkanContext.getContext().getDevice().getHandle(), handle, null);
    }

    private long createHandle(Spirv spirv) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkShaderModuleCreateInfo pCreateInfo = VkShaderModuleCreateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                .pCode(spirv.getBytecode());

            LongBuffer pShaderModule = stack.callocLong(1);
            int error = VK11.vkCreateShaderModule(VulkanContext.getContext().getDevice().getHandle(), pCreateInfo, null, pShaderModule);
            if(error != VK11.VK_SUCCESS)
                throw new AssertionError("Failed to create shader module");

            return pShaderModule.get(0);
        }
    }

    public Spirv getSpirv() {
        return spirv;
    }

    public long getHandle() {
        return handle;
    }
}