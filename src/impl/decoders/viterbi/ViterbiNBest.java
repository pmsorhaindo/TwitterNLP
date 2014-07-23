package impl.decoders.viterbi;

import impl.Model;
import impl.ModelSentence;
import impl.decoders.IDecoder;

import java.util.ArrayList;
import java.util.Arrays;

import edu.berkeley.nlp.util.ArrayUtil;
import util.Util;

public class ViterbiNBest implements IDecoder {
	
	private Model m;
	private int numLabels;
	private Util u;
	private ModelSentence sentence;
	
	public ViterbiNBest(Model m){
		
		u = new Util();
		this.m = m;
		this.numLabels = m.labelVocab.size();
		assert numLabels != 0;
		
	}

	@Override
	public void decode(ModelSentence sentence) {
		this.sentence = sentence;
		Viterbi vit = new Viterbi(m);
		vit.decode(sentence);
		ArrayList<Double> sequenceProbs = vit.getProbs();
		System.out.println("pre viterbi complete!");
		Sequence maxSeq = new Sequence(m.startMarker(), sequenceProbs,sentence.labels);
		viterbiNBest(sentence);
		u.p(sentence.labels);
		System.out.println(maxSeq.getListOfNodes().toString());
		computeCandidates(sentence, maxSeq);

	}

	@Override
	public String decodeSettings() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void computeCandidates(ModelSentence sentence, Sequence maxSeq) {
		//Initialisation
		int T = sentence.T; // Number of tokens to be tagged.
		ArrayList<Sequence> exclusionList = new ArrayList<>();
		ArrayList<Sequence> inclusionList = new ArrayList<>();
		exclusionList.add(maxSeq);
		
		for (int i=0; i<T; i++)
		{
			// Check all probs aren't already calculated
			calculateCandiateSubset(sentence, i, exclusionList,inclusionList);
		}
		
	}
	
	private void calculateCandiateSubset(ModelSentence sentence, int token, ArrayList<Sequence> exclusionList, ArrayList<Sequence> inclusionList) {
		
		for (int t = 0; t< (sentence.T); t++)
		{
			//take the ith  bit of path from max sequence
			Sequence s = exclusionList.get(0).getIthPathSegment(t);
			System.out.println("sequence " + t +" :"+s.getListOfNodes().toString());
			
			double[] prevcurr = new double[m.numLabels];
			computeVitLabelScores(t, exclusionList.get(0).getListOfNodes().get(t), sentence, prevcurr);
			//System.out.println("prevcurr[" + s + "] " + priArr(prevcurr[s]));
			ArrayUtil.logNormalize(prevcurr);
			double[] newProb = ArrayUtil.add(prevcurr, s.getProbabilityOfSequence());
			ArrayList<Integer> nodeStub = new ArrayList<>(); 
			nodeStub.addAll(s.getListOfNodes());
			nodeStub.remove(nodeStub.size()-1);
			for(int i = 0; i<this.numLabels; i++)
			{
			
				//calculate all i to i+1 sequences excluding those comprising the exclusionlist
				
				//check no already a segment
				nodeStub.add(i);
				Sequence newS = new Sequence(newProb[i],nodeStub);
				
				inclusionList.add(newS);
			}
		}
		
	}

	public void viterbiNBest(ModelSentence sentence) {
		int T = sentence.T;
		sentence.labels = new int[T];
		for (int i = 0; i<T; i++) sentence.labels[i]=i;
	}
	
	//TODO remove
	public void computeVitLabelScores(int t, int prior, ModelSentence sentence, double[] labelScores) {
		Arrays.fill(labelScores, 0);
		m.computeBiasScores(labelScores);
		//System.out.println("prior = " + prior);
		viterbiEdgeScores(prior, sentence, labelScores);
		//System.out.println("t = " + t);
		m.computeObservedFeatureScores(t, sentence, labelScores);
	}
	
	/**
	 * @return dim T array s.t. labelScores[t]+=score of label prior followed by
	 *         label t
	 **/
	public void viterbiEdgeScores(int prior, ModelSentence sentence,
			double[] EdgeScores) {
		for (int k = 0; k < numLabels; k++) {
			EdgeScores[k] += m.edgeCoefs[prior][k];
		}
	}
	
}
