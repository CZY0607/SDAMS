
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RatingPanel extends JPanel {

    private Student student;
    private ClassRegistrationService registrationService;
    private InstructorRatingService ratingService;
    private Map<String, String> instructors;
    private StudentDashboard dashboard;

    private static final Color HEADER_BG = new Color(13, 48, 96);
    private static final Color PRIMARY_BTN = new Color(11, 94, 181);
    private static final Color CONTENT_BG = new Color(244, 246, 250);
    private static final Color NAV_BG = new Color(222, 229, 239);
    private static final Color TABLE_HEADER_BG = new Color(228, 236, 247);
    private static final Color WHITE_BG = new Color(255, 255, 255);
    private static final Color ACTIVE_STATUS_BG = new Color(217, 245, 229);
    private static final Color ACTIVE_STATUS_TEXT = new Color(8, 125, 50);
    private static final Color DISABLED_BTN = new Color(232, 236, 243);
    private static final Color MAIN_TEXT = new Color(16, 24, 40);

    private JComboBox<String> classBox;
    private JComboBox<Integer> scoreBox;
    private JLabel instructorLabel;
    private JTextArea feedbackArea;

    private JTable table;
    private DefaultTableModel tableModel;

    public RatingPanel(Student student,
                       ClassRegistrationService registrationService,
                       InstructorRatingService ratingService,
                       Map<String, String> instructors,
                       StudentDashboard dashboard) {

        this.student = student;
        this.registrationService = registrationService;
        this.ratingService = ratingService;
        this.instructors = instructors;
        this.dashboard = dashboard;

        setLayout(new BorderLayout(10, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(CONTENT_BG);

        // Top section of the panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.setBackground(CONTENT_BG);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        selectionPanel.setBackground(CONTENT_BG);

        selectionPanel.setOpaque(false);

        classBox = new JComboBox<>();
        classBox.setPreferredSize(new Dimension(120, 28));

        scoreBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        scoreBox.setSelectedItem(5);

        instructorLabel = new JLabel("Instructor: -");

        selectionPanel.add(new JLabel("Select Class:"));
        selectionPanel.add(classBox);
        selectionPanel.add(instructorLabel);
        selectionPanel.add(new JLabel("Rating Score:"));
        selectionPanel.add(scoreBox);

        topPanel.add(selectionPanel, BorderLayout.NORTH);

        // The feedback section
        JPanel feedbackPanel = new JPanel(new BorderLayout(10, 10));
        feedbackPanel.setBackground(CONTENT_BG);

        feedbackPanel.setBorder(BorderFactory.createTitledBorder("Instructor Feedback"));

        feedbackArea = new JTextArea(4, 30);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);

        JScrollPane feedbackScroll = new JScrollPane(feedbackArea);
        feedbackPanel.add(feedbackScroll, BorderLayout.CENTER);


        JButton submitButton = new JButton("Submit Rating");
        submitButton.setBackground(new Color(35, 95, 170));
        submitButton.setBackground(new Color(11, 94, 181));  // #0B5EB5
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setFocusPainted(false);
        submitButton.setOpaque(true);
        submitButton.setContentAreaFilled(true);
        submitButton.setBorderPainted(false);
        submitButton.setPreferredSize(new Dimension(150, 45));



        feedbackPanel.add(submitButton, BorderLayout.EAST);
        topPanel.add(feedbackPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Previous ratings table
        String[] columns = {"Rating ID", "Class ID", "Instructor ID", "Score", "Feedback", "Rating Date"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);

        add(new JScrollPane(table), BorderLayout.CENTER);

        classBox.setBackground(Color.WHITE);
        classBox.setBackground(MAIN_TEXT);

        scoreBox.setBackground(Color.WHITE);
        scoreBox.setBackground(MAIN_TEXT);

        feedbackArea.setBackground(Color.WHITE);
        feedbackArea.setForeground(MAIN_TEXT);


        instructorLabel.setBackground(MAIN_TEXT);

        submitButton.setBackground(PRIMARY_BTN);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);

        table.setBackground(WHITE_BG);
        table.setForeground(MAIN_TEXT);
        table.setSelectionForeground(new Color(214, 228, 247));
        table.setSelectionForeground(MAIN_TEXT);
        table.setGridColor(new Color(220, 224, 230));
        table.setRowHeight(30);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(MAIN_TEXT);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD,14));




        // Display the assigned instructor
        classBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateInstructor();
            }
        });

        // Submits the rating
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitRating();
            }
        });
    }

    private void updateInstructor() {

        String classID = (String) classBox.getSelectedItem();

        if (classID == null) {
            instructorLabel.setText("Instructor: -");
            return;
        }


        String instructorID = instructors.get(classID);

        if (instructorID == null) {
            instructorLabel.setText("Instructor: Not Assigned");
        } else {
            instructorLabel.setText("Instructor: " + instructorID);
        }

    }

    private void submitRating() {

        String classID = (String) classBox.getSelectedItem();

        if (classID == null) {
            showWarning("Please select a booked class.");
            return;

        }

        String instructorID = instructors.get(classID);

        if (instructorID == null) {
            showWarning("No instructor assigned to this class.");
            return;
        }

        Integer score = (Integer) scoreBox.getSelectedItem();

        if (score == null) {
            showWarning("Please select a rating score.");
            return;

        }

        try {
            ratingService.addRating(student.getID(), classID, instructorID, score, feedbackArea.getText());

            feedbackArea.setText("");
            scoreBox.setSelectedItem(5);

            dashboard.refreshAll();

            JOptionPane.showMessageDialog(this, "Instructor rating submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        }
        catch (IllegalArgumentException | IllegalStateException | RegistrationException e) {
            showWarning(e.getMessage());

        }
        catch (IOException e) {
            showFileError(e.getMessage());
        }

    }


    public void reloadTable() throws IOException {

        String previousSelection =
                (String) classBox.getSelectedItem();

        classBox.removeAllItems();

        // Load the student's previous ratings first
        List<InstructorRating> ratings =
                ratingService.viewByStudent(student.getID());

        // Load the student's registrations
        List<ClassRegistration> registrations =
                registrationService.viewByStudent(student.getID());

        for (ClassRegistration registration : registrations) {

            if (!registration.getStatus().equals("Booked")) {
                continue;
            }

            String classID = registration.getClassID();

            if (!instructors.containsKey(classID)) {
                continue;
            }

            // Check whether the student already rated this class
            boolean alreadyRated = false;

            for (InstructorRating rating : ratings) {

                if (rating.getClassID().equals(classID)) {
                    alreadyRated = true;
                    break;
                }
            }

            // Do not show already-rated classes
            if (alreadyRated) {
                continue;
            }

            // Prevent duplicate class IDs in the dropdown
            boolean alreadyAdded = false;

            for (int i = 0; i < classBox.getItemCount(); i++) {

                if (classID.equals(classBox.getItemAt(i))) {
                    alreadyAdded = true;
                    break;
                }
            }

            if (!alreadyAdded) {
                classBox.addItem(classID);
            }
        }

        // Restore previous selection if still available
        if (previousSelection != null) {

            for (int i = 0; i < classBox.getItemCount(); i++) {

                if (previousSelection.equals(classBox.getItemAt(i))) {
                    classBox.setSelectedItem(previousSelection);
                    break;
                }
            }
        }

        updateInstructor();

        // Refresh the rating history table
        tableModel.setRowCount(0);

        for (InstructorRating rating : ratings) {

            tableModel.addRow(new Object[]{
                    rating.getRatingID(),
                    rating.getClassID(),
                    rating.getInstructorID(),
                    rating.getScore(),
                    rating.getFeedback(),
                    rating.getRatingDate()
            });
        }
    }


    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Action is Not Allowed", JOptionPane.WARNING_MESSAGE);
    }

    private void showFileError(String message) {
        JOptionPane.showMessageDialog(this, message, "File Error", JOptionPane.ERROR_MESSAGE);
    }
}
