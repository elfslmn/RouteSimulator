import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerPipe;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class RouteSim {
	public static HashMap<Integer, Node> nodes = new HashMap<>();
	public static HashMap<DynamicPath, DynamicPath> dynamicPaths = new HashMap<>();
	public static int startNode = -1;
	public static int destNode = -1;
	static Viewer viewer = null;
	static ViewerPipe vp = null;

	private static String styleSheet =
					"edge { " +
					"text-alignment: under;" +
					"size: 3px;" +
					"text-size: 15; }" +
					"node {" +
					"text-color: white;" +
					"size: 30px, 30px; }";

	public static void main(String[] args) {

		try {
			System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
			Graph graph = new SingleGraph("graph_gui");
			graph.addAttribute("ui.quality");
			graph.addAttribute("ui.antialias");
			graph.addAttribute("ui.stylesheet", styleSheet);

			JFrame frame = new JFrame("Control Panel");
			frame.setSize(300,100);
			//frame.add(view);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JPanel panel = new JPanel();
			JLabel label1 = new JLabel("Starting node:");
			JTextField tf1 = new JTextField(3);
			tf1.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {getNumber();}
				public void removeUpdate(DocumentEvent e)  {getNumber();}
				public void insertUpdate(DocumentEvent e)  {getNumber();}
				public void getNumber() {
					if (tf1.getText().equals("")) return;
					startNode = Integer.parseInt(tf1.getText());
					System.out.println("Starting node: " + startNode);
				}
			});

			JLabel label2 = new JLabel("Destination node:");
			JTextField tf2 = new JTextField(3);
			tf2.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {getNumber();}
				public void removeUpdate(DocumentEvent e)  {getNumber();}
				public void insertUpdate(DocumentEvent e)  {getNumber();}
				public void getNumber() {
					if(tf2.getText().equals(""))return;
					destNode = Integer.parseInt(tf2.getText());
					System.out.println("Destination node: "+destNode);
				}
			});

			JLabel label3 = new JLabel("Total Cost:");
			JTextField tf3 = new JTextField(3);
			tf3.setEditable(false);

			JButton show = new JButton("Show MinCost Path");
			show.addActionListener(actionEvent -> {
                System.out.println("Button pressed "+ actionEvent.getActionCommand());
                if(startNode >= nodes.size() || startNode < 0 || destNode >= nodes.size()|| destNode <0){
                    System.out.println("Give a valid node id");
                    return;
                }
                for(Edge e: graph.getEachEdge()){
                    e.addAttribute("ui.style", "fill-color: black;");
                }
                if(startNode == destNode){
					graph.getEdge(startNode+""+startNode).addAttribute("ui.style", "fill-color: red;");
					viewer.getDefaultView().repaint();
					vp.pump();
					return;
				}
                int cur = startNode;
                int cost = nodes.get(cur).getDistanceVector().get(destNode);
                while (cur != destNode){
                    int via = nodes.get(cur).getForwardingTable().get(destNode);
                    System.out.println(cur+"->"+via);
                    int from = Math.min(cur,via);
                    int to = Math.max(cur,via);
                    graph.getEdge(from+""+to).addAttribute("ui.style", "fill-color: red;");
                    cur = via;
                }
                viewer.getDefaultView().repaint();
                vp.pump();
                tf3.setText(""+cost);
            });


			panel.add(label1);
			panel.add(tf1);
			panel.add(label2);
			panel.add(tf2);
			panel.add(label3);
			panel.add(tf3);
			panel.add(show);
			frame.getContentPane().add(panel);
			frame.setVisible(true);

			FileInputStream stream = new FileInputStream("input.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

			String line;
			while ((line = reader.readLine()) != null)
			{
				System.out.println (line);
				String[] split = line.split(",|\\(|\\)");
				ArrayList<Integer> arr = new ArrayList<>();
				for(String s: split){
					if(!s.equals("")){
						s.trim();
						if(s.contains("x")){
							arr.add(-1); // Represent dynamic path
						}else{
							arr.add(Integer.parseInt(s));
						}
					}
				}
				Integer nodeid = arr.get(0);
				Hashtable<Integer, Integer> linkCost = new Hashtable<>();
				for(int i=1; i<arr.size(); i+=2){
					if(arr.get(i+1) < 0 ){ // Dynamic path
						DynamicPath d = new DynamicPath(nodeid, arr.get(i));
						if(dynamicPaths.keySet().contains(d)){
							d = dynamicPaths.get(d);
						}else{
							dynamicPaths.put(d,d);
						}
						linkCost.put(arr.get(i), d.cost);
					}
					else{
						linkCost.put(arr.get(i), arr.get(i+1)); // neighbor , cost
					}

				}
				nodes.put(nodeid, new Node(nodeid, linkCost));
			}
			reader.close();

			// Graph visualization
			for(Integer id : nodes.keySet()){
				graph.addNode(""+id);
			}
			for(Integer id : nodes.keySet()){
				Node currentNode = nodes.get(id);
				for(int nei : currentNode.neighbours){
					int from = Math.min(id,nei);
					int to = Math.max(id,nei);
					try{
						Edge edge = graph.addEdge(from+""+to, from, to);
						edge.addAttribute("ui.label", currentNode.linkCost.get(nei));
					}
					catch (Exception e){}
				}
			}
			for (org.graphstream.graph.Node node : graph) {
				node.addAttribute("ui.label", node.getId());
			}
			viewer = graph.display();
			vp = viewer.newViewerPipe();


			// Simulation
			for(int t=1 ; t < 1000; t++){
				System.out.println("\nTime "+t+" ------------------------------------------------------");
				boolean isConverged = true;
				for(Integer id : nodes.keySet()){
					boolean isUpdated = nodes.get(id).sendUpdate();
					if (isUpdated) System.out.println("RouteSim: Node "+id+" isUpdated="+isUpdated + " and updates are sent");
					else System.out.println("RouteSim: Node "+id+" isUpdated="+isUpdated);
					isConverged = isConverged && (!isUpdated);
				}
				if(isConverged){
					System.out.println("System is converged at time "+t);
					for(Integer id : nodes.keySet()){
						nodes.get(id).printForwardingTable();
						nodes.get(id).printDistanceVector();
					}
					break;
				}

				for(DynamicPath d : dynamicPaths.keySet()){
					boolean isChanged = d.updateCost();
					if(isChanged){
						nodes.get(d.u).onCostChanged(d.v, d.cost);
						nodes.get(d.v).onCostChanged(d.u, d.cost);
						graph.getEdge(d.u+""+d.v).addAttribute("ui.label", d.cost);
					}
				}
			}




		} catch (IOException e) {
			e.printStackTrace();
		}
	}



}
