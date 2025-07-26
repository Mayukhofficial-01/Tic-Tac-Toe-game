import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.Random;
import java.util.ArrayList;

public class TicTacToe extends JFrame {
    private AnimatedButton[][] buttons;
    private char currentPlayer;
    private JLabel statusLabel;
    private boolean isAIMode;
    private Random random;
    private JPanel gamePanel;
    private JPanel mainMenuPanel;
    private JPanel gameContainer;
    private boolean gameActive;
    private ArrayList<Point> winningCells;

    // Custom button class for animations
    private class AnimatedButton extends JButton {
        private float scale = 1f;
        private float underlineOffset = 0f;
        private boolean underlining = false;

        public AnimatedButton(String text) {
            super(text);
        }

        public void startPopIn() {
            scale = 0f;
            Timer popTimer = new Timer(30, null);
            ActionListener popListener = e -> {
                scale += 0.1f;
                if (scale >= 1f) {
                    scale = 1f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            };
            popTimer.addActionListener(popListener);
            popTimer.start();
        }

        public void startUnderline() {
            underlining = true;
            underlineOffset = -getWidth();
            Timer underlineTimer = new Timer(20, null);
            ActionListener underlineListener = e -> {
                underlineOffset += getWidth() / 20f;
                if (underlineOffset >= 0) {
                    underlineOffset = 0;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            };
            underlineTimer.addActionListener(underlineListener);
            underlineTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
            at.translate((getWidth() * (1 - scale)) / (2 * scale), (getHeight() * (1 - scale)) / (2 * scale));
            g2d.setTransform(at);
            g2d.setColor(new Color(0x2D3748)); // Dark gray cell background
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            if (!getText().isEmpty()) {
                g2d.setColor(new Color(0xFFD700)); // Soft gold text
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - fm.getDescent();
                g2d.drawString(getText(), x, y);
            }
            if (underlining) {
                g2d.setColor(new Color(0x38B2AC)); // Teal underline
                g2d.setStroke(new BasicStroke(4));
                g2d.drawLine((int) underlineOffset, getHeight() - 5, (int) underlineOffset + getWidth(), getHeight() - 5);
            }
            g2d.dispose();
        }
    }

    public TicTacToe() {
        // Initialize game state
        currentPlayer = 'X';
        buttons = new AnimatedButton[3][3];
        isAIMode = false;
        random = new Random();
        gameActive = false;
        winningCells = new ArrayList<>();

        // Set up the frame
        setTitle("Tic Tac Toe");
        setMinimumSize(new Dimension(300, 350));
        setPreferredSize(new Dimension(400, 450));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set app logo
        try {
            Image logo = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/logo.png"));
            setIconImage(logo);
        } catch (Exception e) {
            System.out.println("Warning: logo.png not found, using default icon.");
        }

        // Create main menu
        mainMenuPanel = createMainMenu();
        gameContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0x1A202C)); // Dark background
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Create status label
        statusLabel = new JLabel("Welcome to Tic Tac Toe", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
        statusLabel.setForeground(new Color(0x00E5FF)); // Bright cyan

        // Add main menu initially
        add(mainMenuPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Add resize listener to adjust font sizes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateFontSizes();
            }
        });

        pack();
        setVisible(true);
    }

    private JPanel createMainMenu() {
        JPanel menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0x1A202C));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        menuPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("Tic Tac Toe", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0x00E5FF)); // Bright cyan

        JButton pvpButton = createStyledButton("Player vs Player");
        pvpButton.addActionListener(e -> {
            System.out.println("Player vs Player button clicked");
            animateButtonClick(pvpButton, () -> startGame(false));
        });

        JButton aiButton = createStyledButton("Player vs AI");
        aiButton.addActionListener(e -> {
            System.out.println("Player vs AI button clicked");
            animateButtonClick(aiButton, () -> startGame(true));
        });

        menuPanel.add(titleLabel, gbc);
        menuPanel.add(pvpButton, gbc);
        menuPanel.add(aiButton, gbc);

        return menuPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(0x4C51BF)); // Darker teal on hover
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else {
                    g2d.setColor(new Color(0x38B2AC)); // Teal button
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Montserrat", Font.PLAIN, 14));
        button.setForeground(new Color(0xFFFFFF)); // White text
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
        return button;
    }

    private void animateButtonClick(JButton button, Runnable onComplete) {
        Timer timer = new Timer(30, null);
        final float[] offsetY = {0f};
        final int[] count = {0};
        ActionListener bounceListener = e -> {
            count[0]++;
            if (count[0] <= 5) {
                offsetY[0] = count[0] * 2f; // Move down
            } else if (count[0] <= 10) {
                offsetY[0] = (10 - count[0]) * 2f; // Bounce back
            } else {
                timer.stop();
                onComplete.run();
                return;
            }
            button.repaint();
            button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    AffineTransform at = AffineTransform.getTranslateInstance(0, offsetY[0]);
                    g2d.setTransform(at);
                    super.paint(g2d, c);
                    g2d.dispose();
                }
            });
        };
        timer.addActionListener(bounceListener);
        timer.start();
    }

    private void initializeBoard() {
        System.out.println("Initializing game board");
        gamePanel = new JPanel(new GridLayout(3, 3, 10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(new Color(0x1A202C));
            }
        };
        buttons = new AnimatedButton[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new AnimatedButton("");
                buttons[i][j].setFont(new Font("Montserrat", Font.BOLD, 40));
                buttons[i][j].setForeground(new Color(0xFFD700)); // Soft gold
                buttons[i][j].setContentAreaFilled(false);
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].setBorder(BorderFactory.createEmptyBorder());
                buttons[i][j].setEnabled(true);
                int finalI = i;
                int finalJ = j;
                buttons[i][j].addActionListener(e -> {
                    System.out.println("Game board button clicked at position: (" + finalI + ", " + finalJ + ")");
                    animateButtonClick(buttons[finalI][finalJ], () -> handleMove(finalI, finalJ));
                });
                gamePanel.add(buttons[i][j]);
            }
        }
    }

    private void updateFontSizes() {
        int width = getWidth();
        int height = getHeight();
        int baseFontSize = Math.min(width, height) / 20;
        int boardFontSize = Math.min(width, height) / 10;

        statusLabel.setFont(new Font("Montserrat", Font.BOLD, Math.max(12, baseFontSize)));

        Component centerComponent = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent instanceof JPanel) {
            for (Component comp : ((JPanel) centerComponent).getComponents()) {
                if (comp instanceof JLabel) {
                    comp.setFont(new Font("Montserrat", Font.BOLD, Math.max(16, baseFontSize + 4)));
                } else if (comp instanceof JButton) {
                    comp.setFont(new Font("Montserrat", Font.PLAIN, Math.max(12, baseFontSize)));
                }
            }
        }

        if (gamePanel != null) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    buttons[i][j].setFont(new Font("Montserrat", Font.BOLD, Math.max(20, boardFontSize)));
                }
            }
        }

        if (gameContainer != null) {
            Component southComponent = ((BorderLayout) gameContainer.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
            if (southComponent instanceof JPanel) {
                for (Component comp : ((JPanel) southComponent).getComponents()) {
                    if (comp instanceof JButton) {
                        comp.setFont(new Font("Montserrat", Font.PLAIN, Math.max(12, baseFontSize)));
                    }
                }
            }
        }
    }

    private void startGame(boolean aiMode) {
        System.out.println("Starting game with AI mode: " + aiMode);
        SwingUtilities.invokeLater(() -> {
            try {
                isAIMode = aiMode;
                gameActive = true;
                getContentPane().removeAll();
                gameContainer = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(new Color(0x1A202C));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                initializeBoard();

                JButton resetButton = createStyledButton("Reset Game");
                resetButton.addActionListener(e -> {
                    System.out.println("Reset Game button clicked");
                    animateButtonClick(resetButton, this::resetGame);
                });
                JButton mainMenuButton = createStyledButton("Main Menu");
                mainMenuButton.addActionListener(e -> {
                    System.out.println("Main Menu button clicked");
                    animateButtonClick(mainMenuButton, this::returnToMainMenu);
                });

                JPanel buttonPanel = new JPanel(new FlowLayout());
                buttonPanel.setOpaque(false);
                buttonPanel.add(resetButton);
                buttonPanel.add(mainMenuButton);

                gameContainer.add(gamePanel, BorderLayout.CENTER);
                gameContainer.add(buttonPanel, BorderLayout.SOUTH);
                add(gameContainer, BorderLayout.CENTER);
                add(statusLabel, BorderLayout.SOUTH);
                statusLabel.setText("Player X's turn");
                resetGame();
                updateFontSizes();
                getContentPane().revalidate();
                getContentPane().repaint();
                System.out.println("Game UI updated successfully");
                SwingUtilities.invokeLater(() -> repaint());
            } catch (Exception e) {
                System.err.println("Error in startGame: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void returnToMainMenu() {
        System.out.println("Returning to main menu");
        SwingUtilities.invokeLater(() -> {
            gameActive = false;
            getContentPane().removeAll();
            add(mainMenuPanel, BorderLayout.CENTER);
            add(statusLabel, BorderLayout.SOUTH);
            statusLabel.setText("Welcome to Tic Tac Toe");
            updateFontSizes();
            getContentPane().revalidate();
            getContentPane().repaint();
        });
    }

    private void handleMove(int row, int col) {
        if (!gameActive) {
            System.out.println("Game is not active, ignoring move at (" + row + ", " + col + ")");
            return;
        }
        if (buttons[row][col].getText().equals("") && !isGameOver()) {
            buttons[row][col].setText(String.valueOf(currentPlayer));
            buttons[row][col].startPopIn();
            System.out.println("Move made by " + currentPlayer + " at (" + row + ", " + col + ")");
            if (checkWin()) {
                statusLabel.setText("Player " + currentPlayer + " wins!");
                highlightWinningCells();
                disableButtons();
                gameActive = false;
            } else if (checkDraw()) {
                statusLabel.setText("Game is a draw!");
                disableButtons();
                gameActive = false;
            } else {
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                statusLabel.setText("Player " + currentPlayer + "'s turn");
                if (isAIMode && currentPlayer == 'O') {
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            SwingUtilities.invokeLater(() -> makeAIMove());
                        } catch (InterruptedException e) {
                            System.err.println("Error in AI move thread: " + e.getMessage());
                        }
                    }).start();
                }
            }
        } else {
            System.out.println("Invalid move at (" + row + ", " + col + "): button not empty or game over.");
        }
    }

    private void makeAIMove() {
        if (!gameActive) {
            System.out.println("Game not active, skipping AI move");
            return;
        }
        // Medium difficulty: Try to win, block player's win, or move randomly
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().equals("")) {
                    buttons[i][j].setText("O");
                    if (checkWin()) {
                        buttons[i][j].startPopIn();
                        System.out.println("AI wins at (" + i + ", " + j + ")");
                        statusLabel.setText("AI wins!");
                        highlightWinningCells();
                        disableButtons();
                        gameActive = false;
                        return;
                    }
                    buttons[i][j].setText("");
                }
            }
        }
        char temp = currentPlayer;
        currentPlayer = 'X';
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().equals("")) {
                    buttons[i][j].setText("X");
                    if (checkWin()) {
                        buttons[i][j].setText("O");
                        buttons[i][j].startPopIn();
                        currentPlayer = 'X';
                        System.out.println("AI blocks at (" + i + ", " + j + ")");
                        statusLabel.setText("Player X's turn");
                        return;
                    }
                    buttons[i][j].setText("");
                }
            }
        }
        currentPlayer = temp;
        while (true) {
            int i = random.nextInt(3);
            int j = random.nextInt(3);
            if (buttons[i][j].getText().equals("")) {
                buttons[i][j].setText("O");
                buttons[i][j].startPopIn();
                System.out.println("AI moves randomly at (" + i + ", " + j + ")");
                currentPlayer = 'X';
                statusLabel.setText("Player X's turn");
                if (checkWin()) {
                    statusLabel.setText("AI wins!");
                    highlightWinningCells();
                    disableButtons();
                    gameActive = false;
                } else if (checkDraw()) {
                    statusLabel.setText("Game is a draw!");
                    disableButtons();
                    gameActive = false;
                }
                break;
            }
        }
    }

    private void highlightWinningCells() {
        for (Point p : winningCells) {
            buttons[p.x][p.y].startUnderline();
        }
    }

    private boolean checkWin() {
        winningCells.clear();
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(String.valueOf(currentPlayer)) &&
                buttons[i][1].getText().equals(String.valueOf(currentPlayer)) &&
                buttons[i][2].getText().equals(String.valueOf(currentPlayer))) {
                winningCells.add(new Point(i, 0));
                winningCells.add(new Point(i, 1));
                winningCells.add(new Point(i, 2));
                return true;
            }
        }
        // Check columns
        for (int j = 0; j < 3; j++) {
            if (buttons[0][j].getText().equals(String.valueOf(currentPlayer)) &&
                buttons[1][j].getText().equals(String.valueOf(currentPlayer)) &&
                buttons[2][j].getText().equals(String.valueOf(currentPlayer))) {
                winningCells.add(new Point(0, j));
                winningCells.add(new Point(1, j));
                winningCells.add(new Point(2, j));
                return true;
            }
        }
        // Check diagonals
        if (buttons[0][0].getText().equals(String.valueOf(currentPlayer)) &&
            buttons[1][1].getText().equals(String.valueOf(currentPlayer)) &&
            buttons[2][2].getText().equals(String.valueOf(currentPlayer))) {
            winningCells.add(new Point(0, 0));
            winningCells.add(new Point(1, 1));
            winningCells.add(new Point(2, 2));
            return true;
        }
        if (buttons[0][2].getText().equals(String.valueOf(currentPlayer)) &&
            buttons[1][1].getText().equals(String.valueOf(currentPlayer)) &&
            buttons[2][0].getText().equals(String.valueOf(currentPlayer))) {
            winningCells.add(new Point(0, 2));
            winningCells.add(new Point(1, 1));
            winningCells.add(new Point(2, 0));
            return true;
        }
        return false;
    }

    private boolean checkDraw() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().equals("")) {
                    return false;
                }
            }
        }
        return !checkWin();
    }

    private boolean isGameOver() {
        return checkWin() || checkDraw();
    }

    private void disableButtons() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private void resetGame() {
        System.out.println("Resetting game");
        winningCells.clear();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
        currentPlayer = 'X';
        gameActive = true;
        statusLabel.setText("Player X's turn");
        updateFontSizes();
    }

    public static void main(String[] args) {
        // Ensure Montserrat font is available
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, 
                TicTacToe.class.getResourceAsStream("/resources/Montserrat-Regular.ttf")));
        } catch (Exception e) {
            System.out.println("Warning: Montserrat font not found, using default font.");
        }
        SwingUtilities.invokeLater(() -> new TicTacToe());
    }
}