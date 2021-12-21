	// All reducers also have concurrent access to a kv store which maps
	// a key to all the value associated to the key, for instace
	// if reducer_i has fetched ("foo", 1) ("bar",1) ("foo",1)
	// ("foo",1) and ("bar",1) from partition_i,
	// then after injecting them, reducer_i should have updated the KV
        // store to contain ("foo", {1,1,1}) and ("bar", {1,1}). You can use a
	// concurrent hashmap/tree to implement the concurrent KV store
    package framework;
    import java.util.ArrayList;

    public class ConcurrentKVStore {
        private int num_partition;
        private ArrayList<kv> pair;
        private int count = 0;

        private class kv{
            private Object key;
            private ArrayList<Object> value;
            private kv next;

            public kv(Object key, Object value){
                this.key = key;
                this.value = new ArrayList<>();
                this.value.add(value);
                this.next = null;
            }
        }


        public ConcurrentKVStore(int num_reducers) {
            pair = new ArrayList<>();
            num_partition = num_reducers;
        }

        public void insert(Object key){
            if(pair == null || !pair.contains(key))
                pair.add(new kv(key, "1"));
            else{
                kv temp = new kv(key, pair.get(pair.indexOf(key)).value.add("1"));
                pair.set(pair.indexOf(key),temp);
            }
        }

        public int getPartitionNumber() {
            return num_partition;
        }
    }