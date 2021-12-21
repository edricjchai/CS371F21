package framework;
import java.util.ArrayList;
import java.util.LinkedList;

public class MyMapReduce extends MapReduce {
	private ConcurrentKVStore kvStore;
	private ArrayList<PartitionTableTemp> table; // = new ArrayList<>();
	private MapperReducerClientAPI mapperReducerObj;

	@Override
	public void MREmit(Object key, Object value)
	{
		//TODO: your code here. Delete UnsupportedOperationException after your implementation is done.
		int smallestPartition = 0;
		for(int i = 0; i < table.size(); i++){
			if(table.get(i).size() < smallestPartition) smallestPartition = i;
		}
		table.get(smallestPartition).deposit(key);

		//throw new UnsupportedOperationException();
	}

	public Object MRGetNext(Object key, int partition_number) {
		//TODO: your code here. Delete UnsupportedOperationException after your implementation is done.
		return table.get(partition_number).iterator().next();
	}
	@Override
	protected void MRRunHelper(String inputFileName,
							   MapperReducerClientAPI mapperReducerObj,
							   int num_mappers,
							   int num_reducers)
	{
		//TODO: your code here. Delete UnsupportedOperationException after your implementation is done.
		int partitioner = (int)mapperReducerObj.Partitioner(inputFileName, num_mappers);

		table = new ArrayList<>();
		int split = 0;
		int nextSplit = inputFileName.length()/num_mappers; // size of partition table
		int remainder = inputFileName.length()%num_mappers;
		for(int i = 0; i < num_mappers; i++) {

			table.add(new PartitionTableTemp());
			//Splits the file based on the number of mappers then maps them as inputs
			String fileSplit = inputFileName.substring(split, nextSplit);
			//Put the splitted file inputs into the partition table
			int count = 0;
			//mapperReducerObj.Map(fileSplit);
			while(count < nextSplit){
				table.get(i).deposit(fileSplit);
				count++;
				if(remainder > 1){
					table.get(i).deposit(inputFileName.substring(nextSplit, nextSplit+1));
					nextSplit++;
					remainder--;
				}
			}
			split = nextSplit;
			nextSplit += nextSplit;
		}

		kvStore = new ConcurrentKVStore(num_reducers);
		reducer();
		//throw new UnsupportedOperationException();
	}

	/**
	 *
	 */
	public void reducer(){
		LinkedList<Object> temp = new LinkedList<Object>();

		//Store values in concurrentKV
		for(int i = 0; i < table.size();i++){
			temp.add(table.get(i).getList());
			while(temp.iterator().hasNext()) {
				kvStore.insert(temp);
				temp.iterator().next();
			}
		}
	}

}