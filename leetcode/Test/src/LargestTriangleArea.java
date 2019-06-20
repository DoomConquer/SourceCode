
public class LargestTriangleArea {

	public double largestTriangleArea(int[][] points) {
		int len = points.length;
		double max = 0;
		for(int i = 0; i < len; i++)
			for(int j = i + 1; j < len; j++)
				for(int k = j + 1; k < len; k++){
					double area = getArea(new int[][]{{points[i][0],points[i][1]}, {points[j][0], points[j][1]}, {points[k][0], points[k][1]}});
					if(max < area)
						max = area;
				}
		return max;
	}
	private double getArea(int[][] points) {
		double[] side = new double[3];
		side[0] = Math.sqrt(Math.pow(points[0][0] - points[1][0], 2) + Math.pow(points[0][1] - points[1][1], 2));
		side[1] = Math.sqrt(Math.pow(points[0][0] - points[2][0], 2) + Math.pow(points[0][1] - points[2][1], 2));
		side[2] = Math.sqrt(Math.pow(points[2][0] - points[1][0], 2) + Math.pow(points[2][1] - points[1][1], 2));

		if (side[0] + side[1] <= side[2] || side[0] + side[2] <= side[1] || side[1] + side[2] <= side[0])
			return 0;
		double p = (side[0] + side[1] + side[2]) / 2;
		double area = Math.sqrt(p*(p-side[0])*(p-side[1])*(p-side[2])); 
		return area;
	}

	public static void main(String[] args) {
		LargestTriangleArea area = new LargestTriangleArea();
		System.out.println(area.largestTriangleArea(new int[][]{{0,0},{0,1},{1,0},{1,1},{0,2},{2,0}}));
	}

}
