package flowstate;
import java.util.ArrayList;

public interface Storage {
    void saveData(ArrayList<Task> tasks);
    ArrayList<Task> loadData();
}
