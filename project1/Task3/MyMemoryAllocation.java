public class MyMemoryAllocation extends MemoryAllocation{

    private String algo;
    private int blockSize;
    private MyLinkedList availableBlocks;
    private MyLinkedList usedBlocks;

    //MyMemoryAllocation constructor extending MemoryAllocation
    public MyMemoryAllocation(int mem_size, String algorithm){
        super(mem_size, algorithm);
        availableBlocks = new MyLinkedList();
        usedBlocks = new MyLinkedList();
        availableBlocks.setBlock(mem_size);
    }

    //alloc() puts [size call] of availableBlocks into usedBlocks
    public int alloc(int size){
        if(size < availableBlocks.getSize()){
            availableBlocks.split(size);
        }
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
