package ca.artemis.engine.vulkan.api.pipeline;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.pipeline.SharderUtils.ShaderStageKind;
import ca.artemis.engine.vulkan.api.pipeline.SharderUtils.Spirv;

public class ShaderModule {

    private final Spirv spirv;
    private final long handle ;

    public ShaderModule(VulkanDevice device, String path, ShaderStageKind shaderStageKind) {
        this.spirv = SharderUtils.compileShaderFile(path, shaderStageKind);
        this.handle = createHandle(device, this.spirv);
    }
    
    public void destroy(VulkanDevice device) {
        spirv.destroy();
        VK11.vkDestroyShaderModule(device.getHandle(), handle, null);
    }

    private long createHandle(VulkanDevice device, Spirv spirv) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkShaderModuleCreateInfo pCreateInfo = VkShaderModuleCreateInfo.calloc(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                .pCode(spirv.getBytecode());

            LongBuffer pShaderModule = stack.callocLong(1);
            int error = VK11.vkCreateShaderModule(device.getHandle(), pCreateInfo, null, pShaderModule);
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