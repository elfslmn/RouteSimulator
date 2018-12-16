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

		boolean isMessageUpdates = false;
		Integer via = m.senderID;
		Integer costToSender = distanceTable.get(m.senderID).get(via);
		for(Integer dest: m.content.keySet()){
			if(!addItself && dest == id) continue; // path to itself
			if(distanceTable.keySet().contains(dest)){
				int oldcost = distanceTable.get(dest).get(via);
				int newcost = costToSender + m.content.get(dest);
				distanceTable.get(dest).put(via, newcost);
				if(newcost != oldcost){
					isMessageUpdates = true;
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
				isMessageUpdates = true;
			}
		}
		if(isMessageUpdates){
			isTableUpdated = true;
			System.out.println("Table is updated upon message. New distance table of node "+id);
			printDistanceTable();
		}
		else{
			System.out.println("Table is not updated upon message. Distance table is same above.");
		}
		System.out.println();
	}

	// If table is updated call receiveUpdate function of all neighbors
	public boolean sendUpdate(){
		if(isTableUpdated){
			System.out.println("\nNode " + id +" has updates. It will notify "+ neighbours.length +" neighbors...");
			Hashtable<Integer, Integer> distVector = getDistanceVector();
			for(Integer nei : neighbours){
				if(nei == this.id) continue; // Dont send update to itself
				System.out.println("Node "+nei+" is notified with distance vector of node "+id);
				printDistanceVector();
				updateNode(nei, new Message(id, nei, distVector));
			}
			isTableUpdated = false;
			return true;
		}
		else{
			return false;
		}
	}

	public Hashtable<Integer, Integer> getForwardingTable(){ // <destination, via>
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
		}
		System.out.println();
		return ft;
	}

	public void printForwardingTable(){
		Hashtable<Integer, Integer> ft = getForwardingTable();
		System.out.println("Forwarding table of node"+id);
		System.out.println("Dest\tVia");
		for(Integer dest : ft.keySet()){
			System.out.println(dest+"\t\t"+ft.get(dest));
		}
	}

	public Hashtable<Integer, Integer> getDistanceVector(){ // <destination, cost >
		Hashtable<Integer, Integer> distVector = new Hashtable<>();
		for(Integer dest : distanceTable.keySet()){
			int min = INF;
			Hashtable<Integer, Integer> row = distanceTable.get(dest);
			for( Integer via : row.keySet()){
				if(row.get(via) < min){
					min = row.get(via);
				}
			}
			distVector.put(dest, min);
		}
		return distVector;
	}

	public void printDistanceVector(){
		Hashtable<Integer, Integer> dv = getDistanceVector();
		System.out.println("Distance vector of node"+id);
		System.out.println("Dest\tCost");
		for(Integer dest : dv.keySet()){
			System.out.println(dest+"\t\t"+dv.get(dest));
		}
	}

	public void printDistanceTable(){
		if(distanceTable.size() == 0) {
			System.out.println("Distance table is empty");
			return;
		}

		System.out.print("D"+id+" ");
		for(int via : neighbours){
			if(via == id) continue;
			System.out.print(via+" ");
		}
		System.out.println();

		for(Integer dest : distanceTable.keySet()){
			System.out.print(dest+"| ");
			Hashtable mincost = distanceTable.get(dest);
			for(int via : neighbours){
				if(via == id) continue;
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

	private void updateNode(int neighbor, Message message){
		if(!linkCost.containsKey(neighbor)){
			String s = String.format("You(%d) cannot update this node(%d). Because it is not your neighbor",id, neighbor);
			System.out.println(s);
			return;
		}else{
			RouteSim.graph.get(neighbor).receiveUpdate(message);
		}
	}
}
