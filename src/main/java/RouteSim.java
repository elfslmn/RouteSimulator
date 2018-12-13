import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


public class RouteSim {
	public static HashMap<Integer, Node> graph = new HashMap<>();

	public static void main(String[] args) {

		try {
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
						//System.out.println (s);
						arr.add(Integer.parseInt(s.trim()));
					}
				}
				Integer nodeid = arr.get(0);
				Hashtable<Integer, Integer> linkCost = new Hashtable<>();
				for(int i=1; i<arr.size(); i+=2){
					linkCost.put(arr.get(i), arr.get(i+1)); // neighbor , cost
				}
				graph.put(nodeid, new Node(nodeid, linkCost));
			}
			reader.close();

			for(int t=1 ; t < 1000; t++){
				boolean isConverged = true;
				for(Integer id : graph.keySet()){
					//System.out.println("Distance table of node "+ id);
					//graph.get(id).printDistanceTable();

					boolean isUpdated = graph.get(id).sendUpdate();
					System.out.println("Node "+id+" isUpdated="+isUpdated + " and updates are sent\n");
					isConverged = isConverged && (!isUpdated);
				}
				if(isConverged){
					System.out.println("System is converged at time "+t);
					break;
				}
			}




		} catch (IOException e) {
			e.printStackTrace();
		}
	}



}
