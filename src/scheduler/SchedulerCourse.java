package scheduler;
import framework.Course;

public class SchedulerCourse extends SchedulerNode {
	protected int available, taken, scheduled, added, dropped;
	
	public SchedulerCourse(SchedulerNode node) {
		SchedulerNode
		this.name = node.name;
		this.available = node.courseinfo.get(CourseInfo.AVAILABILITY);
		this.taken = node.courseinfo.get(CourseInfo.TAKEN);
		this.scheduled = node.courseinfo.get(CourseInfo.SCHEDULED);
		this.added = node.courseinfo.get(CourseInfo.ADDED);
		this.dropped = node.courseinfo.get(CourseInfo.DROPPED);
		this.children = node.children;
		this.parents = node.parents;
	}
	
	public SchedulerCourse(Course course) {
		this.name = course.name;
	}
	
	public boolean available() { return this.available == 1;}
	public float getPathScore() {
		if (isNull()) {
			return 0;
		} else if (isGate()) {
			return 1 + ((SchedulerGate) getChild()).getSinglePathScore();
		} else {
			return 1 + ((SchedulerCourse) getChild()).getPathScore();
		}
		
	}
	public float getPriceScore() {
		if (isNull()) {
			return 0;
		}else if(isGate()) {
			return 1 + ((SchedulerGate) getChild()).getSinglePriceScore();
		} else {
			return 1 + ((SchedulerCourse)getChild()).getPriceScore();
		}
	}
	public SchedulerNode getChild() {
		return this.children.get(0);	//Only one child because this is a course
	}
}
