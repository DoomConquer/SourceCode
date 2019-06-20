import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlattenNestedListIterator {

	public static void main(String[] args) {
		List<NestedInteger> nestedList = new ArrayList<>();
		NestedIterator iter = new NestedIterator(nestedList);
		iter.next();
	}

}

interface NestedInteger {
	// @return true if this NestedInteger holds a single integer, rather than a nested list.
	public boolean isInteger();
 
	// @return the single integer that this NestedInteger holds, if it holds a single integer
	// Return null if this NestedInteger holds a nested list
	public Integer getInteger();
 
	// @return the nested list that this NestedInteger holds, if it holds a nested list
	// Return null if this NestedInteger holds a single integer
	public List<NestedInteger> getList();
}

class NestedIterator implements Iterator<Integer> {

	private List<Integer> list;
	private int curr = 0;
    public NestedIterator(List<NestedInteger> nestedList) {
        this.list = new ArrayList<>();
        init(nestedList);
    }
    private void init(List<NestedInteger> nestedList){
    	for(NestedInteger nested : nestedList){
    		if(nested.isInteger()){
    			list.add(nested.getInteger());
    		}else{
    			init(nested.getList());
    		}
    	}
    }

    @Override
    public Integer next() {
        if(curr >= list.size()) return null;
        Integer res = list.get(curr);
        curr++;
        return res;
    }

    @Override
    public boolean hasNext() {
        if(curr >= list.size()) return false;
        return true;
    }
}