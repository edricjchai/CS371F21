package framework;

import java.util.ArrayList;

public class MyMapReduce extends MapReduce {
	//TODO: your code here. Define all attributes 
	//What is in a running instance of MapReduce?
	PartitionTable kv = new PartitionTable();
	ConcurrentKVStore kvStore;
	int counter;
	private ArrayList<PartitionTable> table = new ArrayList<>();

	@Override
	public void MREmit(Object key, Object value)
	{
		//TODO: your code here. Delete UnsupportedOperationException after your implementation is done.
		kv.insert(key, value);
		//throw new UnsupportedOperationException();
	}

	public Object MRGetNext(Object key, int partition_number) {

		if(kv.iterator().hasNext()){
			kv.iterator().next();
			return kv.getKey();
		}
		return null;
		//TODO: your code here. Delete UnsupportedOperationException after your implementation is done.
		//throw new UnsupportedOperationException();
	}
	@Override
	protected void MRRunHelper(String inputFileName,
		    		  MapperReducerClientAPI mapperReducerObj,
		    		  int num_mappers, 
		    		  int num_reducers)
	{
		//TODO: your code here. Delete UnsupportedOperationException after your implementation is done.
		int split = 0;
		int nextSplit = inputFileName.length()/num_mappers; // size of partition table
		int remainder = inputFileName.length()%num_mappers;
		for(int i = 0; i < num_mappers; i++) {
			//Splits the file based on the number of mappers then maps them as inputs
			String fileSplit = inputFileName.substring(split, nextSplit);

			//Put the splitted file inputs into the partition table

			mapperReducerObj.Map(fileSplit);
			split = nextSplit;
			nextSplit += nextSplit;
		}

		for(int i = 0; i < num_reducers; i++) {
			//Combine the key value pairs from all the partition table threads into the concurrentKVStore
			PartitionTable temp = kv;
			while(temp.iterator().hasNext()){
				kvStore.insert(temp.getKey());
				temp.iterator().next();
			}
			//Create separate outputs of the key value pairs based on the number of reducers
			//output??
			while (kv.iterator().hasNext()) {
				//mapperReducerObj.Reduce(kv.getKey(),kvStore.getPartitionNumber());
				kv.iterator().next();
			}
		}
		//throw new UnsupportedOperationException();
	}
}