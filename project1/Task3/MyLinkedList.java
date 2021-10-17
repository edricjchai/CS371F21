import java.util.Iterator;

/**
 *  Application of Singly LinkedList for MemoryAllocation blocks
 *
 */
public class MyLinkedList implements Iterator {
    private Node block = null;

    //Node constructor
    public class Node{
        int size;
        Node next;

        Node(int value){
            this.size = value;
            next = null;
        }
    }

    //add method that links the blocks to the list
    public void setBlock(int size){
        block = new Node(size);
    }

    //remove method removes block reference off of list
    public void remove(int removeBlock){
        //code here
    }

    //searchBlock() finds a specific block size
    public Node searchBlock(int findBlock){
        Node holder = block;
        while(holder.size != findBlock){
            holder = holder.next;
        }
        return holder;
    }

    //split method dissects the blocks to the called split size
    public void split(int splitSize){
        if(splitSize < block.size){
            Node rightHalf = new Node(block.size - splitSize);
            if(block.next.next != null)
                rightHalf.next = block.next.next;
            block.size = block.size - rightHalf.size; //current value is left half
            block.next = rightHalf;
        }
    }

    //getSize() returns blockSize
    public int getSize(){
        return block.size;
    }

    //Iterator method implementations
    //hasNext() checks if next in linkedlist is null
    public boolean hasNext(){
        return block.next != null;
    }

    //next() goes to the next reference in linkedlist
    public Node next(){
        if(hasNext()){
            block = block.next;
            return block;
        }
        return null;
    }


}
