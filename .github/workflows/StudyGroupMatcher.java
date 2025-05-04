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
}

// --- CLASS: MatchEngine ---
public class MatchEngine {
    public List<User> findMatches(User user, List<User> allUsers) {
        List<User> matches = new ArrayList<>();
        for (User u : allUsers) {
            if (!u.equals(user) &&
                !Collections.disjoint(u.getCourses(), user.getCourses()) &&
                Objects.equals(u.getPreferredTime(), user.getPreferredTime()) &&
                Objects.equals(u.getLearningStyle(), user.getLearningStyle())) {
                matches.add(u);
            }
        }
        return matches;
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

    public void joinGroup(StudyGroup group, User user) {
        group.addMember(user);
    }

    public void leaveGroup(StudyGroup group, User user) {
        group.removeMember(user);
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
}

// --- CLASS: StudyMatcherGUI ---
class StudyMatcherGUI {
    private JFrame frame;
    private DefaultListModel<User> usersList;
    private List<User> users;
    private final String FILE_PATH = "users.txt";

    public StudyMatcherGUI() {
        users = new ArrayList<>();
        usersList = new DefaultListModel<>();
        loadUsersFromFile();

        frame = new JFrame("Study Group Matcher");
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JButton addUserBtn = new JButton("Register User");
        addUserBtn.addActionListener(e -> registerUser());

        JButton matchBtn = new JButton("Find Matches");
        matchBtn.addActionListener(e -> findMatches());

        JList<User> displayList = new JList<>(usersList);
        JScrollPane scrollPane = new JScrollPane(displayList);

        JPanel panel = new JPanel();
        panel.add(addUserBtn);
        panel.add(matchBtn);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

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
        saveUsersToFile();
    }

    private void findMatches() {
        if (users.isEmpty()) return;
        User selected = users.get(0); // Demo: matches based on first user
        MatchEngine engine = new MatchEngine();
        List<User> matches = engine.findMatches(selected, users);
        JOptionPane.showMessageDialog(frame, "Matches: \n" + matches.toString());
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
