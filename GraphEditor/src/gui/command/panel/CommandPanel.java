package gui.command.panel;

import graph.algorithm.AlgorithmExecutor;
import graph.algorithm.ExecuteResult;
import graph.algorithm.cycles.SimpleCyclesFinder;
import graph.algorithm.cycles.SimpleUndirectedCyclesFinder;
import graph.algorithms.drawing.ConvexDrawing;
import graph.algorithms.drawing.VisibilityRepresentation;
import graph.algorithms.planarity.FraysseixMendezPlanarity;
import graph.algorithms.planarity.PlanarityTestingAlgorithm;
import graph.drawing.Drawing;
import graph.elements.Graph;
import graph.exception.CannotBeAppliedException;
import graph.exception.DSLException;
import graph.layout.dsl.DSLLayouter;
import graph.properties.components.HopcroftTarjanSplitComponent;
import graph.properties.components.SplitPair;
import graph.properties.splitting.AlgorithmErrorException;
import graph.properties.splitting.HopcroftTarjanSplitting;
import graph.properties.splitting.SeparationPairSplitting;
import graph.properties.splitting.Splitting;
import graph.properties.splitting.TriconnectedSplitting;
import graph.symmetry.Permutation;
import graph.symmetry.PermutationAnalyzator;
import graph.symmetry.nauty.McKayGraphLabelingAlgorithm;
import graph.tree.binary.BinaryTree;
import graph.tree.spqr.SPQRTree;
import graph.util.Util;
import gui.main.frame.MainFrame;
import gui.model.GraphEdge;
import gui.model.GraphModel;
import gui.model.GraphVertex;
import gui.view.GraphView;
import gui.view.painters.EdgePainter;
import gui.view.painters.VertexPainter;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class CommandPanel extends JPanel{

	private static final long serialVersionUID = 1L;

	private static String[] commands;
	private JTextField inputField = new JTextField();
	private JTextArea centralArea = new JTextArea(10, 10);
	private List<String> allCommands = new ArrayList<String>();
	private int currentCommandIndex;
	private static PlanarityTestingAlgorithm<GraphVertex, GraphEdge> planarityTest = new FraysseixMendezPlanarity<GraphVertex, GraphEdge>();
	//private static PlanarityTestingAlgorithm<GraphVertex, GraphEdge> planarityTest = new BoyerMyrvoldPlanarity<GraphVertex, GraphEdge>();
	//private static PlanarityTestingAlgorithm<GraphVertex, GraphEdge> planarityTest = new PQTreePlanarity<GraphVertex, GraphEdge>();
	private static Splitting<GraphVertex, GraphEdge> splitting = new Splitting<>();

	public CommandPanel(){
		setLayout(new MigLayout("fill"));

		add(inputField, "dock south, growx");
		inputField.setText("Enter command");

		centralArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(centralArea);
		add(scrollPane, "grow");

		initCommands();

		inputField.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				if (inputField.getText().equals("Enter command"))
					inputField.setText("");
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (inputField.getText().equals(""))
					inputField.setText("Enter command");

			}
		});

		inputField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER){
					String command = inputField.getText();
					String reply = processCommand(command);
					inputField.setText("");
					if (!command.equals("clear"))
						centralArea.setText(centralArea.getText() + "\n" + command + "> " + reply);
				}
				else if (e.getKeyCode() == KeyEvent.VK_UP){
					if (currentCommandIndex > 0)
						currentCommandIndex --;
					inputField.setText(allCommands.get(currentCommandIndex));
				}
				else if (e.getKeyCode() == KeyEvent.VK_DOWN){
					if (currentCommandIndex < allCommands.size() - 1)
						currentCommandIndex ++;
					if (currentCommandIndex < allCommands.size())
						inputField.setText(allCommands.get(currentCommandIndex));
				}

			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});

	}



	private String processCommand(String command){
		command = command.trim();
		allCommands.add(command);
		currentCommandIndex = allCommands.size();


		//TODO svugde vreme osim kod kreiranja

		if (command.startsWith(commands[0])){ //create graph
			command = command.substring(commands[0].length()).trim();
			String[] split = command.split(" ");
			if (split.length == 0)
				return "Plese enter graph's name";
			String name = split[0];
			String directedS = "";
			if (split.length > 1)
				directedS = split[1];
			boolean directed = false;
			if (!directedS.equals("")){
				try{
					directed = Boolean.parseBoolean(directedS);
				}
				catch(Exception ex){

				}
			}

			String ret = directed ? "Directed graph \"" + name + "\" created" : "Undirected graph \"" + name + "\" created";
			Graph<GraphVertex, GraphEdge> newGraph = new Graph<GraphVertex, GraphEdge>(directed);
			GraphView view = new GraphView(newGraph);
			MainFrame.getInstance().addDiagram(view, name);
			return ret;
		}

		if (MainFrame.getInstance().getCurrentView() == null)
			return "Create or open a graph";

		Graph<GraphVertex, GraphEdge> graph = MainFrame.getInstance().getCurrentView().getModel().getGraph();

		if (command.startsWith(commands[1])){ //add vertex
			command = command.substring(commands[1].length()).trim();
			String[] split = command.split(" ");
			if (split.length == 0)
				return "Please enter vertex name and position as (x, y)";
			if (split[0].contains("(") && !split[1].contains("("))
				return "Please enter vertex name and position as (x, y)";
			String content = split[0];
			int positionStart = command.indexOf("(");
			if (positionStart == -1)
				return "Please enter vertex name and position as (x, y)";
			String position = command.substring(positionStart + 1).trim();
			if (!position.endsWith(")"))
				return "Please enter vertex name and position as (x, y)";
			position = position.substring(0, position.length() - 1);
			String[] nums = position.split(",");
			int x = Integer.parseInt(nums[0].trim());
			int y = Integer.parseInt(nums[1].trim());
			Point2D point = new Point2D.Double(x, y);
			if (content.equals(""))
				return "Please enter vertex name";
			GraphVertex vert = new GraphVertex(point, content);
			MainFrame.getInstance().getCurrentView().getModel().addVertex(vert);
			MainFrame.getInstance().getCurrentView().addVertexPainter(new VertexPainter(vert));
			return "Vertex " + content + " added at position " + "(" + x + ", " + y +")";
		}

		if (command.startsWith(commands[2])){
			command = command.substring(commands[3].length()).trim();
			String[] split = command.split(" ");
			if (split.length < 2)
				return "Please enter two vertices";

			String v1 = split[0].trim();
			String v2 = split[1].trim();

			GraphVertex vert1 = MainFrame.getInstance().getCurrentView().getModel().getVertexByContent(v1);
			if (vert1 == null)
				return "Unknown vertex \"" + v1 + "\"";
			GraphVertex vert2 = MainFrame.getInstance().getCurrentView().getModel().getVertexByContent(v2);
			if (vert2 == null)
				return "Unknown vertex \"" + v2 + "\"";
			GraphEdge edge = new GraphEdge(vert1, vert2);
			edge.setNodesBasedOnVertices();
			MainFrame.getInstance().getCurrentView().getModel().addEdge(edge);
			MainFrame.getInstance().getCurrentView().addEdgePainter(new EdgePainter(edge, MainFrame.getInstance().getCurrentView().getModel().getGraph()));
			return "Edge " + v1 + ", " + v2 + " added";
		}


		if (command.startsWith(commands[3])){
			return MainFrame.getInstance().getCurrentView().getModel().getGraph().isConnected() ? "yes" : "no";
		}

		if (command.startsWith(commands[4])){
			return MainFrame.getInstance().getCurrentView().getModel().getGraph().isBiconnected() ? "yes" : "no";

		}

		if (command.startsWith(commands[5])){
			return MainFrame.getInstance().getCurrentView().getModel().getGraph().isCyclic() ? "yes" : "no";

		}

		if (command.startsWith(commands[6])){
			ExecuteResult result = AlgorithmExecutor.execute(planarityTest, "isPlannar", MainFrame.getInstance().getCurrentView().getModel().getGraph());
			return ((Boolean) result.getValue() ? "yes" : "no" )+ " [in " + result.getDuration() + " ms]";
		}

		if (command.startsWith(commands[7])){
			List<GraphVertex> cutVertices = graph.listCutVertices();
			String ret ="";
			if (cutVertices.size() == 0)
				ret = " Graph is biconnected";
			else{
				ret = Util.replaceSquareBrackets(Util.addNewLines(cutVertices.toString(), ",", 30));
			}
			return ret;
		}		

		if (command.startsWith(commands[8])){
			String ret;
			if (graph.isBiconnected()){
				ret = " Graph is biconnected";
				JOptionPane.showMessageDialog(MainFrame.getInstance(), ret, "Biconnected components", JOptionPane.INFORMATION_MESSAGE);
			}
			else{
				List<Graph<GraphVertex, GraphEdge>> blocks = graph.listBiconnectedComponents();
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < blocks.size(); i++){
					Graph<GraphVertex, GraphEdge> block  = blocks.get(i);
					builder.append("Component " + (i+1) + " " + block.printFormat() + "\n");
				}
				ret = builder.toString();
			}
			return ret;
		}	

		if (command.startsWith(commands[9])){
			String ret = "";
			SeparationPairSplitting<GraphVertex, GraphEdge> separationPairsSplitting =
					new SeparationPairSplitting<GraphVertex, GraphEdge>();
			List<SplitPair<GraphVertex, GraphEdge>> separationPairs;
			try {
				separationPairs = separationPairsSplitting.findSeaparationPairs(graph);

				if (separationPairs.size() == 0){
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Graph is triconnected", "Separation pairs", JOptionPane.INFORMATION_MESSAGE);
				}
				ret = separationPairs.toString();
				ret = Util.removeSquareBrackets(Util.addNewLines(ret, "),", 40));
			} catch (AlgorithmErrorException e) {
				e.printStackTrace();
			}
			return ret;
		}	

		if (command.startsWith(commands[10])){
			SeparationPairSplitting<GraphVertex, GraphEdge> separationPairsSplitting =
					new SeparationPairSplitting<GraphVertex, GraphEdge>();

			String answer = "no";
			try {
				answer = separationPairsSplitting.findSeaparationPairs(graph).size() == 0 ? "yes" : "no";
			} catch (AlgorithmErrorException e) {
				e.printStackTrace();
			}
			return answer;
		}

		if (command.startsWith(commands[11])){
			TriconnectedSplitting<GraphVertex, GraphEdge> splitting = new TriconnectedSplitting<GraphVertex, GraphEdge>(graph);
			List<HopcroftTarjanSplitComponent<GraphVertex, GraphEdge>>  components = splitting.formTriconnectedComponents();
			String ret = "";
			if (components.size() == 0){
				ret = "Graph is triconnected";
			}
			else{
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < components.size(); i++){
					HopcroftTarjanSplitComponent<GraphVertex, GraphEdge> component  = components.get(i);
					builder.append("Component " + (i+1) + " " + component.printFormat() + "\n");
				}
				ret = builder.toString();
			}
			return ret;
		}


		if (command.startsWith(commands[12])){
			String ret = "";
			McKayGraphLabelingAlgorithm<GraphVertex, GraphEdge> nauty = new McKayGraphLabelingAlgorithm<GraphVertex,GraphEdge>(graph);
			List<Permutation> automorphisms = nauty.findAutomorphisms();
			for (Permutation p : automorphisms){
				ret += p.cyclicRepresenatation() + "\n";
			}
			return ret;
		}

		if (command.startsWith(commands[13])){
			SimpleCyclesFinder<GraphVertex, GraphEdge> cyclesFinder = new SimpleCyclesFinder<GraphVertex,GraphEdge>();
			List<List<GraphVertex>> cycles = cyclesFinder.findCycles(graph);
			String cyclesStr = "";
			if (cycles.size() == 0)
				cyclesStr = "Graph is not cyclic";
			else{
				for (int i = 0; i < cycles.size(); i++){
					cyclesStr += Util.replaceSquareBrackets(cycles.get(i).toString());
					if (i < cycles.size() - 1)
						cyclesStr += ", ";
				}
				cyclesStr = Util.addNewLines(cyclesStr, "),", 30);
			}
			return cyclesStr;
		}

		//TODO
		if (command.startsWith(commands[14])){
			command = command.substring(commands[14].length()).trim();
			String[] split = command.split(" ");
			if (split.length < 1)
				return "Please enter one edge";

			if (!graph.isBiconnected())
				return "Graph is not biconnected.";


			String sp = split[0].substring(1, split[0].length()-1);
			String[] split2 = sp.split(",");
			if (split2.length < 2)
				return "Please enter an edge consisting of two vertices";
			String v1 = split2[0];
			String v2 = split2[1];

			GraphVertex vert3 = graph.getVertexByContent(v1);
			if (vert3 == null)
				return "Unknown vertex \"" + v1 + "\"";
			GraphVertex vert4 = graph.getVertexByContent(v2);
			if (vert4 == null)
				return "Unknown vertex \"" + v2 + "\"";
			if (!graph.hasEdge(vert3, vert4))
				return "Edge doesn't exist";
			GraphEdge edge = graph.edgeesBetween(vert3, vert4).get(0);

			try {
				new SPQRTree<GraphVertex, GraphEdge>(edge, graph);
				return "";
			} catch (CannotBeAppliedException e) {
				return "Couldn't construct spqr tree: " + e.getMessage();
			}
		}

		if (command.equals(commands[16])){
			McKayGraphLabelingAlgorithm<GraphVertex, GraphEdge> nauty = new McKayGraphLabelingAlgorithm<GraphVertex,GraphEdge>(graph);
			List<Permutation> automorphisms = nauty.findAutomorphisms();
			String ret = "\n";
			for (Permutation p : automorphisms){
				ret += p.cyclicRepresenatation() + "\n";
			}

			return ret;
		}

		if (command.equals(commands[17])){
			SimpleCyclesFinder<GraphVertex, GraphEdge> jcycles = new SimpleCyclesFinder<GraphVertex,GraphEdge>();
			System.out.println(jcycles.findCycles(graph));
			return (jcycles.toString());
		}
		if (command.equals(commands[18])){
			SimpleUndirectedCyclesFinder<GraphVertex, GraphEdge> cycles = 
					new SimpleUndirectedCyclesFinder<GraphVertex, GraphEdge>(graph);
			String ret = "\n";
			for (List<GraphVertex> cycle : cycles.findAllCycles())
				ret += cycle + "\n";
			return ret;
		}

		if (command.equals(commands[19])){
			String ret = "";
			PermutationAnalyzator<GraphVertex, GraphEdge> analyzator =new PermutationAnalyzator<GraphVertex,GraphEdge>(graph);
			ret += analyzator.findReflectionGroups() + "\n";
			ret += analyzator.findRotationGroups() + "\n";
			ret += analyzator.findDihedralGroups();
			return ret;

		}

		if (command.equals(commands[20])){
			System.out.println("convex");
			ConvexDrawing<GraphVertex, GraphEdge> drawing = new ConvexDrawing<GraphVertex,GraphEdge>(graph);
			drawing.execute();
		}

		if (command.equals(commands[21])){
			return graph.listBiconnectedComponents().toString();
		}

		if (command.equals(commands[22])){
			SeparationPairSplitting<GraphVertex, GraphEdge> separationPairsSplitting = new SeparationPairSplitting<GraphVertex, GraphEdge>();
			try {
				List<SplitPair<GraphVertex, GraphEdge>> separationPairs = separationPairsSplitting.findSeaparationPairs(graph);
				return separationPairs.toString();
			} catch (AlgorithmErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return e.getMessage();
			}

		}


		else if (command.equals(commands[24])){
			HopcroftTarjanSplitting<GraphVertex, GraphEdge> hopcroftTarjan = new HopcroftTarjanSplitting<GraphVertex, GraphEdge>(graph);
			try {
				hopcroftTarjan.execute();
			} catch (AlgorithmErrorException e) {
				return e.getMessage();
			}
			return "Done";

		}

		else if (command.equals(commands[23])){
			centralArea.setText("");
			return "";
		}

		else if (command.startsWith(commands[25])){
			//Layout DSL input
			List<GraphVertex> vertices = graph.getVertices();
			List<GraphEdge> edges = graph.getEdges();

			DSLLayouter<GraphVertex, GraphEdge> dslLayout =
					new DSLLayouter<GraphVertex, GraphEdge>(vertices, edges, command);	

			try{
				Drawing<GraphVertex, GraphEdge> drawing = dslLayout.layout();
				GraphView view = MainFrame.getInstance().getCurrentView();
				for (GraphVertex vert : drawing.getVertexMappings().keySet()){
					vert.setPosition(drawing.getVertexMappings().get(vert));
				}
				for (GraphEdge edge : drawing.getEdgeMappings().keySet()){
					List<Point2D> points = drawing.getEdgeMappings().get(edge);
					edge.setLinkNodes(points);
				}
				view.repaint();
				return "Done";
			}
			catch(DSLException ex){
				return ex.getMessage();
			}
		}

		else if (command.trim().equals("Binary tree")){
			BinaryTree<GraphVertex, GraphEdge> binaryTree = new BinaryTree<GraphVertex,GraphEdge>(graph);
			boolean balanced = binaryTree.isBalanced();
			return binaryTree.toString() + "\n Balanced: " + balanced;
		}
		else if (command.trim().equals("is ring")){
			return graph.isRing() + "";
		}
		else if (command.trim().equals("test")){
			//execute whatever that is being tested
			//			try {
			//				//Map<GraphVertex,Integer> ordering = TopologicalOrdering.calculateOrdering(graph);
			//				//System.out.println(ordering);
			//				//BCTree<GraphVertex, GraphEdge> bcTree = new BCTree<GraphVertex, GraphEdge>(graph);
			//				//System.out.println(bcTree);
			//			PlanarAugmentation<GraphVertex, GraphEdge> planarAugmentation = new PlanarAugmentation<GraphVertex, GraphEdge>();
			//			List<GraphEdge> edges = planarAugmentation.planarBiconnected(graph);
			//			return "Should add: " + edges;
			//			} catch (CannotBeAppliedException e) {
			//				e.printStackTrace();
			//			}
			//			GraphVertex s = graph.getVertices().get(0);
			//			GraphVertex t = graph.getVertices().get(1);
			//			STNumbering<GraphVertex, GraphEdge> stNumbering = new STNumbering<GraphVertex, GraphEdge>(graph, s,t);
			//			return stNumbering.getOrder() + "";

			//			try {
			//				//Embedding<GraphVertex, GraphEdge> embedding = PlanarEmbedding.emedGraph(graph);
			//				//return embedding + "";
			//				PlanarFaces<GraphVertex, GraphEdge> planarFaces = new PlanarFaces<GraphVertex, GraphEdge>(graph);
			//				planarFaces.formFaces(null, null);
			//			} catch (NotPlanarException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}

			//VisibilityRepresentation visibilityRepresentation = new VisibilityRepresentation(graph);

		}

		if (command.equals(commands[15])){
			StringBuilder builder = new StringBuilder("Commands:\n");
			builder.append("quit\n");
			builder.append("create graph name [true/false] \n");
			builder.append("add vertex content graph\n");
			builder.append("add edge {vertex1, vertex2}\n");
			builder.append("is connected\n");
			builder.append("is biconnected\n");
			builder.append("is cyclic\n");
			builder.append("is planar\n");
			builder.append("list cut vertices\n");
			builder.append("list blocks graph\n");
			builder.append("list split pairs graph\n");
			builder.append("list split components {u,v}\n");
			builder.append("split graph {u,v} {e1, e2} \n");
			builder.append("maximal split pairs {e1, e2} \n");
			builder.append("construct spqr tree {e1, e2} \n");
			return builder.toString();
		}

		return "Unknown command";
	}


	private static  void initCommands(){
		commands = new String[23];

		commands[0] = "create graph";
		commands[1] = "add vertex";
		commands[2] = "add edge";
		commands[3] = "is connected";
		commands[4] = "is biconnected";
		commands[5] = "is cyclic";
		commands[6] = "is planar";
		commands[7] = "list cut vertices";
		commands[8] = "list blocks";
		commands[9] = "list separation pairs";
		commands[10] = "is triconnected";
		commands[11] = "list triconnected components";
		commands[12] = "list automorphisms";
		commands[13] = "cycles basis";

		//TODO
		commands[14] = "list all cycles";
		commands[15] = "groups"; //atomorphisms groups
		commands[16] = "is tree";
		commands[17] = "is binary tree";
		commands[18] = "is ring";
		commands[19] = "i bipartite";
		commands[20] = "clear";
		commands[21] = "lay out";
		commands[22] = "test";
	}


}
