import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InsertDeleteGetRandom {

	public static void main(String[] args) {
		RandomizedSet set = new RandomizedSet();
		System.out.println(set.insert(1));
		System.out.println(set.insert(2));
		System.out.println(set.insert(3));
		System.out.println(set.insert(1));
		System.out.println(set.remove(1));
		System.out.println(set.remove(2));
		System.out.println(set.remove(3));
		System.out.println(set.getRandom());
		System.out.println(set.getRandom());
		System.out.println(set.getRandom());
		System.out.println(set.getRandom());
		System.out.println(set.getRandom());
		System.out.println(set.getRandom());
		System.out.println(set.getRandom());
	}

}

class RandomizedSet {

    /** Initialize your data structure here. */
	private Map<Integer, Integer> map;
	private List<Integer> list;
	private Random ran = new Random();
    public RandomizedSet() {
        map = new HashMap<>();
        list = new ArrayList<>();
    }
    
    /** Inserts a value to the set. Returns true if the set did not already contain the specified element. */
    public boolean insert(int val) {
        if(map.containsKey(val)) return false;
        list.add(val);
        int index = list.size() - 1;
        map.put(val, index);
        return true;
    }
    
    /** Removes a value from the set. Returns true if the set contained the specified element. */
    public boolean remove(int val) {
        if(!map.containsKey(val)) return false;
        int index = map.get(val);
        if(list.size() == 1){
        	list.clear();
        }else{
        	int last = list.size() - 1;
        	list.set(index, list.get(last));
        	map.put(list.get(last), index);
        	list.remove(last);
        }
        map.remove(val);
        return true;
    }
    
    /** Get a random element from the set. */
    public int getRandom() {
        int size = list.size();
        if(size == 0) return 0;
        int random = ran.nextInt(size);
        return list.get(random);
    }
}