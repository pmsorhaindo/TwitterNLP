package impl.decoders.viterbi;

import java.util.ArrayList;
import java.util.Collections;

public class Sequence {
	
	private ArrayList<Integer> listOfTags = new ArrayList<Integer>();
	private double probabilityOfSequence;
	ArrayList<Sequence> pathSegments = new ArrayList<Sequence>();
	
	
	public Sequence(int startMarker, ArrayList<Double> sequenceProbs, int ... tags){
		this.listOfTags.add(startMarker);
		for(int i=0; i<tags.length; i++)
		{
			this.listOfTags.add(tags[i]);
		}
		
		ArrayList<Integer> tempTags = new ArrayList<>();
		tempTags.addAll(this.listOfTags);
		int count = 1;

		while(tempTags.size()>1)
		{	
			System.out.println("lemons!");
			pathSegments.add(new Sequence(tempTags));
			tempTags.remove((tempTags.size()-1));
			double prob = 1;
			for (int i = 0; i<count; i++)
			{
				prob = prob * sequenceProbs.get(i);
			}
			count++;
			pathSegments.get(pathSegments.size()-1).setProbabilityOfSequence(prob);
			
			System.out.println(pathSegments.get((pathSegments.size()-1)).listOfTags.toString() + " : " + pathSegments.get((pathSegments.size()-1)).getProbabilityOfSequence());
		}
		Collections.reverse(pathSegments);
		System.out.println("we have "+pathSegments.size()+" segments.");
	}
	
	public Sequence(double prob, int startMarker, int ... tags){
		this.listOfTags.add(startMarker);
		for(int i=0; i<tags.length; i++)
		{
			this.listOfTags.add(tags[i]);
		}
		
		this.probabilityOfSequence = prob;
	}


	public Sequence(ArrayList<Integer> tags) {
		for(int i=0; i<tags.size(); i++)
		{
			this.listOfTags.add(tags.get(i));
		}
	}

	public Sequence(double prob, ArrayList<Integer> tags) {
		for(int i=0; i<tags.size(); i++)
		{
			this.listOfTags.add(tags.get(i));
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
	
	public Sequence getIthPathSegment(int index) {
		if (index<0 || index > listOfTags.size()) return null;// TODO check this boundary!!
		
		return pathSegments.get(index);
	}

	public void addSegment(int i) {
		
		listOfTags.add(i);
		// TODO Auto-generated method stub
		
	}
	

}
