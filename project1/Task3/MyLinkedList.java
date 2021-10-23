package temp;

import com.sun.istack.internal.NotNull;
import temp.MyLinkedList.Node;
import temp.MyLinkedList.NodeIterator;

import java.util.Iterator;

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
     * Add block to list
     */
    public @NotNull Node add(@NotNull Block block){
        Node node = new Node(block);
        if(head == null){
            head = node;
            tail = node;
        } else{
            NodeIterator iterator = iterator();
            Node current = null;
            while(iterator.hasNext()){
                current = iterator.next();
                if(current == null)
                    break;
                if(current.block.offset < node.block.offset){
                    node.block.offset = current.block.offset;
                }
                node.block.size += current.block.size;
            }
            if(block.offset + block.size < current.block.offset + current.block.size)
                throw new IllegalStateException("Not enough space");
        }
        return node;
    }

    /**
     * Node constructor for doubly linked-list implementation
     */
    public static class Node{
        Node previous,next;
        Block block;

        public Node(@NotNull Block block){
            this.block = block;
        }

        public @NotNull Node addAfter(@NotNull Block block){
            Node node = new Node(block);
            node.previous = this;
            node.next = next;
            next.previous = node;
            return next = node;
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
            Node current,next = head;

            @Override public boolean hasNext(){
                return next != null;
            }

            @Override public Node next(){
                current = next;
                if(hasNext())
                    next = current.next;
                return current;
            }

            @Override public void remove(){
                if(current == null)
                    throw new IllegalStateException("Next has not been called");
                if(current.previous != null && current.next != null){
                    current.next.previous = current.previous;
                    current.previous.next = current.next;
                } else if(current.previous == null){
                    head = current.next;
                    head.previous = null;
                } else{
                    current.previous.next = null;
                    tail = current.previous;
                }
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
    private static int nextFitPointer = 1;

    private final MyLinkedList
            free = new MyLinkedList(),
            used = new MyLinkedList();
    private final Algorithm algorithm;
    private final int max;

    public MyMemoryAllocation(int mem_size, @NotNull String algorithm){
        super(mem_size,algorithm);
        this.algorithm = Algorithm.find(algorithm);
        max = mem_size;
        free.add(new Block(ADDRESS_START,mem_size));
    }

    @Override public int alloc(int size){
        NodeIterator usedIterator = used.iterator();
        Node usedNode = usedIterator.next();

        // remove from free memory
        Block block = new Block(algorithm.allocate(free,size),size);

        // add to used memory
        if(usedNode == null) {
            return ERROR_CODE;
        }
            while (!usedNode.block.isAdjacent(block) && usedIterator.hasNext())
                usedNode = usedIterator.next();
            if (!usedNode.block.isAdjacent(block))
                return ERROR_CODE;
            if (usedNode.block.offset + usedNode.block.size != block.offset)
                usedNode.block.offset -= size;
            usedNode.block.size += size;

        return block.offset;
    }

    @Override public void free(int address){
        NodeIterator iterator = used.iterator();
        Node node = null;
        while(iterator.hasNext() && (node = iterator.next()).block.offset != address);
        if (node == null || node.block.offset != address)
            throw new IllegalArgumentException("");
        iterator.remove();
        free.add(node.block);
    }

    @Override public int size(){
        int result = 0;
        for(NodeIterator iterator = used.iterator();iterator.hasNext();)
            result += iterator.next().block.size;
        return result;
    }

    @Override public int max_size(){
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
                if(current.block.size < best.block.size && current.block.size < size)
                    best = current;
            }
            if(best.block.size < size)
                return ERROR_CODE;
            best.block.size -= size;
            if(best.block.size == 0)
                iterator.remove();
            return best.block.offset += size;
        }),
        FIRST_FIT("FF",(free,size) -> {
            NodeIterator iterator = free.iterator();
            Node node = null;
            while(iterator.hasNext() && (node = iterator.next()).block.size < size);
            if(node == null || node.block.size < size)
                return ERROR_CODE;
            node.block.size -= size;
            if(node.block.size == 0)
                iterator.remove();
            return node.block.offset += size;
        }),
        NEXT_FIT("NF",(free,size) -> {
            NodeIterator iterator = free.iterator();
            Node node = null;
            while(iterator.hasNext()){
                node = iterator.next();
                if(node.block.offset + node.block.size >= nextFitPointer)
                    break;
            }
            if(node == null)
                return ERROR_CODE;
            if(node.block.offset + node.block.size - nextFitPointer >= size){
                int change = node.block.offset + node.block.size - nextFitPointer;
                node.block.size -= change;
                node = node.addAfter(new Block(nextFitPointer,size));
                if(change > size)
                    node.addAfter(new Block(node.block.offset + size,size - change));
                nextFitPointer += size;
                return node.block.offset;
            } else{
                Node saved = node;
                free.tail.next = free.head;
                while(iterator.hasNext() && (node = iterator.next()).block.size < size && node != saved);
                if(node.block.size < size || node == saved){
                    free.tail.next = null;
                    return ERROR_CODE;
                }
                node.block.size -= size;
                if(node.block.size == 0)
                    iterator.remove();
                free.tail.next = null;
                return node.block.offset += size;
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