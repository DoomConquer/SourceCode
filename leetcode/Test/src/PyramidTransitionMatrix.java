import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PyramidTransitionMatrix {

	public boolean pyramidTransition(String bottom, List<String> allowed) {
		if(bottom == null || bottom.length() == 0 || allowed == null || allowed.size() == 0) return false;
		Map<String, List<String>> map = new HashMap<>();
		for(String s : allowed){
			String prefix = s.substring(0, 2);
			List<String> list = map.getOrDefault(prefix, new ArrayList<>());
			list.add(s.substring(2));
			map.put(prefix, list);
		}
		return pyramid(bottom, map, new StringBuilder(), bottom.length(), 1, 0);
	}
	private boolean pyramid(String bottom, Map<String, List<String>> map, StringBuilder sb, int len, int layer, int strIndex){
		if(layer == len - 1){
			if(map.containsKey(bottom)) return true;
			return false;
		}
		if(sb.length() == bottom.length() - 1){
			return pyramid(sb.toString(), map, new StringBuilder(), len, layer + 1, 0);
		}
		for(int i = strIndex; i < bottom.length() - 1; i++){
			String prefix = bottom.substring(i, i + 2);
			if(map.containsKey(prefix)){
				List<String> list = map.get(prefix);
				for(int j = 0; j < list.size(); j++){
					sb.append(list.get(j));
					if(pyramid(bottom, map, sb, len, layer, i + 1)) return true;
					sb.deleteCharAt(sb.length() - 1);
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		PyramidTransitionMatrix pyramid = new PyramidTransitionMatrix();
		System.out.println(pyramid.pyramidTransition("XYZ", Arrays.asList(new String[]{"XYD", "YZE", "DEA", "FFF"})));
		System.out.println(pyramid.pyramidTransition("BBBBGEAA", Arrays.asList(new String[]{"BGE","BGG","AGE","AGC","AGB","AGA","EGC","CCD","EGA","EGD","GEC","DCF","DCD","DCB","FGF","FGG","FGB","BFB","BFG","BFE","GCG","EDG","DBC","DBB","DBE","DBD","DBG","FFF","FFD","FFC","GBG","GBE","GBB","FDD","GBA","BEC","BEG","BEE","AEC","AEB","AEE","DEA","DEC","EEA","DEF","EEB","CEG","CEC","CEB","CEA","DDE","GEE","BDE","BDD","BDA","GCD","BDC","GCF","AFD","AFA","AFC","DDC","ECC","DDA","EFC","EFF","EFG","CBB","CBA","CBG","CBE","ACC","CGB","ACF","ACE","ACD","BCD","BCF","BCB","FDB","EFA","EFB","ECG","DGC","DGF","DGG","CGA","FDF","CGD","CGG","CGF","GGD","GGF","GGA","FCE","FCG","DDD","FEB","FEC","FEA","BBF","BBC","ADF","ADD","ADB","ADC","DFE","DFD","DFG","DFA","EDB","CDE","CDG","CDA","EDE","GDA","FBA","FBF","FBE","AAE","AAD","AAG","AAC","BAF","BAG","BAD","EED","CAC","GCA","CAF","CAD","DAD","DAE","DAF","DAG","DAA","DAB","DAC","EEC","GAG","DEG","GAB","ABD","ABE","ABF","ABG","DDB","EBF","EBB","CFF","CFB","GFC","GFD","GFE"})));
	}

}
