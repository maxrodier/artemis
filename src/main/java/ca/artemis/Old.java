package ca.artemis;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
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
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
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
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
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
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import ca.artemis.Util.ShaderStageKind;
import ca.artemis.Util.VkImage;

public class Old {
    
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final String MODEL_PATH = "models/viking.obj";
    private static final String TEXTURE_PATH = "textures/viking.png";

    private static final int MAX_FRAMES_IN_FLIGHT = 2;

    static class HelloTriangleApplication {

        private static final int msaaSamples = VK11.VK_SAMPLE_COUNT_1_BIT;

        private long window;

        private VkInstance instance;
        private long surface;
        private VkPhysicalDevice physicalDevice;
        private VkDevice device;
        private long allocator;
        private VkQueue graphicsQueue;
        private VkQueue presentQueue;
        private long swapChain;
        private List<Long> swapChainImages;
        private List<Long> swapChainImageViews;
        private int swapChainImageFormat;
        private VkExtent2D swapChainExtent;
        private long renderPass;
        private long descriptorSetLayout;
        private long pipelineLayout;
        private long graphicsPipeline;
        private long commandPool;
        private VkImage colorImage;
        private VkImage depthImage;
        private long colorImageView;
        private long depthImageView;
        private List<Long> swapChainFramebuffers;
        private Texture texture;
        private Mesh model;
        private List<VulkanBuffer> uniformBuffers;
        private long descriptorPool;
        private List<Long> descriptorSets;
        private List<CommandBuffer> commandBuffers;
        private List<Long> imageAvailableSemaphores;
        private List<Long> renderFinishedSemaphores;
        private List<Long> inFlightFences;

        private int currentFrame = 0;
        private boolean framebufferResized = false;
        private long startTime;

        public void run() {
            initWindow();
            initVulkan();
            System.out.println("Done");
            mainLoop();
            cleanup();
        }

        private void initWindow() {
            GLFW.glfwInit();
            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);

            window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Vulkan", 0, 0);
            //TODO: glfwSetWindowUserPointer(window, this);
            //TODO: glfwSetFramebufferSizeCallback(window, framebufferResizeCallback);
        }

        private void initVulkan() {
            createInstance();
            GLFWErrorCallback.createPrint().set();
            createSurface();
            pickPhysicalDevice();
            createLogicalDevice();
            createMemoryAllocator();
            createSwapChain();
            createImageViews();
            createRenderPass();
            createDescriptorSetLayout();
            createGraphicsPipeline();
            createCommandPool();
            createColorResources();
            createDepthResources();
            createFramebuffers();
            createTexture();
            loadModel();
            createUniformBuffers();
            createDescriptorPool();
            createDescriptorSets();
            createCommandBuffers();
            createSyncObjects();
        }

        private void mainLoop() {
            startTime = System.currentTimeMillis();
            while (!GLFW.glfwWindowShouldClose(window)) {
                GLFW.glfwPollEvents();
                drawFrame();
            }
            VK11.vkDeviceWaitIdle(device);
        }

        void cleanupSwapChain() {

        }

        private void cleanup() {

        }

        private void createInstance() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer ppRequiredExtentions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        
                ByteBuffer[] pEnabledLayerNames = {
                        stack.UTF8("VK_LAYER_KHRONOS_validation")
                };
                PointerBuffer ppEnabledLayerNames = stack.callocPointer(pEnabledLayerNames.length);
                for(ByteBuffer pEnabledLayerName : pEnabledLayerNames)
                    ppEnabledLayerNames.put(pEnabledLayerName);
                ppEnabledLayerNames.flip();
                
                VkApplicationInfo pApplicationInfo = VkApplicationInfo.callocStack(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(stack.UTF8("Hello Triangle"))
                    .applicationVersion(VK11.VK_MAKE_VERSION(1, 0, 0))
                    .pEngineName(stack.UTF8("NO Engine"))
                    .engineVersion(VK11.VK_MAKE_VERSION(1, 0, 0))
                    .apiVersion(VK11.VK_API_VERSION_1_1);
        
                VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.callocStack(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pApplicationInfo(pApplicationInfo)
                    .ppEnabledLayerNames(ppEnabledLayerNames)
                    .ppEnabledExtensionNames(ppRequiredExtentions);
        
                PointerBuffer pInstance = stack.callocPointer(1);
                int error = VK11.vkCreateInstance(pCreateInfo, null, pInstance);
                if(error != VK11.VK_SUCCESS)
                    throw new AssertionError("Failed to create vulkan instance");
        
                instance = new VkInstance(pInstance.get(0), pCreateInfo);
            }
        }

        private void createSurface() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer pSurface = stack.callocLong(1);
                int error = GLFWVulkan.glfwCreateWindowSurface(instance, window, null, pSurface);
                if(error != VK11.VK_SUCCESS) {
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
                List<QueueFamily> indices = QueueFamily.getQueueFamilies(surface, physicalDevice);

                VkDeviceQueueCreateInfo.Buffer ppQueueCreateInfos = VkDeviceQueueCreateInfo.callocStack(1, stack);
                ppQueueCreateInfos.get(0)
                    .sType(VK11.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    .flags(0)
                    .queueFamilyIndex(indices.get(0).getIndex()) //TODO : Select right Queue family
                    .pQueuePriorities(stack.callocFloat(1).put(0.0f).flip());
                
                ByteBuffer[] pEnabledExtensionNames = {
                        stack.UTF8(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
                };
                PointerBuffer ppEnabledExtensionNames = stack.callocPointer(pEnabledExtensionNames.length);
                for (ByteBuffer pEnabledExtensionName : pEnabledExtensionNames)
                    ppEnabledExtensionNames.put(pEnabledExtensionName);
                ppEnabledExtensionNames.flip();
                
                VkPhysicalDeviceFeatures physicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc();
                physicalDeviceFeatures.samplerAnisotropy(true);

                VkDeviceCreateInfo pCreateInfo = VkDeviceCreateInfo.callocStack(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .flags(0)
                    .pQueueCreateInfos(ppQueueCreateInfos)
                    .ppEnabledExtensionNames(ppEnabledExtensionNames)
                    .pEnabledFeatures(physicalDeviceFeatures);
                
                PointerBuffer pDevice = stack.callocPointer(1);
                int error = VK11.vkCreateDevice(physicalDevice, pCreateInfo, null, pDevice);
                if(error != VK11.VK_SUCCESS)
                    throw new AssertionError("Failed to create logical device");
                    
                device = new VkDevice(pDevice.get(0), physicalDevice, pCreateInfo);
	            

                PointerBuffer pGraphicsQueue = stack.callocPointer(1);
                PointerBuffer pPresentQueue = stack.callocPointer(1);
                
                VK11.vkGetDeviceQueue(device, indices.get(0).getIndex(), 0, pGraphicsQueue);
	            VK11.vkGetDeviceQueue(device, indices.get(0).getIndex(), 0, pPresentQueue);

                graphicsQueue = new VkQueue(pGraphicsQueue.get(0), device);
                presentQueue = new VkQueue(pPresentQueue.get(0), device);
            } 
        }

        private void createMemoryAllocator() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VmaVulkanFunctions pVulkanFunctions = VmaVulkanFunctions.callocStack(stack);
                pVulkanFunctions.set(instance, device);

                VmaAllocatorCreateInfo pCreateInfo = VmaAllocatorCreateInfo.callocStack();
                pCreateInfo.physicalDevice(physicalDevice);
                pCreateInfo.device(device);
                pCreateInfo.pVulkanFunctions(pVulkanFunctions);

                PointerBuffer pAllocator = stack.callocPointer(1);
                int error = Vma.vmaCreateAllocator(pCreateInfo, pAllocator);
                if(error != VK11.VK_SUCCESS)
                    throw new AssertionError("Failed to create vulkan memory allocator");

                allocator = pAllocator.get(0);
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
                VkSwapchainCreateInfoKHR pCreateInfo = VkSwapchainCreateInfoKHR.callocStack(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(surface)
                    .minImageCount(imageCount)
                    .imageFormat(surfaceFormat.format())
                    .imageColorSpace(surfaceFormat.colorSpace())
                    .imageExtent(extent)
                    .imageArrayLayers(1)
                    .imageUsage(VK11.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                    .imageSharingMode(VK11.VK_SHARING_MODE_EXCLUSIVE)
                    .preTransform(swapChainSupport.getCapabilities().currentTransform())
                    .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .presentMode(presentMode)
                    .clipped(true);
    
                LongBuffer pSwapchain = stack.callocLong(1);
                KHRSwapchain.vkCreateSwapchainKHR(device, pCreateInfo, null, pSwapchain);
                swapChain = pSwapchain.get(0);

                IntBuffer pSwapchainImageCount = stack.callocInt(1);
                KHRSwapchain.vkGetSwapchainImagesKHR(device, swapChain, pSwapchainImageCount, null);

                LongBuffer pSwapchainImages = MemoryUtil.memCallocLong(pSwapchainImageCount.get(0));
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
                VkAttachmentDescription colorAttachment = VkAttachmentDescription.callocStack(stack)
                    .format(swapChainImageFormat)
                    .samples(msaaSamples)
                    .loadOp(VK11.VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK11.VK_ATTACHMENT_STORE_OP_STORE)
                    .stencilLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                    .stencilStoreOp(VK11.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                    .initialLayout(VK11.VK_IMAGE_LAYOUT_UNDEFINED)
                    .finalLayout(VK11.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

                VkAttachmentDescription depthAttachment = VkAttachmentDescription.callocStack(stack)
                    .format(VK11.VK_FORMAT_D32_SFLOAT) //TODO: This can be a problem
                    .samples(msaaSamples)
                    .loadOp(VK11.VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK11.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                    .stencilLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                    .stencilStoreOp(VK11.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                    .initialLayout(VK11.VK_IMAGE_LAYOUT_UNDEFINED)
                    .finalLayout(VK11.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

                VkAttachmentDescription colorAttachmentResolve = VkAttachmentDescription.callocStack(stack)
                    .format(swapChainImageFormat)
                    .samples(VK11.VK_SAMPLE_COUNT_1_BIT)
                    .loadOp(VK11.VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK11.VK_ATTACHMENT_STORE_OP_STORE)
                    .stencilLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                    .stencilStoreOp(VK11.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                    .initialLayout(VK11.VK_IMAGE_LAYOUT_UNDEFINED)
                    .finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

                VkAttachmentReference.Buffer pColorAttachmentRef = VkAttachmentReference.callocStack(1, stack);
                pColorAttachmentRef.get(0)
                    .attachment(0)
                    .layout(VK11.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

                
                VkAttachmentReference pDepthAttachmentRef = VkAttachmentReference.callocStack(stack)
                    .attachment(1)
                    .layout(VK11.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            
                VkAttachmentReference.Buffer pColorAttachmentResolveRef = VkAttachmentReference.callocStack(1, stack);
                pColorAttachmentResolveRef.get(0)    
                    .attachment(2)
                    .layout(VK11.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

                VkSubpassDescription.Buffer pSubpasses = VkSubpassDescription.callocStack(1, stack);
                pSubpasses.get(0)
                    .pipelineBindPoint(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS)
                    .colorAttachmentCount(1)
                    .pColorAttachments(pColorAttachmentRef)
                    .pDepthStencilAttachment(pDepthAttachmentRef)
                    .pResolveAttachments(pColorAttachmentResolveRef);

                VkSubpassDependency.Buffer pDependencies = VkSubpassDependency.callocStack(1, stack);
                pDependencies.get(0)
                    .srcSubpass(VK11.VK_SUBPASS_EXTERNAL)
                    .dstSubpass(0)
                    .srcStageMask(VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK11.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
                    .srcAccessMask(0)
                    .dstStageMask(VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK11.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
                    .dstAccessMask(VK11.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK11.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);

                VkAttachmentDescription.Buffer pAttachments = VkAttachmentDescription.callocStack(3, stack);
                pAttachments.put(0, colorAttachment);
                pAttachments.put(1, depthAttachment);
                pAttachments.put(2, colorAttachmentResolve);

                VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.callocStack(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                    .pAttachments(pAttachments)
                    .pSubpasses(pSubpasses)
                    .pDependencies(pDependencies);
                
                LongBuffer pRenderPass = stack.callocLong(1);
                if(VK11.vkCreateRenderPass(device, renderPassInfo, null, pRenderPass) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create render pass!");
                }
                renderPass = pRenderPass.get(0);
            }
        }

        public void createDescriptorSetLayout() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorSetLayoutBinding.Buffer pBindings = VkDescriptorSetLayoutBinding.callocStack(2, stack);

                VkDescriptorSetLayoutBinding uboLayoutBinding = pBindings.get(0);
                uboLayoutBinding.binding(0);
                uboLayoutBinding.descriptorCount(1);
                uboLayoutBinding.descriptorType(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                uboLayoutBinding.pImmutableSamplers(null);
                uboLayoutBinding.stageFlags(VK11.VK_SHADER_STAGE_VERTEX_BIT);
        
                VkDescriptorSetLayoutBinding samplerLayoutBinding = pBindings.get(1);
                samplerLayoutBinding.binding(1);
                samplerLayoutBinding.descriptorCount(1);
                samplerLayoutBinding.descriptorType(VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                samplerLayoutBinding.pImmutableSamplers(null);
                samplerLayoutBinding.stageFlags(VK11.VK_SHADER_STAGE_FRAGMENT_BIT);
        
                VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
                layoutInfo.sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
                layoutInfo.pBindings(pBindings);
        
                LongBuffer pSetLayout = stack.callocLong(1);
                if(VK11.vkCreateDescriptorSetLayout(device, layoutInfo, null, pSetLayout) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create descriptor set layout!");
                }
                descriptorSetLayout = pSetLayout.get(0);
            }
        }

        public void createGraphicsPipeline() {
            ByteBuffer vertShaderCode = Util.compileShaderFile("shaders/simple.vert", ShaderStageKind.VERTEX_SHADER).getBytecode();
            ByteBuffer fragShaderCode = Util.compileShaderFile("shaders/simple.frag", ShaderStageKind.FRAGMENT_SHADER).getBytecode();
        
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkShaderModuleCreateInfo vertShaderCreateInfo = VkShaderModuleCreateInfo.callocStack(stack);
                vertShaderCreateInfo.sType(VK11.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
                vertShaderCreateInfo.pCode(vertShaderCode);

                VkShaderModuleCreateInfo fragShaderCreateInfo = VkShaderModuleCreateInfo.callocStack(stack);
                fragShaderCreateInfo.sType(VK11.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
                fragShaderCreateInfo.pCode(fragShaderCode);
        
                LongBuffer pVertShaderModule = stack.callocLong(1);
                if(VK11.vkCreateShaderModule(device, vertShaderCreateInfo, null, pVertShaderModule) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create shader module!");
                }

                LongBuffer pFragShaderModule = stack.callocLong(1);
                if(VK11.vkCreateShaderModule(device, fragShaderCreateInfo, null, pFragShaderModule) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create shader module!");
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

                VkVertexInputBindingDescription.Buffer bindingDescription = Vertex.getBindingDescriptions(stack);
                VkVertexInputAttributeDescription.Buffer attributeDescriptions = Vertex.getAttributeDescriptions(stack);

                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
                vertexInputInfo.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
                vertexInputInfo.pVertexBindingDescriptions(bindingDescription);
                vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions);
        
                VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
                inputAssembly.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
                inputAssembly.topology(VK11.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
                inputAssembly.primitiveRestartEnable(true);

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
        
                VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
                depthStencil.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
                depthStencil.depthTestEnable(true);
                depthStencil.depthWriteEnable(true);
                depthStencil.depthCompareOp(VK11.VK_COMPARE_OP_LESS);
                depthStencil.depthBoundsTestEnable(false);
                depthStencil.stencilTestEnable(false);
        
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
        
                LongBuffer setLayouts = stack.callocLong(1);
                setLayouts.put(0, descriptorSetLayout);

                VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
                pipelineLayoutInfo.sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
                pipelineLayoutInfo.pSetLayouts(setLayouts);

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
                pipelineInfo.pDepthStencilState(depthStencil);
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

        public void createCommandPool() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                List<QueueFamily> indices = QueueFamily.getQueueFamilies(surface, physicalDevice);
        
                VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
                poolInfo.sType(VK11.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
                poolInfo.flags(VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
                poolInfo.queueFamilyIndex(indices.get(0).getIndex());
        
                LongBuffer pCommandPool = stack.callocLong(1);
                if(VK11.vkCreateCommandPool(device, poolInfo, null, pCommandPool) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create graphics command pool!");
                }
                commandPool = pCommandPool.get(0);
            }
        }

        public void createColorResources() {
            int colorFormat = swapChainImageFormat;
    
            colorImage = Util.createImage(device, allocator, swapChainExtent.width(), swapChainExtent.height(), 1, msaaSamples, colorFormat, VK11.VK_IMAGE_TILING_OPTIMAL, VK11.VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT | VK11.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT, Vma.VMA_MEMORY_USAGE_GPU_ONLY);
            colorImageView = Util.createImageView(device, colorImage.image, colorFormat, VK11.VK_IMAGE_ASPECT_COLOR_BIT, 1);
        }
    
        public void createDepthResources() {
            int depthFormat = VK11.VK_FORMAT_D32_SFLOAT;
    
            depthImage = Util.createImage(device, allocator, swapChainExtent.width(), swapChainExtent.height(), 1, msaaSamples, depthFormat, VK11.VK_IMAGE_TILING_OPTIMAL, VK11.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, Vma.VMA_MEMORY_USAGE_GPU_ONLY);
            depthImageView = Util.createImageView(device, depthImage.image, depthFormat, VK11.VK_IMAGE_ASPECT_DEPTH_BIT, 1);
        }

        public void createFramebuffers() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                swapChainFramebuffers = new ArrayList<>();

                for (long swapChainImageView : swapChainImageViews) {
                    LongBuffer pAttachments = stack.callocLong(3);
                    pAttachments.put(0, colorImageView);
                    pAttachments.put(1, depthImageView);
                    pAttachments.put(2, swapChainImageView);
        
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

        public void createTexture() {
            texture = Texture.createTexture(device, allocator, graphicsQueue, commandPool, TEXTURE_PATH);
        }
        
        public void loadModel() {
            model = Mesh.loadModel(device, allocator, graphicsQueue, commandPool, MODEL_PATH); 
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
                    .build(allocator);

                uniformBuffers.add(indexBuffer);
            }
        }

        public void createDescriptorPool() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.callocStack(2, stack);
                VkDescriptorPoolSize poolSize;

                poolSize = poolSizes.get(0);
                poolSize.type(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                poolSize.descriptorCount(MAX_FRAMES_IN_FLIGHT);

                poolSize = poolSizes.get(1);
                poolSize.type(VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                poolSize.descriptorCount(MAX_FRAMES_IN_FLIGHT);
        
                VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
                poolInfo.sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
                poolInfo.pPoolSizes(poolSizes);
                poolInfo.maxSets(MAX_FRAMES_IN_FLIGHT);
        
                LongBuffer pDescriptorPool = stack.callocLong(1);
                if(VK11.vkCreateDescriptorPool(device, poolInfo, null, pDescriptorPool) != VK11.VK_SUCCESS) {
                    throw new RuntimeException("failed to create descriptor pool!");
                }
                descriptorPool = pDescriptorPool.get(0);
            }
        }

        void createDescriptorSets() {
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
                if(VK11.vkAllocateDescriptorSets(device, allocInfo, pDescriptorSets) != VK11.VK_SUCCESS) {
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
        
                    VkDescriptorImageInfo.Buffer pImageInfo = VkDescriptorImageInfo.callocStack(1, stack);
                    VkDescriptorImageInfo imageInfo = pImageInfo.get(0);
                    imageInfo.imageLayout(VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                    imageInfo.imageView(texture.imageView);
                    imageInfo.sampler(texture.sampler);
        
                    VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(2, stack);
                    VkWriteDescriptorSet descriptorWrite;

                    descriptorWrite = descriptorWrites.get(0);
                    descriptorWrite.sType(VK11.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                    descriptorWrite.dstSet(descriptorSets.get(i));
                    descriptorWrite.dstBinding(0);
                    descriptorWrite.dstArrayElement(0);
                    descriptorWrite.descriptorType(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                    descriptorWrite.descriptorCount(1);
                    descriptorWrite.pBufferInfo(pBufferInfo);
        
                    descriptorWrite = descriptorWrites.get(1);
                    descriptorWrite.sType(VK11.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                    descriptorWrite.dstSet(descriptorSets.get(i));
                    descriptorWrite.dstBinding(1);
                    descriptorWrite.dstArrayElement(0);
                    descriptorWrite.descriptorType(VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                    descriptorWrite.descriptorCount(1);
                    descriptorWrite.pImageInfo(pImageInfo);
        
                    VK11.vkUpdateDescriptorSets(device, descriptorWrites, null);
                }
            }
        }

        public void createCommandBuffers() {
            commandBuffers = new ArrayList<>();
            for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                commandBuffers.add(new PrimaryCommandBuffer(device, commandPool));
            }
        }

        public void createSyncObjects() {
            imageAvailableSemaphores = new ArrayList<>();
            renderFinishedSemaphores = new ArrayList<>();
            inFlightFences = new ArrayList<>();
    
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
                semaphoreInfo.sType(VK11.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
        
                VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
                fenceInfo.sType(VK11.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
                fenceInfo.flags(VK11.VK_FENCE_CREATE_SIGNALED_BIT);
        
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
                        throw new RuntimeException("failed to create synchronization objects for a frame!");
                    }
                }
            }
        }

        void drawFrame() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer pFence = stack.callocLong(1);
                pFence.put(0, inFlightFences.get(currentFrame));
                VK11.vkWaitForFences(device, pFence, true, Long.MAX_VALUE);

                IntBuffer pImageIndex = stack.callocInt(1);
                int result = KHRSwapchain.vkAcquireNextImageKHR(device, swapChain, Long.MAX_VALUE, imageAvailableSemaphores.get(currentFrame), VK11.VK_NULL_HANDLE, pImageIndex);
                if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                    //recreateSwapChain();
                    return;
                } else if(result != VK11.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                    throw new RuntimeException("failed to acquire swap chain image!");
                }
                int imageIndex = pImageIndex.get(0);

                updateUniformBuffer(currentFrame, stack);

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
                    throw new RuntimeException("failed to submit draw command buffer!");
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
                    //recreateSwapChain();
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
                throw new RuntimeException("failed to begin recording command buffer!");
            }
    
            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK11.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(renderPass);
            renderPassInfo.framebuffer(swapChainFramebuffers.get(imageIndex));
            renderPassInfo.renderArea().offset().x(0);
            renderPassInfo.renderArea().offset().y(0);
            renderPassInfo.renderArea().extent(swapChainExtent);
    
            VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
            clearValues.get(0).color().float32(0, 0.0f);
            clearValues.get(0).color().float32(1, 0.0f);
            clearValues.get(0).color().float32(2, 0.0f);
            clearValues.get(0).color().float32(3, 1.0f);
            clearValues.get(1).depthStencil().set(1.0f, 0);
    
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
    
                VK11.vkCmdDrawIndexed(commandBuffer, model.getIndexBuffer().getLenght(), 1, 0, 0, 0);
    
            VK11.vkCmdEndRenderPass(commandBuffer);
    
            if (VK11.vkEndCommandBuffer(commandBuffer) != VK11.VK_SUCCESS) {
                throw new RuntimeException("failed to record command buffer!");
            }
        }

        public void updateUniformBuffer(int currentImage, MemoryStack stack) {
            long currentTime = System.currentTimeMillis();
            float time = ((float)(currentImage - startTime)) / 1000.0f;

            mat4 model = new Quaternion(new Vec3(0.0f, 0.0f, 1.0f), (float)Math.toRadians(90.0f)).toRotationMatrix();
            mat4 view = new mat4().initIdentity(); 
            mat4 proj = new mat4().initPerspective((float)Math.toRadians(45.0f), swapChainExtent.width() / (float) swapChainExtent.height(), 0.1f, 10.0f);
            proj.set(1, 1, proj.get(1, 1) * -1);

            UniformBufferObject ubo = new UniformBufferObject(model, view, proj);

            PointerBuffer ppData = stack.callocPointer(1);
            Vma.vmaMapMemory(allocator, uniformBuffers.get(currentImage).getAllocationHandle(), ppData);
            FloatBuffer data = ppData.getFloatBuffer(0, UniformBufferObject.LENGTH);
            data.put(0, ubo.getMemoryLayout());
            Vma.vmaUnmapMemory(allocator, uniformBuffers.get(currentImage).getAllocationHandle());
        }
    }

    public static void main(String[] args) {
        HelloTriangleApplication app = new Old.HelloTriangleApplication();
        app.run();
    }
}
