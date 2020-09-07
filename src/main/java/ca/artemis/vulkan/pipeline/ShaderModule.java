package ca.artemis.vulkan.pipeline;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import ca.artemis.vulkan.context.VulkanDevice;

public class ShaderModule {

    private final long handle ;
    private final int shaderStage;

    public ShaderModule(VulkanDevice device, String path, int shaderStage) {
        this.handle = createHandle(device, path);
        this.shaderStage = shaderStage;
    }
    
    public void destroy(VulkanDevice device) {
        VK11.vkDestroyShaderModule(device.getHandle(), handle, null);
    }

    private long createHandle(VulkanDevice device, String path) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            byte[] code = Files.readAllBytes(Paths.get(path));
            ByteBuffer pCode = stack.calloc(code.length).put(code).flip();

            VkShaderModuleCreateInfo pCreateInfo = VkShaderModuleCreateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                .pCode(pCode);

            LongBuffer pShaderModule = stack.callocLong(1);
            int error = VK11.vkCreateShaderModule(device.getHandle(), pCreateInfo, null, pShaderModule);
            if(error != VK11.VK_SUCCESS)
                throw new AssertionError("Failed to create shader module");

            return pShaderModule.get(0);
        } catch(IOException e) {
           throw new AssertionError("Failed to load shader resource");
        }
    }

    public long getHandle() {
        return handle;
    }

    public int getShaderStage() {
        return shaderStage;
    }
}