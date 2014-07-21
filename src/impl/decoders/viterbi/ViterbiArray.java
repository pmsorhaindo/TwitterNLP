package impl.decoders.viterbi;

import impl.Model;
import impl.ModelSentence;
import impl.decoders.IDecoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import util.Util;
import edu.berkeley.nlp.util.ArrayUtil;

public class ViterbiArray implements IDecoder {
	
	private Model m;
	private int numLabels;
	private int N;
	private Util u;
	
	public ViterbiArray(Model m){
		
		this.N = 4;
		this.u = new Util();
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
		// Initialization
		int T = sentence.T; // Number of tokens to be tagged.
		sentence.labels = new int[T]; // final labeled sequence
		int[][][] bptr = new int [N][T][numLabels]; // backpointer array
		int[][][] bptrV = new int [N][T][numLabels];
		double[][][] vit = new double[T][N][numLabels]; // viterbi Matrix
		double[][] labelScores = new double[N][numLabels]; 
		
		for(int i = 0; i<N; i++) {
			computeVitLabelScores(0, m.startMarker(), sentence, labelScores[i]);
			ArrayUtil.logNormalize(labelScores[i]);
		}
		
		// Assigning first label scores to the first column in the viterbi matrix
		vit[0] = labelScores;
		
		// Assigning the start marker to the first position.
		for(int i = 0; i<N; i++)
		{
			for (int k = 0; k < numLabels; k++) {
				// start marker for all labels
				bptr[i][0][k] = m.startMarker();
			}
		}
		
		// Calculate viterbi label scores.
		for (int t = 1; t < T; t++) {
			double[][][] prevcurr = new double[N][numLabels][numLabels];
			
			for (int s = 0; s < numLabels; s++) {

				for(int i =0; i<N; i++)
				{
					computeVitLabelScores(t, s, sentence, prevcurr[i][s]);
					ArrayUtil.logNormalize(prevcurr[i][s]);

					prevcurr[i][s] = ArrayUtil.add(prevcurr[i][s], labelScores[i][s]);
				}
				
			}
			
			for (int s = 0; s < numLabels; s++) {
				double[] sprobs = new double[N*numLabels]; 
				
				for(int j=0; j<N; j++)
				{
					double[] probsPerTag = u.getColumn(prevcurr[j], s);
					//u.p(probsPerTag);
					for(int k=0; k<prevcurr[j].length; k++)
					{
						//u.p((k+(j*prevcurr[j].length)));
						sprobs[k+(j*prevcurr[j].length)] = probsPerTag[k];
					}
				}
				
				for (int j = 0; j<N; j++)
				{
					int nthLarge = u.nthLargest((j+1),sprobs);
					int pointer = nthLarge % N;
					int previousVMatrix = nthLarge/N;
					bptrV[j][t][s] = previousVMatrix; // fishy?
					bptr[j][t][s] = pointer;
				}
				
				for (int j = 0; j<N; j++)
				{
					vit[j][t][s] = sprobs[bptr[j][t][s]]; // fishy?
				}
			}
			labelScores = vit[t];
		}
		
		u.p(bptr[0]);
		u.p(bptrV[0]);
		
		for (int i = 0; i<T; i++) sentence.labels[i]=i;
		
		sentence.labels[T - 1] = ArrayUtil.argmax(vit[1][T - 1]);
		//System.out.print("***" + labelVocab.name(sentence.labels[T - 1]));
		//System.out.println(" with prob: " + Math.exp(vit[T - 1][sentence.labels[T - 1]]));
		
		int nthChoice = 0; // from 0
		
		int backtrace = bptr[nthChoice][T - 1][sentence.labels[T - 1]];
		int backtraceV = bptr[nthChoice][T-1][sentence.labels[T - 1]];
		for (int i = T - 2; (i >= 0) && (backtrace != m.startMarker()); i--) { // termination
			sentence.labels[i] = backtrace;
			//System.out.println("***" + labelVocab.name(backtrace)
			//		+ " with prob: " + Math.exp(vit[i][backtrace]));
			backtrace = bptr[backtraceV][i][backtrace];
			backtraceV = bptrV[nthChoice][i][backtraceV];
		}
		assert (backtrace == m.startMarker());
	}
	
	// TODO remove
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
