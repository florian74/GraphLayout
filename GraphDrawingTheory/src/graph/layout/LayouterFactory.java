package graph.layout;

import graph.elements.Edge;
import graph.elements.Vertex;
import graph.layout.box.BoxLayouter;
import graph.layout.circle.CircleLayouter;
import graph.layout.force.directed.DAGLayouter;
import graph.layout.force.directed.FruchtermanReingoldLayouter;
import graph.layout.force.directed.KamadaKawaiLayouter;
import graph.layout.force.directed.SpringLayouter;
import graph.layout.organic.JGraphFastorganicLayouter;
import graph.layout.organic.JGraphHierarchicalLayouter;
import graph.layout.organic.JGraphOrganicLayouter;
import graph.layout.organic.JungISOMLayouter;
import graph.layout.partition.JGraphPartitionLayouter;
import graph.layout.stack.JGraphStackLayouter;
import graph.layout.symmetric.SymmetricCircleLayouter;
import graph.layout.symmetric.TutteLayouter;
import graph.layout.tree.BalloonLayouter;
import graph.layout.tree.JGraphCompactTreeLayout;
import graph.layout.tree.RadialTreeLayouter;
import graph.layout.tree.JungTreeLayouter;

public class LayouterFactory<V extends Vertex, E extends Edge<V>> {

	
	public AbstractLayouter<V,E> createLayouter(LayoutAlgorithms algorithm){

		AbstractLayouter<V,E> layouter = null;
		
		if (algorithm == LayoutAlgorithms.KAMADA_KAWAI)
			layouter = new KamadaKawaiLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.FRUCHTERMAN_REINGOLD)
			layouter= new FruchtermanReingoldLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.CIRCLE)
			layouter = new CircleLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.SPRING)
			layouter = new SpringLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.DAG)
			layouter = new DAGLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.BALLOON)
			layouter = new BalloonLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.RADIAL_TREE)
			layouter = new RadialTreeLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.TREE)
			layouter = new JungTreeLayouter<>();
		else if (algorithm == LayoutAlgorithms.BOX)
			layouter = new BoxLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.TUTTE)
			layouter = new TutteLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.CONCENTRIC)
			layouter = new SymmetricCircleLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.ISOM)
			layouter = new JungISOMLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.COMPACT_TREE)
			layouter = new JGraphCompactTreeLayout<V,E>();
		else if (algorithm == LayoutAlgorithms.FAST_ORGANIC)
			layouter = new JGraphFastorganicLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.ORGANIC)
			layouter = new JGraphOrganicLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.HIERARCHICAL)
			layouter = new JGraphHierarchicalLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.STACK)
			layouter = new JGraphStackLayouter<V,E>();
		else if (algorithm == LayoutAlgorithms.PARTITION)
			layouter = new JGraphPartitionLayouter<V,E>();
			
		return layouter;

	}
	
	
	
	
}
