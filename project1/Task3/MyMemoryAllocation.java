public class MyMemoryAllocation extends MemoryAllocation{
    private final int errNull = 0, address = 1;
    private String algorithm;
    private final int max;
    private final MyLinkedList free = new MyLinkedList(), used = new MyLinkedList();

    //MyMemoryAllocation constructor extending MemoryAllocation
    public MyMemoryAllocation(int mem_size, String algorithm){
        super(mem_size,algorithm);
        this.algorithm = algorithm;
        max = mem_size;
        free.add(new Block(1, mem_size));
    }

    //alloc() puts [size call] of availableBlocks into usedBlocks returns pointer of beginning of the memory
    @Override public int alloc(int size){

    }

    public void free(int addr){
    }

    public int size(){
    }

    public int max_size(){
    }

    public void print(){
    }

    private void freeFF(int addr){}
    private void freeBF(int addr){}
    private void freeNF(int addr){}
}