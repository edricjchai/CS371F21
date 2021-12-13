package utils;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer<T> {
    //Please note the biggest difference between this BoundBuffer
    //and the one we demoed in class is <T>
    //implement member functions: deposit() and fetch()

    private int[] buff;
    private int capacity;
    private int front;
    private int rear;
    private int count;
    private Lock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();

    /**
     * constructor for bounded buffer that is instantiated
     * with given capacity
     */
    public BoundedBuffer(int capacity) {
        super();
        buff = new int[capacity];
        this.capacity = capacity;
    }

    /**
     * deposits on the bounded buffer the item, while making
     * sure that it is synchronized for multi-threads with
     * locks and condition variables
     */
    public void deposit(int item) throws InterruptedException {
        lock.lock();
        while (count == capacity)
            notFull.await();
        buff[rear] = item;
        rear = (rear + 1) % capacity;
        count++;
        notEmpty.signal();
        System.out.println(Thread.currentThread()+"produced "+item);
        lock.unlock();
    }

    /**
     * fetch from the bounded buffer the item, while making
     * sure that it is synchronized for multi-threads with
     * locks and condition variables
     */
    public int fetch() throws InterruptedException {
        lock.lock();
        while (count == 0)
            notEmpty.await();
        int temp = (int) buff[front];
        front = (front + 1) % capacity;
        count--;
        notFull.signal();
        System.out.println(Thread.currentThread()+"consumed "+temp);
        lock.unlock();
        return temp;
    }

    public void printBuffer() {
        lock.lock();
        System.out.print("Size: " + count + "; Content: ");
        for (int i = 0; i < count; i++)
            System.out.print(buff[i]+ ", ");
        System.out.println();
        lock.unlock();
    }

    private static class Producer implements Runnable {
        private BoundedBuffer b;
        public Producer (BoundedBuffer b) {
            this.b = b;
        }
        @Override
        public void run() {
            for(int i = 1; i<20000; i=i+1 ) {
                try {
                    b.deposit(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Consumer implements Runnable {
        private BoundedBuffer b;
        public Consumer (BoundedBuffer b) {
            this.b = b;
        }
        @Override
        public void run() {
            while(true) {
                try {
                    int i = b.fetch();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BoundedBuffer b = new BoundedBuffer(10);
        Thread p1 = new Thread( new Producer(b));
        Thread c1 = new Thread( new Consumer(b));
        Thread c2 = new Thread( new Consumer(b));

        p1.start();
        c1.start();
        c2.start();

        p1.join();
        c1.join();
        c2.join();
    }
}