package vm;

public class MyPageTable{
    private static int INITIAL_SIZE = 1024;
    PageTableEntry[] table;
    private Policy tablePolicy;
    private int counter = 0;

    MyPageTable(){
        table = new PageTableEntry[INITIAL_SIZE];
        for(int i = 0; i < INITIAL_SIZE; i++)
            table[i] = null;
    }

    private static class PageTableEntry {
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
        public boolean hasNext() {
            return next != null;
        }

        public String toString(){
            return (String)("VPN: " + vpn + " PFN: " + pfn + " Dirty: " + dirty);
        }
    }

    public boolean contains(int addr) throws PageFaultException {
        return getByVPN(addr/64) == addr;
    }

    public void removeFirst(){
        table[0] = null;
    }
    public void addLast(int vpn, int pfn){
        for(int i = 0; i <= table.length; i++){
            if(table[i] == null){
                table[i] = new PageTableEntry(vpn, pfn);
                break;
            }
        }
    }

    public void removePage(PageTableEntry pte){
        int idx = pte.vpn / 64;
        if(table[idx] != null){
            PageTableEntry[] temp = new PageTableEntry[table.length-1];
            for(int i = 0, j = 0; i < table.length; i++){
                if(!table[i].equals(pte))
                    temp[j++] = temp[i];
            }
            table = temp;
        }
    }

    public void putEntry(int vpn, int pfn){
        int index = vpn / 64;

        PageTableEntry entry = table[index];
        if(table[index] == null){
            entry = new PageTableEntry(vpn, pfn);
        }else{
            while(entry.hasNext() && entry.vpn != vpn){
                entry = entry.next;
            }
            if(entry.hasNext())
                entry.next = new PageTableEntry(vpn, pfn);
            else{
                PageTableEntry temp = entry.next;
                entry.next = new PageTableEntry(vpn, pfn);
                entry.next.next = temp;
            }
        }
        table[index] = entry;
    }

    public int getByVPN(int vpn) throws PageFaultException {
        for(PageTableEntry i : table){
            if(vpn == i.vpn){
                return i.pfn;
            }
        }
        throw new PageFaultException();
    }
    public int getByPFN(int pfn) throws PageFaultException {
        for(PageTableEntry i: table){
            if(pfn == i.pfn) return i.vpn;
        }
        throw new PageFaultException();
    }
    /*public boolean getDirty(int vpn){
        int index = Math.abs(hash(vpn) % INITIAL_SIZE);
        if(table[index].vpn == vpn){
            return table[index].dirty;
        }else{

        }
        return false;
    }
*/
    /*public void rehash(){
        INITIAL_SIZE = INITIAL_SIZE * 2;
        counter = 0;
        PageTableEntry[] temp = table;
        table = new PageTableEntry[INITIAL_SIZE];

        for(int i = 0; i < INITIAL_SIZE; i++){
            if(temp[i] != null){
                continue;
            }
            PageTableEntry add = temp[i];
            int index = Math.abs(add.vpn.hashCode()%INITIAL_SIZE);
            table[index] = temp[i];
            counter++;

        }
    }
*/


}
