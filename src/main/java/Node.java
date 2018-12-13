import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;

public class Node {
	public static final int INF = 999;

	public int id;
	public Hashtable<Integer, Integer> linkCost; //key: neighbor id, value: link cost to that neighbor
	public Hashtable<Integer, Hashtable<Integer, Integer>> distanceTable;

	public boolean isTableUpdated = false;
	private Integer[] neighbours;
	
	public Node(int nodeID, Hashtable<Integer, Integer> linkCost) {
		this.id = nodeID;
		this.linkCost = linkCost;

		distanceTable = new Hashtable<>();
		neighbours = new Integer[linkCost.size()];

		int i = 0;
		for(Integer dest : linkCost.keySet()){
			neighbours[i] = dest;
			Hashtable<Integer, Integer>  mincost = new Hashtable<>(); // min cost to dest via this neighbor
			for(Integer via : linkCost.keySet()){
				mincost.put(via, INF);
			}
			mincost.put(dest, linkCost.get(dest));

			distanceTable.put(dest, mincost);
			i++;
		}

		Arrays.sort(neighbours, Collections.reverseOrder());
		isTableUpdated = true;
	}

	// Updates its node's distance table according to message.
	public void receiveUpdate(Message m){
		System.out.println("receiveUpdate() sender:"+m.senderID+" receiver:"+m.receiverID);
		if(id != m.receiverID){
			System.out.println("Something is wrong in message !!!");
			return;
		}
		System.out.println("Old distance table of node "+id);
		printDistanceTable();

		Integer via = m.senderID;
		Integer costToSender = distanceTable.get(m.senderID).get(via);
		for(Integer dest: m.content.keySet()){
			if(dest == id) continue; // path to itself // TODO add distance table to itself also.
			if(distanceTable.keySet().contains(dest)){
				int oldcost = distanceTable.get(dest).get(via);
				int newcost = costToSender + m.content.get(dest);
				if(newcost < oldcost){
					distanceTable.get(dest).put(via, newcost);
					isTableUpdated = true;
				}
			}
			// Receiver node(this) did not know the destination node at all. Add this new node to distance table
			else{
				Hashtable<Integer, Integer>  row = new Hashtable<>();
				for(int nei: neighbours){
					row.put(nei, INF);
				}
				row.put(via, m.content.get(dest)+costToSender); // <via, cost>
				distanceTable.put(dest, row);
				isTableUpdated = true;
			}
		}

		System.out.println("New distance table of node "+id);
		printDistanceTable();
	}

	// If table is updated call receiveUpdate function of all neighbors
	public boolean sendUpdate(){
		if(isTableUpdated){
			System.out.println("Node" + id +" has updates to send to its "+ neighbours.length +" neighbors...");
			Hashtable<Integer, Integer> distVector = getDistanceVector();
			for(Integer nei : neighbours){
				RouteSim.graph.get(nei).receiveUpdate(new Message(id, nei, distVector));
			}
			isTableUpdated = false;
			return true;
		}
		else{
			return false;
		}
	}

	public Hashtable<Integer, Integer> getForwardingTable(){ // <destination, via>

		return null;
	}

	public Hashtable<Integer, Integer> getDistanceVector(){ // <destination, cost >
		System.out.println("Distance vector of node "+id);
		Hashtable<Integer, Integer> distVector = new Hashtable<>();
		for(Integer dest : distanceTable.keySet()){
			int min = 999;
			int minid = -1;
			Hashtable<Integer, Integer> row = distanceTable.get(dest);
			for( Integer via : row.keySet()){
				if(row.get(via) < min){
					min = row.get(via);
					minid = via;
				}
			}
			distVector.put(dest, min);
			System.out.println("From "+id +" to "+dest +" min cost is "+min+" via node "+minid);
		}
		return distVector;
	}

	public void printDistanceTable(){
		if(distanceTable.size() == 0) {
			System.out.println("Distance table is empty");
			return;
		}

		System.out.print("   ");
		for(int via : neighbours){
			System.out.print(via+" ");
		}
		System.out.println();

		for(Integer dest : distanceTable.keySet()){
			System.out.print(dest+"| ");
			Hashtable mincost = distanceTable.get(dest);
			for(int via : neighbours){
				int cost = (int)mincost.get(via);
				if(cost == INF ) System.out.print("- ");
				else System.out.print(cost + " ");
			}
			System.out.println();
		}
	}


}
