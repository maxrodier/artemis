package ca.artemis.engine.renderer.vulkan;

import ca.artemis.engine.renderer.Backend;

public class VulkanBackend extends Backend{

    @Override
    public boolean initialize() {
        // Initialize context
        return false;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onResized() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean beginFrame() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean endFrame() {
        // TODO Auto-generated method stub
        return true;
    }
    
}
