package ca.artemis;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class Vertex {
    
    public static final int BYTES = Vec3.BYTES + Vec3.BYTES + Vec2.BYTES;

    public Vec3 pos;
    public Vec3 colour;
    public Vec2 texCoord;

    public Vertex(Vec3 pos, Vec2 texCoord) {
        this.pos = pos;
        this.colour = new Vec3(1.0f, 1.0f, 1.0f);
        this.texCoord = texCoord;
    }

    public static VkVertexInputBindingDescription.Buffer getBindingDescriptions(MemoryStack stack) {
        VkVertexInputBindingDescription.Buffer bindingDescriptions = VkVertexInputBindingDescription.callocStack(1, stack);

        VkVertexInputBindingDescription bindingDescription = bindingDescriptions.get(0);
        bindingDescription.binding(0);
        bindingDescription.stride(BYTES);
        bindingDescription.inputRate(VK11.VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescriptions;
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack) {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.callocStack(3, stack);
        VkVertexInputAttributeDescription attributeDescription;

        int offset = 0;
        attributeDescription = attributeDescriptions.get(0);
        attributeDescription.binding(0);
        attributeDescription.location(0);
        attributeDescription.format(VK11.VK_FORMAT_R32G32B32_SFLOAT);
        attributeDescription.offset(offset);

        offset += Vec3.BYTES;
        attributeDescription = attributeDescriptions.get(1);
        attributeDescription.binding(0);
        attributeDescription.location(1);
        attributeDescription.format(VK11.VK_FORMAT_R32G32B32_SFLOAT);
        attributeDescription.offset(offset);

        offset += Vec3.BYTES;
        attributeDescription = attributeDescriptions.get(2);
        attributeDescription.binding(0);
        attributeDescription.location(2);
        attributeDescription.format(VK11.VK_FORMAT_R32G32_SFLOAT);
        attributeDescription.offset(offset);

        return attributeDescriptions;
    }

    public static enum VertexKind {
        POS_COLOUR(6),
        POS_UV(5),
        POS_COLOUR_UV(8);

        public final int size;

        private VertexKind(int size) {
            this. size = size;
        }
    }
}