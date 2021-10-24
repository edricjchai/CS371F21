package Task3;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import Task3.MyLinkedList.Node;
import Task3.MyLinkedList.NodeIterator;

import java.util.Iterator;
import java.util.function.Predicate;

class Block implements Comparable<Block>{
    public int offset,size;

    public Block(int offset, int size){
        this.offset = offset;
        this.size = size;
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


class MyLinkedList implements Iterable{
    Node head,tail;

    public MyLinkedList(){}

    /**
     * Add block to tail of list
     */
    public @NotNull Node add(@NotNull Block block){
        if(head == null){
            Node node = new Node(block);
            head = node;
            tail = node;
            return node;
        }
        return tail.addAfter(block);
    }

    /**
     * Merge blocks together
     */
    public void merge(){
        for(NodeIterator iterator = iterator();iterator.hasNext();){
            Node node = iterator.next();
            if(node != head){
                if(node.previous.block.isAdjacent(node.block)){
                    node.previous.block.size += node.block.size;
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Node constructor for doubly linked-list implementation
     */
    public class Node{
        Node previous,next;
        Block block;

        public Node(@NotNull Block block){
            this.block = block;
        }

        public @NotNull Node addBefore(@NotNull Block block){
            Node node = new Node(block);
            node.previous = previous;
            node.next = this;
            if(previous == null)
                head = node;
            else
                previous.next = node;
            return previous = node;
        }

        public @NotNull Node addAfter(@NotNull Block block){
            Node node = new Node(block);
            node.previous = this;
            node.next = next;
            if(next == null)
                tail = node;
            else
                next.previous = node;
            return next = node;
        }

        public void remove(){
            // node is inner node
            if(previous != null && next != null){
                next.previous = previous;
                previous.next = next;
                return;
            }
            if(this == head){
                head = next;
                if(head != null)
                    head.previous = null;
            }
            if(this == tail){
                tail = previous;
                if(tail != null)
                    tail.next = null;
            }
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

    /**
     * Iterator implementation
     */
    @Override public @NotNull NodeIterator iterator(){
        return new NodeIterator(){
            Node current;

            @Override public boolean hasNext(){
                return current == null ? head != null : current.next != null;
            }

            @Override public Node next(){
                if(hasNext())
                    current = current == null ? head : current.next;
                return current;
            }

            @Override public void remove(){
                if(current == null)
                    throw new IllegalStateException("Next has not been called");
                current.remove();
            }
        };
    }

    public interface NodeIterator extends Iterator{
        @Override @NotNull Node next();
    }
}

@FunctionalInterface interface AllocationStrategy{
    int allocate(@NotNull MyLinkedList free, int size);
}

class MyMemoryAllocation extends MemoryAllocation{
    private static final int ERROR_CODE = 0, ADDRESS_START = 1;
    private static int nextFitPointer;

    private final MyLinkedList
            free = new MyLinkedList(),
            used = new MyLinkedList();
    private final Algorithm algorithm;
    private final int max;

    public MyMemoryAllocation(int mem_size, @NotNull String algorithm){
        super(mem_size,algorithm);
        this.algorithm = Algorithm.find(algorithm);
        nextFitPointer = 1;
        max = mem_size - 1;
        free.add(new Block(ADDRESS_START,max));
    }

    /**
     *  Allocates memory(int) to usedList in order of Strategy(FirstFit,BestFit,NextFit)
     */
    @Override public int alloc(int size){
        NodeIterator usedIterator = used.iterator();
        Node usedNode = usedIterator.next();

        // remove from free memory
        Block block = new Block(algorithm.allocate(free,size),size);

        // add to used memory
        if(usedNode == null)
            return used.add(block).block.offset;
        while (!usedNode.block.isAdjacent(block) && usedIterator.hasNext())
            usedNode = usedIterator.next();
        if (!usedNode.block.isAdjacent(block))
            return ERROR_CODE;
        if (usedNode.block.offset + usedNode.block.size != block.offset)
            return usedNode.addBefore(block).block.offset;
        return usedNode.addAfter(block).block.offset;
    }

    private @Nullable Node skipWhile(@NotNull NodeIterator iterator, @NotNull Predicate<Node> predicate){
        while(iterator.hasNext()){
            Node current = iterator.next();
            if(!predicate.test(current))
                return current;
        }
        return null;
    }

    /**
     * free method, unassigning memory from the usedList and making it available(freeList)
     */
    @Override public void free(int address){
        // remove memory from used list
        NodeIterator iterator = used.iterator();
        Node node = skipWhile(iterator, n -> n.block.offset != address);
        if (node == null || node.block.offset != address) {
            System.err.println("Address is not the start of an allocated block");
            return;
        }
        iterator.remove();

        // add memory to free list
        if(free.head == null)
            free.add(node.block);
        else {
            Node stopNode = skipWhile(
                    free.iterator(),
                    n -> n.block.offset < node.block.offset
            );
            if(stopNode == null)
                free.tail.addAfter(node.block);
            else
                stopNode.addBefore(node.block);
            free.merge();
        }
    }

    /**
     * returns the total size of available memory
     */
    @Override public int size(){
        int result = 0;
        for(NodeIterator iterator = free.iterator();iterator.hasNext();)
            result += iterator.next().block.size;
        return result;
    }

    /**
     * returns the largest size of available memory
     */
    @Override public int max_size(){
        int max = 0;
        for(NodeIterator iterator = free.iterator();iterator.hasNext();)
            max = Math.max(iterator.next().block.size,max);
        return max;
    }

    @Override public void print(){
        System.out.printf("Used: %s%n",used);
        System.out.printf("Free: %s%n",free);
        System.out.printf("Algorithm: %s%n",algorithm.name);
    }

    /**
     * Algorithm strategies to be used in allocation
     */
    private enum Algorithm{
        BEST_FIT("BF",(free,size) -> {
            NodeIterator iterator = free.iterator();
            Node best = free.head;
            while(iterator.hasNext()){
                Node current = iterator.next();
                if(current.block.size < best.block.size && current.block.size >= size)
                    best = current;
            }
            if(best.block.size < size)
                return ERROR_CODE;
            best.block.size -= size;
            if(best.block.size == 0)
                best.remove();
            int result = best.block.offset;
            best.block.offset += size;
            return result;
        }),
        FIRST_FIT("FF",(free,size) -> {
            NodeIterator iterator = free.iterator();
            Node node = null;
            while(iterator.hasNext() && (node = iterator.next()).block.size < size);
            if(node == null || node.block.size < size)
                return ERROR_CODE;
            node.block.size -= size;
            if(node.block.size == 0)
                node.remove();
            int result = node.block.offset;
            node.block.offset += size;
            return result;
        }),
        NEXT_FIT("NF",(free,size) -> { //Finish Scam fit
            NodeIterator iterator = free.iterator();
            Node node = null;
            while(iterator.hasNext()){
                node = iterator.next();
                if(node.block.offset + node.block.size >= nextFitPointer)
                    break;
            }
            if(node == null)
                return ERROR_CODE;
            int space = node.block.offset + node.block.size - nextFitPointer;
            if(space >= size && space <= node.block.size){
                node.block.size -= space;
                if(space > size)
                    node.addAfter(new Block(nextFitPointer + size, space - size));
                if(node.block.size == 0)
                    node.remove();
                if(node.next == null)
                    nextFitPointer = 1;
                else
                    nextFitPointer = node.next.block.offset;
                return node.block.offset;
            } else{
                Node stopNode = node;
                if(free.tail.next == node.next) { //puts pointer back to head
                    iterator = free.iterator(); //reset iterator
                    node = iterator.next();
                }
                while(node.block.size < size && (node.block.offset <= nextFitPointer) || stopNode != node){
                    if(free.tail.next == node.next) {
                        iterator = free.iterator();
                    }
                    if(node.block.size >= size)
                        break;
                    node = iterator.next();
                }
                if(node.block.size < size)
                    return ERROR_CODE;
                // returns the result of offset and sets nextFitPointer
                int result = node.block.offset;
                node.block.offset += size;
                node.block.size -= size;
                if(node.block.size == 0){
                    if(node.next == null)
                        nextFitPointer = 1;
                    else
                        nextFitPointer = node.next.block.offset;
                    node.remove();
                } else {
                    if(node.next == null)
                        nextFitPointer = 1;
                    else
                        nextFitPointer = node.next.block.offset;
                }
                free.tail.next = null;
                free.head.previous = null;
                return result;
            }
        });

        private final String name;
        private final AllocationStrategy strategy;

        public static @NotNull Algorithm find(@NotNull String name){
            for(Algorithm algorithm : values()){
                if(algorithm.name.equals(name))
                    return algorithm;
            }
            throw new IllegalArgumentException("Invalid algorithm");
        }

        Algorithm(@NotNull String name, @NotNull AllocationStrategy strategy){
            this.name = name;
            this.strategy = strategy;
        }

        public int allocate(@NotNull MyLinkedList free, int size){
            return strategy.allocate(free,size);
        }
    }
}