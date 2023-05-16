package ca.artemis;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class Vertex {
    
    public Vector3f pos;
    public Vector2f texCoord;
    public Vector3f normal;

    public Vertex(Vector3f pos, Vector2f texCoord, Vector3f normal) {
        this.pos = pos;
        this.texCoord = texCoord;
        this.normal = normal;
    }

    public static VkVertexInputBindingDescription.Buffer getBindingDescriptions(MemoryStack stack, VertexKind vertexKind) {
        VkVertexInputBindingDescription.Buffer bindingDescriptions = VkVertexInputBindingDescription.callocStack(1, stack);

        VkVertexInputBindingDescription bindingDescription = bindingDescriptions.get(0);
        bindingDescription.binding(0);
        bindingDescription.stride(vertexKind.size * 4);
        bindingDescription.inputRate(VK11.VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescriptions;
    }

    public static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack, VertexKind vertexKind) {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions;
        VkVertexInputAttributeDescription attributeDescription;
        
        switch(vertexKind) {
            case POS_UV_NORMAL:
                attributeDescriptions = VkVertexInputAttributeDescription.callocStack(3, stack);
                break;
            default:
                throw new RuntimeException("Default VertexKind is not valid!");
            
        }

        int offset = 0;
        attributeDescription = attributeDescriptions.get(0);
        attributeDescription.binding(0);
        attributeDescription.location(0);
        attributeDescription.format(VK11.VK_FORMAT_R32G32B32_SFLOAT);
        attributeDescription.offset(offset);

        if(vertexKind == VertexKind.POS_UV_NORMAL) {
            offset += Vector2f.BYTES;
            attributeDescription = attributeDescriptions.get(1);
            attributeDescription.binding(0);
            attributeDescription.location(1);
            attributeDescription.format(VK11.VK_FORMAT_R32G32_SFLOAT);
            attributeDescription.offset(offset);
        }

        if(vertexKind == VertexKind.POS_UV_NORMAL) {
            offset += Vector3f.BYTES;
            attributeDescription = attributeDescriptions.get(2);
            attributeDescription.binding(0);
            attributeDescription.location(2);
            attributeDescription.format(VK11.VK_FORMAT_R32G32B32_SFLOAT);
            attributeDescription.offset(offset);
        }

        return attributeDescriptions;
    }

    public static enum VertexKind {
        POS_UV_NORMAL(8);

        public final int size;

        private VertexKind(int size) {
            this. size = size;
        }
    }
}