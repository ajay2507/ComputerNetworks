import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Ajay
 *
 */

public class DVRRouter implements java.io.Serializable {

	/**
	 * 
	 */
	private String routerName;
	private transient BufferedReader reader;
	private String fileName;
	private static final long serialVersionUID = 1L;
	private ArrayList<Table> table = new ArrayList<Table>();
	private HashMap<String, Double> originals = new HashMap<String, Double>();
	private ArrayList<String> immediateNeighbors = new ArrayList<String>();
	
	
	
	
public synchronized void updateRouterTable(DVRRouter ownTable, DVRRouter receivedTable) {

		
		ArrayList<String> receivedNeighbors = new ArrayList<String>(receivedTable.getImmediateNeighbors());
		ArrayList<String> ownNeighbors = new ArrayList<String>(ownTable.getImmediateNeighbors());
		for(String neighbor : receivedNeighbors) {
			if(!ownNeighbors.contains(neighbor)) {
				Table rObj = new Table(ownTable.getRouterName(), neighbor, "-", 16.0);
				ownTable.table.add(rObj);
				ownTable.immediateNeighbors.add(neighbor);
			}
		}

		
		DVRRouter receivedModified = new DVRRouter(receivedTable);
		Double addThis = 0.0;
		for (Table entry : receivedTable.getTable()) {
			if(entry.getDestination().equals(ownTable.getRouterName())) {
				addThis = entry.getCost();
				break;
			}
		}

		for (Table entry : receivedModified.getTable()) {
			entry.setNextNode(entry.getSource());
			Double costOriginal = entry.getCost();
			entry.setCost(costOriginal+addThis);
		}

		
		for (Table entry : receivedModified.getTable()) {
			if(entry.getNextNode().equals(ownTable.getRouterName())) {
				entry.setCost(Double.MAX_VALUE);
			}
		}

		//compare receivedModified and own table and make updates
		for (Table entryInReceived : receivedModified.getTable()) {
			String destR = entryInReceived.getDestination();
			String hopR = entryInReceived.getNextNode();
			Double costR = entryInReceived.getCost();
			for (Table entryInOwn : ownTable.getTable()) {
				//for the same destination
				if(entryInOwn.getDestination().equals(entryInOwn.getSource())) {
					continue;
				}
				if((entryInOwn.getDestination().equals(destR))) {
					if(!entryInOwn.getNextNode().equals(hopR)) {
						if (entryInOwn.getCost() > costR) {
							entryInOwn.setCost(costR);
							entryInOwn.setNextNode(hopR);
						}
					}
					else {
						entryInOwn.setCost(costR);
						entryInOwn.setNextNode(hopR);
					}
				}
			}
		}
	}



	public String getRouterName() {
		return routerName;
	}

	public void setRouterName(String routerName) {
		this.routerName = routerName;
	}

	

	public DVRRouter() {

	}

	public DVRRouter(DVRRouter instance) {
		this.table = instance.table;
	}

	//construct table from file
	public void createRoutingTable(String fileName) throws IOException {
		
		String[] fileParts = fileName.split(Pattern.quote("."));
		String routerName = String.valueOf(fileParts[0].charAt(fileParts[0].length()-1));
		this.fileName = fileName;
		this.routerName = routerName;
		Table rTable = new Table(routerName, routerName, "-", 0.0);
		immediateNeighbors.add(routerName);
		table.add(rTable);
		originals.put(routerName, 0.0);
         FileReader file = new FileReader(new File(fileName));		
		reader = new BufferedReader(file);
		String line;
		while((line = reader.readLine())!=null) {
			String[] lineParts = line.split("\\s+");
			if(lineParts.length == 2) {
				String dest = lineParts[0];
				Double cost = Double.parseDouble(lineParts[1]);
				rTable = new Table(routerName, dest, dest, cost);
				immediateNeighbors.add(dest);
				table.add(rTable);
				originals.put(dest, cost);
			}
		}
	}

	
	public ArrayList<String> getImmediateNeighbors() {
		return immediateNeighbors;
	}

	public void setImmediateNeighbors(ArrayList<String> immediateNeighbors) {
		this.immediateNeighbors = immediateNeighbors;
	}

	public ArrayList<Table> getTable() {
		return table;
	}

	public void setTable(ArrayList<Table> table) {
		this.table = table;
	}

	// Method to print the table
	public synchronized void printTable(ArrayList<Table> t) {
		for (Table entry : t) {
			System.out.println("Shortest path "+entry.getSource()+"-"+entry.getDestination()+": the next hop is "+entry.getNextNode()+" and the cost is "+entry.getCost());
		}
	}

	//method to check link state change
	public synchronized void readCostChange() throws NumberFormatException, FileNotFoundException, IOException {
		//read the file again and save values in a new RoutingTable object
		DVRRouter newTable = new DVRRouter();
		Table rTable;
		//for changed routers
		ArrayList<String> changedRouters = new ArrayList<String>();
		String[] fileParts = this.fileName.split(Pattern.quote("."));
		String routerName = String.valueOf(fileParts[0].charAt(fileParts[0].length()-1));
		//entry for the same router
		rTable = new Table(routerName, routerName, "-", 0.0);
		newTable.table.add(rTable);
		FileReader file = new FileReader(new File(this.fileName));		
		reader = new BufferedReader(file);
		String line;
		while((line = reader.readLine())!=null) {
			String[] lineParts = line.split("\\s+");
			if(lineParts.length == 2) {
				String dest = lineParts[0];
				Double cost = Double.parseDouble(lineParts[1]);
				rTable = new Table(routerName, dest, dest, cost);
				newTable.table.add(rTable);
			}
		}

		
		for (Map.Entry<String, Double> entry : originals.entrySet()) {
			for (Table entryNew : newTable.getTable()) {
				if(entryNew.getDestination().equals(entry.getKey())) {
					if(Double.compare(entryNew.getCost(), entry.getValue()) != 0) {
						
						changedRouters.add(entry.getKey());
					}
				}
			}
		}

		
		for (Table entry : table) {
			for (Table entryNew : newTable.getTable()) {
				if(entryNew.getDestination().equals(entry.getDestination())) {
					if(changedRouters.contains(entryNew.getDestination())) {
						entry.setCost(entryNew.getCost());
						originals.put(entryNew.getDestination(), entryNew.getCost());
					}
				}
			}
		}
	}
}
