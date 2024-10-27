import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

// Interface for managing users
interface UserManager {
    void loadUsers();
    void addUser(String username, String id);
    boolean userExists(String username);
}

// Class representing a user
class User {
    private String username;
    private String id;

    public User(String username, String id) {
        this.username = username;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }
}

// Class representing a resident
class Resident extends User {
    public Resident(String username, String id) {
        super(username, id);
    }
}

// Class representing a visitor
class Visitor extends User {
    private String visitingResident;

    public Visitor(String username, String visitingResident) {
        super(username, "");
        this.visitingResident = visitingResident;
    }

    public String getVisitingResident() {
        return visitingResident;
    }
}

// Class implementing UserManager interface for managing residents
class ResidentManager implements UserManager {
    protected Map<String, String> residents;
    private final String FILE_NAME = "residents.txt";

    public ResidentManager() {
        residents = new HashMap<>();
        loadUsers();
    }

    @Override
    public void loadUsers() {
        try {
            File file = new File(FILE_NAME);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",", 2);
                residents.put(parts[0].trim(), parts.length > 1 ? parts[1].trim() : "");
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + FILE_NAME);
        }
    }

    @Override
    public void addUser(String username, String id) {
        residents.put(username, id);
        try {
            FileWriter writer = new FileWriter(FILE_NAME, true);
            writer.write(username + "," + id + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while adding the user.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean userExists(String username) {
        return residents.containsKey(username);
    }

    public String getId(String username) {
        return residents.get(username);
    }
}

// Class implementing UserManager interface for managing visitors
class VisitorManager implements UserManager {
    private Set<String> visitors;
    private final String FILE_NAME = "visitors.txt";

    public VisitorManager() {
        visitors = new HashSet<>();
        loadUsers();
    }

    @Override
    public void loadUsers() {
        try {
            File file = new File(FILE_NAME);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                visitors.add(scanner.nextLine().trim());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + FILE_NAME);
        }
    }

    @Override
    public void addUser(String username, String id) {
        visitors.add(username);
        try {
            FileWriter writer = new FileWriter(FILE_NAME, true);
            writer.write(username + "," + id + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while adding the user.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean userExists(String username) {
        return visitors.contains(username);
    }
}

// Class implementing UserManager interface for managing other users
class OtherUserManager implements UserManager {
    private Map<String, String> users;
    private final String FILE_NAME;

    public OtherUserManager(String fileName) {
        this.FILE_NAME = fileName;
        users = new HashMap<>();
        loadUsers();
    }

    @Override
    public void loadUsers() {
        try {
            File file = new File(FILE_NAME);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",", 2);
                users.put(parts[0].trim(), parts.length > 1 ? parts[1].trim() : "");
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + FILE_NAME);
        }
    }

    @Override
    public void addUser(String username, String id) {
        users.put(username, id);
        try {
            FileWriter writer = new FileWriter(FILE_NAME, true);
            writer.write(username + "," + id + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while adding the user.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    // Method to get ID for a given username
    public String getId(String username) {
        return users.get(username);
    }
}

// Main class
public class GatedCommunityChatBot extends Frame implements ActionListener {
    private static final String[] USER_TYPES = {"residents", "salesman", "milkman", "newspaperman", "fruitseller", "junkseller", "visitor"};
    private static final String GATE_OPEN_MESSAGE = "Welcome! The gate is now open.";
    private static final String GATE_CLOSE_MESSAGE = "Access denied! The gate is closed.";
    private static final String HAPPY_MESSAGE = "Welcome and please enjoy your stay with us. Keep your environment clean and have a friendly neighborhood!";
    private static final String RULES_MESSAGE = "Welcome! Please follow these friendly environment rules: Keep the area clean, Respect others, and Report any suspicious activity.";
    private static final int GATE_CLOSE_DELAY = 5000; // 5 seconds

    private TextField usernameField;
    private Choice userTypeChoice;
    private Button enterButton;

    private ResidentManager residentManager = new ResidentManager();
    private VisitorManager visitorManager = new VisitorManager();
    private Map<String, OtherUserManager> otherUserManagers = new HashMap<>();
    private DeniedUserManager deniedUserManager = new DeniedUserManager(); // New addition

    private boolean entryAllowed = true;

    public GatedCommunityChatBot() {
        setLayout(new FlowLayout());

        Label userTypeLabel = new Label("Select User Type:");
        add(userTypeLabel);

        userTypeChoice = new Choice();
        for (String userType : USER_TYPES) {
            userTypeChoice.add(userType);
        }
        add(userTypeChoice);

        Label usernameLabel = new Label("Enter Username:");
        add(usernameLabel);

        usernameField = new TextField(20);
        add(usernameField);

        enterButton = new Button("Enter");
        add(enterButton);

        enterButton.addActionListener(this);

        setTitle("Gated Community Chat Bot");
        setSize(300, 150);
        setVisible(true);

        // Load existing users
        residentManager.loadUsers();
        visitorManager.loadUsers();
        for (String userType : USER_TYPES) {
            if (!userType.equals("residents") && !userType.equals("visitor")) {
                String fileName = userType + ".txt";
                otherUserManagers.put(userType, new OtherUserManager(fileName));
            }
        }
        // Load denied users
        deniedUserManager.loadDeniedUsers();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String userType = userTypeChoice.getSelectedItem();
        String username = usernameField.getText();

        if (userType.equals("visitor")) {
            // Handle visitor entry
            handleVisitorEntry(username);
        } else if (userType.equals("residents")) {
            // Handle residents
            handleResidentEntry(username);
        } else {
            // Handle other user types
            if (entryAllowed) {
                handleOtherUserEntry(userType, username);
            } else {
                showMessageDialog(GATE_CLOSE_MESSAGE);
            }
        }
    }

    private void handleVisitorEntry(String username) {
        Frame residentSelectionFrame = new Frame("Select Resident to Visit");
        residentSelectionFrame.setSize(300, 150);
        residentSelectionFrame.setLayout(new FlowLayout());

        Label residentLabel = new Label("Select Resident:");
        residentSelectionFrame.add(residentLabel);

        Choice residentChoice = new Choice();
        for (String resident : residentManager.residents.keySet()) {
            residentChoice.add(resident);
        }
        residentSelectionFrame.add(residentChoice);

        Button visitButton = new Button("Visit");
        residentSelectionFrame.add(visitButton);

        visitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedResident = residentChoice.getSelectedItem();
                String visitingResident = selectedResident != null ? selectedResident : "";
                Visitor visitor = new Visitor(username, visitingResident);
                visitorManager.addUser(username, visitingResident);
                residentSelectionFrame.dispose();
                displayWelcomeMessage(visitor);
                closeGateAfterDelay();
            }
        });

        residentSelectionFrame.setVisible(true);
    }

    private void handleResidentEntry(String username) {
        if (residentManager.userExists(username)) {
            String uid = showInputDialog("Enter Unique ID:");
            String storedUID = residentManager.getId(username);
            if (uid != null && uid.equals(storedUID)) {
                showMessageDialog(GATE_OPEN_MESSAGE);
                showMessageDialog(HAPPY_MESSAGE);
                closeGateAfterDelay();
            } else {
                showMessageDialog(GATE_CLOSE_MESSAGE);
            }
        } else {
            showMessageDialog(GATE_CLOSE_MESSAGE);
        }
    }

    private void handleOtherUserEntry(String userType, String username) {
        OtherUserManager manager = otherUserManagers.get(userType);
        if (manager != null) {
            if (manager.userExists(username)) {
                // The user exists in the system, prompt for UID verification
                String storedUID = manager.getId(username);
                String enteredUID = showInputDialog("Enter UID:");
                if (enteredUID != null && enteredUID.equals(storedUID)) {
                    showMessageDialog(GATE_OPEN_MESSAGE);
                    showMessageDialog(HAPPY_MESSAGE);
                    closeGateAfterDelay();
                } else {
                    showMessageDialog(GATE_CLOSE_MESSAGE);
                    deniedUserManager.addDeniedUser(username); // Add to denied users
                }
            } else {
                // The user doesn't exist, prompt for UID creation
                String uid = showInputDialog("Enter UID:");
                if (uid != null && !uid.isEmpty()) {
                    manager.addUser(username, uid);
                    entryAllowed = false;
                }
            }
        }
    }

    private void displayWelcomeMessage(Visitor visitor) {
        showMessageDialog(GATE_OPEN_MESSAGE);
        showMessageDialog(RULES_MESSAGE + " You are visiting " + visitor.getVisitingResident());
    }

    private void showMessageDialog(String message) {
        Dialog dialog = new Dialog(this, "Message", true);
        dialog.setLayout(new FlowLayout());
        dialog.add(new Label(message));
        Button okButton = new Button("OK");
        dialog.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        dialog.setSize(300, 100);
        dialog.setVisible(true);
    }

    private String showInputDialog(String message) {
        return JOptionPane.showInputDialog(this, message);
    }

    private void closeGateAfterDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(GATE_CLOSE_DELAY);
                showMessageDialog("Gate is closed.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        new GatedCommunityChatBot();
    }
}

// Class implementing UserManager interface for managing denied users
class DeniedUserManager {
    private Set<String> deniedUsers;
    private final String FILE_NAME = "denied.txt";

    public DeniedUserManager() {
        deniedUsers = new HashSet<>();
        loadDeniedUsers();
    }

    public void loadDeniedUsers() {
        try {
            File file = new File(FILE_NAME);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                deniedUsers.add(scanner.nextLine().trim());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + FILE_NAME);
        }
    }

    public void addDeniedUser(String username) {
        deniedUsers.add(username);
        try {
            FileWriter writer = new FileWriter(FILE_NAME, true);
            writer.write(username + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while adding the denied user.");
            e.printStackTrace();
        }
    }

    public boolean isUserDenied(String username) {
        return deniedUsers.contains(username);
    }
}
