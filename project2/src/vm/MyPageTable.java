package vm;

public class MyPageTable{
    private static final int INITIAL_SIZE = 256;
    PageTableEntry[] table;
    int hashTable[] = new int[INITIAL_SIZE];
    private int counter = 0;
    private int j = -1;

    MyPageTable(){
        table = new PageTableEntry[INITIAL_SIZE];
        for(int i = 0; i < INITIAL_SIZE; i++)
            table[i] = new PageTableEntry(-1, i);
    }

    public static class PageTableEntry {
        int vpn;
        int pfn;
        boolean dirty;
        PageTableEntry next;

        public PageTableEntry(int vpn, int pfn) {
            this.vpn = vpn;
            this.pfn = pfn;
            dirty = false;
            next = null;
        }
        public int getVpn(){
            return this.vpn;
        }

        public void setVpn(int vpn) {
            this.vpn = vpn;
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }
    }

    public void insert(int vpn, int pfn){
        table[pfn].setVpn(vpn);
        table[pfn].setDirty(true);
        counter++;
        if(counter > INITIAL_SIZE){
            counter = 0;
        }
    }

    /*https://stackoverflow.com/questions/682438/hash-function-providing-unique-uint-from-an-integer-coordinate-pair*/
    private int hashCode(int a) {
        a = (a ^ 61) ^ (a >> 16);
        a = a + (a << 3);
        a = a ^ (a >> 4);
        a = a * 0x27d4eb2d;
        a = a ^ (a >> 15);
        return a;
    }

    public void eraseDirty(int pfn){
        table[pfn].dirty = false;
    }

    public void checkUsed(int vpn) throws PageFaultException {
        int index = Math.abs(hashCode(vpn)) % INITIAL_SIZE;
        if(index >= INITIAL_SIZE || index == -1 || table[index].getVpn() != -1 && table[index].dirty) {
            throw new PageFaultException();
        }
    }

    public boolean getDirty(int pfn){
        if(pfn >= INITIAL_SIZE){
            return false;
        }
        return table[pfn].dirty;
    }


}