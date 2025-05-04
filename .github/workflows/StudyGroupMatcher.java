import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// --- CLASS: User ---
public class User {
    private String name;
    private List<String> courses;
    private String preferredTime;
    private String learningStyle;

    public User(String name, List<String> courses, String preferredTime, String learningStyle) {
        this.name = name;
        this.courses = courses;
        this.preferredTime = preferredTime;
        this.learningStyle = learningStyle;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getCourses() { return courses; }
    public void setCourses(List<String> courses) { this.courses = courses; }

    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }

    public String getLearningStyle() { return learningStyle; }
    public void setLearningStyle(String learningStyle) { this.learningStyle = learningStyle; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name + " - " + String.join(", ", courses);
    }

    public String toCSV() {
        return name + ";" + String.join("|", courses) + ";" + preferredTime + ";" + learningStyle;
    }

    public static User fromCSV(String csv) {
        String[] parts = csv.split(";");
        String name = parts[0];
        List<String> courses = Arrays.asList(parts[1].split("\\|"));
        return new User(name, courses, parts[2], parts[3]);
    }
}

// --- CLASS: StudyGroup ---
public class StudyGroup {
    private String groupName;
    private Set<User> members;

    public StudyGroup(String groupName) {
        this.groupName = groupName;
        this.members = new HashSet<>();
    }

    public boolean addMember(User user) {
        if (user == null) return false;
        return members.add(user);
    }

    public boolean removeMember(User user) {
        if (user == null) return false;
        return members.remove(user);
    }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Set<User> getMembers() { return members; }

    @Override
    public String toString() {
        return groupName + " (" + members.size() + " members)";
    }

    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(groupName).append(";");
        List<String> names = new ArrayList<>();
        for (User user : members) {
            names.add(user.getName());
        }
        sb.append(String.join("|", names));
        return sb.toString();
    }

    public static StudyGroup fromCSV(String line, List<User> userPool) {
        String[] parts = line.split(";");
        StudyGroup group = new StudyGroup(parts[0]);
        if (parts.length > 1) {
            String[] names = parts[1].split("\\|");
            for (String name : names) {
                for (User u : userPool) {
                    if (u.getName().equalsIgnoreCase(name.trim())) {
                        group.addMember(u);
                        break;
                    }
                }
            }
        }
        return group;
    }
}

// --- CLASS: GroupManager ---
public class GroupManager {
    private List<StudyGroup> groups;

    public GroupManager() {
        this.groups = new ArrayList<>();
    }

    public void createGroup(String name, User leader) {
        StudyGroup group = new StudyGroup(name);
        group.addMember(leader);
        groups.add(group);
    }

    public void joinGroup(String name, User user) {
        StudyGroup group = findGroupByName(name);
        if (group != null) group.addMember(user);
    }

    public void leaveGroup(String name, User user) {
        StudyGroup group = findGroupByName(name);
        if (group != null) group.removeMember(user);
    }

    public List<StudyGroup> getGroups() {
        return groups;
    }

    public StudyGroup findGroupByName(String name) {
        for (StudyGroup group : groups) {
            if (group.getGroupName().equalsIgnoreCase(name)) {
                return group;
            }
        }
        return null;
    }

    public StudyGroup findGroupByUser(User user) {
        for (StudyGroup group : groups) {
            if (group.getMembers().contains(user)) return group;
        }
        return null;
    }

    public void loadGroupsFromFile(String filePath, List<User> userPool) {
        File file = new File(filePath);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                StudyGroup group = StudyGroup.fromCSV(line, userPool);
                groups.add(group);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveGroupsToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (StudyGroup group : groups) {
                writer.write(group.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// --- CLASS: StudyMatcherGUI ---
class StudyMatcherGUI {
    private JFrame frame;
    private DefaultListModel<User> usersList;
    private List<User> users;
    private GroupManager groupManager;
    private final String FILE_PATH = "users.txt";
    private JComboBox<String> userDropdown;

    public StudyMatcherGUI() {
        users = new ArrayList<>();
        usersList = new DefaultListModel<>();
        groupManager = new GroupManager();
        loadUsersFromFile();

        frame = new JFrame("Study Group Matcher");
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JButton addUserBtn = new JButton("Register User");
        JButton matchBtn = new JButton("Find Matches");
        JButton createGroupBtn = new JButton("Create Group");
        JButton joinGroupBtn = new JButton("Join Group");
        JButton viewGroupBtn = new JButton("View My Group");

        addUserBtn.addActionListener(e -> registerUser());
        matchBtn.addActionListener(e -> findMatches());
        createGroupBtn.addActionListener(e -> createGroup());
        joinGroupBtn.addActionListener(e -> joinGroup());
        viewGroupBtn.addActionListener(e -> viewGroup());

        JList<User> displayList = new JList<>(usersList);
        JScrollPane scrollPane = new JScrollPane(displayList);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        panel.add(addUserBtn);

        userDropdown = new JComboBox<>();
        updateUserDropdown();
        panel.add(new JLabel("Select User:"));
        panel.add(userDropdown);

        panel.add(matchBtn);
        panel.add(createGroupBtn);
        panel.add(joinGroupBtn);
        panel.add(viewGroupBtn);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.EAST);

        frame.setVisible(true);
    }

    private void registerUser() {
        String name = JOptionPane.showInputDialog("Enter name:");
        String coursesStr = JOptionPane.showInputDialog("Enter courses (comma-separated):");
        String preferredTime = JOptionPane.showInputDialog("Preferred time:");
        String learningStyle = JOptionPane.showInputDialog("Learning style:");

        List<String> courses = Arrays.asList(coursesStr.split(","));
        User user = new User(name.trim(), courses, preferredTime.trim(), learningStyle.trim());
        users.add(user);
        usersList.addElement(user);
        updateUserDropdown();
        saveUsersToFile();
    }

    private void updateUserDropdown() {
        if (userDropdown == null) return;
        userDropdown.removeAllItems();
        for (User u : users) {
            userDropdown.addItem(u.getName());
        }
    }

    private User getSelectedUser() {
        String selectedName = (String) userDropdown.getSelectedItem();
        if (selectedName == null) return null;
        for (User u : users) {
            if (u.getName().equalsIgnoreCase(selectedName)) return u;
        }
        return null;
    }

    private void findMatches() {
        User selected = getSelectedUser();
        if (selected == null) return;
        MatchEngine engine = new MatchEngine();
        List<User> matches = engine.findMatches(selected, users);
        JOptionPane.showMessageDialog(frame, "Matches for " + selected.getName() + ": \n" + matches);
    }

    private void createGroup() {
        User user = getSelectedUser();
        if (user == null) return;
        String groupName = JOptionPane.showInputDialog("Enter new group name:");
        groupManager.createGroup(groupName.trim(), user);
        JOptionPane.showMessageDialog(frame, "Group created successfully.");
    }

    private void joinGroup() {
        User user = getSelectedUser();
        if (user == null) return;
        String groupName = JOptionPane.showInputDialog("Enter group name to join:");
        groupManager.joinGroup(groupName.trim(), user);
        JOptionPane.showMessageDialog(frame, "Joined group successfully.");
    }

    private void viewGroup() {
        User user = getSelectedUser();
        if (user == null) return;
        StudyGroup group = groupManager.findGroupByUser(user);
        if (group == null) {
            JOptionPane.showMessageDialog(frame, "User is not in any group.");
        } else {
            StringBuilder sb = new StringBuilder("Group: " + group.getGroupName() + "\nMembers:\n");
            for (User u : group.getMembers()) {
                sb.append("- ").append(u.getName()).append("\n");
            }
            JOptionPane.showMessageDialog(frame, sb.toString());
        }
    }

    private void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user : users) {
                writer.write(user.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsersFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromCSV(line);
                users.add(user);
                usersList.addElement(user);
            }
            updateUserDropdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


// --- Main class ---
public class MainApp {
    public static void main(String[] args) {
        new StudyMatcherGUI();
    }
}
