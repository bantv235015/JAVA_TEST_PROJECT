package TEST;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class Eraser {
    public int x = 20;
    public Point point1;
    public Point point2;
    public boolean isMovingEraser = false;
    public Point eraserDragStartPoint = null;
    public int eraserStartX = 0;

    public Eraser() {
        point1 = new Point(x, 0);
        point2 = new Point(x, MagneticDrawingBoard.Height);
    }
    public void drawEraser(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x - 20, 0, 40, MagneticDrawingBoard.Height);
    }
    public void setX(int newX) {
        // Giới hạn trong phạm vi cho phép
        if (newX < 0) newX = 0;
        if (newX > MagneticDrawingBoard.Width - 40) newX = MagneticDrawingBoard.Width - 40; // 40 là chiều rộng eraser
        x = newX;
        point1.setLocation(x, 0);
        point2.setLocation(x, MagneticDrawingBoard.Height);
    }
    
}
