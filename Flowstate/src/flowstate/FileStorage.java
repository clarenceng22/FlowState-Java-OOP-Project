package flowstate;
import java.io.*;
import java.util.*;

public class FileStorage implements Storage {
    private static final String FILE_NAME = "tasks.txt";

    @Override
    public void saveData(ArrayList<Task> tasks) {
        // This method is kept for backward compatibility but not used
        // Use saveDataByTabs instead
    }

    public void saveDataByTabs(Map<String, ArrayList<Task>> tabTasks) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, ArrayList<Task>> entry : tabTasks.entrySet()) {
                String tabName = entry.getKey();
                ArrayList<Task> tasks = entry.getValue();
                for (Task task : tasks) {
                    String safeDue = task.getDueDate() == null ? "" : task.getDueDate();
                    writer.println(tabName + "|" + task.getTitle() + "," + task.getDescription() + "," +
                                   safeDue + "," + task.getPriority() + "," +
                                   task.isCompleted());
                }
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

    public Map<String, ArrayList<Task>> loadDataByTabs() {
        Map<String, ArrayList<Task>> tabTasks = new LinkedHashMap<>();
        tabTasks.put("Personal", new ArrayList<>());
        tabTasks.put("Work", new ArrayList<>());
        tabTasks.put("General", new ArrayList<>());

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("|")) {
                    String[] parts = line.split("\\|", 2);
                    String tabName = parts[0];
                    String[] data = parts[1].split(",");
                    
                    if (data.length == 5) {
                        Task task = new Task(data[0], data[1], data[2], data[3]);
                        task.setCompleted(Boolean.parseBoolean(data[4]));
                        
                        if (!tabTasks.containsKey(tabName)) {
                            tabTasks.put(tabName, new ArrayList<>());
                        }
                        tabTasks.get(tabName).add(task);
                    }
                }
            }
            System.out.println("Tasks loaded successfully by tabs!");
        } catch (IOException e) {
            System.out.println("No existing tasks found, starting fresh.");
        }
        return tabTasks;
    }
}