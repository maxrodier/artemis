package ca.artemis.systems;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MessageBus {
    
    private static MessageBus messageBus;

    private List<System> systems = new ArrayList<>();
    private LinkedList<Message> messageQueue = new LinkedList<>();

    private MessageBus() { }

    public void addSystem(System system) {
        systems.add(system);
    }

    public void receiveMessage(Message message) {
        messageQueue.add(message);
    }

    public void processMessage() {
        Message message = messageQueue.poll();
        if(message != null) {
            sendMessage(message);
        }
    }

    public void sendMessage(Message message) {
        for(System system : systems) {
            system.receiveMessage(message);
        }
    }

    public static MessageBus getMessageBus() {
        if(messageBus ==  null) {
            messageBus = new MessageBus();
        }
        return messageBus;
    }
}
