package impl.decoders.viterbi;

import impl.Model;
import impl.ModelSentence;
import impl.decoders.DecoderUtils;
import impl.decoders.IDecoder;

import java.util.Arrays;

import util.Util;
import edu.berkeley.nlp.util.ArrayUtil;

public class Viterbi implements IDecoder {

	private int numLabels;
	private Model m;
	private DecoderUtils dUtils;
	private Util u;
	
	public Viterbi(Model m){
		
		numLabels = m.labelVocab.size();
		this.m = m;
		this.dUtils = new DecoderUtils(m);
		this.u = new Util();
	}
	
	@Override
	public void decode(ModelSentence sentence) {
		viterbiDecode(sentence);

	}

	@Override
	public String decodeSettings() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * vit[t][k] is the max probability such that the sequence from 0 to t has
	 * token t labeled with tag k. (0<=t<T) bptr[t][k] gives the max prob. tag
	 * of token t-1 (t=0->startMarker)
	 */
	public void viterbiDecode(ModelSentence sentence) {
		int T = sentence.T;
		sentence.labels = new int[T];
		int[][] bptr = new int[T][numLabels];
		double[][] vit = new double[T][numLabels];
		double[] labelScores = new double[numLabels];

		//System.out.println("start Marker = " + startMarker());
		
		computeVitLabelScores(0, m.startMarker(), sentence, labelScores);
		//System.out.println("label score init: \n" + priArr(labelScores));
		ArrayUtil.logNormalize(labelScores);
		//System.out.println("label score init (log norm'd): \n" + priArr(labelScores));
		// initialization
		vit[0] = labelScores;

		for (int k = 0; k < numLabels; k++) {
			// start marker for all labels
			bptr[0][k] = m.startMarker();
		}

		//System.out.println("Initial back pointer array: " + priArr(bptr[0]));

		// Calculate viterbi label scores.
		for (int t = 1; t < T; t++) {
			//System.out.println(">>>>Token: " + t);
			double[][] prevcurr = new double[numLabels][numLabels];
			for (int s = 0; s < numLabels; s++) {
				//System.out.println("labelScores[" + s + "]" + labelScores[s]);
				computeVitLabelScores(t, s, sentence, prevcurr[s]);
				//System.out.println("prevcurr[" + s + "] " + priArr(prevcurr[s]));
				ArrayUtil.logNormalize(prevcurr[s]);
				prevcurr[s] = ArrayUtil.add(prevcurr[s], labelScores[s]);
			}
			for (int s = 0; s < numLabels; s++) {
				double[] sprobs = u.getColumn(prevcurr, s);
				bptr[t][s] = ArrayUtil.argmax(sprobs); // u.nthLargest(2,
														// sprobs);
				vit[t][s] = sprobs[bptr[t][s]];
			}
			labelScores = vit[t];
		}
		
		sentence.labels[T - 1] = ArrayUtil.argmax(vit[T - 1]);
		//System.out.print("***" + labelVocab.name(sentence.labels[T - 1]));
		//System.out.println(" with prob: "
		//		+ Math.exp(vit[T - 1][sentence.labels[T - 1]]));
		int backtrace = bptr[T - 1][sentence.labels[T - 1]];
		for (int i = T - 2; (i >= 0) && (backtrace != m.startMarker()); i--) { // termination
			sentence.labels[i] = backtrace;
			//System.out.println("***" + labelVocab.name(backtrace)
			//		+ " with prob: " + Math.exp(vit[i][backtrace]));
			backtrace = bptr[i][backtrace];
		}
		assert (backtrace == m.startMarker());
		
	}
	
	public void computeVitLabelScores(int t, int prior, ModelSentence sentence,
			double[] labelScores) {
		Arrays.fill(labelScores, 0);
		dUtils.computeBiasScores(labelScores);
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
