package framework;

import java.util.ArrayList;

public class MyMapReduce extends MapReduce {
	//TODO: your code here. Define all attributes 
	//What is in a running instance of MapReduce?
	PartitionTable kv = new PartitionTable();
	ConcurrentKVStore kvStore;
	int counter;
	//private ArrayList<PartitionTable> table = new ArrayList<>();
	private ArrayList<PartitionTable> table;

	@Override
	public void MREmit(Object key, Object value)
	{
		//TODO: your code here. Delete UnsupportedOperationException after your implementation is done.
		kvStore.insert(key);
		//throw new UnsupportedOperationException();
	}

	public Object MRGetNext(Object key, int partition_number) {
		if(table.iterator().hasNext()){
			return table.iterator().next();
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
		table = new ArrayList<PartitionTable>();
		int split = 0;
		int nextSplit = inputFileName.length()/num_mappers; // size of partition table
		int remainder = inputFileName.length()%num_mappers;
		for(int i = 0; i < num_mappers; i++) {
			table.add(new PartitionTable());
			//Splits the file based on the number of mappers then maps them as inputs
			String fileSplit = inputFileName.substring(split, nextSplit);

			//Put the splitted file inputs into the partition table
			int count = 0;
			//mapperReducerObj.Map(fileSplit);
			while(count < nextSplit){

				table.get(i).insert(inputFileName.indexOf(count*i+1, count*i+2));
				count++;
				if(remainder > 1){
					table.get(i).insert(inputFileName.indexOf(count*i+2, count*i+3));
					remainder--;
				}
			}
			split = nextSplit;
			nextSplit += nextSplit;
		}
		//throw new UnsupportedOperationException();
	}
}
