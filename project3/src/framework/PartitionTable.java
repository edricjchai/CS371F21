package framework;

import java.util.Iterator;

public class PartitionTable implements Iterable{
	//TODO: your codde here
	//Notes:
	// (1) each partition works like a bounded buffer between
	// mappers and a reducer. (you can assume size = 10 or 50)
	// (2) if reducer_i wants to fetch a KV pair it can
	// only fetches from partition_i, but mapper_i can drop messages
	// into different partitions.
    private Word front;

    @Override
    public Iterator iterator() {
        return new Iterator<>() {
            Word current;
            @Override
            public boolean hasNext() {
                return current == null ? front.next != null : current.next != null;
            }

            @Override
            public Object next() {
                if(hasNext())
                    current = current == null ? front : current.next;
                return current;
            }
        };
    }

    private class Word {
        private Object key;
        private Object value;
        private Word next;

        public Word(Object key, Object value){
            this.key = key;
            this.value = value;
            next = null;
        }

        public void setKey(Object key) {
            this.key = key;
        }
        public void setValue(int value) {
            this.value = value;
        }
        public void setNext(Word next) {
            this.next = next;
        }
        public Object getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
        public Word getNext() {
            return next;
        }

    }

    public PartitionTable(){
        front = null;
    }

    public PartitionTable(Word block){
        front = block;
    }

    public void insert(Object key, Object value){
        Word block = new Word(key, value);
        Word temp = front;
        if(front == null){
            front = block;
        }else{
            while(temp != null){
                temp = temp.next;
            }
            temp.next = block;
        }
    }

    public void remove(Object key){
        Word temp = front;
        while(temp != null){
            if(front.key == key && front.next != null){
                front = front.next;
            }
            else if(temp.next.key == key && temp.next.next != null){
                temp.next = temp.next.next;
                return;
            }else if(temp.next.key == key && temp.next.next == null){
                temp.next = null;
            }
            temp = temp.next;
        }
    }
    public boolean hasValue(Object key){
        Word temp = front;
        while(temp.next != null){
            if(temp.getKey().equals(key))
                return true;
            temp = temp.next;
        }
        return false;
    }

    public Object getKey(){
        return front.key;
    }

}

