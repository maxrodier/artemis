package ca.artemis.vulkan.api.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;

public class ColorBlendState {
    
    private List<ColorBlendAttachement> colorBlendAttachements = new ArrayList<>();

    public VkPipelineColorBlendStateCreateInfo buildColorBlendStateCreateInfo(MemoryStack stack) {
        VkPipelineColorBlendAttachmentState.Buffer pAttachments = VkPipelineColorBlendAttachmentState.callocStack(colorBlendAttachements.size(), stack);
        for(int i = 0; i < colorBlendAttachements.size(); i++) {
            ColorBlendAttachement colorBlendAttachement = colorBlendAttachements.get(i);
            pAttachments.get(i)
                .blendEnable(colorBlendAttachement.blendEnable)
                .srcColorBlendFactor(colorBlendAttachement.srcColorBlendFactor)
                .dstColorBlendFactor(colorBlendAttachement.dstColorBlendFactor)
                .colorBlendOp(colorBlendAttachement.colorBlendOp)
                .srcAlphaBlendFactor(colorBlendAttachement.srcAlphaBlendFactor)
                .dstAlphaBlendFactor(colorBlendAttachement.dstAlphaBlendFactor)
                .alphaBlendOp(colorBlendAttachement.alphaBlendOp)
                .colorWriteMask(colorBlendAttachement.colorWriteMask);
        }

        return VkPipelineColorBlendStateCreateInfo.callocStack(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
            .pAttachments(pAttachments);
    }

    public ColorBlendState addColorBlendAttachement(ColorBlendAttachement colorBlendAttachement) {
        this.colorBlendAttachements.add(colorBlendAttachement);
        return this;
    }

    public static class ColorBlendAttachement {
        private boolean blendEnable;
        private int colorWriteMask;
        private int srcColorBlendFactor = VK11.VK_BLEND_FACTOR_ONE;
        private int dstColorBlendFactor = VK11.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
        private int colorBlendOp = VK11.VK_BLEND_OP_ADD;
        private int srcAlphaBlendFactor = VK11.VK_BLEND_FACTOR_ONE;
        private int dstAlphaBlendFactor = VK11.VK_BLEND_FACTOR_ZERO;
        private int alphaBlendOp = VK11.VK_BLEND_OP_ADD;

        public ColorBlendAttachement(boolean blendEnable, int colorWriteMask) {
            this.blendEnable = blendEnable;
            this.colorWriteMask = colorWriteMask;
        }
    }
}