package scheduler;
import java.util.ArrayList;
import java.util.Random;

public class SchedulerGate extends SchedulerNode {
	public int options;
	public int required;
	private Random rand = new Random();

	static int numberOfGates = 0;
	{
		numberOfGates += 1;
	}
	
	public SchedulerGate(SchedulerNode node) throws Exception{
		if (node.gateinfo.isEmpty()) {
			throw new Exception("Tried to initialize a gate with a node that was not a gate");//Throw some error so we know this shouldn't have been happened
		}
		this.children = node.children;
		this.parents = node.parents;
		this.name = node.name;
		this.options = node.gateinfo.get(GateInfo.OPTIONS);
		this.required = node.gateinfo.get(GateInfo.REQUIRED);
	}
	
	public SchedulerGate(int options, int required, ArrayList<SchedulerNode> children) {
		this(options, required);
		this.name = "GATE " + Integer.toString(numberOfGates);
		if(children == null) {
			System.out.println("chilldren are null");
		}
		this.children = children;
	}

	public SchedulerGate(int options, int required) {
		this.name = "GATE " + Integer.toString(numberOfGates);
		this.options = options;
		this.required = required;
		this.children = new ArrayList<SchedulerNode>();
		this.parents  = new ArrayList<SchedulerNode>();
	}
	
	public ArrayList<Float> getPathLengths() {
		assert(selfcheck());
		ArrayList<Float> scores = new ArrayList<Float>();
		for (int i = 0; i < this.children.size(); ++i) {
			scores.add(this.children.get(i).getPathLength());
		}
		return scores;
	}
	
	public float getSinglePathLength() {
		return this.getBestChild(Integer.MAX_VALUE).getPathLength();
	}
	
	public ArrayList<Float> getCosts() {
		assert(selfcheck());
		ArrayList<Float> scores = new ArrayList<Float>();
		for (SchedulerNode node : this.children) {
			scores.add(node.getCost());
		}
		return scores;
	}
	
	public float getCost() {
		/*
		 * Returns the sum of the k smallest price scores
		 */
		assert(selfcheck());
		ArrayList<Float> scores = this.getCosts();
		float sum = 0;
		for (int i = 0; i < scores.size(); ++i) {
			
		}
		return sum;
	}
	
	public SchedulerNode getBestChild(int semesters_remaining) {
		assert(selfcheck());
		System.out.println("Gate name: " + this.name);
		System.out.println("Selecting best child from " + this.required + " required courses out of " + this.options + " options");
		ArrayList<Integer> choices = this.getChoices( semesters_remaining);
		int index = this.getLongestChoice( choices, 0, semesters_remaining);
		SchedulerNode next_node = this.getChild(index);
		return next_node;
	}
	
	public SchedulerNode getGoodChild(int semesters_remaining) {
		assert(selfcheck());
		ArrayList<Integer> choices = this.getChoices(semesters_remaining);
		int index = this.getLongestChoice(choices, 1, semesters_remaining);
		SchedulerNode next_node = this.getChild(index);
		return next_node;
	}
	
	public SchedulerNode getChild(int index) {
		return this.children.get(index);
	}
	
	protected ArrayList<Integer> getChoices(int semesters_remaining) {
		/*
		 * Get the k smallest prices that are feasible to complete, given the semesters left
		 * Returns size k list of ints representing indexes of the k smallest scores
		 */
		//TODO: base this on semesters remaining
		ArrayList<Float> lengths = this.getPathLengths();
		ArrayList<Float> costs = this.getCosts();
		ArrayList<Integer> choices = new ArrayList<>();
		for (int i = 0; i < this.required; ++i) {
			int min_idx = -1;
			float min_val = (float) Integer.MAX_VALUE;
			for (int j = 0; j < costs.size(); ++j) {
				if (costs.get(j) < min_val && lengths.get(j) <= semesters_remaining) {
					min_val = costs.get(j);
					min_idx = j;
				}
			}
			choices.add(min_idx);
			costs.set(min_idx, (float) Integer.MAX_VALUE);
		}
		return choices;
	}
	
	private int getLongestChoice( ArrayList<Integer> choices, int attempt, int semesters_remaining) {
		/*
		 * If attempt = 0, get the longest path value
		 * If attempt != 0, get a random path value unless semesters_remaining = longest_path, in which case you must take longest path
		 * Returns the best index in scores, given that the indexes are in choices
		 */
		ArrayList<Float> scores = this.getPathLengths();
		if (attempt==0) {
			float max_length = 0;
			int max_index = 0;
			for (int i = 0; i < choices.size(); ++i) {
				int choice_idx = choices.get(i);
				float choice_length = scores.get(choice_idx);
				if (choice_length > max_length) {
					max_length = choice_length;
					max_index = choice_idx;
				}
			}
			return max_index;
		} else {
			int actual_longest_choice = this.getLongestChoice(choices, 0, semesters_remaining);
			if (semesters_remaining == actual_longest_choice) {
				return actual_longest_choice;
			}
			int choice_idx = rand.nextInt(choices.size());
			return (int) choices.get(choice_idx);
		}
	}
	
	private boolean selfcheck() {
		/*
		 * Makes sure that all incoming edges have been attached.
		 * Otherwise, the gate is not ready for use
		 */
		return this.options == this.children.size();
	}
	
	public boolean equals(SchedulerNode node) {
		if (node.isGate()){
			if (node.children == this.children) {
				return true;
			}
		}
		return false;
	}
};
