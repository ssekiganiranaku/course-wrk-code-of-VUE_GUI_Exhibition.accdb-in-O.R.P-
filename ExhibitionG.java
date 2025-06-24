package com.mycompany.exhibitionregistration;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExhibitionG extends JFrame {

    private JTextField regIdField, nameField, facultyField, projectTitleField, contactField, emailField;
    private JLabel imageLabel;
    private String imagePath = "";
    private Connection conn;

    public ExhibitionG() {
        // Initialize DB connection
        connectToDatabase();

        // GUI setup
        setTitle("VUE Exhibition Registration");
        setSize(640, 750);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        JLabel header = new JLabel("Innovation & Technology Exhibition");
        header.setBounds(150, 10, 400, 30);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(header);

        addLabelAndField("Registration ID:", regIdField = new JTextField(), 60, labelFont, fieldFont);
        addLabelAndField("Student Name:", nameField = new JTextField(), 100, labelFont, fieldFont);
        addLabelAndField("Faculty:", facultyField = new JTextField(), 140, labelFont, fieldFont);
        addLabelAndField("Project Title:", projectTitleField = new JTextField(), 180, labelFont, fieldFont);
        addLabelAndField("Contact Number:", contactField = new JTextField(), 220, labelFont, fieldFont);
        addLabelAndField("Email Address:", emailField = new JTextField(), 260, labelFont, fieldFont);

        JButton uploadButton = new JButton("Upload Project Image");
        uploadButton.setBounds(30, 310, 200, 30);
        add(uploadButton);

        imageLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        imageLabel.setBounds(180, 310, 400, 180);
        imageLabel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Project Prototype",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                labelFont));
        add(imageLabel);

        String[] buttonNames = {"Register", "Search", "Update", "Delete", "Clear", "Exit"};
        int x = 60;
        for (String name : buttonNames) {
            JButton btn = new JButton(name);
            btn.setBounds(x, 520, 90, 35);
            add(btn);
            x += 95;

            btn.addActionListener(e -> {
                switch (name) {
                    case "Register" -> register();
                    case "Search" -> search();
                    case "Update" -> update();
                    case "Delete" -> delete();
                    case "Clear" -> clearFields();
                    case "Exit" -> System.exit(0);
                }
            });
        }

        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                imagePath = fileChooser.getSelectedFile().getAbsolutePath();
                ImageIcon icon = new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(400, 180, Image.SCALE_SMOOTH));
                imageLabel.setIcon(icon);
                imageLabel.setText("");
            }
        });

        setVisible(true);
    }

    private void addLabelAndField(String label, JTextField field, int y, Font labelFont, Font fieldFont) {
        JLabel jLabel = new JLabel(label);
        jLabel.setBounds(30, y, 150, 25);
        jLabel.setFont(labelFont);
        add(jLabel);

        field.setBounds(180, y, 400, 25);
        field.setFont(fieldFont);
        add(field);
    }

    private void connectToDatabase() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
           String path = "C:\\Users\\HP\\Documents\\NetBeansProjects\\ExhibitionRegistration\\src\\VUE_Exhibition.accdb";
conn = DriverManager.getConnection("jdbc:ucanaccess://" + path);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB Connection Failed: " + ex.getMessage());
        }
    }           
    private void register() {
    if (regIdField.getText().isEmpty() ||
        nameField.getText().isEmpty() ||
        facultyField.getText().isEmpty() ||
        projectTitleField.getText().isEmpty() ||
        contactField.getText().isEmpty() ||
        emailField.getText().isEmpty()) {

        JOptionPane.showMessageDialog(this, "Please fill in all fields.");
        return;
    }

    String contact = contactField.getText();
    if (!contact.matches("\\d{7,15}")) {
        JOptionPane.showMessageDialog(this, "Invalid contact number. Use 7–15 digits.");
        return;
    }

    String email = emailField.getText();
    if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
        JOptionPane.showMessageDialog(this, "Invalid email format.");
        return;
    }

    try {
        String checkSql = "SELECT * FROM Participants WHERE RegID = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, regIdField.getText());
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            JOptionPane.showMessageDialog(this, "Registration ID already exists.");
            return;
        }

        String sql = "INSERT INTO Participants (RegID, Name, Faculty, ProjectTitle, Contact, Email, ImagePath) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, regIdField.getText());
        pst.setString(2, nameField.getText());
        pst.setString(3, facultyField.getText());
        pst.setString(4, projectTitleField.getText());
        pst.setString(5, contactField.getText());
        pst.setString(6, emailField.getText());
        pst.setString(7, imagePath);
        pst.executeUpdate();
        JOptionPane.showMessageDialog(this, "Registered Successfully!");
       
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Registration Error: " + e.getMessage());
    }
}


   private void search() {
    String regID = regIdField.getText().trim();
    if (regID.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter Registration ID to search.");
        return;
    }

    try {
        String sql = "SELECT * FROM Participants WHERE RegID = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, regID);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            nameField.setText(rs.getString("Name"));
            facultyField.setText(rs.getString("Faculty"));
            projectTitleField.setText(rs.getString("ProjectTitle"));
            contactField.setText(rs.getString("Contact"));
            emailField.setText(rs.getString("Email"));
            imagePath = rs.getString("ImagePath");

            if (new File(imagePath).exists()) {
                ImageIcon icon = new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(400, 180, Image.SCALE_SMOOTH));
                imageLabel.setIcon(icon);
                imageLabel.setText("");
            } else {
                imageLabel.setText("Image not found");
                imageLabel.setIcon(null);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No record found!");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Search Error: " + e.getMessage());
    }
}


   private void update() {
    if (regIdField.getText().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter Registration ID to update.");
        return;
    }

    if (nameField.getText().isEmpty() ||
        facultyField.getText().isEmpty() ||
        projectTitleField.getText().isEmpty() ||
        contactField.getText().isEmpty() ||
        emailField.getText().isEmpty()) {

        JOptionPane.showMessageDialog(this, "All fields must be filled to update.");
        return;
    }

    if (!contactField.getText().matches("\\d{7,15}")) {
        JOptionPane.showMessageDialog(this, "Invalid contact number. Use 7–15 digits.");
        return;
    }

    if (!emailField.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
        JOptionPane.showMessageDialog(this, "Invalid email format.");
        return;
    }

    try {
        String sql = "UPDATE Participants SET Name=?, Faculty=?, ProjectTitle=?, Contact=?, Email=?, ImagePath=? WHERE RegID=?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, nameField.getText());
        pst.setString(2, facultyField.getText());
        pst.setString(3, projectTitleField.getText());
        pst.setString(4, contactField.getText());
        pst.setString(5, emailField.getText());
        pst.setString(6, imagePath);
        pst.setString(7, regIdField.getText());
        int updated = pst.executeUpdate();
        if (updated > 0) {
            JOptionPane.showMessageDialog(this, "Record Updated");
        } else {
            JOptionPane.showMessageDialog(this, "Record Not Found");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Update Error: " + e.getMessage());
    }
}


   private void delete() {
    String regID = regIdField.getText().trim();
    if (regID.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter Registration ID to delete.");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;

    try {
        String sql = "DELETE FROM Participants WHERE RegID = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, regID);
        int deleted = pst.executeUpdate();
        if (deleted > 0) {
            JOptionPane.showMessageDialog(this, "Record Deleted");
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Record Not Found");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Delete Error: " + e.getMessage());
    }
}


    private void clearFields() {
        regIdField.setText("");
        nameField.setText("");
        facultyField.setText("");
        projectTitleField.setText("");
        contactField.setText("");
        emailField.setText("");
        imageLabel.setIcon(null);
        imageLabel.setText("No Image Selected");
        imagePath = "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExhibitionG::new);
    }
}