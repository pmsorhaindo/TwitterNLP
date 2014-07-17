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
		ArrayList<ArrayList<List<Double>>> viter = new ArrayList<>();
		double[] labelScores = new double[numLabels];
		ArrayList<List<Double>> labelScoresMult = new ArrayList<>();
		
		// System.out.println("start Marker = "+ startMarker());
		computeVitLabelScores(0, m.startMarker(), sentence, labelScores);
		// System.out.println("label score init: \n" + priArr(labelScores));
		ArrayUtil.logNormalize(labelScores);
		// System.out.println("label score init (log norm'd): \n" +
		// priArr(labelScores));

		// initialization
		ArrayList<List<Double>> v = new ArrayList<>();
		List<Double> v2 = new ArrayList<>();
		List<Double> arrayListLabelScores;
		arrayListLabelScores = Arrays.asList(ArrayUtils.toObject(labelScores));
		for(int q = 0; q<numLabels; q++)
		{
			v.add(arrayListLabelScores);
			
		}
		viter.add(v);
		labelScoresMult.add(v2);
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
			//System.out.println(k+": "+bkptr.get(0).get(k).toString());
		}
		
		// Calculate viterbi scores
		for (int t = 1; t < T; t++) {
			//System.out.println(">>>>Token: " + t);
			ArrayList<ArrayList<Integer>> aT = new ArrayList<>();
			bkptr.add(aT);
			ArrayList<List<Double>> z = new ArrayList<>();
			viter.add(z);			
			
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
				List<Double> y = new ArrayList<>();
				viter.get(t).add(y);
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
				for(int w =0; w<numLabels; w++)
				{
					viter.get(t).get(s).add(sprobs[(bkptr.get(t).get(s).get(0))-1]);
				}
				
			}
			//TODO sth missing here?
			
			labelScores = vit[t];
		}
		
		//TODO old
		//sentence.labels[T - 1] = ArrayUtil.argmax(vit[T - 1]);
		Double[] aa = (Double[])(viter.get(T-1).get(0)).toArray(new Double[viter.get(T-1).get(0).size()]);
		double[] ab = ArrayUtils.toPrimitive(aa);
		u.p(vit[T-1]);
		u.p(ab);
		sentence.labels[T-1] = ArrayUtil.argmax(ab);
		
		//System.out.print("***" + labelVocab.name(sentence.labels[T - 1]));
		//System.out.println(" with prob: "
		//		+ Math.exp(vit[T - 1][sentence.labels[T - 1]]));
		//int backtrace = bptr[T - 1][sentence.labels[T - 1]];
		int backtrace = bkptr.get(T-1).get(0).get(sentence.labels[T-1]);
		for (int i = T - 2; (i >= 0) && (backtrace != m.startMarker()); i--) { // termination
			sentence.labels[i] = backtrace;
			//System.out.println("***" + labelVocab.name(backtrace)
			//		+ " with prob: " + Math.exp(vit[i][backtrace]));
			
			//TODO old
			//backtrace = bptr[i][backtrace];
			backtrace = bkptr.get(i).get(1).get(backtrace);
		}
		assert (backtrace == m.startMarker());
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
