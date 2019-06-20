import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

// ²Î¿¼leetcodeË¼Â·
public class EvaluateDivision {

    public double[] calcEquation(List<List<String>> equations, double[] values, List<List<String>> queries) {
        Map<String, String> root = new HashMap<>();
        Map<String, Double> distToRoot = new HashMap<>();
        for(int i = 0; i < equations.size(); i++){
        	List<String> list = equations.get(i);
        	String root1 = findRoot(root, distToRoot, list.get(0));
        	String root2 = findRoot(root, distToRoot, list.get(1));
        	root.put(root1, root2);
        	distToRoot.put(root1, distToRoot.get(list.get(1)) * values[i] / distToRoot.get(list.get(0)));
        }
        
        double[] res = new double[queries.size()];
        for(int i = 0; i < queries.size(); i++){
        	if(!root.containsKey(queries.get(i).get(0)) || !root.containsKey(queries.get(i).get(1))){
        		res[i] = -1.0;
        		continue;
        	}
        	String root1 = findRoot(root, distToRoot, queries.get(i).get(0));
        	String root2 = findRoot(root, distToRoot, queries.get(i).get(1));
        	if(!root1.equals(root2)){
        		res[i] = -1.0;
        		continue;
        	}
        	res[i] = distToRoot.get(queries.get(i).get(0)) / distToRoot.get(queries.get(i).get(1));
        }
        return res;
    }
    private String findRoot(Map<String, String> root, Map<String, Double> distToRoot, String s){
    	if(!root.containsKey(s)){
    		root.put(s, s);
    		distToRoot.put(s, 1.0);
    		return s;
    	}
    	if(root.get(s).equals(s)) return s;
    	String toNode = root.get(s);
    	String rootNode = findRoot(root, distToRoot, toNode);
    	root.put(s, rootNode);
    	distToRoot.put(s, distToRoot.get(s) * distToRoot.get(toNode));
    	return rootNode;
    }
    
    public double[] calcEquation1(String[][] equations, double[] values, String[][] queries) {
        HashMap<String, ArrayList<String>> pairs = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<Double>> valuesPair = new HashMap<String, ArrayList<Double>>();
        for (int i = 0; i < equations.length; i++) {
            String[] equation = equations[i];
            if (!pairs.containsKey(equation[0])) {
                pairs.put(equation[0], new ArrayList<String>());
                valuesPair.put(equation[0], new ArrayList<Double>());
            }
            if (!pairs.containsKey(equation[1])) {
                pairs.put(equation[1], new ArrayList<String>());
                valuesPair.put(equation[1], new ArrayList<Double>());
            }
            pairs.get(equation[0]).add(equation[1]);
            pairs.get(equation[1]).add(equation[0]);
            valuesPair.get(equation[0]).add(values[i]);
            valuesPair.get(equation[1]).add(1/values[i]);
        }
        
        double[] result = new double[queries.length];
        for (int i = 0; i < queries.length; i++) {
            String[] query = queries[i];
            result[i] = dfs(query[0], query[1], pairs, valuesPair, new HashSet<String>(), 1.0);
            if (result[i] == 0.0) result[i] = -1.0;
        }
        return result;
    }
    private double dfs(String start, String end, HashMap<String, ArrayList<String>> pairs, HashMap<String, ArrayList<Double>> values, HashSet<String> set, double value) {
        if (set.contains(start)) return 0.0;
        if (!pairs.containsKey(start)) return 0.0;
        if (start.equals(end)) return value;
        set.add(start);
        
        ArrayList<String> strList = pairs.get(start);
        ArrayList<Double> valueList = values.get(start);
        double tmp = 0.0;
        for (int i = 0; i < strList.size(); i++) {
            tmp = dfs(strList.get(i), end, pairs, values, set, value*valueList.get(i));
            if (tmp != 0.0) {
                break;
            }
        }
        set.remove(start);
        return tmp;
    }
    
	public static void main(String[] args) {
		EvaluateDivision evaluateDivision = new EvaluateDivision();
		List<List<String>> equations = new ArrayList<>();
		equations.add(Arrays.asList(new String[]{"a", "b"}));
		equations.add(Arrays.asList(new String[]{"b", "c"}));
		List<List<String>> queries = new ArrayList<>();
		queries.add(Arrays.asList(new String[]{"a", "c"}));
		queries.add(Arrays.asList(new String[]{"b", "a"}));
		queries.add(Arrays.asList(new String[]{"a", "e"}));
		queries.add(Arrays.asList(new String[]{"a", "a"}));
		queries.add(Arrays.asList(new String[]{"x", "x"}));
		System.out.println(Arrays.toString(evaluateDivision.calcEquation(equations, new double[]{2.0, 3.0}, queries)));
		
		equations = new ArrayList<>();
		equations.add(Arrays.asList(new String[]{"a", "b"}));
		equations.add(Arrays.asList(new String[]{"e", "f"}));
		equations.add(Arrays.asList(new String[]{"b", "e"}));
		queries = new ArrayList<>();
		queries.add(Arrays.asList(new String[]{"b", "a"}));
		queries.add(Arrays.asList(new String[]{"a", "f"}));
		queries.add(Arrays.asList(new String[]{"f", "f"}));
		queries.add(Arrays.asList(new String[]{"e", "e"}));
		queries.add(Arrays.asList(new String[]{"c", "c"}));
		queries.add(Arrays.asList(new String[]{"a", "c"}));
		queries.add(Arrays.asList(new String[]{"f", "e"}));
		System.out.println(Arrays.toString(evaluateDivision.calcEquation(equations, new double[]{3.4, 1.4, 2.3}, queries)));
	}

}
