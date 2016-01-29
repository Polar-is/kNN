package tud.ke.ml.project.classifier;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import tud.ke.ml.project.framework.classifier.ANearestNeighbor;
import tud.ke.ml.project.util.Pair;

/**
 * This implementation assumes the class attribute is always available (but
 * probably not set)
 * 
 * @author cwirth
 *
 */
public class NearestNeighbor extends ANearestNeighbor implements Serializable {

	// private static final long serialVersionUID = 2010906213520172559L;

	protected double[] scaling;
	protected double[] translation;

	protected List<List<Object>> model;

	@Deprecated
	protected String[] getMatrikelNumbers() {
		return new String[] { getGroupNumber() };
	}

	@Override
	public String getGroupNumber() {
		return "35";
	}

	@Override
	protected void learnModel(List<List<Object>> data) {
		this.model = data;
	}

	protected Map<Object, Double> getUnweightedVotes(List<Pair<List<Object>, Double>> subset) {
		Map<Object, Double> unweightedVotes = new TreeMap<Object, Double>();
		double voteValue;
		Object _class;
		
		for (Pair<List<Object>, Double> pair : subset) {
			_class = pair.getA().get(this.getClassAttribute());
			voteValue = 0.0;
			
			//if the class is already present in the voting, increase the voting count by 1
			//otherwise add it and initialize the voting count for this class with 1
			if (unweightedVotes.containsKey(_class)) {
				voteValue = unweightedVotes.get(_class) + 1.0;
				unweightedVotes.replace(_class, voteValue);
			} else {
				unweightedVotes.put(_class, 1.0);
			}
		}

		return unweightedVotes;
	}

	protected Map<Object, Double> getWeightedVotes(List<Pair<List<Object>, Double>> subset) {
		Map<Object, Double> weightedVotes = new TreeMap<Object, Double>();
		double voteValue, distance;
		Object _class;

		for (Pair<List<Object>, Double> pair : subset) {
			_class = pair.getA().get(this.getClassAttribute());
			voteValue = 0.0;
			distance = ((pair.getB() == 0.0) ? Double.POSITIVE_INFINITY : pair.getB());

			if (weightedVotes.containsKey(_class)) {
				voteValue = weightedVotes.get(_class) + 1 / distance;
				weightedVotes.replace(_class, voteValue);
			} else {
				voteValue = 1 / distance;
				weightedVotes.put(_class, voteValue);
			}
		}

		return weightedVotes;
	}

	protected Object getWinner(Map<Object, Double> votes) {
		Entry<Object, Double> maxEntry = null;

		for (Entry<Object, Double> entry : votes.entrySet()) {
			if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
				maxEntry = entry;
			}
		}

		return maxEntry.getKey();
	}

	protected Object vote(List<Pair<List<Object>, Double>> subset) {
		Map<Object, Double> votes = null;
		
		if (this.isInverseWeighting()) {
			votes = getWeightedVotes(subset);
		} else {
			votes = getUnweightedVotes(subset);
		}

		return getWinner(votes);
	}

	protected List<Pair<List<Object>, Double>> getNearest(List<Object> data) {
		List<Pair<List<Object>, Double>> nearest = new LinkedList<Pair<List<Object>, Double>>();
		int k = getkNearest();
		double distance;
		double[][] normalizationScaling;
		boolean useManhattanDist, isNormalizing = false;
		
		useManhattanDist = (this.getMetric() == 0) ? true : false;

		if (this.isNormalizing()) {
			normalizationScaling = this.normalizationScaling();
			this.scaling = normalizationScaling[0];
			this.translation = normalizationScaling[1];
			
			data = this.normalize(data);
			isNormalizing = true;
		}

		for (List<Object> modelInstance : this.model) {
			if (isNormalizing) {
				modelInstance = this.normalize(modelInstance);
			}

			if (useManhattanDist) {
				distance = this.determineManhattanDistance(modelInstance, data);
			} else {
				distance = this.determineEuclideanDistance(modelInstance, data);
			}
			
			nearest.add(new Pair<List<Object>, Double>(modelInstance, distance));
		}

		Collections.sort(nearest, new Comparator<Pair<List<Object>, Double>>() {
			public int compare(Pair<List<Object>, Double> p1, Pair<List<Object>, Double> p2) {
				return (p1.getB()).compareTo(p2.getB());
			}
		});

		k = Math.min(k, nearest.size());
		nearest = nearest.subList(0, k);

		return nearest;
	}

	protected double determineManhattanDistance(List<Object> instance1, List<Object> instance2) {
		double distance = 0.0;
		
		for (int i = 0; i < instance1.size(); i++) {
			if (i != this.getClassAttribute() && !instance1.get(i).toString().equals(instance2.get(i).toString())) {
				if (instance1.get(i).getClass().getName().equals("java.lang.String")) {
					distance += 1.0;
				} else {
					distance += Math.abs((double) instance1.get(i) - (double) instance2.get(i));
				}
			}
		}

		return distance;
	}

	protected double determineEuclideanDistance(List<Object> instance1, List<Object> instance2) {
		double distance = 0.0;
		for (int i = 0; i < instance1.size(); i++) {
			if (i != this.getClassAttribute() && !instance1.get(i).toString().equals(instance2.get(i).toString())) {				
				if (instance1.get(i).getClass().getName().equals("java.lang.String")) {
					distance += 1.0;
				} else {
					distance += Math.pow(((double) instance1.get(i) - (double) instance2.get(i)), 2);
				}
			}
		}

		distance = Math.sqrt(distance);

		return distance;
	}

	protected double[][] normalizationScaling() {
		double[] scaling = new double[this.model.get(0).size()];
		double[] translation = new double[this.model.get(0).size()];
		double[][] normalizationScaling = new double[2][this.model.get(0).size()];
		
		Arrays.fill(scaling, Double.NEGATIVE_INFINITY);
		Arrays.fill(translation, Double.POSITIVE_INFINITY);
		
		for (List<Object> instance : this.model) {
			for(int i = 0; i < instance.size(); i++) {
				if(instance.get(i).getClass().getName().equals("java.lang.String")){
					scaling[i] = 0.0;
					translation[i] = 0.0;
					continue;
				}
				
				if((double)instance.get(i) > scaling[i]){
					scaling[i] = (double)instance.get(i);
				}
				
				if((double)instance.get(i) < translation[i]){
					translation[i] = (double)instance.get(i);
				}
			}
		}
		
		normalizationScaling[0] = scaling;
		normalizationScaling[1] = translation;
				
		return normalizationScaling;
	}
	
	/**
	 * Normalize the numeric values of the input instance and return the instance with normalized values between 0 and 1.
	 * String values stay the same.
	 * @param list input instance
	 * @return a normalized instance
	 */
	protected List<Object> normalize(List<Object> list){
		List<Object> normalizedList = new LinkedList<Object>();
		double doubleValue;
		
		for(int i = 0; i < list.size(); i++){
			if(list.get(i).getClass().getName().equals("java.lang.String")){
				normalizedList.add(list.get(i));
			}else{
				doubleValue = ((double)list.get(i) - this.translation[i])/(this.scaling[i] - this.translation[i]);
				normalizedList.add((Double.isNaN(doubleValue) ? 0.0 : doubleValue));
			}
		}
				
		return normalizedList;
	}

}
