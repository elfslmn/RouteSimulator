import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


public class RouteSim {
	public static HashMap<Integer, Node> graph = new HashMap<>();
	public static HashMap<DynamicPath, DynamicPath> dynamicPaths = new HashMap<>();

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
				graph.put(nodeid, new Node(nodeid, linkCost));
			}
			reader.close();

			for(int t=1 ; t < 1000; t++){
				System.out.println("\nTime "+t+" ------------------------------------------------------");
				boolean isConverged = true;
				for(Integer id : graph.keySet()){
					boolean isUpdated = graph.get(id).sendUpdate();
					System.out.println("Node "+id+" isUpdated="+isUpdated + " and updates are sent");
					isConverged = isConverged && (!isUpdated);
				}
				if(isConverged){
					System.out.println("System is converged at time "+t);
					break;
				}

				for(DynamicPath d : dynamicPaths.keySet()){
					boolean isChanged = d.updateCost();
					if(isChanged){
						graph.get(d.u).onCostChanged(d.v, d.cost);
						graph.get(d.v).onCostChanged(d.u, d.cost);
					}
				}
			}




		} catch (IOException e) {
			e.printStackTrace();
		}
	}



}
