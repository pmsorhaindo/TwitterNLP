package impl.decoders.viterbi;

import java.util.ArrayList;

public class Sequence {
	
	private ArrayList<Integer> listOfTags = new ArrayList<Integer>();
	private double probabilityOfSequence;
	
	
	public Sequence(int ... tags){
		for(int i=0; i<tags.length; i++)
		{
			this.listOfTags.add(tags[i]);
		}
	}
	
	public Sequence(double prob, int ... tags){
		for(int i=0; i<tags.length; i++)
		{
			this.listOfTags.add(tags[i]);
		}
		
		this.probabilityOfSequence = prob;
	}


	public ArrayList<Integer> getListOfNodes() {
		return listOfTags;
	}


	public void setListOfNodes(ArrayList<Integer> listOfNodes) {
		this.listOfTags = listOfNodes;
	}


	public double getProbabilityOfSequence() {
		return probabilityOfSequence;
	}


	public void setProbabilityOfSequence(double probabilityOfSequence) {
		this.probabilityOfSequence = probabilityOfSequence;
	}
	
	public int get(int i) {
		return this.listOfTags.get(i);
	}
	
	

}
