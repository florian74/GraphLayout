package graph.properties.splitting;

import graph.elements.Edge;
import graph.elements.Graph;
import graph.elements.Vertex;
import graph.properties.components.HopcroftTarjanSplitComponent;
import graph.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;

public class HopcroftTarjanSplitting<V extends Vertex, E extends Edge<V>> {


	/**
	 * array keeping the number of descendants of vertices
	 */
	private int[] nd;
	/**
	 * array keeping the lowest reachable vertex
	 * by traversing zero or more tree arcs in the palm tree followed by at most one frond
	 * for each vertex
	 */
	private int[] lowpt1;
	/**
	 * array keeping the second lowest reachable vertex
	 * by traversing zero or more tree arcs in the palm tree followed by at most one frond
	 * for each vertex
	 */
	private int[] lowpt2;
	/**
	 * numbering of vertices set during the first dfs
	 */
	private int[] number;
	/**
	 * array keeping the information regarding the father vertex
	 * for each vertex 
	 */
	private int[] father;
	/**
	 * new numbering set during the path finding stage (when search is performed
	 * with the ordered adjacency structure)
	 */
	private int[] newnum;
	/**
	 * array keeping the highest reachable vertex (represented by its index in the list of vertices)
	 * by traversing zero or more tree arcs in the palm tree
	 * for each vertex
	 */
	private int[] highpt;
	/**
	 * array keeping number of edges incident to each vertex
	 */
	private int[] degree;
	/**
	 * help variable used to avoid examining one edge more than once
	 */
	private boolean[] flag;
	/**
	 *Graph whose triconnected division shiould be performed 
	 */
	private Graph<V,E> graph;
	/**
	 * List of graph's vertices
	 */
	private List<V> vertices;
	/**
	 * List of tree edges of the dfs tree found during dfs
	 */
	private List<E> treeEdges;
	/**
	 * List of fronds or back edges
	 */
	private List<E> fronds;
	/**
	 * array of first sons of vertices - the first entry in the adjacency list
	 */
	private int[] a1;
	/**
	 * inverse of numbering, takes number as the index of the array and keeps numberings as values
	 */
	private int[] inverseNumbering;

	/**denotes the start vertex of the current path in the pathfinder phase*/ 
	private V s;
	/**denotes the last number assigned to a vertex in the pathfinder phase*/
	private int m;

	private int n;
	private int j;

	private Map<V, List<E>> adjacency;

	private Logger log = Logger.getLogger(HopcroftTarjanSplitting.class);

	private List<SeparationPair<V>> separationPairs;

	private List<HopcroftTarjanSplitComponent<V, E>> splitComponents;

	private Stack<E> estack;

	private Stack<Triple> tstack;

	private Triple endOfStackMarker;

	private Class<?> edgeClass;

	private Map<E,Integer> edgesJMap; //to save j-s

	private List<List<E>> paths = new ArrayList<List<E>>();

	private boolean fflag = false;
	
	private ArrayList<E> virtualEdges = new ArrayList<E>();
	
	public HopcroftTarjanSplitting(Graph<V,E> graph){
		this.graph = graph;
	}
	
	
	//TODO Remove edgesJMap
	//use addEdge and addVirtualEdge methods - record which virtual edges are contained by one split component
	//create a map of virtual edges and split components it contains - will be used for convex drawing
	
	//TODO problem with some triple bonds not being detected and being joined with the other component
	

	public void execute() throws AlgorithmErrorException{

		init();
		//	printVerticesData();
		//System.out.println(treeEdges);
		//	System.out.println(fronds);
		//printPaths();
		//printAdjacency();
		pathsearch(vertices.get(0));
		formLastComponent();
	}

	/**
	 * Procedure to determine split components of a biconnected multigraph
	 * on which steps, such as dfs traversal, path finding, initialization
	 * of lowpts etc. were already carried out
	 * tstack contains triples representing possible type 2 separation pairs
	 * e stack contains edges backed up over during search
	 */
	private void pathsearch(V v){



		System.out.println("Current v: " + v);

		for (E e : adjacency.get(v)){

			int vIndex = vertices.indexOf(v);
			V w = e.getOrigin() == v ? e.getDestination() : e.getOrigin();
			int wIndex = vertices.indexOf(w);


			//if v -> w i.e. if e is a tree edge

			if (treeEdges.contains(e)){

				E savedEdge = null;

				//A:
				if (firstEdgeOfAPath(e)){

					log.info("First edge of a path");
					printTStack();

					int y = 0;

					Triple lastDeletedTriple = null;
					//while (h,a,b) on tastack has a > lowpt1(w)
					while (!tstack.isEmpty()){

						Triple triple = tstack.peek();

						System.out.println("OVDE");
						log.info("Current triple: " + triple);
						int a = triple.getA();
						if (a <= lowpt1[wIndex])
							break;

						int h = triple.getH();
						y = Math.max(y, h);
						tstack.pop();
						log.info("Deleteing triple from stack ");
						lastDeletedTriple = triple;

					}
					//if no triples deleted from tstack add(w + ND(w)-1, lowpt1(w),v) to tstack
					if (lastDeletedTriple == null){
						Triple newTriple = new Triple(newnum[wIndex] + nd[wIndex] - 1, lowpt1[wIndex], newnum[vIndex]);
						log.info("pushing " + newTriple + " to tstack");
						tstack.push(newTriple);
					}
					//add max{y, w+nd(w)-1}, lowpt1(w),b to tstack
					else{
						Triple newTriple = new Triple
								(Math.max(y, newnum[wIndex] + nd[wIndex] - 1), lowpt1[wIndex], lastDeletedTriple.getB());
						log.info("pushing " + newTriple + " to tstack");
						tstack.push(newTriple);
					}
					//add end of stack marker to tstack
					log.info("Pushing end of stack marker");
					tstack.push(endOfStackMarker);

				}
				pathsearch(w);
				//returned from recursion
				estack.push(e);
				log.info("Pushing edge " + e + " to estack");
				//end of A

				//B:
				//while v != 1 and ((degree(w) = 2) and (A1(w) > 2) or (h,a,b) on tstack
				//satisfies (v = a) - test for type 2 pairs


				printTStack();
				printEStack();
				log.info("Backing over edge " + e);
				System.out.println(v);
				System.out.println(w);


				while(true){

					log.info("While lopp, testing for separation pairs");


					if (newnum[vIndex] == 1)
						break;

					boolean firstCondition = degree[wIndex] == 2 && a1[wIndex] > newnum[wIndex];
					System.out.println(a1[wIndex]);


					boolean secondCondition = false;

					Triple triple = null;
					if (!tstack.isEmpty())
						triple = tstack.peek();

					System.out.println("current triple: " + triple );

					if (triple != null)
						secondCondition = newnum[vIndex] == triple.getA();


					System.out.println(degree[wIndex]);
					System.out.println(firstCondition);
					System.out.println(secondCondition);

					if (!(firstCondition || secondCondition))
						break;


					//if triple is edge of stack marker, second condition can't be true - a is -1, number is never -1

					//TEST FOR TYPE 2 PAIRS

					//if (h,a,b) on tstack has (a=v) and father(b) = a
					log.info("Testing for separation pairs");
					int b = triple.getB();
					int bIndex = -1;
					if (b > 0)
						bIndex = inverseNumbering[b - 1];
					System.out.println("B: " + vertices.get(bIndex));

					int a = triple.getA();
					System.out.println("A: " + vertices.get(inverseNumbering[a - 1]));
					System.out.println("Father of b: " + vertices.get(father[bIndex]));

					int h = triple.getH();
					if (secondCondition && newnum[father[bIndex]] == a){
						//delete (h,a,b) from stack
						log.info("removing triple " + triple + " from tstack");
						tstack.pop();
					}
					else{
						int x = -1;
						V xVertex = null;
						E virtualEdge = null;

						//if degree(w) = 2 and a1(w) > w

						if (firstCondition){

							//this means that v, a1(w) is a type 2 separation pair
							//a1(w) is x mentioned below, so no need to anything with it
							HopcroftTarjanSplitComponent<V, E> splitComponent = new HopcroftTarjanSplitComponent<V,E>();
							splitComponents.add(splitComponent);

							j++;

							//add top two edges (v,w) and (w,x) on estack to new component
							//pop edges, we don't need them any more on estack
							E e1 = estack.pop();
							E e2 = estack.pop();
							splitComponent.getEdges().add(e1);
							splitComponent.getEdges().add(e2);
							log.info("Add " + e1 + " " + e2 + "to new component");

							//E e1 = estack.get(estack.size() - 1);
							//E e2 = estack.get(estack.size() - 2);

							int[] dir = getDirectedNodes(e2, newnum);
							int xIndex = dir[1];
							x = newnum[xIndex];
							int vNum = newnum[vIndex];
							xVertex = vertices.get(xIndex);

							log.info("Found separation pair: " + v + ", " + xVertex);
							SeparationPair<V> separationPair = new SeparationPair<V>(v, xVertex, 2);
							separationPairs.add(separationPair);

							virtualEdge = Util.createEdge(v, xVertex, edgeClass);
							virtualEdges.add(virtualEdge);
							splitComponent.getEdges().add(virtualEdge);

							log.info("Add " + vNum + ", " + x + ", " + j + " to new component");


							//if (y,z) on estack has (y,z) = (x,v)
							//basically, try to find an edge (x,v)
							E xvEdge = onEstack(xIndex, vIndex);
							if (xvEdge != null){
								fflag = true;
								//remove from estack and save
								estack.remove(xvEdge);
								savedEdge = xvEdge;
								log.info("Saving edge " + xvEdge);
							}
						}
						//if else if (h,a,b) an tstack satisfies v=a and a!=father(b)
						//E:
						else if (secondCondition && newnum[father[bIndex]] != a){
							j++;
							//delete (h,a,b) from tstack
							tstack.pop();
							HopcroftTarjanSplitComponent<V, E> splitComponent = new HopcroftTarjanSplitComponent<V,E>();
							splitComponents.add(splitComponent);
							V aVertex = vertices.get(inverseNumbering[a - 1]);
							V bVertex = vertices.get(inverseNumbering[b - 1]);
							log.info("Found separation pair " + aVertex + ", " + bVertex);
							SeparationPair<V> separationPair = new SeparationPair<V>(aVertex, bVertex,2);
							separationPairs.add(separationPair);

							
							//while (x,y) on estack has (a<=x<=h) and (a<=y<=h)
							while (!estack.isEmpty()){
								E currentEdge = estack.peek();
								int[] directed = getDirectedNodes(currentEdge, newnum);
								x = newnum[directed[0]];
								int y = newnum[directed[1]];
								boolean condition = a<=x && x<=h && a<=y && y<=h;
								if(!condition)
									break;
								//if (x,y) = (a,b)
								//This is changed so that a back edge would also be detected as already on estack
								//a < b
								if (Math.min(x,y) == a && Math.max(x,y) == b){
									fflag = true;
									//deleted (a,b) from stack and save - (a,b) = (x,y) = e -> delete e from estack
									estack.pop();
									savedEdge = currentEdge;
								}
								else{
									//delete(x,y) from estack and add to current component
									estack.pop();
									log.info("Add edge " + currentEdge + " to current compoenent");
									splitComponent.getEdges().add(currentEdge);
									//decrement degree(x), degree(y)
									int xIndex = directed[0];
									int yIndex = directed[1];
									degree[xIndex]--;
									degree[yIndex]--;
								}
							}
							//add (a,b,j) to new component
							virtualEdge = Util.createEdge(aVertex, bVertex, edgeClass);
							virtualEdges.add(virtualEdge);
							splitComponent.getEdges().add(virtualEdge);
							log.info("add " + aVertex + ", " + bVertex + ", " + j + " to new component");
							//x = b
							x=b;
						}
						if (fflag){
							fflag = false;
							j++;
							//add saved edge, (x,v,j-1), (x,v,j) to new component

							//This should be a triple bond, which means create a new component and add edges to it
							HopcroftTarjanSplitComponent<V, E> splitComponent = new HopcroftTarjanSplitComponent<V,E>();
							splitComponent.getEdges().add(savedEdge);
							splitComponent.getEdges().add(virtualEdge);
							splitComponent.getEdges().add(virtualEdge);

							
							int xIndex = inverseNumbering[x - 1];
							V xVert = vertices.get(xIndex);
							log.info("add saved edge " + savedEdge + "( " + xVert + ", " + v + ", " + j + "-1, ( " + xVert + ", "+ v + ", " + j + ") to new component");
							//decrement  degree(x), degree(v)
							degree[xIndex]--;
							degree[vIndex]--;
						}

						//add (v,x,j) to estack 
						//if second condition is met a = v, x = b, so v,x = a,b which is separation pair and such virtual edge is created
						estack.push(virtualEdge);
						log.info("Pushing edge " + virtualEdge + " to estack");
						edgesJMap.put(virtualEdge, j);


						int xIndex = inverseNumbering[x - 1];
						if (xVertex == null)
							xVertex = vertices.get(xIndex);

						//increment degree x, degree v
						degree[xIndex]++;
						degree[vIndex]++;

						//father(x) = v
						father[xIndex] = vIndex;


						//if (A1(v)) ->*x then a1(v) = x
						int a1VIndex = inverseNumbering[a1[vIndex] - 1];
						V a1vVertex = vertices.get(a1VIndex);
						if (pathFrom(a1vVertex, xVertex))
							a1[vIndex] = x; //x is numbering

						//w = x
						w = xVertex;
						wIndex = xIndex;
					}
				}


				//TEST FOR TYPE 1 PAIR
				//G:
				//lowpts contain numberings
				//if (lowpt2(w)>=v) and ((lowpt1(w) != 1) or (father(v)!=1) or (w>3))
				if (lowpt2[wIndex] >= newnum[vIndex] && (lowpt1[wIndex] != 1 || newnum[father[vIndex]] != 1 || newnum[wIndex] > 3)){
					j++;
					//while (x,y) on top of estack has (w <= x<w +ND(w) or ((w<=y<w+ND(w))

					HopcroftTarjanSplitComponent<V, E> splitComponent = new HopcroftTarjanSplitComponent<V,E>();
					E virtualEdge;

					while (!estack.isEmpty()){
						E currentEdge = estack.peek();
						int[] directed = getDirectedNodes(currentEdge, newnum);
						int xIndex = directed[0];
						int yIndex = directed[1];
						int x = newnum[xIndex];
						int y = newnum[yIndex];

						boolean condition = (newnum[wIndex] <= x && x < newnum[wIndex] + nd[wIndex]) || 
								(newnum[wIndex] <= y && y < newnum[wIndex] + nd[wIndex]);

						if (!condition)
							break;

						//delete (x,y) from estack
						estack.pop();
						//add x,y to new component
						log.info("Add " + currentEdge + " to new component");
						
						splitComponent.getEdges().add(currentEdge);
						//decrement degree(x), degree(y)
						degree[xIndex]--;
						degree[yIndex]--;
					}
					//add (v, lowpt1(w),j) to new component
					V lowpt1W = vertices.get(inverseNumbering[lowpt1[wIndex] - 1]);

					log.info("Add " + v + ", " + lowpt1W + ", " + j +" to new component");
					virtualEdge = Util.createEdge(v, lowpt1W, edgeClass);
					virtualEdges.add(virtualEdge);
					splitComponent.getEdges().add(virtualEdge);

					splitComponents.add(splitComponent);
					SeparationPair<V> separationPair = new SeparationPair<V>(v, lowpt1W, 1);
					separationPairs.add(separationPair);


					//if a1(v) = w then a1(v) = lowpt1(w)
					if (a1[vIndex] == newnum[wIndex])
						a1[vIndex] = lowpt1[wIndex];

					//TEST FOR MULTIPLE EDGE
					//if (x,y) on top of estack has (x,y) = (v, lowpt1(w))

					if (!estack.isEmpty()){
						E currentEdge = estack.peek();
						if (currentEdge != null){
							int[] directed = getDirectedNodes(currentEdge, newnum);
							int xIndex = directed[0];
							int yIndex = directed[1];
							int x = newnum[xIndex];
							int y = newnum[yIndex];

							if (x == newnum[vIndex] && y == lowpt1[wIndex]){
								j++;
								//add (x,y), (v,lowpt1(w), j =1), (v,lowpt1(w),j) to new component
								//create triple bond
								HopcroftTarjanSplitComponent<V, E> tripleBond = new HopcroftTarjanSplitComponent<V,E>();
								tripleBond.getEdges().add(currentEdge);
								estack.pop();
								tripleBond.getEdges().add(virtualEdge);
								tripleBond.getEdges().add(virtualEdge);
								splitComponents.add(tripleBond);

								log.info("add (x,y), (v,lowpt1(w), j =1), (v,lowpt1(w),j) to new component");
								log.info("That is triple bond " + virtualEdge );
								//decrement degree(v), degree(lowpt1(w))
								degree[vIndex]--;
								degree[inverseNumbering[lowpt1[wIndex] - 1]]--;
							}
						}
					}

					//if(lowpt1(w) != father(v)
					if (lowpt1[wIndex] != newnum[father[vIndex]]){
						//add (v, lowpt1(w), j) to estack
						estack.push(virtualEdge);
						edgesJMap.put(virtualEdge, j);
						log.info("Pushing edge " + virtualEdge + " to estack");
						//increment degree(v), degree(lowpt1(w))
						degree[vIndex]++;
						degree[inverseNumbering[lowpt1[wIndex] - 1]]++;
					}
					else{
						j++;
						//add (v, lowpt1(w), j-1), (v, lowpt1(w), j), tree arc (lowpt1(w),v) to new component
						//mark tree arc (lowpt1(w),v) as virtual edge j --is this adding "triple" to estack??

						HopcroftTarjanSplitComponent<V, E> tripleBond = new HopcroftTarjanSplitComponent<V,E>();
						tripleBond.getEdges().add(virtualEdge);
						tripleBond.getEdges().add(virtualEdge);

						//find tree arc lowpt1(w),v
						V otherVertex = vertices.get(inverseNumbering[lowpt1[wIndex] - 1]);
						E treeArc = graph.edgeBetween(otherVertex, v);
						edgesJMap.put(treeArc, j);
						tripleBond.getEdges().add(treeArc);

						log.info("add (v, lowpt1(w), j-1), (v, lowpt1(w), j), tree arc (lowpt1(w),v) to new component");
						log.info("That is triple bond with tree arc " + treeArc);


					}
				}



				//After tstack is updated, if p contains more than one edge, 
				//an end of stack marker is placed
				//when backing over the first edge of p
				//everything down to end of stack marker is deleted

				//CONTINUE HERE
				//C:
				//if v->w is a first edge of a path then delete all entries on tstack down to and including end of stack marker
				if (firstEdgeOfAPath(e))
					while (!tstack.isEmpty()){
						Triple t = tstack.pop();
						log.info("Removing triple " + t + " from tstack");
						if (t == endOfStackMarker)
							break;
					}
				//D:
				//while (h,a,b) on estack has highpt(v) > h do delete(h,a,b,)from tstack 
				while (!tstack.empty()){
					Triple t = tstack.peek();
					if (t == endOfStackMarker)
						break;
					if (highpt[vIndex] > t.getH()){
						tstack.pop();
						log.info("Removing triple " + t + " from tstack");
					}
					else 
						break;
				} 
			} 
			else{
				//F:
				//if v-->w is the first and last edge of a path

				log.info("Back edge");


				if (singleEdgePath(e)){

					log.info("Single edge path");

					int y = 0;
					//while (h,a,b) on tstack has a > w

					Triple last = null;
					while(!tstack.empty()){
						Triple t = tstack.peek();
						if (t.getA() <= newnum[wIndex])
							break;
						y = Math.max(y,t.getH());
						log.info("Removing triple " + t + " from tstack");
						tstack.pop();
						last = t;
					}

					//if no triples deleted from tstack then add(v,w,v) to tstack
					if (last == null){
						Triple newTriple = new Triple(newnum[vIndex],newnum[wIndex],newnum[vIndex]);
						tstack.push(newTriple);
						log.info("Pushing " + newTriple + " to tstack");
					}
					//if (h,a,b ) last triple deleted then add (y,w,b) to tstack
					else{
						Triple newTriple = new Triple(y, newnum[wIndex], last.getB());
						tstack.push(newTriple);
						log.info("Pushing " + newTriple + " to tstack");
					}
				}
				//if w = father(v)
				if (father[vIndex] == wIndex){
					j++;
					//add (v,w), (v,w,j), tree arc (w,v) to new component
					log.info("add (v,w), (v,w,j), tree arc (w,v) to new component");
					//decrement degree v, degree w, mark tree arc (w,v) as virtual edge j
					degree[vIndex]--;
					degree[wIndex]--;
					edgesJMap.put(e,j);
				}
				//add (v,w) to estack
				else{
					estack.push(e);
					log.info("Pushing edge " + e  + " to estack");

				}

			}
		
		} 
	}
	
	private void formLastComponent(){
		HopcroftTarjanSplitComponent<V, E> splitComponent = new HopcroftTarjanSplitComponent<V,E>();
		for (E e : estack)
			splitComponent.getEdges().add(e);
		splitComponents.add(splitComponent);
	}


	private void init() throws AlgorithmErrorException{
		int size = graph.getVertices().size();
		lowpt1 = new int[size];
		lowpt2 = new int[size];
		nd = new int[size];
		father = new int[size];
		newnum = new int[size];
		highpt = new int[size];
		degree = new int[size];
		a1 = new int[size];
		inverseNumbering = new int[size];
		vertices = graph.getVertices();
		//separationPairEndVertices = new HashMap<V,List<SplitPair<V, E>>>();
		//separationPairStartVertices = new HashMap<V,List<SplitPair<V, E>>>();
		separationPairs = new ArrayList<SeparationPair<V>>();
		splitComponents = new ArrayList<HopcroftTarjanSplitComponent<V, E>>();
		estack = new Stack<E>();
		tstack = new Stack<Triple>();
		endOfStackMarker = new Triple(-1,-1,-1);
		edgesJMap = new HashMap<E, Integer>();
		edgeClass = graph.getEdges().get(0).getClass();
		j = 0;

		number = new int[size];
		flag = new boolean[size];
		for (int i = 0; i < size; i++){
			flag[i] = true;
			//number is initially all zeros
		}
		treeEdges = new ArrayList<E>();
		fronds = new ArrayList<E>();

		adjacency = new HashMap<V, List<E>>();
		for (V v : graph.getVertices()){
			adjacency.put(v, new ArrayList<E>(graph.adjacentEdges(v)));
		}

		//step one: perform a depth-first search on the multigraph converting in
		//into a palm tree

		//the search starts at vertex s
		V root = graph.getVertices().get(0);
		dfs(root,null);

		log.info("first dfs traversal finished");
		//tree = new DFSTree<V,E>(root, number, treeEdges, fronds, vertices);
		//System.out.println(tree);

		constructAdjacencyLists(adjacency);

		s = null;
		m = size;

		//find vertex whose number is 1, start with it
		//that will be the previously selected root vertex
		pathfiner(root,paths, null);

		log.info("second dfs completed");

		System.out.println("CHECKING ADJACENCY: " + checkAdjacencyValidity(adjacency, newnum, treeEdges));

		if (!checkAdjacencyValidity(adjacency, newnum, treeEdges))
			throw new AlgorithmErrorException("Error: adjacency structure not valid");

		//tree = new DFSTree<V,E>(root, newnum, treeEdges, fronds, vertices);
		//System.out.println(tree.toString());

		log.info("setting lowpts, inverse numbering etc.");


		int[] inverseOldNumbering = new int[number.length];
		for (int i = 0; i < number.length; i++)
			inverseOldNumbering[number[i] - 1] = i;


		System.out.println(adjacency);

		for (V v : vertices){

			int vIndex = vertices.indexOf(v);
			lowpt1[vIndex] = newnum[inverseOldNumbering[lowpt1[vIndex] - 1]];
			lowpt2[vIndex] = newnum[inverseOldNumbering[lowpt2[vIndex] - 1]];

			degree[vIndex] = graph.vertexDegree(v);

			//a1[v] first entry in the adjacency list
			if (adjacency.get(v).size() > 0){
				E firstEdge = adjacency.get(v).get(0);
				V other = firstEdge.getOrigin() == v ? firstEdge.getDestination()  : firstEdge.getOrigin();
				a1[vIndex] = newnum[vertices.indexOf(other)];
			}

			inverseNumbering[newnum[vIndex] - 1] = vIndex;
		}
	}


	/**
	 * Routine to generate paths in a biconnected palm tree 
	 * with specially ordered adjacency lists
	 * @param v vertex
	 * @param adjacent ordered adjacency structure
	 * @param paths list which will contain all paths
	 * @param currentPath current path being built
	 * @param treeEdges list of all tree edges (arcs)
	 */
	private void pathfiner(V v, 
			List<List<E>> paths, List<E> currentPath){

		int vIndex = vertices.indexOf(v);
		newnum[vIndex] = m - nd[vIndex] + 1;

		//System.out.println("setting newnum " +  v + " = " +  newnum[vIndex]);

		for (E e : adjacency.get(v)){

			V w = e.getOrigin() == v ? e.getDestination() : e.getOrigin();
			int wIndex = vertices.indexOf(w);

			if (s == null){
				s = v;
				currentPath = new ArrayList<E>();
				paths.add(currentPath);
			}
			currentPath.add(e);
			if (treeEdges.contains(e)){
				pathfiner(w, paths, currentPath);
				m--;
			}
			else{ //back edge
				
				if (highpt[newnum[wIndex] - 1] == 0) //-1 since numbering starts from 1, indexes from 1
					highpt[newnum[wIndex] - 1] = newnum[vIndex];


				//output current path
				System.out.println("output " + currentPath);
				s = null;
			}
		} 

	}



	/**
	 * Constructs ordered adjacency lists
	 * @param adjacent
	 */
	private void constructAdjacencyLists(Map<V,List<E>> adjacent){
		Map<Integer, List<E>> bucket = new HashMap<Integer, List<E>>();

		int size = vertices.size();
		V v;

		for (E e : graph.getEdges()){

			//System.out.println("analyzing edge " + e);

			//v is origin
			//w is destination
			int[] directedIndexes = getDirectedNodes(e, number);

			int vIndex = directedIndexes[0];
			int wIndex = directedIndexes[1];


			//compute Fi(v,w)
			Integer fi;
			if (fronds.contains(e))
				fi = 2 * number[wIndex] + 1;
			else if (lowpt2[wIndex] < number[vIndex]) //and is tree edge
				fi = 2 * lowpt1[wIndex];
			else
				fi = 2*lowpt1[wIndex] + 1;

			//System.out.println("Fi " + fi);

			List<E> list = bucket.get(fi);
			if (list == null){
				list = new ArrayList<E>();
				bucket.put(fi, list);
			}

			list.add(e); // add (v,w) to bucket(fi(v,w))
		}

		//clear adjacent
		for (V vert : vertices)
			adjacent.get(vert).clear();

		//System.out.println("Buket: " + bucket);

		for (int i = 1; i <= 2*size + 1; i++){
			if (!bucket.containsKey(i))
				continue;
			for (E e : bucket.get(i)){
				//add w to the end of A(v)


				int vIndex = getDirectedNodes(e, number)[0];

				v = vertices.get(vIndex);
				//add w to end of A(v)
				adjacent.get(v).add(e);
			}
		}

	}

	/**
	 * Routine for depth-first search of a multigraph represented by adjacency list
	 * A(v). Variable n denote the last number assigned to a vertex
	 *  U is the father of vertex v in the spanning tree being constructed
	 *  The graph to be searched is represented by adjacency lists A(v)
	 *  This will be implemented with a map vertex - list of edges 
	 */
	private void dfs(V v, V u){
		int vIndex = vertices.indexOf(v);

		//n:= number(v):=n+1
		n++;
		number[vIndex] = n;

		//a:
		lowpt1[vIndex] = number[vIndex];
		lowpt2[vIndex] = number[vIndex];
		nd[vIndex] = 1;

		for (E e : adjacency.get(v)){
			V w = e.getOrigin() == v ? e.getDestination() : e.getOrigin();
			int wIndex = vertices.indexOf(w);
			if (number[wIndex] == 0){
				//w in as new vertex
				treeEdges.add(e); //(v,w) is a tree arc
				dfs(w, v);

				//b:
				if (lowpt1[wIndex] < lowpt1[vIndex]){
					lowpt2[vIndex] = Math.min(lowpt1[vIndex], lowpt2[wIndex]);
					lowpt1[vIndex] = lowpt1[wIndex];
				}
				else if (lowpt1[wIndex] == lowpt1[vIndex])
					lowpt2[vIndex] = Math.min(lowpt2[vIndex], lowpt2[wIndex]);
				else
					lowpt2[vIndex] = Math.min(lowpt2[vIndex], lowpt1[wIndex]);
				nd[vIndex] += nd[wIndex];
				father[wIndex] = vIndex;

			}
			else if ((number[wIndex] < number[vIndex]) && ((w != u) || !flag[vIndex])){
				//the test in necessary to avoid exploring the edge
				//in both direction. Flag(v) becomes false when the entry in A(v) corresponding
				//to the tree arc (u,v) is examined

				//mark (v,w) as frond
				fronds.add(e);

				//c:
				if (number[wIndex] < lowpt1[vIndex]){
					lowpt2[vIndex] = lowpt1[vIndex];
					lowpt1[vIndex] = number[wIndex];
				}
				else if (number[wIndex] > lowpt1[vIndex])
					lowpt2[vIndex] = Math.min(lowpt2[vIndex], number[wIndex]);
			}
			if (w == u)
				flag[vIndex] = false;
		}
	}





	/**
	 * A dfs tree is a directed graph regardless of this property of the original graph 
	 * Method finds origin and destination of an edge which can either be a back edge (frond)
	 * or an arc (tree edge)
	 * @param edge
	 * @return ret[0] = origin ret[1] = destination
	 */
	private int[] getDirectedNodes(E edge, int[] numbering){

		//if an edge is a tree edge
		//origin is the one with lower number
		//if it is a back edge, the oposite is true
		int[] ret = new int[2];


		int originIndex = vertices.indexOf(edge.getOrigin());
		int destinationIndex = vertices.indexOf(edge.getDestination());
		int originNum = numbering[originIndex];
		int destinationNum = numbering[destinationIndex];

		if (treeEdges.contains(edge)){
			if (originNum < destinationNum){
				ret[0] = originIndex;
				ret[1] = destinationIndex;
			}
			else{
				ret[0] = destinationIndex;
				ret[1] = originIndex;
			}
		}
		else { //back edge
			if (originNum > destinationNum){
				ret[0] = originIndex;
				ret[1] = destinationIndex;
			}
			else{
				ret[0] = destinationIndex;
				ret[1] = originIndex;
			}
		}

		return ret;

	}




	/**
	 * Checks if the adjacency structure is valid according to the following lemma:
	 * Let A(u) be the adjacency list of vertex u. Let u->v and u -> w be tree arcs
	 * with v occurring before w in A(u). Then u<w<v.
	 * @param adjacency
	 * @param numbering
	 * @return true if adjacency structure is valid, false otherwise 
	 */
	private boolean checkAdjacencyValidity(Map<V,List<E>> adjacency, int[] numbering, List<E> treeEdges){

		for (V u : adjacency.keySet()){
			int uIndex = vertices.indexOf(u);
			List<E> adjacent = adjacency.get(u);
			for (int i = 0; i < adjacent.size() - 1; i++){
				E e1 = adjacent.get(i);
				if(!treeEdges.contains(e1))
					continue;
				V v = e1.getOrigin() == u ? e1.getDestination() : e1.getOrigin();
				int vIndex = vertices.indexOf(v);
				for (int j = i + 1; j < adjacent.size(); j++){
					E e2 = adjacent.get(j);
					if(!treeEdges.contains(e2))
						continue;
					V w = e2.getOrigin() == u ? e2.getDestination() : e2.getOrigin();
					int wIndex = vertices.indexOf(w);
					if (!((numbering[uIndex] < numbering[wIndex]) && (numbering[wIndex] < numbering[vIndex])))
						return false;
				}
			}
		}
		return true;

	}

	private boolean pathFrom(V vertex1, V vertex2){

		//if there is a path that contains both vertices among the paths
		//it should be vertex1 ->* vertex2
		//path should only contain one back edge (definition)
		if (vertex1 == vertex2)
			return false;
		for (List<E> path : paths)
			if (path.contains(vertex1) && path.contains(vertex2))
				return true;
		return false;
	}


	private E onEstack(int index1, int index2){
		for (int i = 0; i < estack.size(); i++){
			E e = estack.get(i);
			int[] directedNodes = getDirectedNodes(e, newnum);

			if (directedNodes[0] == index1 && directedNodes[1] == index2)
				return e;
		}

		return null;

	}

	private boolean firstEdgeOfAPath(E e){
		for (List<E> path : paths)
			if (path.get(0) == e)
				return true;
		return false;
	}

	private boolean singleEdgePath(E e){
		for (List<E> path : paths)
			if (path.get(0) == e && path.size() == 1)
				return true;
		return false;
	}

	private void printTStack(){
		System.out.println("TSTACK:");
		for (Triple t : tstack){
			System.out.println(t);
		}
	}

	private void printEStack(){
		System.out.println("ESTACK:");
		for (E e : estack){
			System.out.println(e);
		}
	}

	@SuppressWarnings("unused")
	private void printVerticesData(){
		for (V v : graph.getVertices()){
			int vIndex = vertices.indexOf(v);
			System.out.println("Vertex " + v);
			System.out.println("ND " + nd[vIndex]);
			System.out.println("Numbering " + newnum[vIndex]);
			System.out.println("lowpt1 " + lowpt1[vIndex]);
			System.out.println("lowpt2 " + lowpt2[vIndex]);
			System.out.println("father " + vertices.get(father[vIndex]));
			System.out.println("highpt " + highpt[vIndex]);
			System.out.println("degree " + degree[vIndex]);
			System.out.println("a1 " + a1[vIndex]);
		}
	}

	@SuppressWarnings("unused")
	private void printPaths(){
		for (List<E> path : paths){
			System.out.println(path);
		}
	}

	@SuppressWarnings("unused")
	private void printAdjacency(){
		for (V v : vertices){
			System.out.println("Vertex: " + v);
			for (E e : adjacency.get(v)){
				V other = e.getOrigin() == v ? e.getDestination() : e.getOrigin();
				System.out.println(other + " " + newnum[vertices.indexOf(other)]);
			}
		}

	}

}