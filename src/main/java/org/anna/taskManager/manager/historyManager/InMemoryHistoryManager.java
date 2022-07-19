package org.anna.taskManager.manager.historyManager;

import org.anna.taskManager.tasks.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Long, Node> nodeMap = new HashMap<>();
    private static final int HISTORY_CAPACITY = 10;

    private Node head;
    private Node tail;
    int size;

    @Override
    public ArrayList<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void add(Task task) {
        if (nodeMap.containsKey(task.getId())) {
            remove(task.getId());
        }
        if (size == HISTORY_CAPACITY) {
            removeNode(head);
        }
        linkLast(task);
    }

    @Override
    public void remove(long id) {
        removeNode(nodeMap.get(id));
    }

    // additional methods for the custom LinkedList
    private void linkLast(Task task) {
        final Node lastNode = tail;
        final Node newNode = new Node(lastNode, task, null);
        tail = newNode;
        if (lastNode == null) {
            head = newNode;
        } else {
            lastNode.setNext(newNode);
        }
        nodeMap.put(task.getId(), newNode);
        size++;
    }

    private void removeNode(Node node) {
        Node previousNode = node.getPrev();
        Node nextNode = node.getNext();
        if (previousNode == null && nextNode == null) {
            head = null;
            tail = null;
        } else if (previousNode == null) {
            nextNode.setPrev(null);
            head = nextNode;
        } else if (nextNode == null) {
            previousNode.setNext(null);
            tail = previousNode;
        } else {
            previousNode.setNext(nextNode);
            nextNode.setPrev(previousNode);
        }
        size--;
    }

    private ArrayList<Task> getTasks() {
        ArrayList<Task> list = new ArrayList<>();
        Node currentNode = head;
        while (currentNode != null) {
            list.add(currentNode.getData());
            currentNode = currentNode.getNext();
        }
        return list;
    }
}

class Node {
    private Node prev;
    private final Task data;
    private Node next;

    public Node(Node prev, Task data, Node next) {
        this.prev = prev;
        this.data = data;
        this.next = next;
    }

    public Node getPrev() {
        return prev;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Task getData() {
        return data;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}
