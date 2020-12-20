package ca.artemis.systems;

public abstract class System {
    
    private final MessageBus messageBus;

    public System() {
        this.messageBus = MessageBus.getMessageBus();
        this.messageBus.addSystem(this);
    }

    public void sendMessage(Message message) {
        messageBus.receiveMessage(message);
    }

    public abstract void receiveMessage(Message message);
}
