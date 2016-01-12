package tud.ke.ml.project.classifier;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import tud.ke.ml.project.framework.classifier.ANearestNeighbor;
import tud.ke.ml.project.util.Pair;

/**
 * This implementation assumes the class attribute is always available (but probably not set)
 * @author cwirth
 *
 */
public class NearestNeighbor extends ANearestNeighbor implements Serializable {

	private static final long serialVersionUID = 2010906213520172559L;
	
	protected double[] scaling;
	protected double[] translation;
	
	protected String[] getMatrikelNumbers() {
		return new String[]{};
	}
	
	@Override
	protected void learnModel(List<List<Object>> data) {	
	}

	protected Map<Object,Double> getUnweightedVotes(List<Pair<List<Object>, Double>> subset) {
		return null;
	}

	protected Map<Object, Double> getWeightedVotes(List<Pair<List<Object>, Double>> subset) {
		return null;
	}

	protected Object getWinner(Map<Object, Double> votes) {
		return null;
	}

	protected Object vote(List<Pair<List<Object>, Double>> subset) {
		return null;
	}


	protected List<Pair<List<Object>, Double>> getNearest(List<Object> data) {
		return null;
	}

	protected double determineManhattanDistance(List<Object> instance1,List<Object> instance2) {
		return 0;
	}

	protected double determineEuclideanDistance(List<Object> instance1,List<Object> instance2) {
		return 0;
	}

	protected double[][] normalizationScaling() {
		return null;
	}

}
