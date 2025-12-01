package flowstate;
import java.util.ArrayList;

public class TaskManager {
    private ArrayList<Task> tasks;
    private Storage storage;

    public TaskManager(Storage storage) {
        this.storage = storage;
        this.tasks = storage.loadData();
    }

    public void addTask(Task task) {
        tasks.add(task);
        System.out.println("Task added successfully!");
    }

    public void listTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
        } else {
            for (int i = 0; i < tasks.size(); i++) {
                System.out.println("\nTask #" + (i + 1));
                tasks.get(i).displayTaskDetails();
            }
        }
    }

    public void markTaskCompleted(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.get(index).setCompleted(true);
            System.out.println("Task marked as completed!");
        } else {
            System.out.println("Invalid task number.");
        }
    }

    public void deleteTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
            System.out.println("Task deleted successfully!");
        } else {
            System.out.println("Invalid task number.");
        }
    }
    
    public ArrayList<Task> getTasks() {
        return tasks;
    }
    
    public void updateTask(int index, Task task) {
        tasks.set(index, task);
    }

    public void saveTasks() {
        storage.saveData(tasks);
    }
}