package framework;


import java.util.Iterator;
import java.util.LinkedList;

public class PartitionTableTemp implements Iterable{
    @Override
    public Iterator iterator() {
        return new Iterator<>() {
            Node current;
            @Override
            public boolean hasNext() {
                if(front == null && current == null) return false;
                return current == null ? circularQueue.next != null : current.next != null;
            }

            @Override
            public Object next() {
                if(hasNext())
                    current = current == null ? circularQueue : current.next;
                return current;
            }
        };
    }

    private class Node {
        private Object key;
        private Object value;
        private Node next;

        public Node(Object key, Object value){
            this.key = key;
            this.value = value;
            next = null;
        }
    }
    private Node circularQueue;
    private Node front;
    private int count;

    public PartitionTableTemp() {
    }

    /**
     * Places a word element into the circularQueue based on where the pointer(count) is
     * @param word
     */
    public void deposit(Object word){
        Node item = new Node(word, "1");
        if(circularQueue == null){
            circularQueue = item;
            circularQueue.next = circularQueue;
            front = item;
        }
        else{
            while(circularQueue != front)
                circularQueue = circularQueue.next;
            for(int i = 0; i < count; i++)
                circularQueue = circularQueue.next;

            item.next = circularQueue.next;
            circularQueue.next = item;
            count++;
            if(circularQueue.equals(front))
                count = 0;
        }
    }

    /**
     * Returns a non-circular singly-linked list of the circular queue
     * @return
     */
    public Node getList(){
        LinkedList<Node> temp = new LinkedList();
        Node current = circularQueue;
        current = current.next;
        while(current != circularQueue) {
            Node stepThrough = current.next;
            current.next = null;
            temp.add(current);
            current = stepThrough;
        }
        return temp.get(0);
    }

    public int size(){
        if(circularQueue == null)
            return 0;
        int sum = 1;
        Node current = circularQueue;
        circularQueue = circularQueue.next;
        while(circularQueue != current){
            circularQueue = circularQueue.next;
            sum++;
        }
        return sum;
    }
}