package ca.artemis.game.scenes;

import org.lwjgl.system.MemoryStack;

import ca.artemis.engine.maths.Quaternion;
import ca.artemis.engine.maths.Vector2f;
import ca.artemis.engine.maths.Vector3f;
import ca.artemis.engine.rendering.entity.EntityRenderDataComponent;
import ca.artemis.engine.scenes.GameObject;
import ca.artemis.engine.scenes.Scene;
import ca.artemis.engine.vulkan.core.mesh.Mesh;
import ca.artemis.engine.vulkan.core.mesh.MeshComponent;
import ca.artemis.engine.vulkan.core.mesh.TransformComponent;
import ca.artemis.engine.vulkan.core.mesh.Vertex;
import ca.artemis.engine.vulkan.core.mesh.Vertex.VertexKind;
import ca.artemis.game.rendering.LowPolyRenderSystem;

public class MainScene extends Scene {

    private final LowPolyRenderSystem lowPolyRenderSystem = new LowPolyRenderSystem();

    private Mesh vikingHouseMesh;
    private Mesh mesh;
    private GameObject plane = new GameObject();
    private GameObject plane2 = new GameObject();



    public MainScene() {
        vikingHouseMesh = new Mesh("models/viking.obj");

        Integer[] indices = {
                0, 1, 2, 2, 1, 3
            };

        Vertex[] vertices = new Vertex[] {
            new Vertex(new Vector3f(0f, 0f, 0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector2f(0, 0)),
            new Vertex(new Vector3f(0f, 0f, 10f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector2f(0, 0)),
            new Vertex(new Vector3f(10f, 0f, 0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector2f(0, 0)),
            new Vertex(new Vector3f(10f, 0f, 10f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector2f(0, 0)),
        };

        mesh = new Mesh(indices, vertices, VertexKind.POS_NORMAL_UV);

        plane.addComponent(new EntityRenderDataComponent());
        plane.addComponent(new MeshComponent(mesh));
        plane.addComponent(new TransformComponent(new Vector3f(0, 0, 0)));

        Quaternion rotation = new Quaternion(new Vector3f(1, 0, 0),(float) Math.toRadians(-90.0f));
        rotation = rotation.mul(new Quaternion(new Vector3f(0, 0, 1), (float) Math.toRadians(45.0f)));

        plane2.addComponent(new EntityRenderDataComponent());
        plane2.addComponent(new MeshComponent(vikingHouseMesh));
        plane2.addComponent(new TransformComponent(new Vector3f(5, 5, 5f), rotation, new Vector3f(5, 5, 5)));

        addGameObjectToScene(plane);
        addGameObjectToScene(plane2);
    }

    @Override //This override is temporary. We need to move mesh init somewhere else in a resource manager
    public void init() {
        vikingHouseMesh.init();
        mesh.init();
        super.init();
    }

    @Override
    public void update() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            lowPolyRenderSystem.update(stack, this);

            EntityRenderDataComponent.camera.update();
        }
    }

    @Override
    public void render() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            lowPolyRenderSystem.render(stack);
        }
    }
}
