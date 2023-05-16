package ca.artemis.engine.ecs.systems;

import ca.artemis.engine.core.EngineSettings;
import ca.artemis.engine.core.ecs.BaseSystem;
import ca.artemis.engine.core.ecs.Entity;
import ca.artemis.engine.ecs.components.MaterialComponent;
import ca.artemis.engine.ecs.components.MeshComponent;
import ca.artemis.engine.rendering.resources.Material;
import ca.artemis.engine.rendering.resources.Shader;
import ca.artemis.engine.rendering.resources.managers.MaterialResourcesManager;
import ca.artemis.engine.rendering.resources.managers.MeshResourcesManager;
import ca.artemis.engine.rendering.resources.managers.ShaderResourcesManager;
import ca.artemis.engine.rendering.resources.managers.TextureResourcesManager;
import ca.artemis.engine.util.MeshUtils;
import ca.artemis.engine.util.TextureUtils;

public class ResourcesManagerSystem extends BaseSystem {
    
    private final VulkanRenderingSystem renderingSystem;

    private ShaderResourcesManager shaderResourcesManager = ShaderResourcesManager.instance;
    private TextureResourcesManager textureResourcesManager = TextureResourcesManager.instance;
    private MaterialResourcesManager materialResourcesManager = MaterialResourcesManager.instance;;
    private MeshResourcesManager meshResourcesManager = MeshResourcesManager.instance;

    public ResourcesManagerSystem(VulkanRenderingSystem renderingSystem) {
        this.renderingSystem = renderingSystem;
    }

    @Override
    public void init(EngineSettings settings) {
        for(String shaderName : settings.meshNames) {
            shaderResourcesManager.addShader(shaderName , new Shader(renderingSystem, shaderName) );
        }
        for(String textureName : settings.textureNames) {
            textureResourcesManager.addTexture(textureName , TextureUtils.loadTexture(renderingSystem.getDevice(), renderingSystem.getAllocator(), renderingSystem.getCommandPool(), textureName));
        }
        for(String materialName : settings.materialNames) {
            materialResourcesManager.addMaterial(materialName , new Material(renderingSystem, materialName));
        }
        for(String meshName : settings.meshNames) {
            meshResourcesManager.addMesh(meshName , MeshUtils.loadMesh(renderingSystem.getDevice(), renderingSystem.getAllocator(), renderingSystem.getCommandPool(), meshName));
        }
    }

    @Override
    public void initComponents() {
        for(Entity entity: entities.values()) {
            if(entity.hasComponent(MaterialComponent.class)) {
                MaterialComponent materialComponent = entity.getComponent(MaterialComponent.class);
                materialComponent.setMaterial(materialResourcesManager.getMaterial(materialComponent.getName()));
            }
            if(entity.hasComponent(MeshComponent.class)) {
                MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
                meshComponent.setMesh(meshResourcesManager.getMesh(meshComponent.getName()));
            }
        }
    }

    @Override
    public void destroy() {
        shaderResourcesManager.getShaders().values().forEach(r -> r.destroy(renderingSystem));
        shaderResourcesManager.getShaders().clear();

        textureResourcesManager.getTextures().values().forEach(r -> r.destroy(renderingSystem));
        textureResourcesManager.getTextures().clear();

        materialResourcesManager.getMaterials().values().forEach(r -> r.destroy(renderingSystem));
        materialResourcesManager.getMaterials().clear();

        meshResourcesManager.getMeshes().values().forEach(r -> r.destroy(renderingSystem));
        meshResourcesManager.getMeshes().clear();
    }
}
