package ca.artemis.game;

import ca.artemis.engine.core.ArtemisEngine;
import ca.artemis.engine.core.ecs.Entity;
import ca.artemis.engine.ecs.components.CameraComponent;
import ca.artemis.engine.ecs.components.MeshComponent;
import ca.artemis.engine.ecs.components.TransformComponent;

public class Main {
    
    public static void main(String[] args) {
        ArtemisEngine engine = new ArtemisEngine();

        Entity playerEntity = engine.createEntity();
        playerEntity.addComponent(new CameraComponent());

        Entity houseEntity = engine.createEntity();
        houseEntity.addComponent(new MeshComponent("viking"));
        //houseEntity.addComponent(new MaterialComponent("viking"));
        houseEntity.addComponent(new TransformComponent());

        engine.run();
    }
}
