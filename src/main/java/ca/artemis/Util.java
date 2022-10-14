package ca.artemis;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

public class Util {
    
	public static String[] RemoveEmptyStrings(String[] data) {
		List<String> result = new ArrayList<String>();
		
		for(int i = 0; i < data.length; i++)
			if(!data[i].equals(""))
				result.add(data[i]);
		
		String[] res = new String[result.size()];
		result.toArray(res);
		
		return res;
	}

    public static VkImage createImage(VkDevice device, long allocator, int width, int height, int mipLevels, int numSamples, int format, int tiling, int usage, int memoryUsage) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkExtent3D extent = VkExtent3D.callocStack(stack);
            extent.width(width);
            extent.height(height);
            extent.depth(1);

            VkImageCreateInfo imageInfo = VkImageCreateInfo.callocStack(stack);
            imageInfo.sType(VK11.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
            imageInfo.imageType(VK11.VK_IMAGE_TYPE_2D);
            imageInfo.extent(extent);
            imageInfo.mipLevels(mipLevels);
            imageInfo.arrayLayers(1);
            imageInfo.format(format);
            imageInfo.tiling(tiling);
            imageInfo.initialLayout(VK11.VK_IMAGE_LAYOUT_UNDEFINED);
            imageInfo.usage(usage);
            imageInfo.samples(numSamples);
            imageInfo.sharingMode(VK11.VK_SHARING_MODE_EXCLUSIVE);

            VmaAllocationCreateInfo pAllocationCreateInfo = VmaAllocationCreateInfo.callocStack(stack);
            pAllocationCreateInfo.usage(memoryUsage);

            LongBuffer pImage = stack.callocLong(1);
            PointerBuffer pAllocation = stack.callocPointer(1);
            int error = Vma.vmaCreateImage(allocator, imageInfo, pAllocationCreateInfo, pImage, pAllocation, null);
            if(error != VK11.VK_SUCCESS)
                throw new AssertionError("Failed to create image");

            return new VkImage(pImage.get(0), pAllocation.get(0));
        }
    }

    public static long createImageView(VkDevice device, long image, int format, int aspectFlags, int mipLevels) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .image(image)
                .viewType(VK11.VK_IMAGE_VIEW_TYPE_2D)
                .format(format);
            
            viewInfo.subresourceRange()
                .aspectMask(aspectFlags)
                .baseMipLevel(0)
                .levelCount(mipLevels)
                .baseArrayLayer(0)
                .layerCount(1);

            LongBuffer pView = stack.callocLong(1);
            if(VK11.vkCreateImageView(device, viewInfo, null, pView) != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to create image view!");
            }

            return pView.get(0);
        }
    }

    public static Spirv compileShaderFile(String path, ShaderStageKind shaderStageKind) {
        try {
            String source = readString(path);
            return compileShader(path, source, shaderStageKind);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Spirv compileShader(String path, String source, ShaderStageKind shaderStageKind) {
        long compiler = Shaderc.shaderc_compiler_initialize();
        if(compiler == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }

        long result = Shaderc.shaderc_compile_into_spv(compiler, source, shaderStageKind.getKind(), path, "main", MemoryUtil.NULL);
        if(result == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to compile shader " + path + " into SPIR-V");
        }
        if(Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
            throw new RuntimeException("Failed to compile shader " + path + " into SPIR-V:\n " + Shaderc.shaderc_result_get_error_message(result));
        }

        Shaderc.shaderc_compiler_release(compiler);

        return new Spirv(result, Shaderc.shaderc_result_get_bytes(result), shaderStageKind);
    }

    public static List<String> readLines(String path) throws IOException {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(path)))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            return lines;
        }
    }

    public static String readString(String path) throws IOException {
        try(InputStream inputStream = ClassLoader.getSystemResourceAsStream(path)) {
            return new String(inputStream.readAllBytes());
        }
    }

    public static byte[] readBytes(String path) throws IOException {
        try(InputStream inputStream = ClassLoader.getSystemResourceAsStream(path)) {
            return inputStream.readAllBytes();
        }
    }

    public static BufferedImage getBufferedImage(String path) {
        try(InputStream inputStream = ClassLoader.getSystemResourceAsStream(path)) {
            return ImageIO.read(inputStream);
        } catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load BufferedImage");
        }
    }

    public static class VkImage {

        public final long image;
        public final long allocation;

        public VkImage(long image, long allocation) {
            this.image = image;
            this.allocation = allocation;
        }
    }

    public enum ShaderStageKind {

        VERTEX_SHADER(VK11.VK_SHADER_STAGE_VERTEX_BIT, Shaderc.shaderc_glsl_vertex_shader),
        GEOMETRY_SHADER(VK11.VK_SHADER_STAGE_GEOMETRY_BIT, Shaderc.shaderc_glsl_geometry_shader),
        FRAGMENT_SHADER(VK11.VK_SHADER_STAGE_FRAGMENT_BIT, Shaderc.shaderc_glsl_fragment_shader);

        private final int stage;
        private final int kind;

        ShaderStageKind(int stage, int kind) {
            this.stage = stage;
            this.kind = kind;
        }

        public int getStage() {
            return stage;
        }

        public int getKind() {
            return kind;
        }
    }

    public static final class Spirv {

        private final long handle;
        private final ByteBuffer bytecode;
        private final ShaderStageKind shaderStageKind;

        public Spirv(long handle, ByteBuffer bytecode, ShaderStageKind shaderStageKind) {
            this.handle = handle;
            this.bytecode = bytecode;
            this.shaderStageKind = shaderStageKind;
        }

        public ByteBuffer getBytecode() {
            return bytecode;
        }

        public ShaderStageKind getShaderStageKind() {
            return shaderStageKind;
        }

        public void destroy() {
            Shaderc.shaderc_result_release(handle);
        }
    }
}
