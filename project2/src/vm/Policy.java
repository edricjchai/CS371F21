package vm;
import storage.PhyMemory;

public class Policy {
    MyPageTable table;

    public Policy(MyPageTable table){
        this.table = table;
    }

    //Only call when table is full
    public void FIFO(int vpn, int pfn){
        table.removeFirst();
        table.addLast(vpn, pfn);
    }

    //TODO: public int countFrame(){}

    //TODO: public findAllDirty(){}
}
