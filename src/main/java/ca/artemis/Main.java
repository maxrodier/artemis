package ca.artemis;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
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
import org.lwjgl.vulkan.VkQueue;
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
import org.lwjgl.vulkan.VkViewport;

import ca.artemis.Util.ShaderStageKind;

public class Main {

    private static class HelloTriangleApplication {

        private static final int WIDTH = 800;
        private static final int HEIGHT = 600;

        private static final int MAX_FRAMES_IN_FLIGHT = 2;

        private static final int msaaSamples = VK11.VK_SAMPLE_COUNT_1_BIT;

        private long window;
        private VkInstance instance;
        private long surface;
        private VkPhysicalDevice physicalDevice;
        private VkDevice device;
        private VkQueue graphicsQueue;
        private VkQueue presentQueue;
        private long swapChain;
        private int swapChainImageFormat;
        private VkExtent2D swapChainExtent;
        private List<Long> swapChainImages;
        private List<Long> swapChainImageViews;
        private long renderPass;
        //private long descriptorSetLayout;
        private long pipelineLayout;
        private long graphicsPipeline;
        private List<Long> swapChainFramebuffers;
        private long commandPool;
        private List<CommandBuffer> commandBuffers; //One per frame in flight
        private List<Long> imageAvailableSemaphores; //One per frame in flight
        private List<Long> renderFinishedSemaphores; //One per frame in flight
        private List<Long> inFlightFences; //One per frame in flight

        private int currentFrame = 0;
        private boolean framebufferResized = false;

        public void run() {
            initWindow();
            initVulkan();
            mainLoop();
            cleanup();
        }

        private void initWindow() {
            GLFW.glfwInit();

            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
            //GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

            window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Vulkan", GLFW.GLFW_FALSE, GLFW.GLFW_FALSE);

            GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallbackI() {
                @Override
                public void invoke(long window, int width, int height) {
                    framebufferResized = true;
                }
            });
        }

        private void initVulkan() {
            createInstance();
            createSurface();
            pickPhysicalDevice();
            createLogicalDevice();
            createSwapChain();
            createImageViews();
            createRenderPass();
            createGraphicsPipeline();
            createFramebuffers();
            createCommandPool();
            createCommandBuffers();
            createSyncObjects();
        }

        private void createInstance() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer ppEnabledExtentions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        
                ByteBuffer[] pEnabledLayerNames = {
                        stack.UTF8("VK_LAYER_KHRONOS_validation")
                };
                PointerBuffer ppEnabledLayerNames = stack.callocPointer(pEnabledLayerNames.length);
                for(ByteBuffer pEnabledLayerName : pEnabledLayerNames) {
                    ppEnabledLayerNames.put(pEnabledLayerName);
                }
                ppEnabledLayerNames.flip();
                
                VkApplicationInfo pApplicationInfo = VkApplicationInfo.callocStack(stack);
                pApplicationInfo.sType(VK11.VK_STRUCTURE_TYPE_APPLICATION_INFO);
                pApplicationInfo.pApplicationName(stack.UTF8("Hello Triangle"));
                pApplicationInfo.applicationVersion(VK11.VK_MAKE_VERSION(1, 0, 0));
                pApplicationInfo.pEngineName(stack.UTF8("NO Engine"));
                pApplicationInfo.engineVersion(VK11.VK_MAKE_VERSION(1, 0, 0));
                pApplicationInfo.apiVersion(VK11.VK_API_VERSION_1_1);;
        
                VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.callocStack(stack);
                pCreateInfo.sType(VK11.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
                pCreateInfo.pApplicationInfo(pApplicationInfo);
                pCreateInfo.ppEnabledLayerNames(ppEnabledLayerNames);
                pCreateInfo.ppEnabledExtensionNames(ppEnabledExtentions);
        
                PointerBuffer pInstance = stack.callocPointer(1);
                if(VK11.vkCreateInstance(pCreateInfo, null, pInstance) != VK11.VK_SUCCESS) {
                    throw new AssertionError("Failed to create vulkan instance!");
                }
                instance = new VkInstance(pInstance.get(0), pCreateInfo);
            }
        }

        private void createSurface() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer pSurface = stack.callocLong(1);
                if(GLFWVulkan.glfwCreateWindowSurface(instance, window, null, pSurface) != VK11.VK_SUCCESS) {
                    throw new AssertionError("Failed to create window surface");
                }
                surface = pSurface.get(0);
            }
        }

        private void pickPhysicalDevice() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer pPhysicalDeviceCount = stack.callocInt(1);
                VK11.vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, null);
        
                PointerBuffer pPhysicalDevices = stack.callocPointer(pPhysicalDeviceCount.get(0));
                VK11.vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, pPhysicalDevices);
                
                physicalDevice = new VkPhysicalDevice(pPhysicalDevices.get(0), instance);
            }
        }

        private void createLogicalDevice() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                List<QueueFamily> queueFamilies = QueueFamily.getQueueFamilies(surface, physicalDevice);

                VkDeviceQueueCreateInfo.Buffer ppQueueCreateInfos = VkDeviceQueueCreateInfo.callocStack(1, stack);
                VkDeviceQueueCreateInfo pQueueCreateInfo = ppQueueCreateInfos.get(0);
                pQueueCreateInfo.sType(VK11.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                pQueueCreateInfo.flags(0);
                pQueueCreateInfo.queueFamilyIndex(queueFamilies.get(0).getIndex()); //TODO : Select right Queue family
                pQueueCreateInfo.pQueuePriorities(stack.callocFloat(1).put(1.0f).flip());
                
                ByteBuffer[] pEnabledExtensionNames = {
                        stack.UTF8(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
                };
                PointerBuffer ppEnabledExtensionNames = stack.callocPointer(pEnabledExtensionNames.length);
                for (ByteBuffer pEnabledExtensionName : pEnabledExtensionNames)
                    ppEnabledExtensionNames.put(pEnabledExtensionName);
                ppEnabledExtensionNames.flip();
                
                VkPhysicalDeviceFeatures physicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc();
                physicalDeviceFeatures.samplerAnisotropy(true);

                VkDeviceCreateInfo pCreateInfo = VkDeviceCreateInfo.callocStack(stack);
                pCreateInfo.sType(VK11.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
                pCreateInfo.flags(0);
                pCreateInfo.pQueueCreateInfos(ppQueueCreateInfos);
                pCreateInfo.ppEnabledExtensionNames(ppEnabledExtensionNames);
                pCreateInfo.pEnabledFeatures(physicalDeviceFeatures);
                
                PointerBuffer pDevice = stack.callocPointer(1);
                if(VK11.vkCreateDevice(physicalDevice, pCreateInfo, null, pDevice) != VK11.VK_SUCCESS) {
                    throw new AssertionError("Failed to create logical device");
                }
                device = new VkDevice(pDevice.get(0), physicalDevice, pCreateInfo);
	            
                PointerBuffer pGraphicsQueue = stack.callocPointer(1);
                PointerBuffer pPresentQueue = stack.callocPointer(1);
                
                VK11.vkGetDeviceQueue(device, queueFamilies.get(0).getIndex(), 0, pGraphicsQueue);
	            VK11.vkGetDeviceQueue(device, queueFamilies.get(0).getIndex(), 0, pPresentQueue);

                graphicsQueue = new VkQueue(pGraphicsQueue.get(0), device);
                presentQueue = new VkQueue(pPresentQueue.get(0), device);
            }
        }

        private void createSwapChain() {

            SwapChainSupportDetails swapChainSupport = SwapChainSupportDetails.querySwapChainSupport(surface, physicalDevice);

            VkSurfaceFormatKHR surfaceFormat = swapChainSupport.chooseSwapSurfaceFormat();
            int presentMode = swapChainSupport.chooseSwapPresentMode();
            VkExtent2D extent = swapChainSupport.chooseSwapExtent(surface);
    
            int imageCount = swapChainSupport.getCapabilities().minImageCount() + 1;
            if(swapChainSupport.getCapabilities().maxImageCount() > 0 && imageCount > swapChainSupport.getCapabilities().maxImageCount()) {
                imageCount = swapChainSupport.getCapabilities().maxImageCount();
            }

            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkSwapchainCreateInfoKHR pCreateInfo = VkSwapchainCreateInfoKHR.callocStack(stack);
                pCreateInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
                pCreateInfo.surface(surface);
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
                KHRSwapchain.vkCreateSwapchainKHR(device, pCreateInfo, null, pSwapchain);
                swapChain = pSwapchain.get(0);

                IntBuffer pSwapchainImageCount = stack.callocInt(1);
                KHRSwapchain.vkGetSwapchainImagesKHR(device, swapChain, pSwapchainImageCount, null);

                LongBuffer pSwapchainImages = stack.callocLong(pSwapchainImageCount.get(0));
                KHRSwapchain.vkGetSwapchainImagesKHR(device, swapChain, pSwapchainImageCount, pSwapchainImages);

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
                swapChainImageViews.add(Util.createImageView(device, swapChainImage, swapChainImageFormat, VK11.VK_IMAGE_ASPECT_COLOR_BIT, 1));
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
                if(VK11.vkCreateRenderPass(device, renderPassInfo, null, pRenderPass) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create render pass!");
                }
                renderPass = pRenderPass.get(0);
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
                if(VK11.vkCreateShaderModule(device, vertShaderCreateInfo, null, pVertShaderModule) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create shader module!");
                }

                LongBuffer pFragShaderModule = stack.callocLong(1);
                if(VK11.vkCreateShaderModule(device, fragShaderCreateInfo, null, pFragShaderModule) != VK11.VK_SUCCESS) {
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

                //VkVertexInputBindingDescription.Buffer bindingDescription = Vertex.getBindingDescriptions(stack);
                //VkVertexInputAttributeDescription.Buffer attributeDescriptions = Vertex.getAttributeDescriptions(stack);

                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
                vertexInputInfo.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
                vertexInputInfo.pVertexBindingDescriptions(null); //TODO: Change
                vertexInputInfo.pVertexAttributeDescriptions(null); //TODO: Change
        
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
                rasterizer.frontFace(VK11.VK_FRONT_FACE_CLOCKWISE);
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
        
                //LongBuffer setLayouts = stack.callocLong(1);
                //setLayouts.put(0, descriptorSetLayout);

                VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
                pipelineLayoutInfo.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
                pipelineLayoutInfo.pSetLayouts(null); //TODO: Change

                LongBuffer pPipelineLayout = stack.callocLong(1);
                if(VK11.vkCreatePipelineLayout(device, pipelineLayoutInfo, null, pPipelineLayout) != VK11.VK_SUCCESS) {
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
                if(VK11.vkCreateGraphicsPipelines(device, VK11.VK_NULL_HANDLE, pCreateInfos, null, pGraphicsPipeline) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create graphics pipeline!");
                }
                graphicsPipeline = pGraphicsPipeline.get(0);

                VK11.vkDestroyShaderModule(device, fragShaderModule, null);
                VK11.vkDestroyShaderModule(device, vertShaderModule, null);
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
                    if(VK11.vkCreateFramebuffer(device, framebufferInfo, null, pSwapChainFramebuffer) != VK11.VK_SUCCESS) {
                        throw new RuntimeException("failed to create framebuffer!");
                    }
                    swapChainFramebuffers.add(pSwapChainFramebuffer.get(0));
                }
            }
        }

        public void createCommandPool() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                List<QueueFamily> queueFamilies = QueueFamily.getQueueFamilies(surface, physicalDevice);
        
                VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
                poolInfo.sType(VK11.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
                poolInfo.flags(VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
                poolInfo.queueFamilyIndex(queueFamilies.get(0).getIndex());
        
                LongBuffer pCommandPool = stack.callocLong(1);
                if(VK11.vkCreateCommandPool(device, poolInfo, null, pCommandPool) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create graphics command pool!");
                }
                commandPool = pCommandPool.get(0);
            }
        }

        public void createCommandBuffers() {
            commandBuffers = new ArrayList<>();
            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                commandBuffers.add(new PrimaryCommandBuffer(device, commandPool));
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
                    error = (VK11.vkCreateSemaphore(device, semaphoreInfo, null, pImageAvailableSemaphores) != VK11.VK_SUCCESS) && error;
                    imageAvailableSemaphores.add(pImageAvailableSemaphores.get(0));

                    LongBuffer pRenderFinishedSemaphores = stack.callocLong(1);
                    error = (VK11.vkCreateSemaphore(device, semaphoreInfo, null, pRenderFinishedSemaphores) != VK11.VK_SUCCESS) && error;
                    renderFinishedSemaphores.add(pRenderFinishedSemaphores.get(0));

                    LongBuffer pInFlightFences = stack.callocLong(1);
                    error = (VK11.vkCreateFence(device, fenceInfo, null, pInFlightFences) != VK11.VK_SUCCESS) && error;
                    inFlightFences.add(pInFlightFences.get(0));
                    
                    if(error) {
                        throw new RuntimeException("Failed to create synchronization objects for a frame!");
                    }
                }
            }
        }

        private void mainLoop() {
            while(!GLFW.glfwWindowShouldClose(window)) {
                GLFW.glfwPollEvents();
                drawFrame();
            }
            VK11.vkDeviceWaitIdle(device);
        }

        private void drawFrame() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer pFence = stack.callocLong(1);
                pFence.put(0, inFlightFences.get(currentFrame));
                VK11.vkWaitForFences(device, pFence, true, Long.MAX_VALUE);

                IntBuffer pImageIndex = stack.callocInt(1);
                int result = KHRSwapchain.vkAcquireNextImageKHR(device, swapChain, Long.MAX_VALUE, imageAvailableSemaphores.get(currentFrame), VK11.VK_NULL_HANDLE, pImageIndex);
                if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                    recreateSwapchain();
                    return;
                } else if(result != VK11.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                    throw new RuntimeException("Failed to acquire swapchain image!");
                }
                int imageIndex = pImageIndex.get(0);

                VK11.vkResetFences(device, pFence);

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

                if(VK11.vkQueueSubmit(graphicsQueue, submitInfo, inFlightFences.get(currentFrame)) != VK11.VK_SUCCESS) {
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

                result = KHRSwapchain.vkQueuePresentKHR(presentQueue, presentInfo);
                if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR || framebufferResized) {
                    framebufferResized = false;
                    recreateSwapchain();
                } else if (result != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to present swap chain image!");
                }

                currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
            }
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
    
                VK11.vkCmdDraw(commandBuffer, 3, 1, 0, 0);
    
            VK11.vkCmdEndRenderPass(commandBuffer);
    
            if(VK11.vkEndCommandBuffer(commandBuffer) != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to record command buffer!");
            }
        }

        public void recreateSwapchain() {
            //Code to wait recreation of the swapchain while minimized //Can this pause a game also? //This has not been tested
            try(MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer width = stack.callocInt(1), height = stack.callocInt(1);
                GLFW.glfwGetFramebufferSize(window, width, height);
                while (width.get(0) == 0 || height.get(0) == 0) {
                    System.out.println("Is minimized");
                    width.clear(); height.clear();
                    GLFW.glfwGetFramebufferSize(window, width, height);
                    GLFW.glfwWaitEvents();
                }
            }

            VK11.vkDeviceWaitIdle(device);

            cleanupSwapchain();

            createSwapChain();
            createImageViews();
            createFramebuffers();
        }

        public void cleanupSwapchain() {
            for(long swapChainFramebuffers : swapChainFramebuffers) {
                VK11.vkDestroyFramebuffer(device, swapChainFramebuffers, null);
            }
            for(long swapChainImageView : swapChainImageViews) {
                VK11.vkDestroyImageView(device, swapChainImageView, null);
            }
            KHRSwapchain.vkDestroySwapchainKHR(device, swapChain, null);
        }

        private void cleanup() {
            cleanupSwapchain();
            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                VK11.vkDestroySemaphore(device, imageAvailableSemaphores.get(i), null);
                VK11.vkDestroySemaphore(device, renderFinishedSemaphores.get(i), null);
                VK11.vkDestroyFence(device, inFlightFences.get(i), null);
            }
            VK11.vkDestroyCommandPool(device, commandPool, null);
            VK11.vkDestroyPipeline(device, graphicsPipeline, null);
            VK11.vkDestroyPipelineLayout(device, pipelineLayout, null);
            VK11.vkDestroyRenderPass(device, renderPass, null);
            VK11.vkDestroyDevice(device, null);
            KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
            VK11.vkDestroyInstance(instance, null);
            GLFW.glfwDestroyWindow(window);
            GLFW.glfwTerminate();
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