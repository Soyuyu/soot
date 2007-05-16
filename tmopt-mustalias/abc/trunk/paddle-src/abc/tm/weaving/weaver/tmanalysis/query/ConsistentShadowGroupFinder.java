/*
 * Created on 13-Nov-06
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;

import soot.PointsToSet;
import soot.jimple.toolkits.pointer.FullObjectSet;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.DominatorEdgeLabels;
import abc.tm.weaving.weaver.tmanalysis.DominatorEdgeLabels.PathInfo;
import abc.tm.weaving.weaver.tmanalysis.ds.Intersection;
import abc.tm.weaving.weaver.tmanalysis.stages.FlowInsensitiveAnalysis;

/**
 * This class represents an integral part of the {@link FlowInsensitiveAnalysis}.
 * For a set of shadows of one single tracematch, it computes the subset of shadows that can
 * possibly lead to a complete match based on their variable bindings. 
 *
 * @author Eric Bodden
 */
public class ConsistentShadowGroupFinder {
	
	/**
	 * Computes consistent shadow groups based on the set of shadows that is passed in.
	 * A group is consistent if the points-to sets of all its shadows overlap (per variable)
	 * and all the symbols required for a match are present in the group.
	 * The result is soted in {@link #shadowGroups}.
	 * 
	 * A few examples: (different oi's represent non-overlapping points-to sets) 
	 * <ul>
	 *   <li>pattern: a(x) b(x); shadows: a_1(o1), b_1(o2); result: {}
	 *   <li>pattern: a(x) b(y); shadows: a_1(o1), b_1(o2); result: {{a_1(o1),b_1(o2)}}
	 *   <li>pattern: a(x) a(x) b(x); shadows: a_1(o1), a_2(o1), b_1(o1);  result: {{a_1(o1),b_1(o1)},{a_2(o1),b_1(o1)}};
	 *       note that {a_1(o1),a_2(o1),b_1(o1)} is missing although it could lead to a match;
	 * </ul>
	 * 
	 * @param shadows a set of {@link Shadow}s of a single tracematch 
	 * @param pathInfos the set of {@link PathInfo}s for the associated state machine
	 * @see DominatorEdgeLabels 
	 */
	public Set consistentShadowGroups(TraceMatch tm, Set shadows, Set pathInfos) {
		Map symbolNameToShadows = new HashMap();
		
		//Bench.FI3_NUM_PARTITIONING_ITERATIONS=0;		
		
		//build a mapping from symbol names to shadows with that symbol name
		for (Iterator shadowIter = shadows.iterator(); shadowIter.hasNext();) {
			Shadow s = (Shadow) shadowIter.next();
			
			String symbolName = Naming.getSymbolShortName(s.getUniqueShadowId());
			Set shadowsForThisSymbol = (Set) symbolNameToShadows.get(symbolName);
			if(shadowsForThisSymbol==null) {
				shadowsForThisSymbol = new HashSet();
				symbolNameToShadows.put(symbolName, shadowsForThisSymbol);
			}
			shadowsForThisSymbol.add(s);
		}
		
		Set shadowGroups = new HashSet();
		
		NextDomSet:
		for (Iterator domSetIter = pathInfos.iterator(); domSetIter.hasNext();) {
			PathInfo pathInfo = (PathInfo) domSetIter.next();
			Bag domLabels = pathInfo.getDominatingLabels();

			Bag toCross = new HashBag();
			
			for (Iterator labIter = domLabels.iterator(); labIter.hasNext();) {
				String domLabel = (String) labIter.next();
				
				Set shadowsWithDomLabel = (Set) symbolNameToShadows.get(domLabel);
				if(shadowsWithDomLabel==null) continue NextDomSet;
				
				toCross.add(shadowsWithDomLabel);
				
			}

			Set crossProduct = consistentCrossProduct(toCross);
			//assert crossProductOrderIndependent(toCross);
			
			Set labelsToAdd = pathInfo.getSkipLoopLabels();

			for (Iterator compIter = crossProduct.iterator(); compIter.hasNext();) {
				ConsistentShadowBag shadowSet = (ConsistentShadowBag) compIter.next();
				ShadowGroup shadowGroup = new ShadowGroup(tm,shadowSet);
				
				//add all skip-loop shadows with overlapping points-to sets
				for (Iterator labIter = labelsToAdd.iterator(); labIter.hasNext();) {
					String label = (String) labIter.next();
					Set shadowsWithThisLabel = (Set) symbolNameToShadows.get(label);
					if(shadowsWithThisLabel!=null) {
						for (Iterator iterator = shadowsWithThisLabel.iterator(); iterator.hasNext();) {
							Shadow shadow = (Shadow) iterator.next();
							//add it if points-to set overlap
							shadowGroup.addSkipShadow(shadow);
						}
					}
				}
				
				shadowGroups.add(shadowGroup);
			}			
		}
		return shadowGroups;
	}
	
	/**
	 * Builds the cross product of a set of sets of shadows.
	 * The results contains all sets in the cross product which have a consistent variable binding.
	 * @param toCross a set of sets s1, s2, ..., sn of any kind of {@link Object}s
	 * @return s1 \times s2, ..., \times sn, where all shadows in each set in the result have a
	 * consistent variable binding
	 */
	protected Set consistentCrossProduct(Bag toCross) {
		HashSet result = new HashSet();
		result.add(new ConsistentShadowBag());
		
		//for all input sets currSet in s1, ..., sn 
		for (Iterator toCrossIter = toCross.iterator(); toCrossIter.hasNext();) {
			Set currSet = (Set) toCrossIter.next();
			
			HashSet newResult = new HashSet();
			
			//for all sets resSet already in the result set
			for (Iterator resIter = result.iterator(); resIter.hasNext();) {
				ConsistentShadowBag resSet = (ConsistentShadowBag) resIter.next();
				
				//for all shadows in currSet 
				for (Iterator shadowIter = currSet.iterator(); shadowIter.hasNext();) {
					Object element = shadowIter.next();
					
					//make a copy of resSet
					ConsistentShadowBag currCopy = (ConsistentShadowBag) resSet.clone(); 

					//and if the shadow could be added, retain the extended copy for the next step
					if(currCopy.add(element)) {
						newResult.add(currCopy);
					}
				}
			}
			
			result = newResult;
		}

		//remove the empty set as it was only used as a seed
		result.remove(Collections.EMPTY_SET);

		//remove all sets whose size is smaller than the required size
		//as those sets are incomplete
		final int fullSize = toCross.size();
		for (Iterator resIter = result.iterator(); resIter.hasNext();) {
			Collection resSet = (Collection) resIter.next();
			if(resSet.size()<fullSize) resIter.remove();
		}
		
		return result;
	}

	/**
	 * A bag of shadows, associated with a single shadow s.
	 * The component holds all shadows that have overlapping points-to sets with s.
	 *
	 * @author Eric Bodden
	 */
	public class ConsistentShadowBag extends HashBag {
		
		
		/** mapping of variables to points-to sets; needed to narrow down
		 *  points-to sets during inlining */
		protected HashMap varToPts;
		
		/**
		 * Creates a new component for the given shadow.
		 * @param shadow the shadow to associate this component with
		 */
		protected ConsistentShadowBag() {
			varToPts = new HashMap();
		}
		
		/**
		 * Adds the shadow o to this component, but only if its points-to set has a non-empty
		 * intersection with the sets of the shadows contained already in this component.
		 * @param o any {@link Shadow} 
		 * @return true if the shadow was added
		 */
		public boolean add(Object o) {
			Map newVarToPts = new HashMap();
			if(o instanceof Shadow) {
				Shadow s = (Shadow)o;
				
				for (Iterator varIter = s.getBoundVariables().iterator(); varIter.hasNext();) {
					String var = (String) varIter.next();
					
					PointsToSet thisPts = (PointsToSet) varToPts.get(var);
					if(thisPts==null) thisPts = FullObjectSet.v();
					
					PointsToSet othPts = s.getPointsToSet(var);  
					if(othPts==null) othPts = FullObjectSet.v();
					
					PointsToSet intersection = Intersection.intersect(thisPts, othPts);
					if(intersection.isEmpty()) {
						//cannot add this shadow, because the points-to sets are
						//not intersecting and hence no consistent binding is possible
						return false;
					} else {
						//store intersection
						newVarToPts.put(var,intersection);					
					}
				}
				
				//if we came here, this means we had intersecting points-to sets for all variables 
				//so store the intersection and add the shadow to the set
				
				//store the intersections
				for (Iterator intIter = newVarToPts.entrySet().iterator(); intIter.hasNext();) {
					Entry entry = (Entry) intIter.next();
					varToPts.put(entry.getKey(), entry.getValue());
				}
				
				//commit the shadow
				super.add(o);
				return true;
			} else {
				throw new IllegalArgumentException("o must be of type Shadow");
			}			
		}
		
		private void fastAdd(Shadow s) {
			super.add(s);
		}
		
		public boolean hasNonEmptyIntersection(Shadow s) {
			for (Iterator varIter = s.getBoundVariables().iterator(); varIter.hasNext();) {
				String var = (String) varIter.next();
				
				PointsToSet thisPts = (PointsToSet) varToPts.get(var);
				if(thisPts==null) thisPts = FullObjectSet.v();
				
				PointsToSet othPts = s.getPointsToSet(var);  
				if(othPts==null) othPts = FullObjectSet.v();
				
				PointsToSet intersection = Intersection.intersect(thisPts, othPts);
				if(intersection.isEmpty()) {
					//cannot add this shadow, because the points-to sets are
					//not intersecting and hence no consistent binding is possible
					return false;
				} 
			}
			return true;			
		}

		/**
		 * {@inheritDoc}
		 */
		public Object clone() {
			//unfortunately, HashBag does not support clone()
			ConsistentShadowBag clone = new ConsistentShadowBag();
			clone.varToPts = (HashMap) varToPts.clone();
			for (Iterator iterator = iterator(); iterator.hasNext();) {
				Shadow shadow = (Shadow) iterator.next();
				clone.fastAdd(shadow);
			}
			return clone;
		}
		
	}
	
//	/**
//	 * Returns true if {@link #crossProductOrderIndependent(Set)} returns
//	 * the same result for different random shufflings of the input set.
//	 * Used for error checking only.
//	 * @param toCross the sets to build the cross product over
//	 * @return true if the results were all the same
//	 */
//	private boolean crossProductOrderIndependent(Bag toCross) {
//		List l = new ArrayList(toCross);
//		
//		Set originalResult = consistentCrossProduct(toCross);
//		
//		for(int i=0;i<toCross.size();i++) {
//			Collections.shuffle(l);
//			LinkedHashSet ls = new LinkedHashSet(l);
//			Set thisResult = consistentCrossProduct(ls);
//			if(!thisResult.equals(originalResult)) {
//				return false;
//			}
//		}
//		
//		return true;
//	}

	//singleton pattern
	
	protected static ConsistentShadowGroupFinder instance;

	private ConsistentShadowGroupFinder() {}
	
	public static ConsistentShadowGroupFinder v() {
		if(instance==null) {
			instance = new ConsistentShadowGroupFinder();
		}
		return instance;		
	}
	
}