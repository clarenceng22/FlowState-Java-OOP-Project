package flowstate;

public class WorkTask extends Task {
    private String projectName;

    public WorkTask(String title, String description, String dueDate, String priority, String projectName) {
        super(title, description, dueDate, priority);
        this.projectName = projectName;
    }

    @Override
    public void displayTaskDetails() {
        super.displayTaskDetails();
        System.out.println("Project: " + projectName);
    }
}
