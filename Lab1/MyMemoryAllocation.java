public class MyMemoryAllocation extends MemoryAllocation{

    public MyMemoryAllocation(int mem_size, String algorithm){
        super(mem_size, algorithm);
        MyLinkedList availableBlocks = new MyLinkedList();
        MyLinkedList usedBlocks = new MyLinkedList();
        availableBlocks.add(mem_size);
    }

    public int alloc(int size){

        return 0;
    }

    public void free(int addr){

    }

    public int size(){
        return 0;
    }

    public int max_size(){
        return 0;
    }

    public void print(){

    }
}
