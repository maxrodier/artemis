package ca.artemis.engine.rendering;

import org.lwjgl.system.MemoryStack;

import ca.artemis.engine.scenes.Component;

public abstract class RenderDataComponent extends Component {
    
    public abstract void update(MemoryStack stack, int frameIndex);
}
