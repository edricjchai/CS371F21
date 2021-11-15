package vm;

import storage.PhyMemory;

public class VirtMemory extends Memory {
    MyPageTable pageTable = new MyPageTable();
    PhyMemory pM = new PhyMemory();
    private int writeCounter = 0;

    public VirtMemory() {
        super(new PhyMemory());
    }

    public VirtMemory(PhyMemory ram) {
        super(ram);
    }

    public void AddressTranslation(){

    }

    @Override
    public void write(int addr, byte value){
        if(addr >= 0xFFFFFF || addr < 0){
            System.err.println();
            return;
        }
        int writeOverflow = value + writeCounter;
        if(writeOverflow >= 32){
            pM.load(addr / 64 , addr);
            sync_to_disk();
            writeCounter = 0;
        }
        pM.write(addr, value);
        writeCounter++;
    }


    private boolean load(int addr){


        return true;
    }

    @Override
    public byte read(int addr) {
        int vpn = getVPN(addr);
        int offset = getOffset(addr);
        return 0;
    }

    @Override
    protected void sync_to_disk() {

    }
    private int getVPN(int addr){
        return addr/64;
    }
    private int getOffset(int addr){
        return addr%64;
    }

    private byte addrTranslation(int addr) throws PageFaultException {
        // 1. extract VPN from VA
        int vpn = getVPN(addr);
        int offset = getOffset(addr);

        // 2. calculate addr of PTE
        int pfn = pageTable.getByVPN(vpn);

        // 3. Get the memory address by concatenating the pfn + offset
        String memAddrString = String.valueOf(pfn) + String.valueOf(offset);
        int memAddr = Integer.parseInt(memAddrString);

        // 4. read PTE from memory
        return pM.read(memAddr);
    }
}
