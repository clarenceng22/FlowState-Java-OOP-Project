package flowstate;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.Timer;

public class FlowStateGUI extends JFrame {

    private TaskManager manager;
    private DefaultListModel<String> listModel;
    private JList<String> taskList;

    private JScrollPane scrollPane;
    private JPanel buttonPanel;
    private JPanel bottomContainer;
    private JPanel topBar;
    private JLabel toastLabel;
    private JLabel titleLabel;
    private JButton darkModeButton;

    private JButton addButton;
    private JButton editButton;
    private JButton completeButton;
    private JButton deleteButton;
    private JButton saveButton;

    private Timer toastTimer;
    private boolean darkMode = false;

    public FlowStateGUI() {

        Storage storage = new FileStorage();
        manager = new TaskManager(storage);

        setTitle("FlowState");
        setSize(750, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        topBar = new JPanel(new BorderLayout());
        titleLabel = new JLabel("FlowState");
        titleLabel.setFont(nunitoBold(22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        darkModeButton = new JButton("Dark Mode");
        darkModeButton.setFont(nunitoBold(12));
        darkModeButton.addActionListener(e -> {
            darkMode = !darkMode;
            applyTheme();
            darkModeButton.setText(darkMode ? "Light Mode" : "Dark Mode");
        });

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightTop.add(darkModeButton);
        rightTop.setOpaque(false);

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(rightTop, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setFont(nunitoBold(18));
        taskList.setFixedCellHeight(35);
        taskList.setCellRenderer(new TaskRenderer());

        scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240), 2, true));

        add(scrollPane, BorderLayout.CENTER);

        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        completeButton = new JButton("Complete");
        deleteButton = new JButton("Delete");
        saveButton = new JButton("Save");

        addButton.addActionListener(e -> addTaskDialog());
        editButton.addActionListener(e -> editSelectedTask());
        completeButton.addActionListener(e -> completeSelectedTask());
        deleteButton.addActionListener(e -> deleteSelectedTask());
        saveButton.addActionListener(e -> {
            manager.saveTasks();
            showToast("Tasks saved");
        });

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);

        toastLabel = new JLabel(" ");
        toastLabel.setHorizontalAlignment(SwingConstants.CENTER);
        toastLabel.setFont(nunitoBold(12));

        bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(toastLabel, BorderLayout.NORTH);
        bottomContainer.add(buttonPanel, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);

        applyTheme();
        refreshTaskList();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                manager.saveTasks();
                dispose();
            }
        });

        taskList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openTaskDetails();
            }
        });
    }

    private Font nunitoBold(int size) {
        try {
            Font font = Font.createFont(
                    Font.TRUETYPE_FONT,
                    new java.io.File("fonts/Nunito-VariableFont_wght.ttf")
            );
            return font.deriveFont(Font.BOLD, size);
        } catch (Exception e) {
            return new Font("Arial", Font.BOLD, size);
        }
    }

    private void styleButton(JButton button, Color bg, Color border, Color text) {
        button.setFont(nunitoBold(14));
        button.setBackground(bg);
        button.setForeground(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 2),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        button.setOpaque(true);
    }

    private void applyTheme() {
        Color bgMain;
        Color bgList;
        Color selList;
        Color btnBg;
        Color btnBorder;
        Color textColor;
        Color toastColor;

        if (!darkMode) {
            bgMain = new Color(245, 250, 255);
            bgList = Color.white;
            selList = new Color(200, 225, 255);
            btnBg = new Color(180, 210, 255);
            btnBorder = new Color(150, 180, 230);
            textColor = Color.black;
            toastColor = new Color(70, 90, 130);
        } else {
            bgMain = new Color(30, 34, 40);
            bgList = new Color(35, 39, 45);
            selList = new Color(70, 90, 140);
            btnBg = new Color(70, 100, 150);
            btnBorder = new Color(90, 120, 170);
            textColor = new Color(235, 235, 245);
            toastColor = new Color(220, 230, 245);
        }

        getContentPane().setBackground(bgMain);
        topBar.setBackground(bgMain);
        buttonPanel.setBackground(bgMain);
        bottomContainer.setBackground(bgMain);

        titleLabel.setForeground(textColor);

        scrollPane.getViewport().setBackground(bgList);
        taskList.setBackground(bgList);
        taskList.setSelectionBackground(selList);
        taskList.setForeground(textColor);

        styleButton(addButton, btnBg, btnBorder, textColor);
        styleButton(editButton, btnBg, btnBorder, textColor);
        styleButton(completeButton, btnBg, btnBorder, textColor);
        styleButton(deleteButton, btnBg, btnBorder, textColor);
        styleButton(saveButton, btnBg, btnBorder, textColor);
        styleButton(darkModeButton, btnBg, btnBorder, textColor);

        toastLabel.setForeground(toastColor);

        refreshTaskList();
    }

    private void showToast(String message) {
        if (toastTimer != null && toastTimer.isRunning()) {
            toastTimer.stop();
        }
        toastLabel.setText(message);
        toastTimer = new Timer(2000, e -> toastLabel.setText(" "));
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private void refreshTaskList() {
        listModel.clear();
        ArrayList<Task> tasks = manager.getTasks();

        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter prettyFormat = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        LocalDate today = LocalDate.now();

        for (Task t : tasks) {
            String formattedDate = "";
            LocalDate dueDate = null;

            try {
                if (t.getDueDate() != null && !t.getDueDate().trim().isEmpty()) {
                    dueDate = LocalDate.parse(t.getDueDate(), inputFormat);
                    formattedDate = dueDate.format(prettyFormat);
                }
            } catch (Exception e) {
                formattedDate = t.getDueDate();
            }

            String priorityColor = "#77dd77";
            if (t.getPriority().equalsIgnoreCase("High")) priorityColor = "#ff6b6b";
            else if (t.getPriority().equalsIgnoreCase("Medium")) priorityColor = "#ffcc66";

            String baseColor;
            String dueColor;

            if (t.isCompleted()) {
                baseColor = darkMode ? "#aaaaaa" : "#777777";
                dueColor = baseColor;
            } else if (dueDate != null && dueDate.isBefore(today)) {
                baseColor = "#ff6b6b";
                dueColor = "#ff6b6b";
            } else if (dueDate != null && dueDate.equals(today)) {
                baseColor = "#ff9933";
                dueColor = "#ff9933";
            } else {
                baseColor = darkMode ? "#f5f5f5" : "#000000";
                dueColor = darkMode ? "#e0e0e0" : "#444444";
            }

            String left = "<span style='color:" + baseColor + ";'><b>" +
                    t.getTitle() + "</b> </span>" +
                    "<span style='color:" + priorityColor + ";'>[" + t.getPriority() + "]</span>";

            String right = "<span style='color:" + dueColor + ";'>Due: " + formattedDate + "</span>";

            listModel.addElement(left + ":::" + right);
        }
    }

    private void openTaskDetails() {
        int index = taskList.getSelectedIndex();
        if (index < 0) return;

        Task task = manager.getTasks().get(index);

        String status = task.isCompleted() ? "Completed" : "Not Completed";

        String message =
                "Title: " + task.getTitle() +
                "\nPriority: " + task.getPriority() +
                "\nDue: " + task.getDueDate() +
                "\nStatus: " + status +
                "\n\nDescription:\n" + task.getDescription();

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(nunitoBold(14));
        textArea.setBackground(new Color(250,250,255));
        textArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(textArea),
                "Task Details",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private String showDatePickerDialog() {
        String[] months = {
                "01 - January", "02 - February", "03 - March", "04 - April",
                "05 - May", "06 - June", "07 - July", "08 - August",
                "09 - September", "10 - October", "11 - November", "12 - December"
        };

        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.format("%02d", i + 1);

        String[] years = new String[6];
        int yearNow = LocalDate.now().getYear();
        for (int i = 0; i < 6; i++) years[i] = String.valueOf(yearNow + i);

        JComboBox<String> monthBox = new JComboBox<>(months);
        JComboBox<String> dayBox = new JComboBox<>(days);
        JComboBox<String> yearBox = new JComboBox<>(years);

        JPanel panel = new JPanel(new GridLayout(2, 3, 5, 5));
        panel.add(new JLabel("Month:"));
        panel.add(new JLabel("Day:"));
        panel.add(new JLabel("Year:"));
        panel.add(monthBox);
        panel.add(dayBox);
        panel.add(yearBox);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Select due date",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return null;

        String month = ((String) monthBox.getSelectedItem()).substring(0, 2);
        String day = (String) dayBox.getSelectedItem();
        String year = (String) yearBox.getSelectedItem();

        return month + "/" + day + "/" + year;
    }

    private void addTaskDialog() {
        String title = JOptionPane.showInputDialog(this, "Task title:");
        if (title == null || title.trim().isEmpty()) return;

        String desc = JOptionPane.showInputDialog(this, "Description:");
        if (desc == null) return;

        String due = showDatePickerDialog();
        if (due == null) return;

        String[] priorities = {"High", "Medium", "Low"};
        String priority = (String) JOptionPane.showInputDialog(
                this, "Priority:", "Priority",
                JOptionPane.PLAIN_MESSAGE, null,
                priorities, "Medium"
        );
        if (priority == null) return;

        Task task = new Task(title, desc, due, priority);
        manager.addTask(task);
        manager.saveTasks();
        refreshTaskList();
        showToast("Task added");
    }

    private void editSelectedTask() {
        int index = taskList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Select a task first.");
            return;
        }

        Task oldTask = manager.getTasks().get(index);

        String newTitle = JOptionPane.showInputDialog(this, "Edit title:", oldTask.getTitle());
        if (newTitle == null || newTitle.trim().isEmpty()) newTitle = oldTask.getTitle();

        String newDesc = JOptionPane.showInputDialog(this, "Edit description:", oldTask.getDescription());
        if (newDesc == null) newDesc = oldTask.getDescription();

        String newDue = showDatePickerDialog();
        if (newDue == null) newDue = oldTask.getDueDate();

        String[] priorities = {"High", "Medium", "Low"};
        String newPriority = (String) JOptionPane.showInputDialog(
                this, "Edit priority:", "Priority",
                JOptionPane.PLAIN_MESSAGE, null,
                priorities, oldTask.getPriority()
        );
        if (newPriority == null) newPriority = oldTask.getPriority();

        Task updated = new Task(newTitle, newDesc, newDue, newPriority);
        if (oldTask.isCompleted()) {
            updated.setCompleted(true);
        }

        manager.updateTask(index, updated);

        manager.saveTasks();
        refreshTaskList();
        showToast("Task updated");
    }

    private void completeSelectedTask() {
        int index = taskList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Select a task first.");
            return;
        }
        manager.markTaskCompleted(index);
        manager.saveTasks();
        refreshTaskList();
        showToast("Task completed");
    }

    private void deleteSelectedTask() {
        int index = taskList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Select a task first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, "Delete this task?", "Confirm",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        manager.deleteTask(index);
        manager.saveTasks();
        refreshTaskList();
        showToast("Task deleted");
    }

    private class TaskRenderer extends JPanel implements ListCellRenderer<String> {

        private JLabel leftLabel;
        private JLabel rightLabel;

        public TaskRenderer() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

            leftLabel = new JLabel();
            leftLabel.setFont(nunitoBold(16));

            rightLabel = new JLabel();
            rightLabel.setFont(nunitoBold(13));
            rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            add(leftLabel, BorderLayout.WEST);
            add(rightLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list,
                String value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            String[] parts = value.split(":::");
            String left = parts[0];
            String right = parts.length > 1 ? parts[1] : "";

            leftLabel.setText("<html>" + left + "</html>");
            rightLabel.setText("<html>" + right + "</html>");

            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }

            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlowStateGUI gui = new FlowStateGUI();
            gui.setVisible(true);
        });
    }
}