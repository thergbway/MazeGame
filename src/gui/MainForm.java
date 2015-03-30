package gui;

import settings.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainForm extends JFrame {
    private JPanel rootPanel;
    private JButton generateButton;
    private JButton findPathButton;
    private MazeVisualizer mazeVisualizer;

    public MainForm() {
        super(Settings.getMainFrameTitle());

        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(300, 100, getWidth(), getHeight());
        setResizable(false);
        pack();
        setVisible(true);

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mazeVisualizer.generateNewMaze();
            }
        });
        findPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mazeVisualizer.generateNewPath();
            }
        });
        mazeVisualizer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                mazeVisualizer.addAppropriateFlagIfPossible(e.getY(), e.getX());
            }
        });
    }

    private void createUIComponents() {
        mazeVisualizer = new MazeVisualizer();
    }
}
