package ca.artemis;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import ca.artemis.Util.ShaderStageKind;
import ca.artemis.Vertex.VertexKind;
import ca.artemis.vulkan.api.context.VulkanContext;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;

public class Main {

    private static class HelloTriangleApplication {

        private static final int MAX_FRAMES_IN_FLIGHT = 2;

        private static final Vertex[] vertices = {
            new Vertex(new Vector3f(-0.5f, -0.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f)),
            new Vertex(new Vector3f(0.5f, -0.5f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f)),
            new Vertex(new Vector3f(0.5f, 0.5f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f)),
            new Vertex(new Vector3f(-0.5f, 0.5f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f)),
        };

        private static final Integer[] indices = {
            0, 1, 2, 2, 3, 0
        };

        private static final int msaaSamples = VK11.VK_SAMPLE_COUNT_1_BIT;

        private VulkanContext context;
        private long swapChain;
        private int swapChainImageFormat;
        private VkExtent2D swapChainExtent;
        private List<Long> swapChainImages;
        private List<Long> swapChainImageViews;
        private long renderPass;
        private long descriptorSetLayout;
        private long pipelineLayout;
        private long graphicsPipeline;
        private List<Long> swapChainFramebuffers;
        private Mesh model;
        private long commandPool;
        private List<VulkanBuffer> uniformBuffers; //One per frame in flight
        private long descriptorPool;
        private List<Long> descriptorSets; //One per frame in flight
        private List<CommandBuffer> commandBuffers; //One per frame in flight
        private List<Long> imageAvailableSemaphores; //One per frame in flight
        private List<Long> renderFinishedSemaphores; //One per frame in flight
        private List<Long> inFlightFences; //One per frame in flight

        private int currentFrame = 0;
        private boolean framebufferResized = false;

        public void run() {
            initVulkan();
            mainLoop();
            cleanup();
        }

        private void setFramebufferSizeCallback() {
            GLFW.glfwSetFramebufferSizeCallback(context.getWindow().getHandle(), new GLFWFramebufferSizeCallbackI() {
                @Override
                public void invoke(long window, int width, int height) {
                    framebufferResized = true;
                }
            });
        }

        private void initVulkan() {
            createContext();
            setFramebufferSizeCallback();
            createSwapChain();
            createImageViews();
            createRenderPass();
            createDescriptorSetLayout();
            createGraphicsPipeline();
            createFramebuffers();
            createCommandPool();
            createModel();
            createUniformBuffers();
            createDescriptorPool();
            createDescriptorSets();
            createCommandBuffers();
            createSyncObjects();
        }

        private void createContext() {
            context = VulkanContext.create();
        }

        private void createSwapChain() {

            SwapChainSupportDetails swapChainSupport = SwapChainSupportDetails.querySwapChainSupport(context.getSurface().getHandle(), context.getPhysicalDevice().getHandle());

            VkSurfaceFormatKHR surfaceFormat = swapChainSupport.chooseSwapSurfaceFormat();
            int presentMode = swapChainSupport.chooseSwapPresentMode();
            VkExtent2D extent = swapChainSupport.chooseSwapExtent(context.getSurface().getHandle());
    
            int imageCount = swapChainSupport.getCapabilities().minImageCount() + 1;
            if(swapChainSupport.getCapabilities().maxImageCount() > 0 && imageCount > swapChainSupport.getCapabilities().maxImageCount()) {
                imageCount = swapChainSupport.getCapabilities().maxImageCount();
            }

            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkSwapchainCreateInfoKHR pCreateInfo = VkSwapchainCreateInfoKHR.callocStack(stack);
                pCreateInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
                pCreateInfo.surface(context.getSurface().getHandle());
                pCreateInfo.minImageCount(imageCount);
                pCreateInfo.imageFormat(surfaceFormat.format());
                pCreateInfo.imageColorSpace(surfaceFormat.colorSpace());
                pCreateInfo.imageExtent(extent);
                pCreateInfo.imageArrayLayers(1);
                pCreateInfo.imageUsage(VK11.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
                pCreateInfo.imageSharingMode(VK11.VK_SHARING_MODE_EXCLUSIVE);
                pCreateInfo.preTransform(swapChainSupport.getCapabilities().currentTransform());
                pCreateInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
                pCreateInfo.presentMode(presentMode);
                pCreateInfo.clipped(true);
    
                LongBuffer pSwapchain = stack.callocLong(1);
                KHRSwapchain.vkCreateSwapchainKHR(context.getDevice().getHandle(), pCreateInfo, null, pSwapchain);
                swapChain = pSwapchain.get(0);

                IntBuffer pSwapchainImageCount = stack.callocInt(1);
                KHRSwapchain.vkGetSwapchainImagesKHR(context.getDevice().getHandle(), swapChain, pSwapchainImageCount, null);

                LongBuffer pSwapchainImages = stack.callocLong(pSwapchainImageCount.get(0));
                KHRSwapchain.vkGetSwapchainImagesKHR(context.getDevice().getHandle(), swapChain, pSwapchainImageCount, pSwapchainImages);

                swapChainImages = new ArrayList<>();
                for(int i = 0; i < pSwapchainImages.limit(); i++) {
                    swapChainImages.add(pSwapchainImages.get(i));
                }

                swapChainImageFormat = surfaceFormat.format();
                swapChainExtent = extent;
            }
        }

        public void createImageViews() {
            swapChainImageViews = new ArrayList<>();

            for (long swapChainImage : swapChainImages) {
                swapChainImageViews.add(Util.createImageView(context.getDevice().getHandle(), swapChainImage, swapChainImageFormat, VK11.VK_IMAGE_ASPECT_COLOR_BIT, 1));
            }
        }

        public void createRenderPass() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkAttachmentDescription colorAttachment = VkAttachmentDescription.callocStack(stack);
                colorAttachment.format(swapChainImageFormat);
                colorAttachment.samples(msaaSamples);
                colorAttachment.loadOp(VK11.VK_ATTACHMENT_LOAD_OP_CLEAR);
                colorAttachment.storeOp(VK11.VK_ATTACHMENT_STORE_OP_STORE);
                colorAttachment.stencilLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
                colorAttachment.stencilStoreOp(VK11.VK_ATTACHMENT_STORE_OP_DONT_CARE);
                colorAttachment.initialLayout(VK11.VK_IMAGE_LAYOUT_UNDEFINED);
                colorAttachment.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

                VkAttachmentReference.Buffer pColorAttachmentRef = VkAttachmentReference.callocStack(1, stack);
                VkAttachmentReference colorAttachmentRef = pColorAttachmentRef.get(0);
                colorAttachmentRef.attachment(0);
                colorAttachmentRef.layout(VK11.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

                VkSubpassDescription.Buffer pSubpasses = VkSubpassDescription.callocStack(1, stack);
                VkSubpassDescription subpass = pSubpasses.get(0);
                subpass.pipelineBindPoint(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS);
                subpass.colorAttachmentCount(1);
                subpass.pColorAttachments(pColorAttachmentRef);

                VkSubpassDependency.Buffer pDependencies = VkSubpassDependency.callocStack(1, stack);
                VkSubpassDependency dependency = pDependencies.get(0);
                dependency.srcSubpass(VK11.VK_SUBPASS_EXTERNAL);
                dependency.dstSubpass(0);
                dependency.srcStageMask(VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
                dependency.srcAccessMask(0);
                dependency.dstStageMask(VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
                dependency.dstAccessMask(VK11.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

                VkAttachmentDescription.Buffer pAttachments = VkAttachmentDescription.callocStack(1, stack);
                pAttachments.put(0, colorAttachment);

                VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.callocStack(stack);
                renderPassInfo.sType(VK11.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
                renderPassInfo.pAttachments(pAttachments);
                renderPassInfo.pSubpasses(pSubpasses);
                renderPassInfo.pDependencies(pDependencies);
                
                LongBuffer pRenderPass = stack.callocLong(1);
                if(VK11.vkCreateRenderPass(context.getDevice().getHandle(), renderPassInfo, null, pRenderPass) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create render pass!");
                }
                renderPass = pRenderPass.get(0);
            }
        }

        public void createDescriptorSetLayout() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorSetLayoutBinding.Buffer pBindings = VkDescriptorSetLayoutBinding.callocStack(1, stack);
                VkDescriptorSetLayoutBinding uboLayoutBinding = pBindings.get(0); 
                uboLayoutBinding.binding(0);
                uboLayoutBinding.descriptorType(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                uboLayoutBinding.descriptorCount(1);
                uboLayoutBinding.stageFlags(VK11.VK_SHADER_STAGE_VERTEX_BIT);

                VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
                layoutInfo.sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
                layoutInfo.pBindings(pBindings);

                LongBuffer pDescriptorSetLayout = stack.callocLong(1);
                if(VK11.vkCreateDescriptorSetLayout(context.getDevice().getHandle(), layoutInfo, null, pDescriptorSetLayout) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create descriptor set layout!");
                }
                descriptorSetLayout = pDescriptorSetLayout.get(0);
            }
        }

        public void createGraphicsPipeline() {
            ByteBuffer vertShaderCode = Util.compileShaderFile("shaders/tuto.vert", ShaderStageKind.VERTEX_SHADER).getBytecode();
            ByteBuffer fragShaderCode = Util.compileShaderFile("shaders/tuto.frag", ShaderStageKind.FRAGMENT_SHADER).getBytecode();
        
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkShaderModuleCreateInfo vertShaderCreateInfo = VkShaderModuleCreateInfo.callocStack(stack);
                vertShaderCreateInfo.sType(VK11.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
                vertShaderCreateInfo.pCode(vertShaderCode);

                VkShaderModuleCreateInfo fragShaderCreateInfo = VkShaderModuleCreateInfo.callocStack(stack);
                fragShaderCreateInfo.sType(VK11.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
                fragShaderCreateInfo.pCode(fragShaderCode);
        
                LongBuffer pVertShaderModule = stack.callocLong(1);
                if(VK11.vkCreateShaderModule(context.getDevice().getHandle(), vertShaderCreateInfo, null, pVertShaderModule) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create shader module!");
                }

                LongBuffer pFragShaderModule = stack.callocLong(1);
                if(VK11.vkCreateShaderModule(context.getDevice().getHandle(), fragShaderCreateInfo, null, pFragShaderModule) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create shader module!");
                }
                
                long vertShaderModule = pVertShaderModule.get(0);
                long fragShaderModule = pFragShaderModule.get(0);

                VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(2, stack);
                VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
                vertShaderStageInfo.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                vertShaderStageInfo.stage(VK11.VK_SHADER_STAGE_VERTEX_BIT);
                vertShaderStageInfo.module(vertShaderModule);
                vertShaderStageInfo.pName(stack.UTF8("main"));
        
                VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);
                fragShaderStageInfo.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                fragShaderStageInfo.stage(VK11.VK_SHADER_STAGE_FRAGMENT_BIT);
                fragShaderStageInfo.module(fragShaderModule);
                fragShaderStageInfo.pName(stack.UTF8("main"));

                VkVertexInputBindingDescription.Buffer bindingDescription = Vertex.getBindingDescriptions(stack, VertexKind.POS_COLOUR);
                VkVertexInputAttributeDescription.Buffer attributeDescriptions = Vertex.getAttributeDescriptions(stack, VertexKind.POS_COLOUR);

                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
                vertexInputInfo.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
                vertexInputInfo.pVertexBindingDescriptions(bindingDescription);
                vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions);
        
                VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
                inputAssembly.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
                inputAssembly.topology(VK11.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
                inputAssembly.primitiveRestartEnable(false); //TODO: Change

                VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
                viewportState.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
                viewportState.viewportCount(1);
                viewportState.scissorCount(1);

                VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
                rasterizer.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
                rasterizer.depthClampEnable(false);
                rasterizer.rasterizerDiscardEnable(false);
                rasterizer.polygonMode(VK11.VK_POLYGON_MODE_FILL);
                rasterizer.lineWidth(1.0f);
                rasterizer.cullMode(VK11.VK_CULL_MODE_BACK_BIT);
                rasterizer.frontFace(VK11.VK_FRONT_FACE_COUNTER_CLOCKWISE);
                rasterizer.depthBiasEnable(false);
        
                VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
                multisampling.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
                multisampling.sampleShadingEnable(false);
                multisampling.rasterizationSamples(msaaSamples);
        
                //VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
                //depthStencil.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
                //depthStencil.depthTestEnable(true);
                //depthStencil.depthWriteEnable(true);
                //depthStencil.depthCompareOp(VK11.VK_COMPARE_OP_LESS);
                //depthStencil.depthBoundsTestEnable(false);
                //depthStencil.stencilTestEnable(false);
        
                VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachments = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
                VkPipelineColorBlendAttachmentState colorBlendAttachment = colorBlendAttachments.get(0);
                colorBlendAttachment.colorWriteMask(VK11.VK_COLOR_COMPONENT_R_BIT | VK11.VK_COLOR_COMPONENT_G_BIT | VK11.VK_COLOR_COMPONENT_B_BIT | VK11.VK_COLOR_COMPONENT_A_BIT);
                colorBlendAttachment.blendEnable(false);
        
                VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
                colorBlending.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
                colorBlending.logicOpEnable(false);
                colorBlending.logicOp(VK11.VK_LOGIC_OP_COPY);
                colorBlending.pAttachments(colorBlendAttachments);
                colorBlending.blendConstants(0, 0.0f);
                colorBlending.blendConstants(1, 0.0f);
                colorBlending.blendConstants(2, 0.0f);
                colorBlending.blendConstants(3, 0.0f);

                IntBuffer dynamicStates = stack.callocInt(2);
                dynamicStates.put(0, VK11.VK_DYNAMIC_STATE_VIEWPORT);
                dynamicStates.put(1, VK11.VK_DYNAMIC_STATE_SCISSOR);

                VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.callocStack(stack);
                dynamicState.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO);
                dynamicState.pDynamicStates(dynamicStates);
        
                LongBuffer pSetLayouts = stack.callocLong(1);
                pSetLayouts.put(0, descriptorSetLayout);

                VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
                pipelineLayoutInfo.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
                pipelineLayoutInfo.pSetLayouts(pSetLayouts);

                LongBuffer pPipelineLayout = stack.callocLong(1);
                if(VK11.vkCreatePipelineLayout(context.getDevice().getHandle(), pipelineLayoutInfo, null, pPipelineLayout) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create pipeline layout!");
                }
                pipelineLayout = pPipelineLayout.get(0);

                VkGraphicsPipelineCreateInfo.Buffer pCreateInfos = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
                VkGraphicsPipelineCreateInfo pipelineInfo = pCreateInfos.get(0);
                pipelineInfo.sType(VK11.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
                pipelineInfo.pStages(shaderStages);
                pipelineInfo.pVertexInputState(vertexInputInfo);
                pipelineInfo.pInputAssemblyState(inputAssembly);
                pipelineInfo.pViewportState(viewportState);
                pipelineInfo.pRasterizationState(rasterizer);
                pipelineInfo.pMultisampleState(multisampling);
                //pipelineInfo.pDepthStencilState(depthStencil);
                pipelineInfo.pColorBlendState(colorBlending);
                pipelineInfo.pDynamicState(dynamicState);
                pipelineInfo.layout(pipelineLayout);
                pipelineInfo.renderPass(renderPass);
                pipelineInfo.subpass(0);
                pipelineInfo.basePipelineHandle(VK11.VK_NULL_HANDLE);        

                LongBuffer pGraphicsPipeline = stack.callocLong(1);
                if(VK11.vkCreateGraphicsPipelines(context.getDevice().getHandle(), VK11.VK_NULL_HANDLE, pCreateInfos, null, pGraphicsPipeline) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create graphics pipeline!");
                }
                graphicsPipeline = pGraphicsPipeline.get(0);

                VK11.vkDestroyShaderModule(context.getDevice().getHandle(), fragShaderModule, null);
                VK11.vkDestroyShaderModule(context.getDevice().getHandle(), vertShaderModule, null);
            }
        }

        public void createFramebuffers() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                swapChainFramebuffers = new ArrayList<>();

                for (long swapChainImageView : swapChainImageViews) {
                    LongBuffer pAttachments = stack.callocLong(1);
                    pAttachments.put(0, swapChainImageView);
        
                    VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
                    framebufferInfo.sType(VK11.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
                    framebufferInfo.renderPass(renderPass);
                    framebufferInfo.pAttachments(pAttachments);
                    framebufferInfo.width(swapChainExtent.width());
                    framebufferInfo.height(swapChainExtent.height());
                    framebufferInfo.layers(1);
                    
                    LongBuffer pSwapChainFramebuffer = stack.callocLong(1);
                    if(VK11.vkCreateFramebuffer(context.getDevice().getHandle(), framebufferInfo, null, pSwapChainFramebuffer) != VK11.VK_SUCCESS) {
                        throw new RuntimeException("failed to create framebuffer!");
                    }
                    swapChainFramebuffers.add(pSwapChainFramebuffer.get(0));
                }
            }
        }

        public void createCommandPool() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                List<QueueFamily> queueFamilies = QueueFamily.getQueueFamilies(context.getSurface().getHandle(), context.getPhysicalDevice().getHandle());
        
                VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
                poolInfo.sType(VK11.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
                poolInfo.flags(VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
                poolInfo.queueFamilyIndex(queueFamilies.get(0).getIndex());
        
                LongBuffer pCommandPool = stack.callocLong(1);
                if(VK11.vkCreateCommandPool(context.getDevice().getHandle(), poolInfo, null, pCommandPool) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create graphics command pool!");
                }
                commandPool = pCommandPool.get(0);
            }
        }

        public void createModel() {
            model = new Mesh(context.getDevice().getHandle(), context.getMemoryAllocator().getHandle(), context.getDevice().getGraphicsQueue(), commandPool, vertices, indices, VertexKind.POS_COLOUR);
        }

        public void createUniformBuffers() {
            int bufferSize = UniformBufferObject.BYTES;
    
            uniformBuffers = new ArrayList<>();
    
            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                VulkanBuffer indexBuffer = new VulkanBuffer.Builder()
                    .setLength(1)
                    .setSize(bufferSize)
                    .setBufferUsage(VK11.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
                    .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
                    .build(context.getMemoryAllocator().getHandle());

                uniformBuffers.add(indexBuffer);
            }
        }

        public void createDescriptorPool() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.callocStack(1, stack);
                VkDescriptorPoolSize poolSize;

                poolSize = poolSizes.get(0);
                poolSize.type(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                poolSize.descriptorCount(MAX_FRAMES_IN_FLIGHT);
        
                VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
                poolInfo.sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
                poolInfo.pPoolSizes(poolSizes);
                poolInfo.maxSets(MAX_FRAMES_IN_FLIGHT);
        
                LongBuffer pDescriptorPool = stack.callocLong(1);
                if(VK11.vkCreateDescriptorPool(context.getDevice().getHandle(), poolInfo, null, pDescriptorPool) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create descriptor pool!");
                }
                descriptorPool = pDescriptorPool.get(0);
            }
        }

        public void createDescriptorSets() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer layouts = stack.callocLong(MAX_FRAMES_IN_FLIGHT);
                for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                    layouts.put(i, descriptorSetLayout);
                }

                VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
                allocInfo.sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
                allocInfo.descriptorPool(descriptorPool);
                allocInfo.pSetLayouts(layouts);

                LongBuffer pDescriptorSets = stack.callocLong(MAX_FRAMES_IN_FLIGHT);
                if(VK11.vkAllocateDescriptorSets(context.getDevice().getHandle(), allocInfo, pDescriptorSets) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to allocate descriptor sets!");
                }
                descriptorSets = new ArrayList<>();
                for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                    descriptorSets.add(pDescriptorSets.get(i));

                    VkDescriptorBufferInfo.Buffer pBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
                    VkDescriptorBufferInfo bufferInfo = pBufferInfo.get(0);
                    bufferInfo.buffer(uniformBuffers.get(i).getHandle());
                    bufferInfo.offset(0);
                    bufferInfo.range(UniformBufferObject.BYTES);
        
                    VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(1, stack);
                    VkWriteDescriptorSet descriptorWrite;

                    descriptorWrite = descriptorWrites.get(0);
                    descriptorWrite.sType(VK11.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                    descriptorWrite.dstSet(descriptorSets.get(i));
                    descriptorWrite.dstBinding(0);
                    descriptorWrite.dstArrayElement(0);
                    descriptorWrite.descriptorType(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                    descriptorWrite.descriptorCount(1);
                    descriptorWrite.pBufferInfo(pBufferInfo);
        
                    VK11.vkUpdateDescriptorSets(context.getDevice().getHandle(), descriptorWrites, null);
                }
            }
        }

        public void createCommandBuffers() {
            commandBuffers = new ArrayList<>();
            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                commandBuffers.add(new PrimaryCommandBuffer(context.getDevice().getHandle(), commandPool));
            }
        }

        public void createSyncObjects() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
                semaphoreInfo.sType(VK11.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
        
                VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
                fenceInfo.sType(VK11.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
                fenceInfo.flags(VK11.VK_FENCE_CREATE_SIGNALED_BIT);

                imageAvailableSemaphores = new ArrayList<>();
                renderFinishedSemaphores = new ArrayList<>();
                inFlightFences = new ArrayList<>();

                for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                    boolean error = false;
                    
                    LongBuffer pImageAvailableSemaphores = stack.callocLong(1);
                    error = (VK11.vkCreateSemaphore(context.getDevice().getHandle(), semaphoreInfo, null, pImageAvailableSemaphores) != VK11.VK_SUCCESS) && error;
                    imageAvailableSemaphores.add(pImageAvailableSemaphores.get(0));

                    LongBuffer pRenderFinishedSemaphores = stack.callocLong(1);
                    error = (VK11.vkCreateSemaphore(context.getDevice().getHandle(), semaphoreInfo, null, pRenderFinishedSemaphores) != VK11.VK_SUCCESS) && error;
                    renderFinishedSemaphores.add(pRenderFinishedSemaphores.get(0));

                    LongBuffer pInFlightFences = stack.callocLong(1);
                    error = (VK11.vkCreateFence(context.getDevice().getHandle(), fenceInfo, null, pInFlightFences) != VK11.VK_SUCCESS) && error;
                    inFlightFences.add(pInFlightFences.get(0));
                    
                    if(error) {
                        throw new RuntimeException("Failed to create synchronization objects for a frame!");
                    }
                }
            }
        }

        private void mainLoop() {
            while(!GLFW.glfwWindowShouldClose(context.getWindow().getHandle())) {
                GLFW.glfwPollEvents();
                drawFrame();
            }
            VK11.vkDeviceWaitIdle(context.getDevice().getHandle());
        }

        private void drawFrame() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer pFence = stack.callocLong(1);
                pFence.put(0, inFlightFences.get(currentFrame));
                VK11.vkWaitForFences(context.getDevice().getHandle(), pFence, true, Long.MAX_VALUE);

                IntBuffer pImageIndex = stack.callocInt(1);
                int result = KHRSwapchain.vkAcquireNextImageKHR(context.getDevice().getHandle(), swapChain, Long.MAX_VALUE, imageAvailableSemaphores.get(currentFrame), VK11.VK_NULL_HANDLE, pImageIndex);
                if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                    recreateSwapchain();
                    return;
                } else if(result != VK11.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                    throw new RuntimeException("Failed to acquire swapchain image!");
                }
                int imageIndex = pImageIndex.get(0);

                updateUniformBuffer(currentFrame, stack);

                VK11.vkResetFences(context.getDevice().getHandle(), pFence);

                VK11.vkResetCommandBuffer(commandBuffers.get(currentFrame).commandBuffer, 0);
                recordCommandBuffer(commandBuffers.get(currentFrame).commandBuffer, imageIndex, stack);

                VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
                submitInfo.sType(VK11.VK_STRUCTURE_TYPE_SUBMIT_INFO);

                LongBuffer pWaitSemaphores = stack.callocLong(1);
                pWaitSemaphores.put(0, imageAvailableSemaphores.get(currentFrame));

                IntBuffer pWaitDstStageMask = stack.callocInt(1);
                pWaitDstStageMask.put(0, VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

                submitInfo.waitSemaphoreCount(1);
                submitInfo.pWaitSemaphores(pWaitSemaphores);
                submitInfo.pWaitDstStageMask(pWaitDstStageMask);

                PointerBuffer pCommandBuffers = stack.callocPointer(1);
                pCommandBuffers.put(0, commandBuffers.get(currentFrame).commandBuffer);

                submitInfo.pCommandBuffers(pCommandBuffers);

                LongBuffer pSignalSemaphores = stack.callocLong(1);
                pSignalSemaphores.put(0, renderFinishedSemaphores.get(currentFrame));
        
                submitInfo.pSignalSemaphores(pSignalSemaphores);

                if(VK11.vkQueueSubmit(context.getDevice().getGraphicsQueue(), submitInfo, inFlightFences.get(currentFrame)) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("Failed to submit draw command buffer!");
                }

                VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
                presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
                presentInfo.pWaitSemaphores(pSignalSemaphores);

                LongBuffer pSwapChains = stack.callocLong(1);
                pSwapChains.put(0, swapChain);

                presentInfo.swapchainCount(1);
                presentInfo.pSwapchains(pSwapChains);
                presentInfo.pImageIndices(pImageIndex);

                result = KHRSwapchain.vkQueuePresentKHR(context.getDevice().getGraphicsQueue(), presentInfo);
                if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR || framebufferResized) {
                    framebufferResized = false;
                    recreateSwapchain();
                } else if (result != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to present swap chain image!");
                }

                currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
            }
        }

        public void updateUniformBuffer(int imageIndex, MemoryStack stack) {
            Mat4 model = new Mat4(1.0f).rotate((float)Math.toRadians(0.0f), new Vec3(0.0f, 0.0f, 1.0f));
            Mat4 view = new Mat4(1.0f).lookAt(new Vec3(2.0f, 2.0f, 2.0f), new Vec3(0.0f, 0.0f, 0.0f), new Vec3(0.0f, 0.0f, 1.0f));
            Mat4 proj = new Mat4(1.0f).perspective((float) Math.toRadians(45.0f), swapChainExtent.width() / (float) swapChainExtent.height(), 0.1f, 10.0f);
            proj.set(1, 1, proj.m11 * -1);

            PointerBuffer ppData = stack.callocPointer(1);
            Vma.vmaMapMemory(context.getMemoryAllocator().getHandle(), uniformBuffers.get(imageIndex).getAllocationHandle(), ppData);
            FloatBuffer data = ppData.getFloatBuffer(0, 48);
            model.toDfb(data, 0);
            view.toDfb(data, 16);
            proj.toDfb(data, 32);
            Vma.vmaUnmapMemory(context.getMemoryAllocator().getHandle(), uniformBuffers.get(imageIndex).getAllocationHandle());
        }

        public void recordCommandBuffer(VkCommandBuffer commandBuffer, int imageIndex, MemoryStack stack) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
    
            if(VK11.vkBeginCommandBuffer(commandBuffer, beginInfo) != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to begin recording command buffer!");
            }
    
            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK11.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(renderPass);
            renderPassInfo.framebuffer(swapChainFramebuffers.get(imageIndex));
            renderPassInfo.renderArea().offset().x(0);
            renderPassInfo.renderArea().offset().y(0);
            renderPassInfo.renderArea().extent(swapChainExtent);
    
            VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
            clearValues.get(0).color().float32(0, 0.0f);
            clearValues.get(0).color().float32(1, 0.0f);
            clearValues.get(0).color().float32(2, 0.0f);
            clearValues.get(0).color().float32(3, 1.0f);
            
            renderPassInfo.pClearValues(clearValues);
    
            VK11.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK11.VK_SUBPASS_CONTENTS_INLINE);
    
                VK11.vkCmdBindPipeline(commandBuffer, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
    
                VkViewport.Buffer viewports = VkViewport.callocStack(1, stack);
                VkViewport viewport = viewports.get(0);
                viewport.x(0.0f);
                viewport.y(0.0f);
                viewport.width((float) swapChainExtent.width());
                viewport.height((float) swapChainExtent.height());
                viewport.minDepth(0.0f);
                viewport.maxDepth(1.0f);
                VK11.vkCmdSetViewport(commandBuffer, 0, viewports);
    
                VkOffset2D offset = VkOffset2D.callocStack(stack);
                offset.x(0);
                offset.y(0);

                VkRect2D.Buffer scissors = VkRect2D.callocStack(1, stack);
                VkRect2D scissor = scissors.get(0);
                scissor.offset(offset);
                scissor.extent(swapChainExtent);
                VK11.vkCmdSetScissor(commandBuffer, 0, scissors);

                LongBuffer pBuffers = stack.callocLong(1);
                pBuffers.put(0, model.getVertexBuffer().getHandle());
                LongBuffer pOffsets = stack.callocLong(1);
                pOffsets.put(0, 0L);
                VK11.vkCmdBindVertexBuffers(commandBuffer, 0, pBuffers, pOffsets);

                VK11.vkCmdBindIndexBuffer(commandBuffer, model.getIndexBuffer().getHandle(), 0, VK11.VK_INDEX_TYPE_UINT32);

                LongBuffer pDescriptorSets = stack.callocLong(1);
                pDescriptorSets.put(0, descriptorSets.get(currentFrame));
                VK11.vkCmdBindDescriptorSets(commandBuffer, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, pDescriptorSets, null);

                VK11.vkCmdDrawIndexed(commandBuffer, indices.length, 1, 0, 0, 0);
    
            VK11.vkCmdEndRenderPass(commandBuffer);
    
            if(VK11.vkEndCommandBuffer(commandBuffer) != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to record command buffer!");
            }
        }

        public void recreateSwapchain() {
            //Code to wait recreation of the swapchain while minimized //Can this pause a game also? //This has not been tested
            try(MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer width = stack.callocInt(1), height = stack.callocInt(1);
                GLFW.glfwGetFramebufferSize(context.getWindow().getHandle(), width, height);
                while (width.get(0) == 0 || height.get(0) == 0) {
                    System.out.println("Is minimized");
                    width.clear(); height.clear();
                    GLFW.glfwGetFramebufferSize(context.getWindow().getHandle(), width, height);
                    GLFW.glfwWaitEvents();
                }
            }

            VK11.vkDeviceWaitIdle(context.getDevice().getHandle());

            cleanupSwapchain();

            createSwapChain();
            createImageViews();
            createFramebuffers();
        }

        public void cleanupSwapchain() {
            for(long swapChainFramebuffers : swapChainFramebuffers) {
                VK11.vkDestroyFramebuffer(context.getDevice().getHandle(), swapChainFramebuffers, null);
            }
            for(long swapChainImageView : swapChainImageViews) {
                VK11.vkDestroyImageView(context.getDevice().getHandle(), swapChainImageView, null);
            }
            KHRSwapchain.vkDestroySwapchainKHR(context.getDevice().getHandle(), swapChain, null);
        }

        private void cleanup() {
            cleanupSwapchain();
            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                VK11.vkDestroySemaphore(context.getDevice().getHandle(), imageAvailableSemaphores.get(i), null);
                VK11.vkDestroySemaphore(context.getDevice().getHandle(), renderFinishedSemaphores.get(i), null);
                VK11.vkDestroyFence(context.getDevice().getHandle(), inFlightFences.get(i), null);
                uniformBuffers.get(i).destroy(context.getMemoryAllocator().getHandle());
            }
            model.destroy(context.getMemoryAllocator().getHandle());
            VK11.vkDestroyDescriptorPool(context.getDevice().getHandle(), descriptorPool, null);
            VK11.vkDestroyCommandPool(context.getDevice().getHandle(), commandPool, null);
            VK11.vkDestroyPipeline(context.getDevice().getHandle(), graphicsPipeline, null);
            VK11.vkDestroyPipelineLayout(context.getDevice().getHandle(), pipelineLayout, null);
            VK11.vkDestroyDescriptorSetLayout(context.getDevice().getHandle(), descriptorSetLayout, null);
            VK11.vkDestroyRenderPass(context.getDevice().getHandle(), renderPass, null);
            context.destroy();
        }
    }

    public static void main(String[] args) {
        HelloTriangleApplication app = new HelloTriangleApplication();

        try {
            app.run();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}