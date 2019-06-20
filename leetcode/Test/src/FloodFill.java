
public class FloodFill {

	public int[][] floodFill(int[][] image, int sr, int sc, int newColor) {
		if(image == null || image.length == 0) return image;
		if(image[sr][sc] == newColor) return image;
		flood(image, sr, sc, image.length, image[0].length, image[sr][sc], newColor);
		return image;
	}
	private void flood(int[][] image, int x, int y, int width, int height, int color, int newColor){
		if(x < 0 || x >= width || y < 0 || y >= height) return;
		if(image[x][y] != color) return;
		image[x][y] = newColor;
		flood(image, x - 1, y, width, height, color, newColor);
		flood(image, x + 1, y, width, height, color, newColor);
		flood(image, x, y - 1, width, height, color, newColor);
		flood(image, x, y + 1, width, height, color, newColor);
	}
	
	public static void main(String[] args) {
		FloodFill flood = new FloodFill();
		int[][] res = flood.floodFill(new int[][]{{1,1,1},{1,1,0},{1,0,1}}, 1, 1, 2);
		for(int i = 0; i < res.length; i++){
			for(int j = 0; j < res[i].length; j++)
				System.out.print(res[i][j] + " ");
			System.out.println();
		}
		res = flood.floodFill(new int[][]{{0,0,0},{0,1,1}}, 1, 1, 1);
		for(int i = 0; i < res.length; i++){
			for(int j = 0; j < res[i].length; j++)
				System.out.print(res[i][j] + " ");
			System.out.println();
		}
	}

}
