package vm;

public class Policy {
    private int counter;

    public Policy(){
        counter = 0;
    }

    public int writeCounter(){
        counter++;
        if(counter > 256){
            counter = 0;
        }
        return counter;
    }

}