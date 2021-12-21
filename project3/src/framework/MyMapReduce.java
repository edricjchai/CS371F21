package framework;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MyMapReduce extends MapReduce {
	private ConcurrentKVStore kvStore;
	private ArrayList<PartitionTableTemp> table; // = new ArrayList<>();
	private MapperReducerClientAPI mapperReducerObj;
	private ArrayList<String> text = new ArrayList<>();

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
							   int num_reducers) {
		//TODO: your code here. Delete UnsupportedOperationException after your implementation is done.
		int partitioner = (int)mapperReducerObj.Partitioner(inputFileName, num_mappers);

		readFile(inputFileName);

		table = new ArrayList<>();
		int split = 0;
		int nextSplit = text.size()/num_mappers; // size of partition table
		int remainder = text.size()%num_mappers;
		for(int i = 0; i < num_mappers; i++) {

			table.add(new PartitionTableTemp());
			//Splits the file based on the number of mappers then maps them as inputs
			//Put the splitted file inputs into the partition table
			List<String> partition = text.subList(split,nextSplit);
			int j = 0;
			for(; j < partition.size(); j++)
				table.get(i).deposit(partition.get(j));

			split = nextSplit;
			nextSplit += nextSplit;

			while(remainder > 0){
				table.get(i).deposit(partition.get(j-1));
				nextSplit++;
				j++;
				remainder--;
			}

		}

		kvStore = new ConcurrentKVStore(num_reducers);
		reducer();
		//throw new UnsupportedOperationException();
	}

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

	private void readFile(String inputFileName) {
		try {
			Scanner scan = new Scanner(new File(inputFileName));
			while(scan.hasNext())
				text.add(scan.next());
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}