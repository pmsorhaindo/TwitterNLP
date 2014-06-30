package util;

import java.util.ArrayList;

public class ViterbiPaths {
	
	private ArrayList<ArrayList<Double>> paths;
	private double probs[];
	
	public ViterbiPaths() {
		
	}

	public ArrayList<ArrayList<Double>> getPaths() {
		return paths;
	}

	public void setPaths(ArrayList<ArrayList<Double>> paths) {
		this.paths = paths;
	}
	
	public void addPaths(double[][] newPaths)
	{
		for(int i = 0; i<newPaths.length; i++)
		{
			ArrayList<Double> arr = new ArrayList<Double>();
			paths.add(arr);
			for(int j =0; j<newPaths[i].length; j++)
			{
				paths.get(i).add(newPaths[i][j]);
			}
		}
	}

	public double[] getProbs() {
		return probs;
	}

	public void setProbs(double probs[]) {
		this.probs = probs;
	}
	
	

}
