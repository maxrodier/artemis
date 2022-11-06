package ca.artemis.engine.core.components;

import ca.artemis.engine.core.Component;

public class FontRenderer extends Component {

    @Override
    public void start() {
        if(parent.getComponent(SpriteRenderer.class) != null) {
            System.out.println("Found the sibling component SpriteRenderer!");
        }
    }

    @Override
    public void update(float dt) {

    }
}
