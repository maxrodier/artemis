package ca.artemis.engine.utils;

public class Timer {
    
    private Runnable task;
    private long delay;
    private long lastTime;
    private boolean isRunning;

    public Timer(long delay, Runnable task) {
        this.delay = delay;
        this.task = task;
        this.lastTime = System.currentTimeMillis();
        this.isRunning = false;
    }

    public void start() {
        if(!isRunning) {
            isRunning = true;
            lastTime = System.currentTimeMillis();
        }
    }

    public void restart() {
        if(!isRunning) {
            isRunning = true;
        }
        lastTime = System.currentTimeMillis();
    }

    public void stop() {
        isRunning = false;
    }

    public void update() {
        if(isRunning && System.currentTimeMillis() - lastTime >= delay) {
            task.run();
            isRunning = false;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
