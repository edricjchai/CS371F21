import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.Iterator;

/**
 *  Application of LinkedList for MemoryAllocation blocks
 *
 */

class Block implements Comparable<Block>{
    int offset,size;

    public Block(int offset, int size){
        this.offset = offset;
        this.size = size;
    }

    public int getOffset(){
        return offset;
    }

    public int getSize(){
        return size;
    }

    public boolean isAdjacent(@NotNull Block other){
        int end = offset + size;
        int otherEnd = other.offset + other.size;
        return offset == otherEnd || end == other.offset;
    }

    @Override public int compareTo(@NotNull Block other){
        return Integer.compare(this.offset,other.offset);
    }

    @Override public String toString(){
        return String.format("(address=%d,size=%d)",offset,size);
    }
}

public class MyLinkedList implements Iterable {
    int address, size;
    Node head, tail;

    public MyLinkedList(){}

    public @NotNull Node add(@NotNull Block block){
        Node next = new Node(block);
        tail.next = next;
        return tail = next;
    }

    //Node constructor
    public static class Node{
        Node prev, next;
        Block block;
=
        public Node(@NotNull Block block){
            this.block = block;
        }

        public @NotNull Node addAfter(int address, int size){
            Node node = new Node(block);
            node.prev = this;
            node.next = next;
            next.prev = node;
            return next = node;
        }

        public void linkLeft(@Nullable Node node){
            this.prev = node;
            if(node != null)
                node.prev = this;
        }
        public void linkRight(@Nullable Node node) {
            this.next = node;
            if (node != null)
                node.next = this;
        }
        @Override public @NotNull String toString(){
            return block.toString();
        }
    }

    @Override public String toString(){
        String result = "";
        for(Iterator iterator = iterator();iterator.hasNext();)
            result += iterator.next() + ",";
        return result.length() > 0 ? result.substring(0,result.length() - 1) : result;
    }
    // add method inserts a node into the current list
    public @NotNull Node add(int address, int size){
        Node next = new Node(address, size);
        tail.next = next;
        return tail = next;
    }

    //remove method removes current block reference off of list
    public @NotNull void remove(){

    }

    //searchBlock() finds a specific block size
    public Node searchBlock(int findBlock){

    }

    //Iterator method implementations
    //hasNext() checks if next in linkedlist is null
    @Override public @NotNull Iterator iterator(){
        return new Iterator() {
            Node current, next;
            boolean started;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Object next() {
                current = next;
                if(hasNext())
                    next = current.next;
                return current;
            }

            @Override public void remove(){
                if(current == null)
                    throw new IllegalStateException("Next has not been called");
                if(current.prev != null && current.next != null){
                    current.next.prev = current.prev;
                    current.prev.next = current.next;
                } else if(current.prev == null){
                    head = current.next;
                    head.prev = null;
                } else{
                    current.prev.next = null;
                    tail = current.prev;
                }
            }
        };
    }


}
