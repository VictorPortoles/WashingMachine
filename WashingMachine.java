import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.*;

public class WashingMachine extends JFrame {
    private int currentAngle = 0; // Current angle of the selector
    private int currentProgramIndex = 0; // Index of the currently selected program
    private final String[] programs = {"Normal", "Delicate", "Quick", "Eco", "Heavy"}; // Program names
    private final int[] angles = {0, 72, 144, 216, 288}; // Predefined angles for each program
    private final int[] frequencies = {400, 500, 600, 700, 800}; // Beep frequencies for each program
    private JLabel programLabel; // Label to display the selected program

    public WashingMachine() {
        // Configure the main window
        setTitle("Washing Machine Panel");
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel to display the selected program, with a border around it
        JPanel programPanel = new JPanel();
        programPanel.setBackground(Color.WHITE);
        programPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Black border
        programLabel = new JLabel("Program: " + programs[currentProgramIndex], SwingConstants.CENTER);
        programLabel.setFont(new Font("Arial", Font.BOLD, 20));
        programPanel.add(programLabel);
        add(programPanel, BorderLayout.NORTH);

        // Dial panel (circular selector)
        JPanel dialPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw the circle
                int diameter = 250;
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillOval(x, y, diameter, diameter);

                // Draw the marker on the selector
                g2d.setColor(Color.RED);
                int markerX = (int) (x + diameter / 2 + (diameter / 2 - 10) * Math.cos(Math.toRadians(currentAngle)));
                int markerY = (int) (y + diameter / 2 - (diameter / 2 - 10) * Math.sin(Math.toRadians(currentAngle)));
                g2d.fillOval(markerX - 10, markerY - 10, 20, 20);

                // Draw labels for the programs around the circle
                for (int i = 0; i < programs.length; i++) {
                    int labelX = (int) (x + diameter / 2 + (diameter / 2 + 30) * Math.cos(Math.toRadians(angles[i])));
                    int labelY = (int) (y + diameter / 2 - (diameter / 2 + 30) * Math.sin(Math.toRadians(angles[i])));
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(programs[i], labelX - 20, labelY);
                }
            }
        };

        // Mouse events for dragging the selector
        dialPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int centerX = dialPanel.getWidth() / 2;
                int centerY = dialPanel.getHeight() / 2;

                // Calculate the new angle based on the mouse position
                double deltaX = e.getX() - centerX;
                double deltaY = centerY - e.getY(); // Invert Y-axis
                int newAngle = (int) Math.toDegrees(Math.atan2(deltaY, deltaX));
                if (newAngle < 0) {
                    newAngle += 360;
                }

                // Find the closest program based on the angle
                int closestProgramIndex = 0;
                int minDifference = 360;
                for (int i = 0; i < angles.length; i++) {
                    int difference = Math.abs(newAngle - angles[i]);
                    if (difference > 180) {
                        difference = 360 - difference;
                    }
                    if (difference < minDifference) {
                        minDifference = difference;
                        closestProgramIndex = i;
                    }
                }

                // Play beep only if the program changes
                if (currentProgramIndex != closestProgramIndex) {
                    currentProgramIndex = closestProgramIndex;
                    currentAngle = angles[currentProgramIndex];
                    generateBeep(frequencies[currentProgramIndex]); // Play beep
                    updateProgram();
                }

                dialPanel.repaint();
            }
        });

        add(dialPanel, BorderLayout.CENTER);

        // Button at the bottom to start the program
        JButton startButton = new JButton("Start Program");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.addActionListener(e -> {
            // Play a start sound when the program starts
            playStartSound();
            JOptionPane.showMessageDialog(
                    WashingMachine.this,
                    "Starting: " + programs[currentProgramIndex],
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        add(startButton, BorderLayout.SOUTH);

        // Make the window visible
        setVisible(true);
    }

    private void updateProgram() {
        // Update the label to display the current program
        programLabel.setText("Program: " + programs[currentProgramIndex]);
    }

    private void generateBeep(int frequency) {
        try {
            // Create a tone with the specified frequency
            float sampleRate = 44100;
            byte[] buffer = new byte[4410];
            for (int i = 0; i < buffer.length; i++) {
                double angle = 2.0 * Math.PI * i * frequency / sampleRate;
                buffer[i] = (byte) (Math.sin(angle) * 127);
            }

            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playStartSound() {
        try {
            // Create a longer tone for the start sound
            float sampleRate = 44100;
            byte[] buffer = new byte[8820]; // Longer than the beep
            for (int i = 0; i < buffer.length; i++) {
                double angle = 2.0 * Math.PI * i * 440 / sampleRate; // Fixed frequency: 440 Hz
                buffer[i] = (byte) (Math.sin(angle) * 127);
            }

            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WashingMachine::new);
    }
}