import java.util.ArrayList;
import java.util.List;

public class BinaryWatch {

	public List<String> readBinaryWatch(int num) {
        int[] times = new int[]{8,4,2,1,32,16,8,4,2,1};
        List<String> res = new ArrayList<String>();
        read(res, new ArrayList<Integer>(), 0, num, times);
        return res;
    }
	private void read(List<String> res, List<Integer> seq, int start, int num, int[] times){
		if(seq.size() == num){
			int hour = 0;
			int minute = 0;
			for(int i : seq){
				if(i < 4)
					hour += times[i];
				else
					minute += times[i];
			}
			if(hour < 12 && minute < 60){
				String time = String.valueOf(hour) + ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute));
				res.add(time);
			}
		}else{
			for(int i = start; i < 10; i++){
				seq.add(i);
				read(res, seq, i + 1, num, times);
				seq.remove(seq.size() - 1);
			}
		}
	}
	
	public static void main(String[] args) {
		BinaryWatch watch = new BinaryWatch();
		System.out.println(watch.readBinaryWatch(8));
	}

}
