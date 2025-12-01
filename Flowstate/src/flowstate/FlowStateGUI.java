package flowstate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;

public class FlowStateGUI extends JFrame {

    private TaskManager manager;
    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;
    
    // Tab management
    private Map<String, ArrayList<Task>> tabTasks;
    private String currentTab = "General";
    private JPanel tabPanel;
    private Map<String, JButton> tabButtons;

    // UI Components
    private JScrollPane scrollPane;
    private JPanel mainContainer;
    private JPanel leftSidebar;
    private JPanel rightPanel;
    private JLabel toastLabel;
    private JLabel titleLabel;
    private JButton addTabButton;

    private Timer toastTimer;
    
    // Right panel components
    private JLabel selectedTaskTitle;
    private JTextField editTitleField;
    private JTextArea editDescriptionArea;
    private JLabel editDateLabel;
    private JComboBox<String> priorityBox;
    private JCheckBox completedCheckbox;
    private Task currentSelectedTask;
    private int currentSelectedIndex = -1;
    private JPanel calendarPanelContainer;
    private boolean calendarVisible = false;
    private boolean completedTasksVisible = true;

    public FlowStateGUI() {
        Storage storage = new FileStorage();
        manager = new TaskManager(storage);

        // Initialize tab system
        tabTasks = new HashMap<>();
        tabButtons = new HashMap<>();
        initializeTabs();

        setTitle("FlowState - Task Management");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);

        mainContainer = new JPanel(new BorderLayout());
        
        // Top bar
        JPanel topBar = createTopBar();
        mainContainer.add(topBar, BorderLayout.NORTH);

        // Left sidebar with tabs
        leftSidebar = createLeftSidebar();
        
        // Center task list
        createTaskList();
        
        // Right panel for editing
        rightPanel = createRightPanel();

        // Create wrapper for task list with hint label at bottom
        JPanel taskListWrapper = new JPanel(new BorderLayout());
        taskListWrapper.add(scrollPane, BorderLayout.CENTER);
        JLabel hintLabel = new JLabel("Right-click to add task");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintLabel.setForeground(new Color(150, 150, 150));
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        taskListWrapper.add(hintLabel, BorderLayout.SOUTH);

        // Main content: left sidebar + task list + right panel
        JPanel centerContent = new JPanel(new BorderLayout());
        centerContent.add(leftSidebar, BorderLayout.WEST);
        centerContent.add(taskListWrapper, BorderLayout.CENTER);
        centerContent.add(rightPanel, BorderLayout.EAST);
        
        mainContainer.add(centerContent, BorderLayout.CENTER);

        // Bottom toast
        toastLabel = new JLabel(" ");
        toastLabel.setHorizontalAlignment(SwingConstants.CENTER);
        toastLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        mainContainer.add(toastLabel, BorderLayout.SOUTH);

        add(mainContainer);
        applyTheme();
        selectTab("General");

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveTasks();
                dispose();
            }
        });

        setVisible(true);
    }

    private void initializeTabs() {
        FileStorage fileStorage = new FileStorage();
        tabTasks = fileStorage.loadDataByTabs();
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        titleLabel = new JLabel("FlowState");
        titleLabel.setFont(nunitoBold(26));
        topBar.add(titleLabel, BorderLayout.WEST);
        
        return topBar;
    }

    private JPanel createLeftSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setBackground(Color.white);

        tabPanel = new JPanel();
        tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));
        tabPanel.setBackground(Color.white);

        // Create default tabs
        createTabButton("Personal");
        createTabButton("Work");
        createTabButton("General");

        JScrollPane tabScroll = new JScrollPane(tabPanel);
        tabScroll.setBorder(BorderFactory.createEmptyBorder());
        sidebar.add(tabScroll);

        // Add new tab button
        addTabButton = new JButton("+ New Tab");
        addTabButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        addTabButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        addTabButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addTabButton.addActionListener(e -> addNewTabDialog());
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(addTabButton);

        sidebar.add(Box.createVerticalGlue());
        
        return sidebar;
    }

    private void createTabButton(String tabName) {
        JButton tabBtn = new JButton(tabName);
        tabBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        tabBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Flat modern appearance
        tabBtn.setFocusPainted(false);
        tabBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        tabBtn.setContentAreaFilled(true);
        tabBtn.setOpaque(true);
        tabBtn.setBackground(Color.white);
        tabBtn.setForeground(new Color(45, 55, 72));
        tabBtn.setMargin(new Insets(6, 8, 6, 8));
        tabBtn.addActionListener(e -> selectTab(tabName));
        
        // Right-click to rename/delete
        tabBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showTabContextMenu(tabBtn, tabName, e.getX(), e.getY());
                }
            }
        });

        tabPanel.add(tabBtn);
        tabPanel.add(Box.createVerticalStrut(5));
        tabButtons.put(tabName, tabBtn);
    }

    private void showTabContextMenu(JButton tabBtn, String tabName, int x, int y) {
        if (tabName.equals("General")) {
            JOptionPane.showMessageDialog(this, "Cannot rename or delete the General tab.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem renameItem = new JMenuItem("Rename");
        renameItem.addActionListener(e -> renameTabDialog(tabName, tabBtn));
        menu.add(renameItem);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> deleteTab(tabName, tabBtn));
        menu.add(deleteItem);

        menu.show(tabBtn, x, y);
    }

    private void renameTabDialog(String oldName, JButton tabBtn) {
        String newName = JOptionPane.showInputDialog(this, "New tab name:", oldName);
        if (newName != null && !newName.trim().isEmpty()) {
            newName = newName.trim();
            tabTasks.put(newName, tabTasks.remove(oldName));
            tabButtons.put(newName, tabButtons.remove(oldName));
            tabBtn.setText(newName);
            currentTab = newName;
            showToast("Tab renamed to: " + newName);
        }
    }

    private void deleteTab(String tabName, JButton tabBtn) {
        int confirm = JOptionPane.showConfirmDialog(this, "Delete tab '" + tabName + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tabTasks.remove(tabName);
            tabButtons.remove(tabName);
            tabPanel.remove(tabBtn);
            tabPanel.revalidate();
            tabPanel.repaint();
            selectTab("General");
            showToast("Tab deleted");
        }
    }

    private void addNewTabDialog() {
        String tabName = JOptionPane.showInputDialog(this, "New tab name:");
        if (tabName != null && !tabName.trim().isEmpty()) {
            tabName = tabName.trim();
            if (tabTasks.containsKey(tabName)) {
                JOptionPane.showMessageDialog(this, "Tab already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            tabTasks.put(tabName, new ArrayList<>());
            createTabButton(tabName);
            tabPanel.revalidate();
            tabPanel.repaint();
            selectTab(tabName);
            showToast("Tab created: " + tabName);
        }
    }

    private void selectTab(String tabName) {
        currentTab = tabName;
        
        // Update button styles
        for (Map.Entry<String, JButton> entry : tabButtons.entrySet()) {
            if (entry.getKey().equals(tabName)) {
                entry.getValue().setBackground(new Color(59, 130, 246));
                entry.getValue().setForeground(Color.white);
                entry.getValue().setOpaque(true);
            } else {
                entry.getValue().setBackground(UIManager.getColor("Button.background"));
                entry.getValue().setForeground(UIManager.getColor("Button.foreground"));
            }
        }

        refreshTaskList();
    }

    private void createTaskList() {
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(nunitoBold(14));
        taskList.setFixedCellHeight(45);
        taskList.setCellRenderer(new TaskRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        taskList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int displayIndex = taskList.getSelectedIndex();
                if (displayIndex >= 0) {
                    String itemText = taskListModel.getElementAt(displayIndex).toString();
                    // Skip header and empty states
                    if (!itemText.equals("---COMPLETED_HEADER---") && !itemText.contains("(empty)")) {
                        // Find the actual task by counting only non-header/non-empty items
                        ArrayList<Task> tasks = tabTasks.get(currentTab);
                        ArrayList<Task> incompleteTasks = new ArrayList<>();
                        ArrayList<Task> completedTasks = new ArrayList<>();
                        
                        for (Task t : tasks) {
                            if (t.isCompleted()) {
                                completedTasks.add(t);
                            } else {
                                incompleteTasks.add(t);
                            }
                        }
                        
                        // Count position in the list model
                        int taskCounter = 0;
                        for (int i = 0; i < displayIndex; i++) {
                            String text = taskListModel.getElementAt(i).toString();
                            if (!text.equals("---COMPLETED_HEADER---") && !text.contains("(empty)")) {
                                taskCounter++;
                            }
                        }
                        
                        // Determine if this task is incomplete or completed
                        if (taskCounter < incompleteTasks.size()) {
                            // This is an incomplete task
                            currentSelectedTask = incompleteTasks.get(taskCounter);
                        } else if (completedTasksVisible) {
                            // This is a completed task
                            int completedIndex = taskCounter - incompleteTasks.size();
                            if (completedIndex >= 0 && completedIndex < completedTasks.size()) {
                                currentSelectedTask = completedTasks.get(completedIndex);
                            }
                        }
                        updateRightPanel();
                    }
                }
            }
        });

        // Right-click context menu
        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = taskList.locationToIndex(e.getPoint());
                    Rectangle cellBounds = (index >= 0) ? taskList.getCellBounds(index, index) : null;
                    
                    // Check if click is within an actual cell's bounds
                    if (index >= 0 && cellBounds != null && cellBounds.contains(e.getPoint())) {
                        String itemText = taskListModel.getElementAt(index).toString();
                        // Only show delete for actual tasks (not headers or empty messages)
                        if (!itemText.equals("---COMPLETED_HEADER---") && !itemText.contains("(empty)")) {
                            // Set the selected task
                            taskList.setSelectedIndex(index);
                            // Get the actual task from the properly tracked selection
                            ArrayList<Task> tasks = tabTasks.get(currentTab);
                            ArrayList<Task> incompleteTasks = new ArrayList<>();
                            ArrayList<Task> completedTasks = new ArrayList<>();
                            
                            for (Task t : tasks) {
                                if (t.isCompleted()) {
                                    completedTasks.add(t);
                                } else {
                                    incompleteTasks.add(t);
                                }
                            }
                            
                            // Count position
                            int taskCounter = 0;
                            for (int i = 0; i < index; i++) {
                                String text = taskListModel.getElementAt(i).toString();
                                if (!text.equals("---COMPLETED_HEADER---") && !text.contains("(empty)")) {
                                    taskCounter++;
                                }
                            }
                            
                            // Get the actual task
                            if (taskCounter < incompleteTasks.size()) {
                                currentSelectedTask = incompleteTasks.get(taskCounter);
                            } else if (completedTasksVisible) {
                                int completedIndex = taskCounter - incompleteTasks.size();
                                if (completedIndex >= 0 && completedIndex < completedTasks.size()) {
                                    currentSelectedTask = completedTasks.get(completedIndex);
                                }
                            }
                            
                            showDeleteTaskContextMenu(e.getX(), e.getY());
                        } else {
                            // Clicked on header or empty state - show add task
                            showAddTaskContextMenu(e.getX(), e.getY());
                        }
                    } else {
                        // Clicked in empty space below tasks - show add task
                        showAddTaskContextMenu(e.getX(), e.getY());
                    }
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    // Handle left-click on completed header to toggle visibility
                    int index = taskList.locationToIndex(e.getPoint());
                    Rectangle cellBounds = (index >= 0) ? taskList.getCellBounds(index, index) : null;
                    
                    if (index >= 0 && cellBounds != null && cellBounds.contains(e.getPoint())) {
                        String itemText = taskListModel.getElementAt(index).toString();
                        if (itemText.equals("---COMPLETED_HEADER---")) {
                            // Toggle completed tasks visibility (header remains visible)
                            completedTasksVisible = !completedTasksVisible;
                            refreshTaskList();
                        }
                    }
                }
            }
        });

        scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
    }

    private void showAddTaskContextMenu(int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        menu.setOpaque(true);
        menu.setBackground(Color.white);
        menu.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JMenuItem addItem = new JMenuItem("+ Add Task");
        addItem.setOpaque(true);
        addItem.setBackground(Color.white);
        addItem.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        addItem.addActionListener(e -> addNewTask());
        menu.add(addItem);

        menu.show(taskList, x, y);
    }

    private void showDeleteTaskContextMenu(int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        menu.setOpaque(true);
        menu.setBackground(Color.white);
        menu.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JMenuItem deleteItem = new JMenuItem("Delete Task");
        deleteItem.setOpaque(true);
        deleteItem.setBackground(Color.white);
        deleteItem.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        deleteItem.addActionListener(e -> deleteTask());
        menu.add(deleteItem);

        menu.show(taskList, x, y);
    }

    private JPanel createRightPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setPreferredSize(new Dimension(380, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        selectedTaskTitle = new JLabel("Select a task");
        selectedTaskTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mainPanel.add(selectedTaskTitle);
        mainPanel.add(Box.createVerticalStrut(20));

        // Title section with label on left
        JPanel titleSection = new JPanel(new BorderLayout(10, 0));
        titleSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        titleSection.setBackground(Color.white);
        JLabel titleLabel = new JLabel("Title");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setPreferredSize(new Dimension(80, 20));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(Color.white);
        editTitleField = new JTextField();
        editTitleField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        editTitleField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        editTitleField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (currentSelectedTask != null && !editTitleField.getText().equals(currentSelectedTask.getTitle())) {
                    currentSelectedTask.setTitle(editTitleField.getText());
                    saveTasks();
                    refreshTaskList();
                    selectedTaskTitle.setText(currentSelectedTask.getTitle());
                }
            }
        });
        titleSection.add(titleLabel, BorderLayout.WEST);
        titleSection.add(editTitleField, BorderLayout.CENTER);
        mainPanel.add(titleSection);
        mainPanel.add(Box.createVerticalStrut(15));

        // Description section with label on left
        JPanel descSection = new JPanel(new BorderLayout(10, 5));
        descSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        descSection.setBackground(Color.white);
        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descLabel.setVerticalAlignment(SwingConstants.TOP);
        descLabel.setOpaque(true);
        descLabel.setBackground(Color.white);
        editDescriptionArea = new JTextArea(4, 20);
        editDescriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        editDescriptionArea.setLineWrap(true);
        editDescriptionArea.setWrapStyleWord(true);
        editDescriptionArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        editDescriptionArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (currentSelectedTask != null && !editDescriptionArea.getText().equals(currentSelectedTask.getDescription())) {
                    currentSelectedTask.setDescription(editDescriptionArea.getText());
                    saveTasks();
                }
            }
        });
        JScrollPane descScroll = new JScrollPane(editDescriptionArea);
        descScroll.getViewport().setBackground(Color.white);
        descSection.add(descLabel, BorderLayout.WEST);
        descSection.add(descScroll, BorderLayout.CENTER);
        mainPanel.add(descSection);
        mainPanel.add(Box.createVerticalStrut(15));

        // Date section with label on left
        JPanel dateSection = new JPanel(new BorderLayout(10, 0));
        dateSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        dateSection.setBackground(Color.white);
        JLabel dateLabel = new JLabel("Date");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateLabel.setPreferredSize(new Dimension(80, 20));
        editDateLabel = new JLabel("No date");
        editDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        editDateLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editDateLabel.setForeground(new Color(59, 130, 246));
        editDateLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleCalendar();
            }
        });
        dateLabel.setOpaque(true);
        dateLabel.setBackground(Color.white);
        dateSection.add(dateLabel, BorderLayout.WEST);
        dateSection.add(editDateLabel, BorderLayout.CENTER);
        mainPanel.add(dateSection);
        mainPanel.add(Box.createVerticalStrut(8));

        // Calendar panel (initially hidden)
        calendarPanelContainer = new JPanel();
        calendarPanelContainer.setLayout(new BoxLayout(calendarPanelContainer, BoxLayout.Y_AXIS));
        calendarPanelContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        calendarPanelContainer.setVisible(false);
        calendarPanelContainer.setBackground(Color.white);
        mainPanel.add(calendarPanelContainer);
        mainPanel.add(Box.createVerticalStrut(15));

        // Priority section with label on left
        JPanel prioritySection = new JPanel(new BorderLayout(10, 0));
        prioritySection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        prioritySection.setBackground(Color.white);
        JLabel priorityLabel = new JLabel("Priority");
        priorityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        priorityLabel.setPreferredSize(new Dimension(80, 20));
        String[] priorities = {"Low", "Medium", "High"};
        priorityBox = new JComboBox<>(priorities);
        priorityBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        priorityBox.addActionListener(e -> {
            if (currentSelectedTask != null) {
                currentSelectedTask.setPriority((String) priorityBox.getSelectedItem());
                saveTasks();
                refreshTaskList();
            }
        });
        prioritySection.add(priorityLabel, BorderLayout.WEST);
        prioritySection.add(priorityBox, BorderLayout.CENTER);
        mainPanel.add(prioritySection);
        mainPanel.add(Box.createVerticalStrut(15));

        // Completed checkbox
        completedCheckbox = new JCheckBox("Mark as Completed");
        completedCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        completedCheckbox.setBackground(Color.white);
        completedCheckbox.setOpaque(true);
        completedCheckbox.addActionListener(e -> {
            if (currentSelectedTask != null) {
                currentSelectedTask.setCompleted(completedCheckbox.isSelected());
                saveTasks();
                refreshTaskList();
                // Only clear selection if marking as completed
                if (completedCheckbox.isSelected()) {
                    currentSelectedIndex = -1;
                    currentSelectedTask = null;
                    updateRightPanel();
                } else {
                    // Task was uncompleted, keep it selected so user can see it
                    updateRightPanel();
                }
                showToast(completedCheckbox.isSelected() ? "Task completed" : "Task incomplete");
            }
        });
        mainPanel.add(completedCheckbox);
        
        mainPanel.add(Box.createVerticalGlue());
        
        return mainPanel;
    }

    private void updateRightPanel() {
        if (currentSelectedTask == null) {
            selectedTaskTitle.setText("Select a task");
            editTitleField.setText("");
            editDescriptionArea.setText("");
            editDateLabel.setText("No date");
            priorityBox.setSelectedItem("Medium");
            completedCheckbox.setSelected(false);
            return;
        }

        selectedTaskTitle.setText(currentSelectedTask.getTitle());
        editTitleField.setText(currentSelectedTask.getTitle());
        editDescriptionArea.setText(currentSelectedTask.getDescription());
        editDateLabel.setText(hasValidDueDate(currentSelectedTask) ? currentSelectedTask.getDueDate() : "No date");
        priorityBox.setSelectedItem(currentSelectedTask.getPriority());
        completedCheckbox.setSelected(currentSelectedTask.isCompleted());
    }

    private boolean hasValidDueDate(Task t) {
        if (t == null) return false;
        String d = t.getDueDate();
        return d != null && !d.trim().isEmpty() && !d.trim().equalsIgnoreCase("null");
    }

    private void toggleCalendar() {
        if (currentSelectedTask == null) return;

        calendarVisible = !calendarVisible;

        if (calendarVisible) {
            // Build and show calendar
            calendarPanelContainer.removeAll();

            LocalDate currentDate = hasValidDueDate(currentSelectedTask)
                ? LocalDate.parse(currentSelectedTask.getDueDate(), DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                : LocalDate.now();

            // Year and month controls
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
            controlPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            
            String[] months = {"January", "February", "March", "April", "May", "June",
                              "July", "August", "September", "October", "November", "December"};
            JComboBox<String> monthBox = new JComboBox<>(months);
            monthBox.setSelectedIndex(currentDate.getMonthValue() - 1);
            monthBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            monthBox.setPreferredSize(new Dimension(120, 30));
            
            JComboBox<Integer> yearBox = new JComboBox<>();
            int yearNow = LocalDate.now().getYear();
            for (int i = 0; i < 10; i++) yearBox.addItem(yearNow + i - 2);
            yearBox.setSelectedItem(currentDate.getYear());
            yearBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            yearBox.setPreferredSize(new Dimension(80, 30));
            
            controlPanel.add(monthBox);
            controlPanel.add(yearBox);
            
            // Calendar grid with better spacing
            JPanel gridPanel = new JPanel(new GridLayout(7, 7, 8, 8));
            gridPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
            gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 0, 8));
            gridPanel.setBackground(Color.white);
            
            String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String day : dayNames) {
                JLabel dayLabel = new JLabel(day);
                dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
                dayLabel.setOpaque(false);
                gridPanel.add(dayLabel);
            }
            
            JButton[] allDayButtons = new JButton[42];
            int[] selectedDay = {currentDate.getDayOfMonth()};
            
            updateCalendarGrid(gridPanel, allDayButtons, (Integer) yearBox.getSelectedItem(), 
                              monthBox.getSelectedIndex() + 1, selectedDay, currentDate);
            
            monthBox.addActionListener(e -> updateCalendarGrid(gridPanel, allDayButtons, 
                                      (Integer) yearBox.getSelectedItem(), 
                                      monthBox.getSelectedIndex() + 1, selectedDay, currentDate));
            
            yearBox.addActionListener(e -> updateCalendarGrid(gridPanel, allDayButtons, 
                                     (Integer) yearBox.getSelectedItem(), 
                                     monthBox.getSelectedIndex() + 1, selectedDay, currentDate));
            
            calendarPanelContainer.add(controlPanel);
            calendarPanelContainer.add(gridPanel);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
            buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            cancelButton.setPreferredSize(new Dimension(80, 30));
            cancelButton.addActionListener(e -> toggleCalendar());
            
            JButton saveButton = new JButton("Done");
            saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            saveButton.setPreferredSize(new Dimension(80, 30));
            saveButton.addActionListener(e -> {
                int month = monthBox.getSelectedIndex() + 1;
                int year = (Integer) yearBox.getSelectedItem();
                String dueDate = String.format("%02d/%02d/%d", month, selectedDay[0], year);
                
                currentSelectedTask.setDueDate(dueDate);
                editDateLabel.setText(dueDate);
                saveTasks();
                refreshTaskList();
                toggleCalendar();
            });
            
            buttonPanel.add(cancelButton);
            buttonPanel.add(saveButton);
            
            calendarPanelContainer.add(buttonPanel);
            calendarPanelContainer.setVisible(true);
            
            rightPanel.revalidate();
            rightPanel.repaint();
        } else {
            calendarPanelContainer.removeAll();
            calendarPanelContainer.setVisible(false);
            rightPanel.revalidate();
            rightPanel.repaint();
        }
    }

    private void updateCalendarGrid(JPanel gridPanel, JButton[] dayButtons, int year, int month, int[] selectedDay, LocalDate currentDate) {
        // Remove old day buttons (keep 7 day headers)
        while (gridPanel.getComponentCount() > 7) {
            gridPanel.remove(7);
        }
        
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();
        int startDay = firstDay.getDayOfWeek().getValue() % 7; // Sunday = 0
        
        int dayCounter = 1;
        for (int i = 0; i < 42; i++) {
            JButton dayButton = new JButton();
            dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            dayButton.setFocusPainted(false);
            dayButton.setMargin(new Insets(4, 4, 4, 4));
            dayButton.setBackground(Color.white);
            dayButton.setForeground(new Color(30, 30, 30));
            dayButton.setOpaque(true);
            dayButton.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            dayButton.setPreferredSize(new Dimension(35, 35));
            
            if (i < startDay || dayCounter > daysInMonth) {
                dayButton.setEnabled(false);
                dayButton.setText("");
                dayButton.setBackground(new Color(250, 250, 250));
            } else {
                final int day = dayCounter;
                dayButton.setText(String.valueOf(day));
                dayButton.setEnabled(true);
                
                if (day == selectedDay[0]) {
                    dayButton.setBackground(new Color(59, 130, 246));
                    dayButton.setForeground(Color.white);
                    dayButton.setFont(dayButton.getFont().deriveFont(Font.BOLD));
                } else {
                    dayButton.setBackground(Color.white);
                }
                
                dayButton.addActionListener(e -> {
                    selectedDay[0] = day;
                    updateCalendarGrid(gridPanel, dayButtons, year, month, selectedDay, currentDate);
                });
                
                dayCounter++;
            }
            
            gridPanel.add(dayButton);
            dayButtons[i] = dayButton;
        }
        
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void addNewTask() {
        Task task = new Task("New Task", "", null, "Medium");
        tabTasks.get(currentTab).add(task);
        manager.addTask(task);
        saveTasks();
        refreshTaskList();
        currentSelectedIndex = tabTasks.get(currentTab).size() - 1;
        taskList.setSelectedIndex(currentSelectedIndex);
        currentSelectedTask = task;
        updateRightPanel();
        editTitleField.requestFocus();
        editTitleField.selectAll();
        showToast("Task added");
    }

    private void deleteTask() {
        if (currentSelectedTask == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this task?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        ArrayList<Task> currentTabTasks = tabTasks.get(currentTab);
        boolean removed = currentTabTasks.remove(currentSelectedTask);
        
        if (removed) {
            saveTasks();
            refreshTaskList();
            currentSelectedIndex = -1;
            currentSelectedTask = null;
            updateRightPanel();
            showToast("Task deleted");
        }
    }

    private Font nunitoBold(int size) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("fonts/Nunito-VariableFont_wght.ttf"));
            return font.deriveFont(Font.BOLD, size);
        } catch (Exception e) {
            return new Font("Arial", Font.BOLD, size);
        }
    }

    private void styleButton(JButton button, Color bg, Color border, Color text) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBackground(bg);
        button.setForeground(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        button.setOpaque(true);
    }

    private void applyTheme() {
        Color bgWhite = Color.white;
        Color bgLight = new Color(250, 250, 250);
        Color selList = new Color(240, 244, 248);
        Color btnBg = new Color(59, 130, 246);
        Color btnBorder = new Color(37, 99, 235);
        Color textColor = new Color(20, 25, 40);
        Color toastColor = new Color(59, 130, 246);

        mainContainer.setBackground(bgWhite);
        leftSidebar.setBackground(bgWhite);
        rightPanel.setBackground(bgWhite);

        scrollPane.getViewport().setBackground(bgWhite);
        taskList.setBackground(bgWhite);
        taskList.setSelectionBackground(selList);
        taskList.setForeground(textColor);

        toastLabel.setForeground(toastColor);

        styleButton(addTabButton, btnBg, btnBorder, Color.white);

        // Style tab buttons for a modern flat look
        for (JButton btn : tabButtons.values()) {
            if (btn.getText().equals(currentTab)) {
                btn.setBackground(btnBg);
                btn.setForeground(Color.white);
                btn.setBorder(BorderFactory.createLineBorder(btnBorder, 1));
            } else {
                btn.setBackground(bgWhite);
                btn.setForeground(textColor);
                btn.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
            }
            btn.setFocusPainted(false);
            btn.setOpaque(true);
        }

        editTitleField.setBackground(bgLight);
        editTitleField.setForeground(textColor);
        editTitleField.setCaretColor(textColor);

        editDescriptionArea.setBackground(bgLight);
        editDescriptionArea.setForeground(textColor);
        editDescriptionArea.setCaretColor(textColor);

        // Make form fields have white backgrounds for better contrast
        editTitleField.setBackground(bgWhite);
        editDescriptionArea.setBackground(bgWhite);
        if (editDateLabel != null) {
            editDateLabel.setOpaque(true);
            editDateLabel.setBackground(bgWhite);
        }
        if (priorityBox != null) {
            priorityBox.setBackground(bgWhite);
            priorityBox.setOpaque(true);
        }

        refreshTaskList();
    }

    private void showToast(String message) {
        if (toastTimer != null && toastTimer.isRunning()) {
            toastTimer.stop();
        }
        toastLabel.setText(message);
        toastTimer = new Timer(3000, e -> toastLabel.setText(" "));
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private void refreshTaskList() {
        taskListModel.clear();
        ArrayList<Task> tasks = tabTasks.get(currentTab);
        ArrayList<Task> incompleteTasks = new ArrayList<>();
        ArrayList<Task> completedTasks = new ArrayList<>();

        // Separate completed and incomplete tasks
        for (Task t : tasks) {
            if (t.isCompleted()) {
                completedTasks.add(t);
            } else {
                incompleteTasks.add(t);
            }
        }

        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter prettyFormat = DateTimeFormatter.ofPattern("MMM d");
        LocalDate today = LocalDate.now();

        // Add incomplete tasks
        if (incompleteTasks.isEmpty() && completedTasks.isEmpty()) {
            taskListModel.addElement("(empty) No tasks yet.");
            return;
        }

        for (Task t : incompleteTasks) {
            String dateStatus = "";
            LocalDate dueDate = null;

            try {
                if (hasValidDueDate(t)) {
                    dueDate = LocalDate.parse(t.getDueDate(), inputFormat);
                    String formattedDate = dueDate.format(prettyFormat);
                    
                    if (dueDate.isBefore(today)) {
                        dateStatus = formattedDate + " [OVERDUE]";
                    } else if (dueDate.equals(today)) {
                        dateStatus = "Today [NOW]";
                    } else {
                        dateStatus = formattedDate;
                    }
                }
            } catch (Exception e) {
                // Leave dateStatus blank if date is invalid
            }

            String priorityIcon = "[L]";
            String priorityColor = "#7FD56F";
            if (t.getPriority().equalsIgnoreCase("High")) {
                priorityColor = "#EF4444";
                priorityIcon = "[H]";
            } else if (t.getPriority().equalsIgnoreCase("Medium")) {
                priorityColor = "#FBBF24";
                priorityIcon = "[M]";
            }

            String baseColor;
            if (dueDate != null && dueDate.isBefore(today)) {
                baseColor = "#EF4444";
            } else if (dueDate != null && dueDate.equals(today)) {
                baseColor = "#F59E0B";
            } else {
                baseColor = "#1F2937";
            }

            String left = "<span style='color:" + baseColor + ";'><b>" + t.getTitle() + "</b></span> " +
                    "<span style='color:" + priorityColor + ";'>" + priorityIcon + "</span>";
            String right = "<span style='color:" + baseColor + ";'>" + dateStatus + "</span>";

            taskListModel.addElement(left + ":::" + right);
        }

        // Add completed tasks section (always add header when there are completed tasks)
        if (!completedTasks.isEmpty()) {
            taskListModel.addElement("---COMPLETED_HEADER---");
            if (completedTasksVisible) {
                for (Task t : completedTasks) {
                String dateStatus = "";
                try {
                    if (hasValidDueDate(t)) {
                        LocalDate dueDate = LocalDate.parse(t.getDueDate(), inputFormat);
                        dateStatus = dueDate.format(prettyFormat);
                    }
                } catch (Exception e) {
                    // Leave dateStatus blank if date is invalid
                }

                String priorityIcon = "[L]";
                String priorityColor = "#7FD56F";
                if (t.getPriority().equalsIgnoreCase("High")) {
                    priorityColor = "#EF4444";
                    priorityIcon = "[H]";
                } else if (t.getPriority().equalsIgnoreCase("Medium")) {
                    priorityColor = "#FBBF24";
                    priorityIcon = "[M]";
                }

                String left = "<span style='color:#999999;'><b>" + t.getTitle() + "</b></span> " +
                        "<span style='color:" + priorityColor + ";'>" + priorityIcon + "</span>";
                String right = "<span style='color:#999999;'>" + dateStatus + " [done]</span>";

                taskListModel.addElement(left + ":::" + right);
                }
            }
        }
    }

    private void saveTasks() {
        FileStorage fileStorage = new FileStorage();
        fileStorage.saveDataByTabs(tabTasks);
    }

    private class TaskRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel leftLabel;
        private JLabel rightLabel;

        public TaskRenderer() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            leftLabel = new JLabel();
            leftLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            rightLabel = new JLabel();
            rightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            add(leftLabel, BorderLayout.WEST);
            add(rightLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String[] parts = value.split(":::");
            String left = parts[0];
            String right = parts.length > 1 ? parts[1] : "";

            // Handle completed header
            if (value.equals("---COMPLETED_HEADER---")) {
                JPanel headerPanel = new JPanel(new BorderLayout());
                headerPanel.setBackground(new Color(248, 248, 248));
                headerPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(12, 12, 8, 12)
                ));
                headerPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                JLabel headerLabel = new JLabel("> Completed");
                headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                headerLabel.setForeground(new Color(120, 120, 120));
                headerPanel.add(headerLabel, BorderLayout.WEST);
                headerPanel.setOpaque(true);
                
                return headerPanel;
            }

            // Handle empty state
            if (left.contains("(empty)")) {
                leftLabel.setText("<html>" + left + "</html>");
                rightLabel.setText("");
                setBackground(new Color(255, 255, 255));
                setOpaque(true);
                return this;
            }

            leftLabel.setText("<html>" + left + "</html>");
            rightLabel.setText("<html>" + right + "</html>");

            if (isSelected) {
                setBackground(new Color(240, 244, 248));
            } else {
                setBackground(new Color(255, 255, 255));
            }
            
            setOpaque(true);
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FlowStateGUI();
        });
    }
}
