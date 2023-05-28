package ca.artemis.engine.vulkan.api.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class VertexInputState {

    private final List<VertexInputBindingDescription> bindingDescriptions = new ArrayList<>(); 

    public VkPipelineVertexInputStateCreateInfo buildVertexInputStateCreateInfo(MemoryStack stack) {
        List<VertexInputAttributeDescription> attributeDescriptions = new ArrayList<>(); 

        VkVertexInputBindingDescription.Buffer pVertexInputBindingDescription = VkVertexInputBindingDescription.callocStack(bindingDescriptions.size(), stack);
        for(int i = 0; i < bindingDescriptions.size(); i++) {
            VertexInputBindingDescription bindingDescription = bindingDescriptions.get(i); 
            pVertexInputBindingDescription.get(i)
                .binding(bindingDescription.binding)
                .stride(bindingDescription.stride)
                .inputRate(bindingDescription.inputRate);

            attributeDescriptions.addAll(bindingDescription.attributeDescriptions);
        }

        VkVertexInputAttributeDescription.Buffer pVertexInputAttributeDescription = VkVertexInputAttributeDescription.callocStack(attributeDescriptions.size(), stack);
        for(int i = 0; i < attributeDescriptions.size(); i++) {
            VertexInputAttributeDescription attributeDescription = attributeDescriptions.get(i); 
            pVertexInputAttributeDescription.get(i)
                .binding(attributeDescription.binding)
                .location(attributeDescription.location)
                .format(attributeDescription.format)
                .offset(attributeDescription.offset);
        }

        return VkPipelineVertexInputStateCreateInfo.callocStack(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
            .pVertexBindingDescriptions(pVertexInputBindingDescription)
            .pVertexAttributeDescriptions(pVertexInputAttributeDescription);
    }

    public VertexInputState addBinding(VertexInputBindingDescription vertexInputBindingDescription) {
        bindingDescriptions.add(vertexInputBindingDescription);
        return this;
    }

    public static class VertexInputBindingDescription {
        
        private final int binding;
        private final int stride;
        private final int inputRate;
        private final List<VertexInputAttributeDescription> attributeDescriptions = new ArrayList<>(); 

        public VertexInputBindingDescription(int binding, int stride, int inputRate) {
            this.binding = binding;
            this.stride = stride;
            this.inputRate = inputRate;
        }

        public VertexInputBindingDescription addAttributes(int location, int format, int offset) {
            attributeDescriptions.add(new VertexInputAttributeDescription(binding, location, format, offset));
            return this;
        }
    }
    
    private static class VertexInputAttributeDescription {

        private final int binding;
        private final int location;
        private final int format;
        private final int offset;

        public VertexInputAttributeDescription(int binding, int location, int format, int offset) {
            this.binding = binding;
            this.location = location;
            this.format = format;
            this.offset = offset;
        }
    }
}