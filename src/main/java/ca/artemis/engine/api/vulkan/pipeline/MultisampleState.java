package ca.artemis.engine.api.vulkan.pipeline;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;

public class MultisampleState {

    private List<Integer> sampleMasks = new ArrayList<>();
    private int rasterizationSamples = VK11.VK_SAMPLE_COUNT_1_BIT;
    private boolean sampleShadingEnable;
    private float minSampleShading;
    private boolean alphaToCoverageEnable;
    private boolean alphaToOneEnable;

    public VkPipelineMultisampleStateCreateInfo buildMultisampleStateCreateInfo(MemoryStack stack) {
        IntBuffer pSampleMask = sampleMasks.size() == 0 ? null : stack.callocInt(sampleMasks.size());
        for(int i = 0; i < sampleMasks.size(); i++) {
            pSampleMask.put(i, sampleMasks.get(i));
        }

        return VkPipelineMultisampleStateCreateInfo.calloc(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
            .pSampleMask(pSampleMask)
            .rasterizationSamples(rasterizationSamples)
            .sampleShadingEnable(sampleShadingEnable)
            .minSampleShading(minSampleShading)
            .alphaToCoverageEnable(alphaToCoverageEnable)
            .alphaToOneEnable(alphaToOneEnable);
    }

    public MultisampleState addSampleMask(int sampleMask) {
        this.sampleMasks.add(sampleMask);
        return this;
    }

    public MultisampleState setRasterizationSamples(int rasterizationSamples) {
        this.rasterizationSamples = rasterizationSamples;
        return this;
    }

    public MultisampleState setSampleShadingEnable(boolean sampleShadingEnable) {
        this.sampleShadingEnable = sampleShadingEnable;
        return this;
    }

    public MultisampleState setMinSampleShading(float minSampleShading) {
        this.minSampleShading = minSampleShading;
        return this;
    }

    public MultisampleState setAlphaToCoverageEnable(boolean alphaToCoverageEnable) {
        this.alphaToCoverageEnable = alphaToCoverageEnable;
        return this;
    }

    public MultisampleState setAlphaToOneEnable(boolean alphaToOneEnable) {
        this.alphaToOneEnable = alphaToOneEnable;
        return this;
    }
}