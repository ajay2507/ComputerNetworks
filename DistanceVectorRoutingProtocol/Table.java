/**
 * @author Ajay
 *
 */

public class Table implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String source;
	private String destination;
	private String nextNode;
	private double cost;
	
	public Table(String source, String destination, String nextNode, Double cost) {
		this.source = source;
		this.destination = destination;
		this.nextNode = nextNode;
		this.cost = cost;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * @return the nextNode
	 */
	public String getNextNode() {
		return nextNode;
	}

	/**
	 * @param nextNode the nextNode to set
	 */
	public void setNextNode(String nextNode) {
		this.nextNode = nextNode;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	
	
}
