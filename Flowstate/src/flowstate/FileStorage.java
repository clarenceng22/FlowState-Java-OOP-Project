package flowstate;
import java.io.*;
import java.util.*;

public class FileStorage implements Storage {
    private static final String FILE_NAME = "tasks.txt";

    @Override
    public void saveData(ArrayList<Task> tasks) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Task task : tasks) {
                writer.println(task.getTitle() + "," + task.getDescription() + "," +
                               task.getDueDate() + "," + task.getPriority() + "," +
                               task.isCompleted());
            }
            System.out.println("Tasks saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    @Override
    public ArrayList<Task> loadData() {
        ArrayList<Task> tasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5) {
                    Task task = new Task(data[0], data[1], data[2], data[3]);
                    task.setCompleted(Boolean.parseBoolean(data[4]));
                    tasks.add(task);
                }
            }
            System.out.println("Tasks loaded successfully!");
        } catch (IOException e) {
            System.out.println("No existing tasks found, starting fresh.");
        }
        return tasks;
    }
}
