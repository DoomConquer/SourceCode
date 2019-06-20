import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class InsertDeleteGetRandomDuplicatesallowed {

	public static void main(String[] args) {
		RandomizedCollection collection = new RandomizedCollection();
		System.out.println(collection.insert(4));
		System.out.println(collection.insert(3));
		System.out.println(collection.insert(4));
		System.out.println(collection.insert(2));
		System.out.println(collection.insert(4));
		System.out.println(collection.remove(4));
		System.out.println(collection.remove(3));
		System.out.println(collection.remove(4));
		System.out.println(collection.remove(4));
		System.out.println(collection.getRandom());
		System.out.println(collection.getRandom());
		System.out.println(collection.getRandom());
		System.out.println(collection.getRandom());
	}

}

class RandomizedCollection {

    /** Initialize your data structure here. */
	private Map<Integer, Set<Integer>> map;
	private List<Integer> list;
	private Random ran;
    public RandomizedCollection() {
        map = new HashMap<>();
        list = new ArrayList<>();
        ran = new Random();
    }
    
    /** Inserts a value to the collection. Returns true if the collection did not already contain the specified element. */
    public boolean insert(int val) {
    	boolean flag = true;
    	if(map.containsKey(val)) flag = false;
    	list.add(val);
    	Set<Integer> set = map.getOrDefault(val, new HashSet<>());
    	set.add(list.size() - 1);
    	map.put(val, set);
        return flag;
    }
    
    /** Removes a value from the collection. Returns true if the collection contained the specified element. */
    public boolean remove(int val) {
        if(!map.containsKey(val) || map.get(val).size() == 0) return false;
        Set<Integer> set = map.get(val);
    	int index = set.iterator().next();
		int last = list.size() - 1;
		if(list.get(index) != list.get(last)){
			list.set(index, list.get(last));
			Set<Integer> s = map.get(list.get(last));
			s.remove(last);
			s.add(index);
			map.put(list.get(last), s);
			set.remove(index);
		}else{
			set.remove(last);
		}
		list.remove(last);
		map.put(val, set);
        return true;
    }
    
    /** Get a random element from the collection. */
    public int getRandom() {
        return list.get(ran.nextInt(list.size()));
    }
}