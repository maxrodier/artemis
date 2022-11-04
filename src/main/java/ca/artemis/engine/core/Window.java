package ca.artemis.engine.core;

public abstract class Window {

    public abstract void destroy();

    public abstract void update();

    public abstract boolean isCloseRequested();
    public abstract WindowSize getSize();

    protected class WindowSize {
        
        public final int width;
        public final int height;

        public WindowSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public static abstract class Builder {

        protected String title;
        protected int width;
        protected int height;
        protected boolean isResizable = false;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder isResizable(boolean isResizable) {
            this.isResizable = isResizable;
            return this;
        }

        public abstract Window build();
    }
}
