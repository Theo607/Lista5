import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.Vector;
import java.util.function.Consumer;

enum Tool {
    NONE, CIRCLE, RECTANGLE, PATH
}

class PaintMenuBar extends JMenuBar {
    public PaintMenuBar(Consumer<Tool> toolSelector, Consumer<Color> colorSelector, Consumer<Color> fillColorSelector, Consumer<Integer> strokeSelector, PaintCanvas canvas) {
        JMenu Info = new JMenu("Info");
        JMenu File = new JMenu("File");
        JMenu Tools = new JMenu("Tools");

        JMenuItem About = new JMenuItem("About");
        JMenuItem Help = new JMenuItem("Help");
        JMenuItem Exit = new JMenuItem("Exit");
        JMenuItem Save = new JMenuItem("Save");
        JMenuItem Open = new JMenuItem("Open");
        JMenuItem New = new JMenuItem("New");
        JMenuItem Circle = new JMenuItem("Circle");
        JMenuItem Rectangle = new JMenuItem("Rectangle");
        JMenuItem Path = new JMenuItem("Path");

        Circle.addActionListener(e -> toolSelector.accept(Tool.CIRCLE));
        Rectangle.addActionListener(e -> toolSelector.accept(Tool.RECTANGLE));
        Path.addActionListener(e -> toolSelector.accept(Tool.PATH)); // Not implemented in canvas yet

        Info.add(About);
        Info.add(Help);
        Info.add(Exit);

        File.add(Save);
        File.add(Open);
        File.add(New);

        Tools.add(Circle);
        Tools.add(Rectangle);
        Tools.add(Path);

        // About button functionality
        About.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "Paint Application\nVersion 1.0\nCreated by Mateusz Smuga\nSimple paint program to draw and manipulate shapes.", "About", JOptionPane.INFORMATION_MESSAGE);
        });

        // Help button functionality
        Help.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "Instructions:\n- Use the toolbar to select drawing tools (Circle, Rectangle, Path)."+
            "\n- Choose colors and stroke width from the settings.\n- Right-click to edit selected shapes.\n - Press E or R to rotate selected shape.", "Help", JOptionPane.INFORMATION_MESSAGE);
        });

        // Exit button functionality
        Exit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });


        JMenu Settings = new JMenu("Settings");

        // Outline color chooser item
        JMenuItem colorItem = new JMenuItem("Select Outline Color");
        colorItem.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(null, "Choose Outline Color", Color.BLACK);
            if (selected != null) {
                colorSelector.accept(selected);
            }
        });
        Settings.add(colorItem);

        // Fill color chooser item
        JMenuItem fillItem = new JMenuItem("Fill Shape");
        fillItem.addActionListener(e -> {
            JCheckBox fillCheck = new JCheckBox("Fill shape", false);
            int result = JOptionPane.showConfirmDialog(null, fillCheck, "Fill Shape", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                canvas.setFillShape(fillCheck.isSelected());
            }
        });
        Settings.add(fillItem);
        JMenuItem fillColorItem = new JMenuItem("Select Fill Color");
        fillColorItem.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(null, "Choose Fill Color", Color.WHITE);
            if (selected != null) {
                fillColorSelector.accept(selected);
            }
        });
        Settings.add(fillColorItem);

        // Stroke width selector
        JComboBox<Integer> strokeBox = new JComboBox<>(new Integer[] { 1, 2, 4, 6, 8, 10 });
        strokeBox.setSelectedItem(2);
        strokeBox.addActionListener(e -> strokeSelector.accept((Integer) strokeBox.getSelectedItem()));
        Settings.add(new JMenuItem("Stroke Width")); // optional label
        Settings.add(strokeBox);

        this.add(Info);
        this.add(File);
        this.add(Tools);
        this.add(Settings);
    }
}

class PaintCanvas extends JPanel {
    private Vector<DrawableShape> shapes;
    private Figures figures;
    private Tool currentTool = Tool.NONE;

    private Point firstClick = null;
    private Path2D currentPath = null;

    private DrawableShape draggedShape = null;
    private Point lastMousePosition = null;

    private Point lastClickPoint = null;
    private int selectionCycleIndex = 0;

    private boolean fillShape = false;
    private Color currentOutlineColor = Color.BLACK;
    private Color currentFillColor = Color.WHITE; // Added field for fill color
    private int currentStrokeWidth = 2;

    public void setFillShape(boolean fillShape) {
        this.fillShape = fillShape;
    }

    public PaintCanvas(Figures figures) {
        this.figures = figures;
        this.shapes = figures.GetFigures();
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();

                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(p);
                    return;
                }

                // Skip selection if we're in drawing mode
                if (currentTool != Tool.NONE) {
                    // We are actively drawing; ignore the shape selection process.
                    handleClickForTool(p);
                    return;
                }

                // Shape selection logic (this part is only executed if no tool is selected)
                boolean hit = false;
                if (p.equals(lastClickPoint)) {
                    Vector<DrawableShape> hits = new Vector<>();
                    for (int i = shapes.size() - 1; i >= 0; i--) {
                        DrawableShape ds = shapes.get(i);
                        if (ds.contains(p)) {
                            hits.add(ds);
                        }
                    }
                    if (!hits.isEmpty()) {
                        selectionCycleIndex = (selectionCycleIndex + 1) % hits.size();
                        figures.deselectAll();
                        hits.get(selectionCycleIndex).setSelected(true);
                        hit = true;
                    }
                } else {
                    lastClickPoint = p;
                    selectionCycleIndex = 0;
                    for (int i = shapes.size() - 1; i >= 0; i--) {
                        DrawableShape ds = shapes.get(i);
                        if (ds.contains(p)) {
                            figures.deselectAll();
                            ds.setSelected(true);
                            hit = true;
                            break;
                        }
                    }
                }

                // If no shape was hit, we proceed with tool drawing logic
                if (!hit) {
                    figures.deselectAll();
                    handleClickForTool(p);
                }

                for (int i = shapes.size() - 1; i >= 0; i--) {
                    DrawableShape ds = shapes.get(i);
                    if (ds.selected && ds.contains(p)) {
                        draggedShape = ds;
                        lastMousePosition = p;
                        break;
                    }
                }

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedShape = null;
                lastMousePosition = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedShape != null && lastMousePosition != null) {
                    int dx = e.getX() - lastMousePosition.x;
                    int dy = e.getY() - lastMousePosition.y;
                    AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
                    draggedShape.shape = at.createTransformedShape(draggedShape.shape);
                    lastMousePosition = e.getPoint();
                    repaint();
                }
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setCurrentTool(Tool.NONE);
                }
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    figures.deleteSelected();
                    repaint();
                }
                if (currentTool == Tool.PATH && e.getKeyCode() == KeyEvent.VK_ENTER && currentPath != null) {
                    currentPath.closePath();
                    figures.AddPath(currentPath, currentOutlineColor, currentFillColor, fillShape, currentStrokeWidth);
                    currentPath = null;
                    firstClick = null;
                    currentTool = Tool.NONE;
                    repaint();
                }

                // Handle rotation key press
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    rotateSelectedShapes(1);  // Rotate 15 degrees clockwise
                } else if (e.getKeyCode() == KeyEvent.VK_E) {
                    rotateSelectedShapes(-1); // Rotate 15 degrees counterclockwise
                }
            }
        });

        addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            double scaleFactor = (rotation < 0) ? 1.1 : 0.9;
            for (DrawableShape ds : shapes) {
                if (ds.selected) {
                    Rectangle bounds = ds.getBounds();
                    double centerX = bounds.getCenterX();
                    double centerY = bounds.getCenterY();
                    AffineTransform at = AffineTransform.getTranslateInstance(centerX, centerY);
                    at.scale(scaleFactor, scaleFactor);
                    at.translate(-centerX, -centerY);
                    ds.shape = at.createTransformedShape(ds.shape);
                }
            }
            repaint();
        });
    }

    private void rotateSelectedShapes(double angle) {
        for (DrawableShape ds : shapes) {
            if (ds.selected) {
                Rectangle bounds = ds.getBounds();
                double centerX = bounds.getCenterX();
                double centerY = bounds.getCenterY();

                // Create the AffineTransform for rotation
                AffineTransform at = AffineTransform.getTranslateInstance(centerX, centerY);
                at.rotate(Math.toRadians(angle));  // Rotate the shape by the given angle in degrees
                at.translate(-centerX, -centerY);

                // Apply the transformation to the shape
                ds.shape = at.createTransformedShape(ds.shape);
            }
        }
        repaint();
    }

    private void showContextMenu(Point location) {
        for (DrawableShape ds : shapes) {
            if (ds.selected) {
                JCheckBox fillCheck = new JCheckBox("Fill shape", ds.filled);
                JButton fillBtn = new JButton("Select fill color");
                JButton outlineBtn = new JButton("Select outline color");

                final Color[] fillColor = {ds.fillColor != null ? ds.fillColor : Color.WHITE};
                final Color[] outlineColor = {ds.outlinecolor};

                fillBtn.addActionListener(e -> {
                    Color chosen = JColorChooser.showDialog(null, "Choose Fill Color", fillColor[0]);
                    if (chosen != null) fillColor[0] = chosen;
                });

                outlineBtn.addActionListener(e -> {
                    Color chosen = JColorChooser.showDialog(null, "Choose Outline Color", outlineColor[0]);
                    if (chosen != null) outlineColor[0] = chosen;
                });

                JPanel panel = new JPanel(new GridLayout(0, 1));
                panel.add(fillCheck);
                panel.add(fillBtn);
                panel.add(outlineBtn);

                int result = JOptionPane.showConfirmDialog(null, panel, "Shape Options", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    ds.filled = fillCheck.isSelected();
                    ds.fillColor = fillColor[0];
                    ds.outlinecolor = outlineColor[0];
                    repaint();
                }
                break;
            }
        }
    }

    public void setCurrentTool(Tool tool) {
        currentTool = tool;
        firstClick = null;
        currentPath = null;
        figures.deselectAll();
        repaint();
        requestFocusInWindow();
    }

    public void setCurrentColor(Color c) {
        currentOutlineColor = c;
    }

    public void setCurrentFillColor(Color c) { // Added this method to set the fill color
        currentFillColor = c;
    }

    public void setCurrentStrokeWidth(int w) {
        currentStrokeWidth = w;
    }

    private void handleClickForTool(Point p) {
        switch (currentTool) {
            case CIRCLE:
                if (firstClick == null) {
                    firstClick = p;
                } else {
                    int dx = p.x - firstClick.x;
                    int dy = p.y - firstClick.y;
                    int radius = (int) Math.sqrt(dx * dx + dy * dy);
                    figures.AddCircle(firstClick.x, firstClick.y, radius, currentOutlineColor, currentFillColor, fillShape, currentStrokeWidth);
                    shapes.lastElement().fillColor = currentFillColor; // Set fill color for new circle
                    firstClick = null;
                    currentTool = Tool.NONE;
                }
                break;
            case RECTANGLE:
                if (firstClick == null) {
                    firstClick = p;
                } else {
                    int x = Math.min(firstClick.x, p.x);
                    int y = Math.min(firstClick.y, p.y);
                    int width = Math.abs(p.x - firstClick.x);
                    int height = Math.abs(p.y - firstClick.y);
                    figures.AddRectangle(x, y, width, height, currentOutlineColor, currentFillColor, fillShape, currentStrokeWidth);
                    shapes.lastElement().fillColor = currentFillColor; // Set fill color for new rectangle
                    firstClick = null;
                    currentTool = Tool.NONE;
                }
                break;
            case PATH:
                if (currentPath == null) {
                    currentPath = new Path2D.Double();
                    currentPath.moveTo(p.x, p.y);
                } else {
                    currentPath.lineTo(p.x, p.y);
                }
                break;
            default:
                break;
        }
        repaint();
    }
    

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        for (DrawableShape ds : shapes) {
            ds.draw(g2d);
        }
        if (currentTool == Tool.PATH && currentPath != null) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(currentStrokeWidth));
            g2d.draw(currentPath);
        }
    }
}

class DrawableShape {
    public Shape shape;
    public Color outlinecolor;
    public Color fillColor;
    public boolean filled;
    public int strokeWidth;
    public boolean selected;

    public DrawableShape(Shape shape, Color colorOutline, Color colorFill, boolean filled, int strokeWidth) {
        this.shape = shape;
        this.outlinecolor = colorOutline;
        this.fillColor = colorFill;
        this.filled = filled;
        this.strokeWidth = strokeWidth;
        this.selected = false;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean contains(Point p) {
        return shape.contains(p);
    }

    public Rectangle getBounds() {
        return shape.getBounds();
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    public void setStrokeWidth(int width) {
        this.strokeWidth = width;
    }

    public void setOutlineColor(Color color) {
        this.outlinecolor = color;
    }

    public void draw(Graphics2D g2d) {
        if (filled) {
            g2d.setColor(fillColor);
            g2d.fill(shape);  // This ensures the path is filled if 'filled' is true
        }
        g2d.setColor(outlinecolor);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.draw(shape);  // Draw the outline of the shape
    
        if (selected) {
            g2d.setColor(Color.RED);
            g2d.draw(shape.getBounds());  // Drawing the selection outline
        }
    }
    
}

class Figures {
    private Vector<DrawableShape> shapes = new Vector<>();

    public void AddCircle(int x, int y, int radius, Color colorOutline, Color colorFill, boolean filled, int strokeWidth) {
        Shape shape = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
        shapes.add(new DrawableShape(shape, colorOutline, colorFill, filled, strokeWidth));
    }

    public void AddRectangle(int x, int y, int width, int height, Color colorOutline, Color colorFill, boolean filled, int strokeWidth) {
        Shape shape = new Rectangle2D.Double(x, y, width, height);
        shapes.add(new DrawableShape(shape, colorOutline, colorFill, filled, strokeWidth));
    }

    public void AddPath(Shape path, Color colorOutline, Color colorFill, boolean filled, int strokeWidth) {
        shapes.add(new DrawableShape(path, colorOutline, colorFill, filled, strokeWidth));
    }

    public Vector<DrawableShape> GetFigures() {
        return shapes;
    }

    public void deleteSelected() {
        shapes.removeIf(shape -> shape.selected);
    }

    public void deselectAll() {
        for (DrawableShape shape : shapes) {
            shape.setSelected(false);
        }
    }
}

class Paint {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Paint");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);

        Figures figures = new Figures();
        PaintCanvas canvas = new PaintCanvas(figures);

        PaintMenuBar menuBar = new PaintMenuBar(canvas::setCurrentTool, canvas::setCurrentColor, canvas::setCurrentFillColor, canvas::setCurrentStrokeWidth, canvas);
        frame.setJMenuBar(menuBar);
        frame.add(canvas);
        new Timer(16, e -> canvas.repaint()).start();
        frame.setVisible(true);
    }
}