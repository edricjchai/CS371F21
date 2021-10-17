import java.util.Iterator;

/**
 *  LinkedList data structure for MemoryAllocation
 *  @param <Integer>
 */
public class MyLinkedList<Integer> implements Iterator<Integer> {


    private Node current = null;

    public boolean hasNext(){
        return current != null;
    }
    public Integer next(){
        if(hasNext()){
            Integer value = current.data;
            current = current.next;
            return value;
        }
        return null;
    }

    private class Node{
        Integer data;
        Node next;

        Node(Integer value){
            this.data = value;
            next = null;
        }
    }
    //put method that automatically links the blocks in numerical order
    public void add(Integer value){
        new Node(value);
    }
}
