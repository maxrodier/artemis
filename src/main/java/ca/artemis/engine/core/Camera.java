package ca.artemis.engine.core;

import org.lwjgl.glfw.GLFW;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.maths.Matrix4f;
import ca.artemis.engine.maths.Quaternion;
import ca.artemis.engine.maths.Vector3f;
import ca.artemis.engine.vulkan.api.context.VulkanContext;

public class Camera {
    
    private static float movSpeed = 0.05f;
    private static float rotSpeed = 0.05f;

    private Matrix4f projection;
    private Vector3f position;
    private Quaternion rotation;

    public Camera() {
        VulkanContext context = LowPolyEngine.instance().getContext();

        this.projection = new Matrix4f().initPerspective((float) Math.toRadians(45.0f), context.getSurfaceSupportDetails().getSurfaceExtent().width() / (float) context.getSurfaceSupportDetails().getSurfaceExtent().height(), 0.01f, 100.0f);;
        this.position = new Vector3f(0, 5, 0);
        this.rotation = new Quaternion(new Vector3f(0, 1, 0), 0);
    }

    public Camera(Matrix4f projection, Vector3f position, Quaternion rotation) {
        this.projection = projection;
        this.position = position;
        this.rotation = rotation;
    }

    public void update() {
        LowPolyEngine engine = LowPolyEngine.instance();
        
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_W)) {
            position = position.add(rotation.getForward().mul(movSpeed));
        }
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_S)) {
            position = position.sub(rotation.getForward().mul(movSpeed));
        }
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_D)) {
            position = position.add(rotation.getRight().mul(movSpeed));
        }
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_A)) {
            position = position.sub(rotation.getRight().mul(movSpeed));
        }
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_SPACE)) {
            position = position.add(rotation.getUp().mul(movSpeed));
        }
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            position = position.sub(rotation.getUp().mul(movSpeed));
        }

        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_E)) {
            rotation = rotation.mul(new Quaternion(rotation.getForward(), rotSpeed));
        }
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_Q)) {
            rotation = rotation.mul(new Quaternion(rotation.getForward(), -rotSpeed));
        }
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
            rotation = rotation.mul(new Quaternion(rotation.getUp(), -rotSpeed));
        }
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_LEFT)) {
            rotation = rotation.mul(new Quaternion(rotation.getUp(), rotSpeed));
        }
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_DOWN)) {
            rotation = rotation.mul(new Quaternion(rotation.getRight(), -rotSpeed));
        }
        if(engine.getKeyboard().isKeyDown(GLFW.GLFW_KEY_UP)) {
            rotation = rotation.mul(new Quaternion(rotation.getRight(), rotSpeed));
        }

        if(engine.getMouse().getDx() != 0) {
            rotation = rotation.mul(new Quaternion(rotation.getUp(), engine.getMouse().getDx() * -rotSpeed / 10));
        }
        if(engine.getMouse().getDy() != 0) {
            rotation = rotation.mul(new Quaternion(rotation.getRight(), engine.getMouse().getDy() * -rotSpeed / 10));
        }


        System.out.println("Forward: " + rotation.getForward());
        System.out.println("Right: " + rotation.getRight());
        System.out.println("Up: " + rotation.getUp());
        System.out.println("Position: " + position);
    }

    public Matrix4f getViewMatrix() {
        Matrix4f translation = new Matrix4f().initTranslation(-position.x, -position.y, -position.z);

        return  rotation.toRotationMatrix().mul(translation);
    }

    public Matrix4f getProjectionMatrix() {
        return projection;
    }
}
