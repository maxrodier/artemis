package ca.artemis.engine.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.vulkan.KHRSwapchain;

public class EngineSettings {
    
    public static int WIDTH = 800;
    public static int HEIGHT = 600;

    public List<String> deviceRequiredExtensions = new ArrayList<>();
    
    public Set<String> shaderNames = new HashSet<>();
    public Set<String> textureNames = new HashSet<>();
    public Set<String> materialNames = new HashSet<>();
    public Set<String> meshNames = new HashSet<>();


    public EngineSettings() {
        deviceRequiredExtensions.add(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);

        textureNames.add("viking");

        meshNames.add("viking");
    }
}
