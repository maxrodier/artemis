package ca.artemis.engine.scene3.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ca.artemis.engine.scene3.Node;
import ca.artemis.engine.scene3.View;
import ca.artemis.engine.scene3.annotations.YamlView;

@YamlView(path = "src/main/resources/game/mainView.yaml")
@JsonDeserialize(builder = MainView.Builder.class)
public class MainView extends View {
   
    public int x;
    public int y;

    private final MainViewController mainViewController;
    
    protected MainView(Builder<?> builder) {
        super(builder);
        this.mainViewController = new MainViewController(this);

        this.x = builder.x;
        this.y = builder.y;
    }

    @Override
    public MainViewController getController() {
        return mainViewController;
    }

    public static class Builder<T extends Builder<T>> extends Node.Builder<Builder<?>> {

        private int x;
        private int y;

        @JsonProperty("x")
        public Builder<T> setX(int x) {
            this.x = x;
            return this;
        }

        @JsonProperty("y")
        public Builder<T> setY(int y) {
            this.y = y;
            return this;
        }

        public MainView build() {
            return new MainView(this);
        }
    }


}