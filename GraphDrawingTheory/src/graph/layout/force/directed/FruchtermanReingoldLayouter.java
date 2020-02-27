package graph.layout.force.directed;

import java.awt.Dimension;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import graph.elements.Edge;
import graph.elements.Vertex;
import graph.layout.GraphLayoutProperties;
import graph.layout.PropertyEnums.FruchtermanReingoldProperties;

/**
 * Layouter which uses JUNG framework's Fruchterman-Reingold layout
 * @author Renata
 * @param <V> The vertex type
 * @param <E> The edge type 
 */
public class FruchtermanReingoldLayouter <V extends Vertex, E extends Edge<V>> extends AbstractForceDirectedLayouter<V,E> {

	@Override
	protected void initLayouter(GraphLayoutProperties layoutProperties) {
		
		
		Object attractionMultiplier = layoutProperties.getProperty(FruchtermanReingoldProperties.ATTRACTION_MULTIPLIER);
		Object repulsionMultiplier = layoutProperties.getProperty(FruchtermanReingoldProperties.REPULSION_MULTIPLIER);
		Object maximumIterations = layoutProperties.getProperty(FruchtermanReingoldProperties.MAXIMUM_ITERATIONS);
		Object graphDimension = layoutProperties.getProperty(FruchtermanReingoldProperties.DIMENSION);
		
		FRLayout<V,E> frLayout;
		if ( graphDimension != null) {
			frLayout = new FRLayout<V, E>(jungGraph, (Dimension) graphDimension);
		} else {
			frLayout = new FRLayout<V, E>(jungGraph);
		}
		
				 
		
		if (attractionMultiplier != null && attractionMultiplier instanceof Double)
			frLayout.setAttractionMultiplier((double)attractionMultiplier);
		if (repulsionMultiplier != null && repulsionMultiplier instanceof Double)
			frLayout.setRepulsionMultiplier((double)repulsionMultiplier);
		if (maximumIterations != null && maximumIterations instanceof Double)
			frLayout.setMaxIterations(((Double)maximumIterations).intValue());
		
		layouter = frLayout;
		
		
	}

}
