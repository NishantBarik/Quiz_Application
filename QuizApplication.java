import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;   // Explicitly import List from java.util
import java.util.ArrayList;
import java.util.Collections;  // Import Collections for shuffling

public class QuizApplication {
    private java.util.List<String[]> questions = new java.util.ArrayList<>();
    private int currentQuestion = 0, score = 0, timer = 10;
    private javax.swing.Timer countdownTimer;

    private JFrame frame;
    private JLabel questionLabel, timerLabel;
    private JRadioButton[] options = new JRadioButton[4];
    private ButtonGroup group;
    private JButton submitButton, nextButton;

    private static final String LEADERBOARD_FILE = "leaderboard.txt";

    public QuizApplication() {
        frame = new JFrame("Quiz Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 350);
        frame.setLayout(new BorderLayout(10, 10));

        loadQuestions("questions.txt");

        JPanel topPanel = new JPanel();
        timerLabel = new JLabel("Time Left: " + timer + " seconds", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(timerLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(5, 1));
        questionLabel = new JLabel("", JLabel.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(questionLabel);

        group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Arial", Font.PLAIN, 14));
            group.add(options[i]);
            centerPanel.add(options[i]);
        }
        frame.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        submitButton = new JButton("Submit");
        nextButton = new JButton("Next");
        nextButton.setEnabled(false);
        bottomPanel.add(submitButton);
        bottomPanel.add(nextButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        loadQuestion();

        submitButton.addActionListener(e -> submitAnswer());
        nextButton.addActionListener(e -> nextQuestion());

        frame.setVisible(true);
    }

    private void loadQuestions(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                questions.add(line.split(","));
            }
            // Shuffle the questions to randomize the order
            Collections.shuffle(questions);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error loading questions!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadQuestion() {
        if (currentQuestion < questions.size()) {
            String[] q = questions.get(currentQuestion);
            questionLabel.setText(q[0]);

            // Create a list of options and shuffle them
            List<String> shuffledOptions = new ArrayList<>();
            shuffledOptions.add(q[1]);
            shuffledOptions.add(q[2]);
            shuffledOptions.add(q[3]);
            shuffledOptions.add(q[4]);
            Collections.shuffle(shuffledOptions);

            // Set shuffled options
            for (int i = 0; i < 4; i++) {
                options[i].setText(shuffledOptions.get(i));
                options[i].setForeground(Color.BLACK);
                options[i].setEnabled(true);
                options[i].setSelected(false);
            }

            submitButton.setEnabled(true);
            nextButton.setEnabled(false);
            timer = 10;
            startTimer();
        } else {
            showResults();
        }
    }

    private void startTimer() {
        countdownTimer = new javax.swing.Timer(1000, e -> {
            timer--;
            timerLabel.setText("Time Left: " + timer + " seconds");
            if (timer <= 0) {
                countdownTimer.stop();
                submitAnswer();
            }
        });
        countdownTimer.start();
    }

    private void submitAnswer() {
        countdownTimer.stop();
        submitButton.setEnabled(false);
    
        try {
            // Ensure currentQuestion is within bounds
            if (currentQuestion < 0 || currentQuestion >= questions.size()) {
                throw new IndexOutOfBoundsException("Current question index is out of bounds.");
            }
    
            // Ensure the question array has at least 6 elements
            String[] question = questions.get(currentQuestion);
            if (question == null || question.length < 6) {
                throw new IllegalArgumentException("Invalid question format.");
            }
    
            // Find the correct option index in the shuffled list
            int correctOptionIndex = Integer.parseInt(question[5]) - 1;
            if (correctOptionIndex < 0 || correctOptionIndex >= 4) {
                throw new IndexOutOfBoundsException("Correct option index is out of bounds.");
            }
            String correctOption = question[correctOptionIndex + 1];
    
            int selectedOption = -1;
            // Check if any option is selected
            for (int i = 0; i < 4; i++) {
                options[i].setEnabled(false);
                if (options[i].isSelected()) selectedOption = i;
                if (options[i].getText().equals(correctOption)) {
                    options[i].setForeground(Color.GREEN); // Green for correct
                } else {
                    options[i].setForeground(Color.RED); // Red for wrong
                }
            }
    
            // If the selected option matches the correct answer, increment score
            if (selectedOption != -1 && options[selectedOption].getText().equals(correctOption)) {
                score++;
            }
            nextButton.setEnabled(true);
    
        } catch (NumberFormatException e) {
            System.err.println("Error parsing correct option index: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Index out of bounds: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid question format: " + e.getMessage());
        }
    }

    private void nextQuestion() {
        currentQuestion++;
        loadQuestion();
    }

    private void showResults() {
        saveScore();
        displayLeaderboard();
        int choice = JOptionPane.showConfirmDialog(frame, "Quiz Over! Your score: " + score + "/" + questions.size() + "\nDo you want to restart?", "Results", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) restartQuiz();
        else frame.dispose();
    }

    private void saveScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LEADERBOARD_FILE, true))) {
            writer.write("Player - " + score + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving score!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayLeaderboard() {
        StringBuilder leaderboard = new StringBuilder("Leaderboard:\n");
        try (BufferedReader reader = new BufferedReader(new FileReader(LEADERBOARD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                leaderboard.append(line).append("\n");
            }
        } catch (IOException e) {
            leaderboard.append("No scores available.");
        }
        JOptionPane.showMessageDialog(frame, leaderboard.toString(), "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
    }

    private void restartQuiz() {
        currentQuestion = 0;
        score = 0;
        loadQuestion();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizApplication::new);
    }
}
