package toutiao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MergeIntervals {

    public List<Interval> merge(List<Interval> intervals) {
    	if(intervals == null || intervals.size() == 0) return Collections.emptyList();
		Collections.sort(intervals, new Comparator<Interval>(){
			@Override
			public int compare(Interval o1, Interval o2) {
				return o1.start > o2.start ? 1 : o1.start == o2.start ? (o1.end > o2.end ? 1 : (o1.end == o2.end ? 0 : -1)) : -1;
			}
		});
		List<Interval> res = new ArrayList<>();
		int start = intervals.get(0).start, end = intervals.get(0).end;
		for(int i = 1; i < intervals.size(); i++){
			if(intervals.get(i).start <= end && intervals.get(i).end > end){
				end = intervals.get(i).end;
			}else if(intervals.get(i).start > end){
				Interval interval = new Interval();
				interval.start = start;
				interval.end = end;
				res.add(interval);
				start = intervals.get(i).start;
				end = intervals.get(i).end;
			}
		}
		Interval interval = new Interval();
		interval.start = start;
		interval.end = end;
		res.add(interval);
		return res;
    }
    
	public static void main(String[] args) {
		MergeIntervals mergeIntervals= new MergeIntervals();
		List<Interval> intervals = new ArrayList<>();
		intervals.add(new Interval(1,4));
		intervals.add(new Interval(0,4));
		intervals.add(new Interval(5,8));
		intervals.add(new Interval(4,6));
		intervals.add(new Interval(8,10));
		intervals.add(new Interval(15,18));
		List<Interval> res = mergeIntervals.merge(intervals);
		for(Interval in : res) System.out.print("[" + in.start + "->" + in.end + "]  ");
	}

}

class Interval {
	 int start;
	 int end;
	 Interval() { start = 0; end = 0; }
	 Interval(int s, int e) { start = s; end = e; }
}