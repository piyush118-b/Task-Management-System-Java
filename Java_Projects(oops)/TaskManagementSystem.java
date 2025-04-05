import java.util.*;
import java.util.Scanner;
import java.util.PriorityQueue;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

// Task class representing a single task1
abstract class Task implements Comparable<Task> { // Implements Comparable for sorting
    protected String title;
    protected String description;
    protected String deadline;
    protected boolean isCompleted;
    protected int priority; // 1 = High, 2 = Medium, 3 = Low

    public Task(String title, String description, String deadline, int priority) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.isCompleted = false;
        this.priority = priority;
    }

    public abstract String getTaskType();

    public void markCompleted() {
        this.isCompleted = true;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(Task other) {
        return Integer.compare(this.priority, other.priority); // Sorting by priority
    }

    public void displayTask() {
        System.out.println("[" + getTaskType() + "] Title: " + title);
        System.out.println("Description: " + description);
        System.out.println("Deadline: " + deadline);
        System.out.println("Priority: " + (priority == 1 ? "High" : priority == 2 ? "Medium" : "Low"));
        System.out.println("Status: " + (isCompleted ? "Completed" : "Pending"));
        System.out.println("----------------------");
    }
}


class WorkTask extends Task {
    private String projectName;

    public WorkTask(String title, String description, String deadline, int priority, String projectName) {
        super(title, description, deadline, priority);
        this.projectName = projectName;
    }

    @Override
    public String getTaskType() {
        return "Work Task";
    }
}

class PersonalTask extends Task {
    private String category;

    public PersonalTask(String title, String description, String deadline, int priority, String category) {
        super(title, description, deadline, priority);
        this.category = category;
    }

    @Override
    public String getTaskType() {
        return "Personal Task";
    }
}

class TaskFactory {
    public static Task createTask(String type, String title, String description, String deadline, int priority, String extra) {
        if (type.equalsIgnoreCase("Work")) {
            return new WorkTask(title, description, deadline, priority, extra);
        } else if (type.equalsIgnoreCase("Personal")) {
            return new PersonalTask(title, description, deadline, priority, extra);
        } else {
            throw new IllegalArgumentException("Invalid task type!");
        }
    }
}
// TaskManager class to handle task operations

class TaskManager {
    private PriorityQueue<Task> taskQueue = new PriorityQueue<>();
    private Stack<Task> undoStack = new Stack<>();
    private final String FILE_NAME = "tasks.txt"; // File to store tasks

    public TaskManager() {
        loadTasksFromFile(); // Load tasks when program starts
    }

    public void addTask(String type, String title, String description, String deadline, int priority, String extra) {
        Task newTask = TaskFactory.createTask(type, title, description, deadline, priority, extra);
        taskQueue.add(newTask);
        saveTasksToFile();
        System.out.println("Task added successfully!\n");
    }

    public void displayTasks() {
        if (taskQueue.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }
        System.out.println("Task List (Sorted by Priority):");
        for (Task task : taskQueue) {
            task.displayTask();
        }
    }

    public void removeTask() {
        if (!taskQueue.isEmpty()) {
            Task removedTask = taskQueue.poll();
            undoStack.push(removedTask);
            saveTasksToFile();
            System.out.println("Task removed successfully! You can undo this action.");
        } else {
            System.out.println("No tasks to remove.");
        }
    }

    public void undoLastRemoval() {
        if (!undoStack.isEmpty()) {
            Task lastRemovedTask = undoStack.pop();
            taskQueue.add(lastRemovedTask);
            saveTasksToFile();
            System.out.println("Undo successful! Task has been restored.");
        } else {
            System.out.println("No actions to undo.");
        }
    }

    // ✅ FIX: Improved saveTasksToFile() with better error handling
    private void saveTasksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Task task : taskQueue) {
                writer.write(task.getTaskType() + "," + task.title + "," + task.description + "," + task.deadline + "," + task.priority + "\n");
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving tasks to file: " + e.getMessage());
        }
    }

    // ✅ FIX: Improved loadTasksFromFile() with validation
    private void loadTasksFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("ℹ️ No previous tasks found. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 5) {
                    System.out.println("⚠️ Skipping invalid line: " + line);
                    continue;
                }

                String type = parts[0].trim();
                String title = parts[1].trim();
                String description = parts[2].trim();
                String deadline = parts[3].trim();
                
                // ✅ FIX: Handle invalid integer values safely
                int priority;
                try {
                    priority = Integer.parseInt(parts[4].trim());
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Invalid priority value in line: " + line);
                    continue;
                }

                String extra = parts.length > 5 ? parts[5].trim() : ""; // Extra info

                // Create task and add to queue
                Task task = TaskFactory.createTask(type, title, description, deadline, priority, extra);
                taskQueue.add(task);
            }
        } catch (IOException e) {
            System.out.println("❌ Error loading tasks from file: " + e.getMessage());
        }
    }
}


public class TaskManagementSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TaskManager taskManager = new TaskManager();

        while (true) {
            System.out.println("\nTask Management System");
            System.out.println("1. Add Task");
            System.out.println("2. View Tasks");
            System.out.println("3. Remove Task");
            System.out.println("4. Undo Last Removal");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter task type (Work/Personal): ");
                    String type = scanner.nextLine();

                    System.out.print("Enter task title: ");
                    String title = scanner.nextLine();

                    System.out.print("Enter task description: ");
                    String description = scanner.nextLine();

                    System.out.print("Enter deadline (YYYY-MM-DD): ");
                    String deadline = scanner.nextLine();

                    System.out.print("Enter priority (1 = High, 2 = Medium, 3 = Low): ");
                    int priority = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    System.out.print("Enter extra info (Project Name for Work, Category for Personal): ");
                    String extra = scanner.nextLine();

                    taskManager.addTask(type, title, description, deadline, priority, extra);
                    break;

                case 2:
                    taskManager.displayTasks();
                    break;

                case 3:
                    taskManager.removeTask();
                    break;

                case 4:
                    taskManager.undoLastRemoval();
                    break;

                case 5:
                    System.out.println("Exiting Task Manager. Goodbye!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }
}