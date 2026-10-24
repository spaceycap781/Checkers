import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.util.List;

public class CheckersPanel extends JPanel {

    private CheckersGame game;
    private boolean restartHovered = false;
    private boolean playAgainHovered = false;

    private final int rBtnX = 660;
    private final int rBtnY = 818;
    private final int rBtnW = 100;
    private final int rBtnH = 44;

    public CheckersPanel(CheckersGame game) {
        this.game = game;
        setPreferredSize(new Dimension(800, 880));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
        });
    }

    private void handleMouseClick(int mouseX, int mouseY) {
        int winner = game.getWinner();

        if (winner != 0) {
            if (mouseX >= 310 && mouseX <= 490 && mouseY >= 490 && mouseY <= 536) {
                game.resetGame();
                playAgainHovered = false;
                repaint();
            }
            return;
        }

        if (mouseY >= 800) {
            if (mouseX >= rBtnX && mouseX <= rBtnX + rBtnW &&
                mouseY >= rBtnY && mouseY <= rBtnY + rBtnH) {
                game.resetGame();
                repaint();
            }
            return;
        }

        int tileSize = 800 / 8;
        int row = mouseY / tileSize;
        int col = mouseX / tileSize;

        if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            return;
        }

        int selRow = game.getSelectedRow();
        int selCol = game.getSelectedCol();

        if (selRow == -1) {
            game.selectPiece(row, col);
        } else {
            List<CheckersGame.Move> moves = game.getValidMovesForPiece(selRow, selCol);
            boolean moved = false;

            for (CheckersGame.Move m : moves) {
                if (m.toRow == row && m.toCol == col) {
                    game.makeMove(row, col);
                    moved = true;
                    break;
                }
            }

            if (!moved) {
                if (!game.selectPiece(row, col)) {
                    game.clearSelection();
                }
            }
        }

        repaint();
    }

    private void handleMouseMove(int mouseX, int mouseY) {
        int winner = game.getWinner();

        if (winner != 0) {
            boolean over = mouseX >= 310 && mouseX <= 490 && mouseY >= 490 && mouseY <= 536;

            if (over != playAgainHovered) {
                playAgainHovered = over;
                repaint();
            }
            return;
        }

        boolean over = mouseX >= rBtnX && mouseX <= rBtnX + rBtnW &&
                       mouseY >= rBtnY && mouseY <= rBtnY + rBtnH;

        if (over != restartHovered) {
            restartHovered = over;
            repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int tileSize = 800 / 8;
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBoard(g2, tileSize);
        drawSelectionAndMoves(g2, tileSize);
        drawPieces(g2, tileSize);
        drawBottomHUD(g2);

        int winner = game.getWinner();
        if (winner != 0) {
            drawWinOverlay(g2, winner);
        }
    }

    private void drawBoard(Graphics2D g2, int tileSize) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int x = col * tileSize;
                int y = row * tileSize;

                Color c1;
                Color c2;

                if ((row + col) % 2 == 0) {
                    c1 = new Color(245, 245, 220);
                    c2 = new Color(225, 225, 190);
                } else {
                    c1 = new Color(101, 67, 33);
                    c2 = new Color(60, 40, 20);
                }

                g2.setPaint(new GradientPaint(x, y, c1, x + tileSize, y + tileSize, c2));
                g2.fillRect(x, y, tileSize, tileSize);
            }
        }
    }

    private void drawSelectionAndMoves(Graphics2D g2, int tileSize) {
        int selRow = game.getSelectedRow();
        int selCol = game.getSelectedCol();

        if (selRow == -1) {
            return;
        }

        g2.setColor(new Color(255, 215, 0, 70));
        g2.fillRect(selCol * tileSize, selRow * tileSize, tileSize, tileSize);

        List<CheckersGame.Move> validMoves = game.getValidMovesForPiece(selRow, selCol);

        for (CheckersGame.Move m : validMoves) {
            int centerX = m.toCol * tileSize + tileSize / 2;
            int centerY = m.toRow * tileSize + tileSize / 2;

            g2.setColor(new Color(46, 204, 113, 140));
            g2.fillOval(centerX - 10, centerY - 10, 20, 20);

            g2.setColor(new Color(46, 204, 113, 180));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(m.toCol * tileSize + 15, m.toRow * tileSize + 15,
                        tileSize - 30, tileSize - 30);
        }
    }

    private void drawPieces(Graphics2D g2, int tileSize) {
        int selRow = game.getSelectedRow();
        int selCol = game.getSelectedCol();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = game.getPieceAt(row, col);

                if (piece != 0) {
                    int x = col * tileSize + 10;
                    int y = row * tileSize + 10;
                    int size = tileSize - 20;

                    boolean isKing = piece == 3 || piece == 4;

                    if (row == selRow && col == selCol) {
                        g2.setColor(new Color(255, 215, 0));
                        g2.setStroke(new BasicStroke(4));
                        g2.drawOval(x - 3, y - 3, size + 6, size + 6);
                    }

                    if (piece == 1 || piece == 3) {
                        g2.setPaint(new GradientPaint(x, y, new Color(255, 100, 100),
                                x + size, y + size, new Color(160, 0, 0)));
                    } else {
                        g2.setPaint(new GradientPaint(x, y, new Color(90, 90, 90),
                                x + size, y + size, new Color(10, 10, 10)));
                    }

                    g2.fillOval(x, y, size, size);

                    g2.setColor(new Color(255, 255, 255, 120));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(x, y, size, size);

                    g2.setColor(new Color(255, 255, 255, 45));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawOval(x + 12, y + 12, size - 24, size - 24);

                    if (isKing) {
                        drawCrown(g2, x + size / 2, y + size / 2);
                    }
                }
            }
        }
    }

    private void drawCrown(Graphics2D g2, int centerX, int centerY) {
        Path2D.Double crown = new Path2D.Double();

        crown.moveTo(centerX - 15, centerY + 8);
        crown.lineTo(centerX + 15, centerY + 8);
        crown.lineTo(centerX + 15, centerY - 8);
        crown.lineTo(centerX + 7, centerY);
        crown.lineTo(centerX, centerY - 15);
        crown.lineTo(centerX - 7, centerY);
        crown.lineTo(centerX - 15, centerY - 8);
        crown.closePath();

        g2.setPaint(new GradientPaint(centerX - 15, centerY - 15,
                new Color(255, 223, 0),
                centerX + 15, centerY + 8,
                new Color(200, 140, 0)));
        g2.fill(crown);

        g2.setColor(new Color(130, 90, 0));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(crown);
    }

    private void drawBottomHUD(Graphics2D g2) {
        g2.setPaint(new GradientPaint(0, 800, new Color(18, 18, 22),
                800, 880, new Color(34, 34, 38)));
        g2.fillRect(0, 800, 800, 80);

        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawLine(0, 800, 800, 800);

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();

        g2.setColor(new Color(255, 110, 110));
        g2.drawString("RED: " + game.getRedCount(), 75, 845);

        g2.setColor(new Color(210, 210, 210));
        g2.drawString("BLACK: " + game.getBlackCount(), 585, 845);

        drawTurnBox(g2);
        drawRestartButton(g2, fm);
    }

    private void drawTurnBox(Graphics2D g2) {
        int x = 290;
        int y = 818;
        int w = 220;
        int h = 44;

        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();

        String text;

        if (game.getWinner() != 0) {
            text = game.getWinner() == 1 ? "RED WINS!" : "BLACK WINS!";
        } else if (game.getTurn() == 1) {
            text = game.isJumpInProgress() ? "RED MUST JUMP!" : "RED'S TURN";
        } else {
            text = game.isJumpInProgress() ? "BLACK MUST JUMP!" : "BLACK'S TURN";
        }

        if (game.getTurn() == 1) {
            g2.setColor(new Color(90, 30, 30));
        } else {
            g2.setColor(new Color(55, 55, 60));
        }

        g2.fillRoundRect(x, y, w, h, 10, 10);

        g2.setColor(new Color(255, 255, 255, 90));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + 27);
    }

    private void drawRestartButton(Graphics2D g2, FontMetrics fm) {
        if (restartHovered) {
            g2.setColor(new Color(75, 75, 80));
        } else {
            g2.setColor(new Color(45, 45, 48));
        }

        g2.fillRoundRect(rBtnX, rBtnY, rBtnW, rBtnH, 10, 10);

        g2.setColor(new Color(255, 255, 255, 90));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(rBtnX, rBtnY, rBtnW, rBtnH, 10, 10);

        g2.setColor(Color.WHITE);
        g2.drawString("RESTART", rBtnX + (rBtnW - fm.stringWidth("RESTART")) / 2, rBtnY + 26);
    }

    private void drawWinOverlay(Graphics2D g2, int winner) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, 800, 880);

        int w = 550;
        int h = 320;
        int x = 125;
        int y = 280;

        g2.setPaint(new GradientPaint(x, y, new Color(30, 30, 34),
                x + w, y + h, new Color(15, 15, 18)));
        g2.fillRoundRect(x, y, w, h, 20, 20);

        if (winner == 1) {
            g2.setColor(new Color(255, 90, 90));
        } else {
            g2.setColor(new Color(210, 210, 210));
        }

        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, w, h, 20, 20);

        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        FontMetrics fmBig = g2.getFontMetrics();

        g2.setColor(new Color(255, 215, 0));
        String title = "VICTORY";
        g2.drawString(title, x + (w - fmBig.stringWidth(title)) / 2, y + 65);

        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fmWinner = g2.getFontMetrics();

        String winMsg = winner == 1 ? "RED WINS THE GAME" : "BLACK WINS THE GAME";
        g2.setColor(winner == 1 ? new Color(255, 110, 110) : new Color(220, 220, 220));
        g2.drawString(winMsg, x + (w - fmWinner.stringWidth(winMsg)) / 2, y + 120);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        FontMetrics fmSub = g2.getFontMetrics();

        g2.setColor(new Color(170, 170, 170));
        String subText = "All opponent pieces captured or blocked.";
        g2.drawString(subText, x + (w - fmSub.stringWidth(subText)) / 2, y + 160);

        int btnX = 310;
        int btnY = 490;
        int btnW = 180;
        int btnH = 46;

        if (playAgainHovered) {
            g2.setColor(new Color(75, 75, 80));
        } else {
            g2.setColor(new Color(45, 45, 48));
        }

        g2.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);

        g2.setColor(new Color(255, 255, 255, 90));
        g2.drawRoundRect(btnX, btnY, btnW, btnH, 10, 10);

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fmBtn = g2.getFontMetrics();

        g2.setColor(Color.WHITE);
        String btnText = "PLAY AGAIN";
        g2.drawString(btnText, btnX + (btnW - fmBtn.stringWidth(btnText)) / 2, btnY + 28);
    }
}