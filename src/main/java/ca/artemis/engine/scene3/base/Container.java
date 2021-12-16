package ca.artemis.engine.scene3.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ca.artemis.engine.scene3.Base;
import ca.artemis.engine.scene3.Node;

@JsonDeserialize(builder = Container.Builder.class)
public class Container extends Base {
    
    private String type;

    protected Container(Builder<?> builder) {
        super(builder);
        this.type = builder.type;
    }

    public String getType() {
        return type;
    }

    public static class Builder<T extends Builder<T>> extends Node.Builder<Builder<?>> {

        private String type;

        @JsonProperty("type")
        public Builder<T> setType(String type) {
            this.type = type;
            return this;
        }

        public Container build() {
            return new Container(this);
        }
    }
}
