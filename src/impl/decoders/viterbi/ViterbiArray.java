package impl.decoders.viterbi;

import impl.Model;
import impl.ModelSentence;
import impl.decoders.IDecoder;

import java.util.ArrayList;
import java.util.Arrays;

import util.Util;
import util.ViterbiPaths;
import edu.berkeley.nlp.util.ArrayUtil;

public class ViterbiArray implements IDecoder {
	
	private Model m;
	private int numLabels;
	private Util u;
	
	public ViterbiArray(Model m){
		
		u = new Util();
		this.m = m;
		this.numLabels = m.labelVocab.size();
		assert numLabels != 0;
		
	}

	@Override
	public void decode(ModelSentence sentence) {
		viterbiArrayDecode(sentence);

	}

	@Override
	public String decodeSettings() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public void viterbiArrayDecode(ModelSentence sentence) {
		int T = sentence.T;
		sentence.labels = new int[T];
		int[][] bptr = new int[T][numLabels];
		ArrayList<ArrayList<ArrayList<Integer>>> bkptr = new ArrayList<>();
		double[][] vit = new double[T][numLabels];
		double[] labelScores = new double[numLabels];

		// System.out.println("start Marker = "+ startMarker());
		computeVitLabelScores(0, m.startMarker(), sentence, labelScores);
		// System.out.println("label score init: \n" + priArr(labelScores));
		ArrayUtil.logNormalize(labelScores);
		// System.out.println("label score init (log norm'd): \n" +
		// priArr(labelScores));

		// initialization
		vit[0] = labelScores;

		
		for (int k = 0; k < numLabels; k++) {
			// start marker for all labels
			bptr[0][k] = m.startMarker();
			
			ArrayList<ArrayList<Integer>> e = new ArrayList<>();
			bkptr.add(e);
			ArrayList<Integer> i = new ArrayList<>();
			bkptr.get(0).add(i);
			
			for(int l = 0; l<numLabels; l++ )
			{
				bkptr.get(0).get(k).add(m.startMarker());
			}
		}
		
		// Calculate viterbi scores
		for (int t = 1; t < T; t++) {
			//System.out.println(">>>>Token: " + t);
			ArrayList<ArrayList<Integer>> aT = new ArrayList<>();
			bkptr.add(aT);
			
			double[][] prevcurr = new double[numLabels][numLabels];
			for (int s = 0; s < numLabels; s++) {
				//vit ArrayList<Integer> ???
				//System.out.println("labelScores[" + s + "]" + labelScores[s]);
				computeVitLabelScores(t, s, sentence, prevcurr[s]);
				//System.out.println("prevcurr[" + s + "] " + priArr(prevcurr[s]));
				ArrayUtil.logNormalize(prevcurr[s]);
				prevcurr[s] = ArrayUtil.add(prevcurr[s], labelScores[s]);
			}
			//System.out.println("prevcurr: ");
			//u.p(prevcurr);
			for (int s = 0; s < numLabels; s++) {
				ArrayList<Integer> x = new ArrayList<>();
				bkptr.get(t).add(x);
				double[] sprobs = u.getColumn(prevcurr, s);
				//if (t == divergePoint) {
				//	bptr[t][s] = u.nthLargest(2, sprobs);
				//} else {
					bptr[t][s] = ArrayUtil.argmax(sprobs); // u.nthLargest(2, sprobs);
					for(int w =0; w<numLabels; w++)
					{
						int f = u.nthLargest(w+1, sprobs);
						bkptr.get(t).get(s).add(f);
					}
				//}

				vit[t][s] = sprobs[bptr[t][s]];
			}
			labelScores = vit[t];
		}
	}
	
	//TODO remove
	public void computeVitLabelScores(int t, int prior, ModelSentence sentence,
			double[] labelScores) {
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
