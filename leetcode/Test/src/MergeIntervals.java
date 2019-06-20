import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MergeIntervals {

	public List<Interval> merge(List<Interval> intervals) {
		Collections.sort(intervals, (o1, o2) -> { return o1.start - o2.start; });
		List<Interval> res = new ArrayList<>();
		if(intervals == null || intervals.size() == 0) return res;
		int start = intervals.get(0).start;
		int end = intervals.get(0).end;
		for(int i = 1; i < intervals.size(); i++){
			Interval interval = intervals.get(i);
			if(end >= interval.start && end < interval.end){
				end = interval.end;
			}else if(end < interval.start){
				Interval inter = new Interval(start, end);
				res.add(inter);
				start = interval.start;
				end = interval.end;
			}
		}
		Interval inter = new Interval(start, end);
		res.add(inter);
		return res;
	}
	
	public static void main(String[] args) {
		MergeIntervals merge = new MergeIntervals();
		List<Interval> intervals = new ArrayList<>();
		intervals.add(new Interval(1,2));
		intervals.add(new Interval(1,2));
		intervals.add(new Interval(2,5));
		intervals.add(new Interval(6,7));
		intervals.add(new Interval(10,12));
		System.out.println(merge.merge(intervals));
	}

}

class Interval {
     int start;
     int end;
     Interval() { start = 0; end = 0; }
     Interval(int s, int e) { start = s; end = e; }
     public String toString(){
    	 return start + " -> " + end;
     }
 }