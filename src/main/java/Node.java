import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;

public class Node {
	public static final int INF = 999;

	public int id;
	public Hashtable<Integer, Integer> linkCost; //key: neighbor id, value: link cost to that neighbor
	public Hashtable<Integer, Hashtable<Integer, Integer>> distanceTable; // <dest, <via,cost>>

	public boolean isTableUpdated = false;
	private Integer[] neighbours; // contains node itself, too
	boolean addItself = true;
	
	public Node(int nodeID, Hashtable<Integer, Integer> linkCost) {
		this.id = nodeID;
		this.linkCost = linkCost;

		if(addItself) linkCost.put(id,0); // Node path to itself.

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
			if(!addItself && dest == id) continue; // path to itself
			if(distanceTable.keySet().contains(dest)){
				int oldcost = distanceTable.get(dest).get(via);
				int newcost = costToSender + m.content.get(dest);
				distanceTable.get(dest).put(via, newcost);
				if(newcost != oldcost){
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
				if(nei == this.id) continue; // Dont send update to itself
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
		System.out.println("Forwarding table of node "+id);
		Hashtable<Integer, Integer> ft = new Hashtable<>();

		for(Integer dest : distanceTable.keySet()){
			int min = INF;
			int minid = -1;
			Hashtable<Integer, Integer> row = distanceTable.get(dest);
			for( Integer via : row.keySet()){
				if(row.get(via) < min){
					min = row.get(via);
					minid = via;
				}
			}
			ft.put(dest, minid);
			System.out.println("To arrive node " + dest + " go to node "+ minid+" direction.");
		}
		System.out.println();
		return ft;
	}

	public Hashtable<Integer, Integer> getDistanceVector(){ // <destination, cost >
		System.out.println("Distance vector of node "+id);
		Hashtable<Integer, Integer> distVector = new Hashtable<>();
		for(Integer dest : distanceTable.keySet()){
			int min = INF;
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

		System.out.print("D"+id+" ");
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

	public void onCostChanged(int neighbor, int newCost){
		System.out.println("OnCostChanged(neignbor: "+ neighbor+" newCost: "+ newCost );
		System.out.println("Old distance table of node "+id);
		printDistanceTable();
		Hashtable<Integer, Integer> ht = distanceTable.get(neighbor); // ht : <via,cost>
		int oldCost = ht.get(neighbor);
		int diff = newCost - oldCost;

		/*Hashtable<Integer, Integer> ft = getForwardingTable(); // ft: <destination, via>
		for(Integer dest : ft.keySet()){
			if(ft.get(dest) == neighbor){ // via neighbor
				Hashtable<Integer, Integer> row = distanceTable.get(dest); // row : <via,cost>
				int oldc = row.get(neighbor);
				row.put(neighbor, oldc + diff);
				distanceTable.put(dest, row);
			}
		}*/

		for(Integer dest : distanceTable.keySet()){
			Hashtable<Integer, Integer> row = distanceTable.get(dest); // row : <via,cost>
			int oldc = row.get(neighbor);
			row.put(neighbor, oldc + diff);
			distanceTable.put(dest, row);
		}
		System.out.println("New distance table of node "+id);
		printDistanceTable();

		isTableUpdated = true;
	}
}
