	// All reducers also have concurrent access to a kv store which maps
	// a key to all the value associated to the key, for instace
	// if reducer_i has fetched ("foo", 1) ("bar",1) ("foo",1)
	// ("foo",1) and ("bar",1) from partition_i,
	// then after injecting them, reducer_i should have updated the KV
        // store to contain ("foo", {1,1,1}) and ("bar", {1,1}). You can use a
	// concurrent hashmap/tree to implement the concurrent KV store
    package framework;
    import java.util.ArrayList;
    import java.util.Iterator;
    import java.util.NoSuchElementException;

    public class ConcurrentKVStore {
        private int num_partition;
        private ArrayList<kv> pair;
        private int count = 0;

        private class kv{
            private Object key;
            private int value;
            private kv next;

            public kv(Object key, int value){
                this.key = key;
                this.value = value;
                this.next = null;
            }
        }


        public ConcurrentKVStore(int num_reducers) {
            pair = new ArrayList<>();
            num_partition = num_reducers;
        }

        public void insert(Object key){
            kv kvPair = new kv(key, 1);
            pair.add(kvPair);
        }

        public int getPartitionNumber() {
            return num_partition;
        }
    }