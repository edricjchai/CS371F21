package vm;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class VirtMemoryTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }
    @Test
    public void test1_OutOfRange() {
        Memory m = new VirtMemory();
        m.startup();
        m.write(0xFFFFFF, Byte.parseByte("-1"));
        MatcherAssert.assertThat(0, not(errContent.toString().length()));
        byte x = m.read(0xFFFFFF);
        MatcherAssert.assertThat(0, not(errContent.toString().length()));
        //Code review q1: what is the max legit address for m.write()??
        /**
         * The biggest address that m is able to write to is 0xFFFFFF, the virtual memory is 2^16 bytes which means the address space is 16 bits.
         * This 16-bit divided into the ratio of 10:6, the 10 bits (2^10) is 1024 VPNs and the 6 bits (2^6) is 64 bytes are the page sizes.
         */
        m.shutdown();
    }
    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
    @Test
    public void test2_SingleWrite() {
        Memory m = new VirtMemory();
        m.startup();
        m.write(0x8000, Byte.parseByte("-1")); //write it to somewhere way beyond 16K
        m.shutdown();
        //now the disk should have persisted your write, reboot
        m = new VirtMemory();
        m.startup();
        byte data = m.read(0x8000);
        m.shutdown();
        assertEquals(Byte.parseByte("-1"), data);
    }
    @Test
    public void test3_WriteBackToSameBlock() {
        //every 32 writes triggers a write-back to disk.
        Memory m = new VirtMemory();
        m.startup();
        for(int i=0; i<32; i++) {
            m.write(i, Byte.parseByte("-1")); //32 writes <-- looping 1 write 32 times
        }
        m.shutdown();
        int writeCount = m.getPhyMemory().writeCountDisk();
        int readCount = m.getPhyMemory().readCountDisk();
        assertEquals(1, writeCount);
        //Code review q2: why is there only 1 disk write?
        /**
         *  the write counter in the physical memory has triggered a write-back due to being written to 32 times which is half a page which is considered worthy to be written back to
         */
        assertEquals(1, readCount);
        //Code review q3: why is there only 1 disk read?
        /**
         * read is referring to the written page in the physical memory written-back to 1 time
         */
    }
    @Test
    public void test4_WriteBackToMultiBlocks() {
        //every 32 writes triggers a write-back to disk.
        Memory m = new VirtMemory();
        m.startup();
        for(int i=0; i<32; i++) {
            m.write(i*64, Byte.parseByte("-1")); //64 writes * 32 times <- 64 writes = 1 page. (32 writes = 1/2 page. 1writeback)
        }

        m.shutdown();
        int writeCount = m.getPhyMemory().writeCountDisk();
        int readCount = m.getPhyMemory().readCountDisk();
        assertEquals(32, writeCount);
        //Code review q4: why are there 32 disk writes?
        /**
         * 64 bytes are the page sizes. Each times to access the write method it times the 64 means each time to access the write method
         * take 1 disk writes and the for loop access the write method 32 times means there is 32 disk writes.
         */

        assertEquals(32, readCount);
        //Code review q5: why are there 32 disk read?
        /**
         * read counter is referring to the written page in the physical memory that has been written-back to 32 times
         */
    }
    //the following are more realistic workloads
    static final int TEST_SIZE = 64*1024;// 64K, test on max address space!
    static byte fce(int adr) {
        return (byte) ((adr * 5 + 6) % 256 - 128);
    }
    static byte fce2(int adr) {
        return (byte) ((adr * 7 + 5) % 256 - 128);
    }
    @Test
    public void test5_End2EndForward() {
        Memory m = new VirtMemory();
        m.startup();
        boolean result = true;
        for (int i = 0; i < TEST_SIZE; i++)
            m.write(i, fce(i));
        for (int i = 0; i < TEST_SIZE; i++)
            if (m.read(i) != fce(i))
                result = false;
        assertEquals(true, result);
        m.shutdown();
        assertEquals(2048, m.getPhyMemory().writeCountDisk());
        //Code review q6: why are there 2048 disk writes?
        /**
         * The first for-loop write the data into the virtual memory with the max address space 16 bits. Every 32 writes are half page and cause one write-back.
         * One page is 2*32 = 64 bytes. There are 1024 pages which is 2 * 1024 = 2048 disk writes.
         */
        assertEquals(2048, m.getPhyMemory().readCountDisk());
        //Code review q7: why are there 2048 disk reads?
        /**
         * The read method is to load the page from the disk to physical memory.
         * The second for-loop is comparing the the used space and free space in the disk, to check is there a place to import new data into the disk instead of override the used pages.
         * The write loop take 2048 disk writes to store the data and the read loop also need the same amount of disk to check the memory space.
         */
    }
    @Test
    public void test6_End2EndBackward() {
        Memory m = new VirtMemory();
        m.startup();
        boolean result = true;
        for (int i = 0; i < TEST_SIZE; i++)
            m.write(i, fce(i));
        for (int i = TEST_SIZE-1; i >= 0; i--)
            if (m.read(i) != fce(i))
                result = false;
        assertEquals(true, result);
        m.shutdown();
        assertEquals(2048, m.getPhyMemory().writeCountDisk());
        //Code review q8: why are there 2048 disk writes?
        /**
         * 32 writes is half page and cause one write back to the disk immediately. 1024 page times two is 2048 disk writes.
         */
        assertEquals(1792, m.getPhyMemory().readCountDisk());
        //Code review q9: why are there 1792 disk writes? Why is it different from test5?
        /**
         * It is different from test5 because it start from the TEST_SIZE-1 and stop when i >= 0, and test5 is start from 0 and stop when i < TEST_SIDE.
         * The read method on for-loop is walking backward.
         * 16K/64B = 256, the physical memory is holding 256 pages. Because the read method is going backward and it already read this 256 pages.
         * The other pages need to read is 1024-256=768 pages, the read counter need to reload this 768 pages.
         * The total number of read is 1024 + 768 = 1792 reads.
         */
    }
    @Test
    public void test7_End2EndMix() {
        Memory m = new VirtMemory();
        m.startup();
        boolean result = true;
        for (int i = TEST_SIZE-1; i >= 0; i--)
            m.write(i, fce(i));
        for (int posun = 0; posun < TEST_SIZE; posun += 100) {
            for (int i = 0; i < TEST_SIZE; i++) {
                int adr = (i+posun)%TEST_SIZE;
                if (m.read(adr) != fce(adr))
                    result = false;
            }
        }
        int posun_zapis=55;
        for (int i = 0; i < TEST_SIZE; i++) {
            int adr = (i+posun_zapis)%TEST_SIZE;
            m.write(adr, fce2(adr));
        }
        for (int posun = 20; posun < TEST_SIZE; posun += 100) {
            for (int i = 0; i < TEST_SIZE; i++) {
                int adr = (i+posun)%TEST_SIZE;
                if (m.read(adr) != fce2(adr))
                    result = false;
            }
        }
        assertEquals(true, result);
        m.shutdown();
    }
}
