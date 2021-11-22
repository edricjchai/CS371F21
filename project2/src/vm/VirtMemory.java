package vm;

import storage.PhyMemory;

public class VirtMemory extends Memory {

    MyPageTable pageTable = new MyPageTable();
    Policy policy = new Policy();
    private int writeCounter = 0;
    private int address;
    private int vpn;
    private int pfn;
    private int offset;


    public VirtMemory() {
        super(new PhyMemory());
    }

    private void setPaging(int addr){
        this.address = addr;
        this.vpn = addr/64;
        this.offset = addr%64;
        //this.pfn = policy.writeCounter();
    }

    @Override
    public void write(int addr, byte value) {
        if(addr >= 65536 || addr < 0){
            System.err.println();
            return;
        }
        setPaging(addr);
        //checks if addr is writing back more than half a page and has written at first index
        if( (addr >= 32 && ram.read(0) == -1)) {
            sync_to_disk();
            writeCounter = 0;
        }
        //Single-writes
        //pfn = policy.getPFN();
        try{
            int phyAddr = addrTranslation(addr);
            if(phyAddr < 0 || phyAddr >= 2048)
                throw new NoPFNException();
            pageTable.checkUsed(vpn);
            pfn = policy.writeCounter();
            vpn = address/64;
            pageTable.insert(vpn, pfn);
            //pfn = pageTable.vpnToPfn(vpn);
            writeCounter++;
            ram.write(phyAddr, value);
        }catch(PageFaultException | NoPFNException e){
            e.printStackTrace();
            System.err.println();
        }
        //Detect write overflow
        if(writeCounter >= 32 || addr >= 64){
            sync_to_disk();
            writeCounter = 0;

        }
    }


    @Override
    public byte read(int addr)  {
        int phyAddr = addrTranslation(address);
        ram.load(pfn, phyAddr);
        return ram.read(phyAddr);
    }

    @Override
    public void sync_to_disk() {
        if(address >= 65536 || address < 0){
            System.err.println();
            return;
        }
        int pfnCounter=0;
        for(int i = 0; i < 256; i++){
            if(pageTable.getDirty(i)){
                pageTable.eraseDirty(i);
                pfnCounter++;
            }
        }
        if(pfnCounter > 0){
            ram.store(writeCounter, 0);
            ram.load(writeCounter, 0);
        }


    }


    private int addrTranslation(int addr) {
        try {
            setPaging(addr);
            if(pfn < 0){
                throw new PageFaultException();
            }
            int memAddr = (pfn << 6) + offset;
            return memAddr;
        }
        catch(PageFaultException e){
            e.printStackTrace();
            return -1;
        }
    }
}
