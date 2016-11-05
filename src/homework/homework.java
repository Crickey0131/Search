package homework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class homework {
	
	private static PrintWriter out;

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		// parsing input
		Map<String, Node> createdNodes = new HashMap<>();
		String algo = "";
		String startState = "";
		String goalState = "";
		int numTrafficLines = 0;
		int numSundayLines = 0;
		// Straight Line Distance
		Map<Node, Integer> sld = new HashMap<>();
		
		File inputFile = new File("./input.txt");
		Scanner in = new Scanner(inputFile);
		
		algo = in.nextLine();
		startState = in.nextLine();
		goalState = in.nextLine();
		numTrafficLines = in.nextInt();
		
		for(int i = 0; i < numTrafficLines; i++) {
			in.nextLine();
			
			String fromState = in.next();
			Node fromNode;
			String toState = in.next();
			Node toNode;
			int time = in.nextInt();
			
			// we set the toState Node as child of fromState Node
			if(!createdNodes.containsKey(toState)) {
				toNode = new Node(toState, new HashMap<Node, Integer>());
				createdNodes.put(toState, toNode);
			} else {
				toNode = createdNodes.get(toState);
			}
			
			if(!createdNodes.containsKey(fromState)) {
				fromNode = new Node(fromState, new HashMap<Node, Integer>());
				createdNodes.put(fromState, fromNode);				
			} else {
				fromNode = createdNodes.get(fromState);
			}
			
			fromNode.getChildren().put(toNode, time);
			fromNode.getOrder().add(toState);// to guarantee the order
		}
		
		
		numSundayLines = in.nextInt();
		
		for(int i = 0; i < numSundayLines; i++) {
			in.nextLine();
			String state = in.next();
			int time = in.nextInt();
			sld.put(createdNodes.get(state), time);	
		}	
		

		// output the result
		if(algo.equals("BFS")) {
			bfs(startState, goalState, createdNodes);
		} else if(algo.equals("DFS")) {
			dfs(startState, goalState, createdNodes);
		} else if(algo.equals("UCS")) {
			ucs(startState, goalState, createdNodes);
		} else if(algo.equals("A*")) {
			as(startState, goalState, sld, createdNodes);
		}
		in.close();
	}
	
	// 1 BFS: print the path
	private static void bfs(String start, String goal, Map<String, Node> createdNodes) throws FileNotFoundException {

		out = new PrintWriter("./output.txt");		
		if(start.equals(goal)) {
			out.println(goal + " 0");
			out.close();
			return;
		}
		
		Queue<Node> frontier = new LinkedList<>();
		Node node = createdNodes.get(start);
		frontier.add(node);
		Set<Node> explored = new HashSet<>();
		
		while(!frontier.isEmpty()) {
			
			Node n = frontier.poll();
			explored.add(n);

			Queue<String> order = n.getOrder();
			while(!order.isEmpty()) {
				String s = order.poll();
				Node child = createdNodes.get(s);
				
				if(!frontier.contains(child) && !explored.contains(child)) {
					
					child.setParent(n);
					child.setSteps(n.getSteps()+1);
					
					if(child.getState().equals(goal)) {// find the destination!!
						
						Stack<Node> stack = new Stack<>(); //path
						while(child != null) {
							stack.push(child);
							child = child.getParent();
						}

						while(!stack.empty()) {
							Node nd = stack.pop();
							out.println(nd.getState() + " " + nd.getSteps());
						}					
						out.close();
						return;
					}					
				}
				frontier.add(child); //each node in the frontier has a parent				
			}
		}
	}
	
	
	// 2 DFS: print the path
	private static void dfs(String start, String goal, Map<String, Node> createdNodes) throws FileNotFoundException {
		
		out = new PrintWriter("./output.txt");
		
		if(start.equals(goal)) {
			out.println(goal + " 0");
			out.close();
			return;
		}		
		
		Stack<Node> stack = new Stack<>();// path
		Stack<Node> frontier = new Stack<>();
		frontier.add(createdNodes.get(start));
		Set<Node> explored = new HashSet<>();
		
		while(!frontier.isEmpty()) {
			
			Node n = frontier.pop();
			explored.add(n);
			
			Stack<Node> tmp = new Stack<>(); //reorder
			
			Queue<String> order = n.getOrder();
			while(!order.isEmpty()) {
				
				String s = order.poll();
				Node child = createdNodes.get(s);
				
				if(!frontier.contains(child) && !explored.contains(child)) {// if child not met before...
					child.setParent(n);
					child.setSteps(n.getSteps()+1);
					
					if(child.getState().equals(goal)) {// find the destination!!
						
						//push the path to the stack
						while(child != null) {
							stack.push(child);
							child = child.getParent();
						}
						
						while(!stack.empty()) {// print the path
							Node nd = stack.pop();
							out.println(nd.getState() + " " + nd.getSteps());
						}		
						out.close();
						return;
					}
					
					tmp.push(child);
				}
			}
			while(!tmp.empty()) {
				frontier.push(tmp.pop());
			}
		}
	}	
	
	// 3 Uniform-cost search
	private static void ucs(String start, String goal, Map<String, Node> createdNodes) throws FileNotFoundException {

		out = new PrintWriter("./output.txt");
		if(start.equals(goal)) {
			out.println(goal + " 0");
			out.close();
			return;
		}
		
		PriorityQueue<Node> frontier = new PriorityQueue<>(); // queue order by path cost..
		Node node = createdNodes.get(start);
		node.setActualPathCost(0);
		frontier.add(node);
		
		Set<Node> explored = new HashSet<>();
		
		while(!frontier.isEmpty()) {
			
			Node n = frontier.poll(); //expand
			
			if(n.getState().equals(goal)) {// find the destination!!
				
				printPath(n, out);
			}
			
			explored.add(n);
			
			Queue<String> order = n.getOrder();// expand...I need a way to ensure the way to get the child
			while(!order.isEmpty()) {
				String s = order.poll();
				Node child = createdNodes.get(s);
				if(!frontier.contains(child) && !explored.contains(child)) {
					
					child.setParent(n);
					child.setActualPathCost(n.getActualPathCost() + n.getChildren().get(child));
					frontier.add(child); //each node in the frontier has a parent
				} else if(frontier.contains(child) && child.getActualPathCost() > (n.getActualPathCost() + n.getChildren().get(child))) {
					
					child.setParent(n);
					child.setActualPathCost(n.getActualPathCost() + n.getChildren().get(child));
				}				
			}
		}
	}	
	
	// 4 A* Search
	private static void as(String start, String goal, Map<Node, Integer> sld, Map<String, Node> createdNodes) throws FileNotFoundException {

		out = new PrintWriter("./output.txt");
		
		if(start.equals(goal)) {
			out.println(goal + " 0");
			out.close();
			return;
		}		
		
		PriorityQueue<Node> frontier = new PriorityQueue<>(); // queue order by pathCost..
		Node node = createdNodes.get(start);
		node.setPathCost(0 + sld.get(node));
		node.setActualPathCost(0);
		frontier.add(node);
		
		while(!frontier.isEmpty()) {
			
			Node n = frontier.poll();
			
			// find the destination!!
			if(n.getState().equals(goal) && (frontier.peek() == null || n.getActualPathCost() <= frontier.peek().getActualPathCost())) {
				
				printPath(n, out);
			}
			for(Node child : n.getChildren().keySet()) { //for revisit
				
				if(child.getActualPathCost() > (n.getActualPathCost() + n.getChildren().get(child))) {
					
					child.setParent(n);
					child.setActualPathCost(n.getActualPathCost() + n.getChildren().get(child));
					child.setPathCost(n.getActualPathCost() + n.getChildren().get(child) + sld.get(child));
				}
				frontier.add(child); //each node in the frontier has a parent
			}
		}		
	}
	
	private static void printPath(Node n, PrintWriter out) {
		Stack<Node> stack = new Stack<>();// path
		while(n != null) {//push the path to the stack
			stack.push(n);
			n = n.getParent();
		}
		while(!stack.empty()) {// print the path
			Node nd = stack.pop();
			out.println(nd.getState() + " " + nd.getActualPathCost());
		}		
		out.close();
		return;
	}
	
}



class Node implements Comparable<Node>{
	
	private String state;
	private Map<Node, Integer> children;
	private Node parent;
	private int steps;	// for BFS and DFS
	private int pathCost; //distance from starting point
	private int actualPathCost; // for A* Search
	private Queue<String> order;
	
	public Node(String name, Map<Node, Integer> map) {
		state = name;
		children = map;
		parent = null;
		steps = 0;
		pathCost = Integer.MAX_VALUE;
		actualPathCost = Integer.MAX_VALUE;
		order = new LinkedList<>();
	}
	public void setParent(Node parent) {
		this.parent = parent;
	}
	public Node getParent() {
		return parent;
	}
	
	public String getState() {
		return state;
	}
	public Map<Node, Integer> getChildren() {
		return children;
	}
	
	public Queue<String> getOrder() {
		return order;//FIXIT: should return the copy...
	}
	
	public void setSteps(int steps) {
		this.steps = steps;
	}
	public int getSteps() {
		return steps;
	}
	
	public void setPathCost(int pathCost) {
		this.pathCost = pathCost;
	}
	public int getPathCost() {
		return pathCost;
	}
	
	// actual path cost
	public void setActualPathCost(int pathCost) {
		actualPathCost = pathCost;
	}
	public int getActualPathCost() {
		return actualPathCost;
	}
	
	// implements the comparable interface
	public int compareTo(Node other) {
		// i'm small..
		return pathCost - other.pathCost;
	}	
}