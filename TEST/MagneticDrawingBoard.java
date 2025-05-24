package TEST;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

public class MagneticDrawingBoard extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private BufferedImage canvas;
    private double scale = 1.0;
    private int offsetX = 0, offsetY = 0;
    private boolean isDrawing = false;
    private boolean isErasing = false;
    private boolean isPanning = false;
    private Point lastDrawPos = null;
    private Point lastErasePos = null;
    private Point lastPanPos = null;
    public static int Height = 1000;
    public static int Width = 1500;
    public Eraser eraser;
    private java.util.List<Line> strokes = new ArrayList<>();
    public BufferedImage backGround;
    public Point p1, p2, p3, p4;

    public MagneticDrawingBoard(int width, int height) {
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        eraser = new Eraser();
        try {
            backGround = javax.imageio.ImageIO.read(new java.io.File("Pic/khung.png"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        p1 = new Point(10, 10);
        p2 = new Point(10, Height - 60);
        p4 = new Point(Width - 33, 10);
        p3 = new Point(Width - 33, Height - 60);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(backGround, 0, 2, Width - 17, Height - 39, null);
        drawHexGrid(g2);

        g2.setTransform(AffineTransform.getTranslateInstance(offsetX, offsetY));
        g2.scale(scale, scale);
        g2.drawImage(canvas, 0, 0, null);

        // Vẽ thanh tẩy không bị ảnh hưởng bởi scale và offset (vẽ ở toạ độ màn hình)
        g2.setTransform(new AffineTransform()); 
        eraser.drawEraser(g2);
        eraseLine(eraser.point1, eraser.point2);
        eraseLine(p1, p2);
        eraseLine(p2, p3);
        eraseLine(p3, p4);
        eraseLine(p1, p4);
    }


    private Point toCanvasCoords(Point p) {
        return new Point((int) ((p.x - offsetX) / scale), (int) ((p.y - offsetY) / scale));
    }

    private void drawLine(Point p1, Point p2) {
        Graphics2D g2 = canvas.createGraphics();
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.BLACK);
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        g2.dispose();
        strokes.add(new Line(p1, p2));
        repaint();
    }

    private void eraseAt(Point pos) {
        Graphics2D g2 = canvas.createGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillOval(pos.x - 20, pos.y - 20, 40, 40);
        g2.dispose();
        repaint();
    }

    private void eraseLine(Point p1, Point p2) {
        double dist = p1.distance(p2);
        int steps = (int) Math.ceil(dist / 5.0);
        for (int i = 0; i <= steps; i++) {
            int x = (int) (p1.x + (p2.x - p1.x) * i / (double) steps);
            int y = (int) (p1.y + (p2.y - p1.y) * i / (double) steps);
            eraseAt(new Point(x, y));
        }
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = toCanvasCoords(e.getPoint());
    
        // Chuyển tọa độ sang tọa độ canvas với scale, offset

        // Kiểm tra nếu chuột nhấn trên eraser (theo tọa độ gốc)
        int eraserLeft = eraser.x - 20;
        int eraserRight = eraser.x + 20; // chiều rộng eraser giả định 40px

        if (p.x >= eraserLeft && p.x <= eraserRight) {
            eraser.isMovingEraser = true;
            eraser.eraserDragStartPoint = e.getPoint();
            eraser.eraserStartX = eraser.x;
            return; // ưu tiên di chuyển eraser nên thoát luôn
        }

        // Phần còn lại giữ nguyên
        if (SwingUtilities.isLeftMouseButton(e)) {
            isDrawing = true;
            lastDrawPos = p;
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            isPanning = true;
            lastPanPos = e.getPoint();
        } else if (SwingUtilities.isRightMouseButton(e)) {
            isErasing = true;
            lastErasePos = p;
            eraseAt(p);
        }
    }


    @Override
    public void mouseReleased(MouseEvent e) {
        isDrawing = false;
        isErasing = false;
        isPanning = false;
        eraser.isMovingEraser = false;

        lastDrawPos = null;
        lastErasePos = null;
        lastPanPos = null;
        eraser.eraserDragStartPoint = null;
    }


    @Override
public void mouseDragged(MouseEvent e) {
    Point currentCanvasPos = toCanvasCoords(e.getPoint());

    if (eraser.isMovingEraser && eraser.eraserDragStartPoint != null) {
        // Tính delta x của chuột
        int deltaX = e.getPoint().x - eraser.eraserDragStartPoint.x;
        int newEraserX = eraser.eraserStartX + deltaX;

        eraser.setX(newEraserX);
        repaint();
        return; // ưu tiên di chuyển eraser nên không vẽ hay xóa khác
    }

    if (isDrawing && lastDrawPos != null) {
        drawLine(lastDrawPos, currentCanvasPos);
        lastDrawPos = currentCanvasPos;
    } else if (isErasing && lastErasePos != null) {
        eraseLine(lastErasePos, currentCanvasPos);
        lastErasePos = currentCanvasPos;
    } 
    eraseLine(eraser.point1, eraser.point2);
    // else if (isPanning && lastPanPos != null) {
    //     Point now = e.getPoint();
    //     offsetX += now.x - lastPanPos.x;
    //     offsetY += now.y - lastPanPos.y;
    //     lastPanPos = now;
    //     repaint();
    // }
}


    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // double zoomFactor = e.getPreciseWheelRotation() < 0 ? 1.1 : 0.9;
        // Point mouse = e.getPoint();
        // Point beforeZoom = toCanvasCoords(mouse);
        // scale *= zoomFactor;
        // Point afterZoom = toCanvasCoords(mouse);
        // offsetX += (afterZoom.x - beforeZoom.x) * scale;
        // offsetY += (afterZoom.y - beforeZoom.y) * scale;
        // repaint();
    }

    ///////////////////////////
    void drawHexGrid(Graphics2D g) {
        double size = 20;
        double hexHeight = Math.sqrt(3) * size;
        double hexWidth = 2 * size;
        double vertDist = hexHeight;
        double horizDist = 0.75 * hexWidth;

        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(0.5f));

        int cols = (int) (getWidth() / horizDist / scale) + 2;
        int rows = (int) (getHeight() / vertDist / scale) + 2;

        for (int col = -1; col < cols; col++) {
            for (int row = -1; row < rows; row++) {
                double x = col * horizDist;
                double y = row * vertDist + ((col % 2 == 0) ? 0 : vertDist / 2);
                drawHex(g, x, y, size);
            }
        }
        // g.setColor(Color.BLACK);
        // g.fillRect(0, 0, Width, 30);
        // g.fillRect(0, 0, 30, Height);
        // g.fillRect(Width - 45, 0, 30, Height);
        // g.fillRect(0, Height - 65, Width, 30);
    }

    void drawHex(Graphics2D g, double x, double y, double size) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i;
            double px = x + size * Math.cos(angle);
            double py = y + size * Math.sin(angle);
            if (i == 0) path.moveTo(px, py);
            else path.lineTo(px, py);
        }
        path.closePath();
        g.draw(path);
    }
    /// ///////////////////////////
    // Unused methods
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    private static class Line {
        Point p1, p2;
        Line(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Magnetic Drawing Board (Java Swing)");
            MagneticDrawingBoard board = new MagneticDrawingBoard(Width, Height);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JScrollPane(board));
            frame.setSize(Width, Height);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
