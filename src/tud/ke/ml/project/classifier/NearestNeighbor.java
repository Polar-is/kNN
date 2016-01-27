package tud.ke.ml.project.classifier;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.ArrayList;
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
 * This implementation assumes the class attribute is always available (but probably not set)
 * @author cwirth
 *
 */
public class NearestNeighbor extends ANearestNeighbor implements Serializable {

//	private static final long serialVersionUID = 2010906213520172559L;
	
	protected double[] scaling;
	protected double[] translation;
	
	protected List<List<Object>> model;
	
	@Deprecated protected String[] getMatrikelNumbers() {
		return new String[]{getGroupNumber()};
	}

	@Override
	public String getGroupNumber() {
		return "35";
	}

	@Override
	protected void learnModel(List<List<Object>> data) {	
		this.model = data;
	}

	protected Map<Object,Double> getUnweightedVotes(List<Pair<List<Object>, Double>> subset) {
		Map<Object, Double> unweightedVotes = new TreeMap<Object, Double>();
		double voteValue;
		
		for(Pair<List<Object>, Double> pair : subset){
			Object _class = pair.getA().get(this.getClassAttribute());
			//System.out.println(_class.toString());
			voteValue = 0.0;
			
			if(unweightedVotes.containsKey(_class)){
				voteValue = unweightedVotes.get(_class) + 1.0;
				unweightedVotes.replace(_class, voteValue);
			}else{
				unweightedVotes.put(_class, 1.0);
			}
			
		}
		//System.out.println(unweightedVotes.toString());
		return unweightedVotes;
	}

	protected Map<Object, Double> getWeightedVotes(List<Pair<List<Object>, Double>> subset) {
		Map<Object, Double> weightedVotes = new TreeMap<Object, Double>();
		double voteValue, distance;
		
		for(Pair<List<Object>, Double> pair : subset){
			Object _class = pair.getA().get(this.getClassAttribute());
			//System.out.println(_class.toString());
			voteValue = 0.0;
			distance = ((pair.getB() == 0.0) ? Double.POSITIVE_INFINITY : pair.getB());
			
			if(weightedVotes.containsKey(_class)){
				voteValue = weightedVotes.get(_class) + 1/distance;  
				weightedVotes.replace(_class, voteValue);
			}else{
				voteValue = 1/distance;
				weightedVotes.put(_class, voteValue);
			}	
			
		}
		
		//System.out.println(weightedVotes.toString());
		
		return weightedVotes;
	}

	protected Object getWinner(Map<Object, Double> votes) {
		Entry<Object, Double> maxEntry = null;

		for(Entry<Object, Double> entry : votes.entrySet()) {
		    if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
		        maxEntry = entry;
		    }
		}
		
		//System.out.println(maxEntry);
		
		return maxEntry.getKey();
	}

	protected Object vote(List<Pair<List<Object>, Double>> subset) {
		Map<Object,Double> votes = null;

		if(this.isInverseWeighting()){
			votes = getWeightedVotes(subset);
		}else{
			votes = getUnweightedVotes(subset);
		}
		
		Object winner = getWinner(votes);
		//System.out.println(winner.toString());
		return winner;
	}


	protected List<Pair<List<Object>, Double>> getNearest(List<Object> data) {
		List<Pair<List<Object>, Double>> nearest = new LinkedList<Pair<List<Object>, Double>>();
		int k = getkNearest();
		double distance;
		double[][] normalizationScaling;
		boolean useManhattanDist, isNormalizing = false;
		
		if(this.getMetric() == 0){
			useManhattanDist = true;
		}else{
			useManhattanDist = false;
		}
		
		if(this.isNormalizing()){
			normalizationScaling = this.normalizationScaling();
			this.scaling = normalizationScaling[0];
			this.translation = normalizationScaling[1];
			isNormalizing = true;
		}
		
		//System.out.println(this.model.size());
		for(List<Object> modelInstance : this.model){
			if(isNormalizing){
				
			}
			
			if(useManhattanDist){
				distance = this.determineManhattanDistance(modelInstance, data);
			}else{
				distance = this.determineEuclideanDistance(modelInstance, data);
			}
			nearest.add(new Pair<List<Object>, Double>(modelInstance, distance));
		}
		
		Collections.sort(nearest, new Comparator<Pair<List<Object>, Double>>() {
			public int compare(Pair<List<Object>, Double> p1,
					Pair<List<Object>, Double> p2) {
				return (p1.getB()).compareTo(p2.getB());
			}
		});
		
		//System.out.println(data.toString());
		//System.out.println(nearest.toString());
		//System.out.println("k:"+k);
		
		k = Math.min(k, nearest.size());
		nearest = nearest.subList(0, k);
				
		//System.out.println(nearest.toString());
		
		return nearest;
	}

	protected double determineManhattanDistance(List<Object> instance1,List<Object> instance2) {
		//System.out.println("---");
		double distance = 0.0;
		for(int i = 0; i<instance1.size(); i++){
			if(i != this.getClassAttribute()  && !instance1.get(i).toString().equals(instance2.get(i).toString())){
				//distance += 1.0;
				//System.out.println(instance1.get(i).toString() + " - " + instance2.get(i).toString());
				if(instance1.get(i).getClass().getName().equals("java.lang.String")){
					distance += 1.0;
				}else{
					distance += Math.abs((double)instance1.get(i) - (double)instance2.get(i));
				}
			}
				
		}
		//System.out.println("distance " + String.valueOf(distance) + "\n");
		
		return distance;
	}

	protected double determineEuclideanDistance(List<Object> instance1,List<Object> instance2) {
		double distance = 0.0;
		for(int i = 0; i<instance1.size(); i++){
			if(i != this.getClassAttribute() && !instance1.get(i).toString().equals(instance2.get(i).toString())){
				//System.out.println(instance1.get(i).getClass().getName());
				if(instance1.get(i).getClass().getName().equals("java.lang.String")){
					distance += 1.0;
				}else{
					distance += Math.pow(((double)instance1.get(i) - (double)instance2.get(i)), 2);
				}
			}
			//System.out.println(instance1.get(i).toString());
			//System.out.println(instance2.get(i).toString());
		}
		//System.out.println("distance " + String.valueOf(distance));
		
		distance = Math.sqrt(distance);
		
		return distance;
	}

	protected double[][] normalizationScaling() {
		return null;
	}


}
