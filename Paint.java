// AWT and Swing for GUI components and event handling
import javax.swing.*; // Provides classes for building graphical user interfaces
import javax.swing.Timer; // Provides a timer for scheduling tasks
import javax.swing.filechooser.FileNameExtensionFilter; // Allows filtering file types in file choosers
import java.awt.*; // Provides classes for graphics, colors, and shapes
import java.awt.event.*; // Provides classes for handling user input events
import java.awt.geom.*; // Provides classes for geometric shapes and transformations
import java.awt.Shape; // Represents a geometric shape
import java.awt.BasicStroke; // Defines the stroke style for drawing shapes
import java.awt.Point; // Represents a point in 2D space
import java.awt.Rectangle; // Represents a rectangle in 2D space
import java.awt.geom.Path2D; // Represents a geometric path
import java.awt.geom.PathIterator; // Provides iteration over the segments of a path

// Data structures
import java.util.*; // Provides utility classes like Vector and List
import java.util.List; // Represents an ordered collection of elements
import java.util.Vector; // Represents a dynamic array
import java.util.function.Consumer; // Represents a function that accepts a single input and returns no result

// File I/O
import java.io.*; // Provides classes for file input and output

// Gson for JSON serialization and deserialization
import com.google.gson.Gson; // Provides methods for converting Java objects to JSON and vice versa
import com.google.gson.GsonBuilder; // Provides a builder for creating Gson instances

/**
 * Enum representing the available drawing tools in the Paint application.
 */
enum Tool {
    NONE,       // No tool selected
    CIRCLE,     // Circle drawing tool
    RECTANGLE,  // Rectangle drawing tool
    PATH        // Path drawing tool
}

/**
 * Represents the menu bar for the Paint application.
 * Provides options for file operations, tools, and settings.
 */
class PaintMenuBar extends JMenuBar {

    /**
     * Constructs the PaintMenuBar with the given callbacks for tool, color, and stroke selection.
     *
     * @param toolSelector       Callback for selecting a drawing tool.
     * @param colorSelector      Callback for selecting the outline color.
     * @param fillColorSelector  Callback for selecting the fill color.
     * @param strokeSelector     Callback for selecting the stroke width.
     * @param canvas             The PaintCanvas instance to interact with.
     */
    public PaintMenuBar(Consumer<Tool> toolSelector, Consumer<Color> colorSelector, Consumer<Color> fillColorSelector,
            Consumer<Integer> strokeSelector, PaintCanvas canvas) {
        // Create menus
        JMenu Info = new JMenu("Info"); // Menu for application information
        JMenu File = new JMenu("File"); // Menu for file operations
        JMenu Tools = new JMenu("Tools"); // Menu for drawing tools

        // Create menu items for Info menu
        JMenuItem About = new JMenuItem("About"); // Displays application information
        JMenuItem Help = new JMenuItem("Help"); // Displays usage instructions
        JMenuItem Exit = new JMenuItem("Exit"); // Exits the application

        // Create menu items for File menu
        JMenuItem Save = new JMenuItem("Save"); // Saves the current canvas to a file
        JMenuItem Open = new JMenuItem("Open"); // Opens a saved canvas from a file
        JMenuItem New = new JMenuItem("New"); // Creates a new canvas

        // Create menu items for Tools menu
        JMenuItem Circle = new JMenuItem("Circle"); // Selects the circle drawing tool
        JMenuItem Rectangle = new JMenuItem("Rectangle"); // Selects the rectangle drawing tool
        JMenuItem Path = new JMenuItem("Path"); // Selects the path drawing tool

        // Add action listeners for tool selection
        Circle.addActionListener(e -> toolSelector.accept(Tool.CIRCLE));
        Rectangle.addActionListener(e -> toolSelector.accept(Tool.RECTANGLE));
        Path.addActionListener(e -> toolSelector.accept(Tool.PATH));

        // Add menu items to Info menu
        Info.add(About);
        Info.add(Help);
        Info.add(Exit);

        // Add menu items to File menu
        File.add(Save);
        File.add(Open);
        File.add(New);

        // Add menu items to Tools menu
        Tools.add(Circle);
        Tools.add(Rectangle);
        Tools.add(Path);

        // Add functionality for the "New" menu item
        New.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to create a new canvas? Unsaved changes will be lost.", "New Canvas", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                canvas.getFigures().clear(); // Clear all shapes from the canvas
                canvas.repaint(); // Repaint the canvas
            }
        });

        // Add functionality for the "Save" menu item
        Save.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json")); // Filter for JSON files

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // Ensure the file has a .json extension
                if (!file.getName().toLowerCase().endsWith(".json")) {
                    file = new File(file.getAbsolutePath() + ".json");
                }
                canvas.getFigures().saveToFile(file); // Save the shapes to the file
            }
        });

        // Add functionality for the "Open" menu item
        Open.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json")); // Filter for JSON files

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                canvas.getFigures().loadFromFile(file); // Load shapes from the file
                canvas.repaint(); // Repaint the canvas
            }
        });

        // Add functionality for the "About" menu item
        About.addActionListener(e -> {
            JOptionPane.showMessageDialog(null,
                    "Paint Application\nVersion 1.0\nCreated by Mateusz Smuga\nSimple paint program to draw and manipulate shapes.",
                    "About", JOptionPane.INFORMATION_MESSAGE);
        });

        // Add functionality for the "Help" menu item
        Help.addActionListener(e -> {
            JOptionPane.showMessageDialog(null,
                    "Instructions:\n- Use the toolbar to select drawing tools (Circle, Rectangle, Path)." +
                            "\n- Choose colors and stroke width from the settings.\n- Right-click to edit selected shapes.\n - Press E or R to rotate selected shape.",
                    "Help", JOptionPane.INFORMATION_MESSAGE);
        });

        // Add functionality for the "Exit" menu item
        Exit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Exit",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0); // Exit the application
            }
        });

        // Create the Settings menu
        JMenu Settings = new JMenu("Settings");

        // Add functionality for selecting the outline color
        JMenuItem colorItem = new JMenuItem("Select Outline Color");
        colorItem.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(null, "Choose Outline Color", Color.BLACK);
            if (selected != null) {
                colorSelector.accept(selected); // Set the selected outline color
            }
        });
        Settings.add(colorItem);

        // Add functionality for selecting the fill color
        JMenuItem fillColorItem = new JMenuItem("Select Fill Color");
        fillColorItem.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(null, "Choose Fill Color", Color.WHITE);
            if (selected != null) {
                fillColorSelector.accept(selected); // Set the selected fill color
            }
        });
        Settings.add(fillColorItem);

        // Add functionality for selecting the stroke width
        JComboBox<Integer> strokeBox = new JComboBox<>(new Integer[] { 1, 2, 4, 6, 8, 10 });
        strokeBox.setSelectedItem(2); // Default stroke width
        strokeBox.addActionListener(e -> strokeSelector.accept((Integer) strokeBox.getSelectedItem()));
        Settings.add(new JMenuItem("Stroke Width")); // Optional label
        Settings.add(strokeBox);

        // Add menus to the menu bar
        this.add(Info);
        this.add(File);
        this.add(Tools);
        this.add(Settings);
    }
}

/**
 * PaintCanvas is a custom JPanel that provides a drawing surface
 * for creating, selecting, transforming, and deleting shapes.
 * <p>
 * Supported tools include CIRCLE, RECTANGLE, and freehand PATH.
 * Shapes may be filled or outlined, colored, dragged, rotated,
 * and scaled via mouse and keyboard interactions.
 * </p>
 */
class PaintCanvas extends JPanel {

    /** The collection of drawable shapes currently on the canvas. */
    private Vector<DrawableShape> shapes;

    /** The Figures model managing creation/deletion of shapes. */
    private Figures figures;

    /** The currently selected drawing tool (or NONE). */
    private Tool currentTool = Tool.NONE;

    /** The first click point used when drawing two-click shapes (circle/rectangle). */
    private Point firstClick = null;

    /** The Path2D being built in PATH tool mode. */
    private Path2D currentPath = null;

    /** The shape currently being dragged by the user, if any. */
    private DrawableShape draggedShape = null;

    /** The last mouse position during dragging, used to compute deltas. */
    private Point lastMousePosition = null;

    /** The last click point used for cycling through overlapping shapes. */
    private Point lastClickPoint = null;

    /** Index into overlapping shapes for cycling selection. */
    private int selectionCycleIndex = 0;

    /** Whether new shapes should be filled (true) or only outlined (false). */
    private boolean fillShape = false;

    /** Current outline color for new shapes. */
    private Color currentOutlineColor = Color.BLACK;

    /** Current fill color for new shapes. */
    private Color currentFillColor = Color.WHITE;

    /** Current stroke width (outline thickness) for new shapes. */
    private int currentStrokeWidth = 2;


    /**
     * Constructs a PaintCanvas bound to the given Figures model.
     * <p>
     * This constructor initializes mouse, mouse-motion, key, and
     * mouse-wheel listeners to handle user interaction for drawing,
     * selecting, dragging, transforming, and deleting shapes.
     * </p>
     *
     * @param figures the Figures instance that manages shape data
     */
    public PaintCanvas(Figures figures) {
        this.figures = figures;
        this.shapes = figures.GetFigures();
        setBackground(Color.WHITE);

        // Mouse listener: handles mouse press (selection, drawing, context menu),
        // and mouse release (stop dragging).
        addMouseListener(new MouseAdapter() {
            /**
             * Called when the mouse is pressed. Handles:
             * - right-click for context menu,
             * - drawing-mode clicks,
             * - selection and cycling through overlapping shapes,
             * - beginning drag of a selected shape.
             */
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();

                // Show context menu on right-click
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(p);
                    return;
                }

                // If a drawing tool is active, handle as drawing click
                if (currentTool != Tool.NONE) {
                    handleClickForTool(p);
                    return;
                }

                // No tool: perform shape selection
                boolean hit = false;
                if (p.equals(lastClickPoint)) {
                    // Cycle through shapes under the same click point
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
                    // First click: select topmost shape or prepare for cycling
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

                // If no existing shape was hit, start drawing if a tool is set
                if (!hit) {
                    figures.deselectAll();
                    handleClickForTool(p);
                }

                // If a shape is selected at this point, begin dragging it
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

            /**
             * Called when the mouse is released. Stops any ongoing drag.
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                draggedShape = null;
                lastMousePosition = null;
            }
        });

        // Mouse-motion listener: handles dragging of selected shapes.
        addMouseMotionListener(new MouseMotionAdapter() {
            /**
             * Called when the mouse is dragged. Moves the dragged shape
             * by the delta from the last mouse position.
             */
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

        // Key listener: handles Escape (cancel tool), Delete (remove), Enter (finish path),
        // and R/E for rotation.
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            /**
             * Called when a key is pressed. Supports:
             * - ESC: cancel current tool
             * - DELETE: delete selected shapes
             * - ENTER (in PATH tool): close and add path
             * - R/E: rotate selected shapes by ±15°
             */
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
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    rotateSelectedShapes(15); // 15° clockwise
                } else if (e.getKeyCode() == KeyEvent.VK_E) {
                    rotateSelectedShapes(-15); // 15° counter-clockwise
                }
            }
        });

        // Mouse-wheel listener: handles scaling of selected shapes.
        addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            double scaleFactor = (rotation < 0) ? 1.1 : 0.9;
            for (DrawableShape ds : shapes) {
                if (ds.selected) {
                    Rectangle bounds = ds.getBounds();
                    double cx = bounds.getCenterX();
                    double cy = bounds.getCenterY();
                    AffineTransform at = AffineTransform.getTranslateInstance(cx, cy);
                    at.scale(scaleFactor, scaleFactor);
                    at.translate(-cx, -cy);
                    ds.shape = at.createTransformedShape(ds.shape);
                }
            }
            repaint();
        });
    }

    /**
     * Returns the Figures model associated with this canvas.
     *
     * @return the Figures instance managing shapes
     */
    public Figures getFigures() {
        return figures;
    }

    /**
     * Sets whether new shapes should be filled or only outlined.
     *
     * @param fillShape true to fill new shapes, false for outlines only
     */
    public void setFillShape(boolean fillShape) {
        this.fillShape = fillShape;
    }

    /**
     * Sets the current drawing tool.
     *
     * @param tool the Tool enum value (NONE, CIRCLE, RECTANGLE, PATH)
     */
    public void setCurrentTool(Tool tool) {
        this.currentTool = tool;
        this.firstClick = null;
        this.currentPath = null;
        figures.deselectAll();
        repaint();
        requestFocusInWindow();
    }

    /**
     * Sets the outline color for subsequently drawn shapes.
     *
     * @param c the Color to use for new shape outlines
     */
    public void setCurrentColor(Color c) {
        this.currentOutlineColor = c;
    }

    /**
     * Sets the fill color for subsequently drawn shapes.
     *
     * @param c the Color to use for new shape fills
     */
    public void setCurrentFillColor(Color c) {
        this.currentFillColor = c;
    }

    /**
     * Sets the stroke width (outline thickness) for subsequently drawn shapes.
     *
     * @param w the stroke width in pixels
     */
    public void setCurrentStrokeWidth(int w) {
        this.currentStrokeWidth = w;
    }

    /**
     * Paints all shapes and, if in PATH tool mode, the current in-progress path.
     *
     * @param g the Graphics context for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Draw each existing shape
        for (DrawableShape ds : shapes) {
            ds.draw(g2d);
        }
        // If drawing a freehand path, draw its current outline
        if (currentTool == Tool.PATH && currentPath != null) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(currentStrokeWidth));
            g2d.draw(currentPath);
        }
    }

    /**
     * Rotates each selected shape around its center by the specified angle.
     *
     * @param angleDegrees angle in degrees (positive = clockwise, negative = ccw)
     */
    private void rotateSelectedShapes(double angleDegrees) {
        for (DrawableShape ds : shapes) {
            if (ds.selected) {
                Rectangle bounds = ds.getBounds();
                double cx = bounds.getCenterX();
                double cy = bounds.getCenterY();
                AffineTransform at = AffineTransform.getTranslateInstance(cx, cy);
                at.rotate(Math.toRadians(angleDegrees));
                at.translate(-cx, -cy);
                ds.shape = at.createTransformedShape(ds.shape);
            }
        }
        repaint();
    }

    /**
     * Displays a context menu at the given point for the first selected shape.
     * <p>
     * Allows toggling fill, choosing fill color, and choosing outline color.
     * </p>
     *
     * @param location the point (in canvas coordinates) to show the menu
     */
    private void showContextMenu(Point location) {
        for (DrawableShape ds : shapes) {
            if (ds.selected) {
                JCheckBox fillCheck = new JCheckBox("Fill shape", ds.filled);
                JButton fillBtn = new JButton("Select fill color");
                JButton outlineBtn = new JButton("Select outline color");

                final Color[] fillColor = { ds.fillColor != null ? ds.fillColor : currentFillColor };
                final Color[] outlineColor = { ds.outlinecolor };

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

                int result = JOptionPane.showConfirmDialog(
                        null, panel, "Shape Options", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    ds.filled = fillCheck.isSelected();
                    ds.fillColor = fillColor[0];
                    ds.outlinecolor = outlineColor[0];
                    repaint();
                }
                break; // Only show menu for the first selected shape
            }
        }
    }

    /**
     * Handles mouse clicks when a drawing tool is active.
     * <p>
     * - For CIRCLE and RECTANGLE: first click sets start corner/center,
     *   second click finishes shape creation.
     * - For PATH: each click extends the current path.
     * </p>
     *
     * @param p the point where the mouse was clicked
     */
    private void handleClickForTool(Point p) {
        switch (currentTool) {
            case CIRCLE:
                if (firstClick == null) {
                    // First click records center of circle
                    firstClick = p;
                } else {
                    // Second click sets radius and adds circle
                    int dx = p.x - firstClick.x;
                    int dy = p.y - firstClick.y;
                    int radius = (int) Math.sqrt(dx * dx + dy * dy);
                    figures.AddCircle(
                        firstClick.x, firstClick.y, radius,
                        currentOutlineColor, currentFillColor,
                        fillShape, currentStrokeWidth
                    );
                    shapes.lastElement().fillColor = currentFillColor;
                    firstClick = null;
                    currentTool = Tool.NONE;
                }
                break;
            case RECTANGLE:
                if (firstClick == null) {
                    // First click records one corner
                    firstClick = p;
                } else {
                    // Second click defines opposite corner and adds rectangle
                    int x = Math.min(firstClick.x, p.x);
                    int y = Math.min(firstClick.y, p.y);
                    int w = Math.abs(p.x - firstClick.x);
                    int h = Math.abs(p.y - firstClick.y);
                    figures.AddRectangle(
                        x, y, w, h,
                        currentOutlineColor, currentFillColor,
                        fillShape, currentStrokeWidth
                    );
                    shapes.lastElement().fillColor = currentFillColor;
                    firstClick = null;
                    currentTool = Tool.NONE;
                }
                break;
            case PATH:
                if (currentPath == null) {
                    // Start a new path
                    currentPath = new Path2D.Double();
                    currentPath.moveTo(p.x, p.y);
                } else {
                    // Continue the existing path
                    currentPath.lineTo(p.x, p.y);
                }
                break;
            default:
                // No drawing tool active
                break;
        }
        repaint();
    }
}

/**
 * Represents a serializable version of a shape.
 * This class is used for saving and loading shapes to/from JSON files.
 */
class SerializableShape {
    /** The type of the shape (e.g., "CIRCLE", "RECTANGLE", "PATH"). */
    String type;

    /** The parameters defining the shape (e.g., coordinates, dimensions). */
    double[] params;

    /** The outline color of the shape in hexadecimal format (e.g., "#FF0000"). */
    String outlineColor;

    /** The fill color of the shape in hexadecimal format (e.g., "#00FF00"). */
    String fillColor;

    /** Whether the shape is filled (true) or only outlined (false). */
    boolean filled;

    /** The stroke width (outline thickness) of the shape. */
    int strokeWidth;

    /**
     * Constructs a SerializableShape with the given attributes.
     *
     * @param type          The type of the shape (e.g., "CIRCLE", "RECTANGLE", "PATH").
     * @param params        The parameters defining the shape (e.g., coordinates, dimensions).
     * @param outlineColor  The outline color of the shape in hexadecimal format.
     * @param fillColor     The fill color of the shape in hexadecimal format.
     * @param filled        Whether the shape is filled (true) or only outlined (false).
     * @param strokeWidth   The stroke width (outline thickness) of the shape.
     */
    public SerializableShape(String type, double[] params, String outlineColor, String fillColor, boolean filled,
            int strokeWidth) {
        this.type = type;
        this.params = params;
        this.outlineColor = outlineColor;
        this.fillColor = fillColor;
        this.filled = filled;
        this.strokeWidth = strokeWidth;
    }
}

/**
 * Represents a drawable shape on the canvas.
 * Contains the shape's geometry, colors, stroke width, and selection state.
 */
class DrawableShape {
    /** The geometric shape (e.g., circle, rectangle, or path). */
    public Shape shape;

    /** The outline color of the shape. */
    public Color outlinecolor;

    /** The fill color of the shape. */
    public Color fillColor;

    /** Whether the shape is filled (true) or only outlined (false). */
    public boolean filled;

    /** The stroke width (outline thickness) of the shape. */
    public int strokeWidth;

    /** Whether the shape is currently selected. */
    public boolean selected;

    /**
     * Constructs a DrawableShape with the given attributes.
     *
     * @param shape         The geometric shape.
     * @param colorOutline  The outline color of the shape.
     * @param colorFill     The fill color of the shape.
     * @param filled        Whether the shape is filled.
     * @param strokeWidth   The stroke width of the shape.
     */
    public DrawableShape(Shape shape, Color colorOutline, Color colorFill, boolean filled, int strokeWidth) {
        this.shape = shape;
        this.outlinecolor = colorOutline;
        this.fillColor = colorFill;
        this.filled = filled;
        this.strokeWidth = strokeWidth;
        this.selected = false;
    }

    /**
     * Sets the selection state of the shape.
     *
     * @param selected Whether the shape is selected.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Checks if the given point is contained within the shape.
     *
     * @param p The point to check.
     * @return True if the point is within the shape, false otherwise.
     */
    public boolean contains(Point p) {
        return shape.contains(p);
    }

    /**
     * Gets the bounding rectangle of the shape.
     *
     * @return The bounding rectangle of the shape.
     */
    public Rectangle getBounds() {
        return shape.getBounds();
    }

    /**
     * Sets the fill color of the shape.
     *
     * @param color The new fill color.
     */
    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    /**
     * Sets the stroke width of the shape.
     *
     * @param width The new stroke width.
     */
    public void setStrokeWidth(int width) {
        this.strokeWidth = width;
    }

    /**
     * Sets the outline color of the shape.
     *
     * @param color The new outline color.
     */
    public void setOutlineColor(Color color) {
        this.outlinecolor = color;
    }

    /**
     * Draws the shape on the given Graphics2D context.
     *
     * @param g2d The Graphics2D context to draw on.
     */
    public void draw(Graphics2D g2d) {
        if (filled) {
            g2d.setColor(fillColor);
            g2d.fill(shape);
        }
        g2d.setColor(outlinecolor);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.draw(shape);

        // Draw a red bounding box if the shape is selected
        if (selected) {
            g2d.setColor(Color.RED);
            g2d.draw(shape.getBounds());
        }
    }

    /**
     * Converts the DrawableShape to a SerializableShape for saving to JSON.
     *
     * @return The SerializableShape representation of this shape.
     */
    public SerializableShape toSerializableShape() {
        String type;
        double[] params;

        if (shape instanceof Ellipse2D.Double) {
            Ellipse2D.Double ellipse = (Ellipse2D.Double) shape;
            type = "CIRCLE";
            double centerX = ellipse.getCenterX();
            double centerY = ellipse.getCenterY();
            double radius = ellipse.width / 2;
            params = new double[] { centerX, centerY, radius };
        } else if (shape instanceof Rectangle2D.Double) {
            Rectangle2D.Double rect = (Rectangle2D.Double) shape;
            type = "RECTANGLE";
            params = new double[] { rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight() };
        } else if (shape instanceof Path2D.Double) {
            Path2D.Double path = (Path2D.Double) shape;

            // Ensure the path is closed before serializing
            path.closePath();

            PathIterator iterator = path.getPathIterator(null);
            List<Double> coordsList = new ArrayList<>();
            while (!iterator.isDone()) {
                double[] coords = new double[6];
                int typeSeg = iterator.currentSegment(coords);
                if (typeSeg != PathIterator.SEG_CLOSE) {
                    coordsList.add(coords[0]);
                    coordsList.add(coords[1]);
                }
                iterator.next();
            }
            type = "PATH";
            params = coordsList.stream().mapToDouble(Double::doubleValue).toArray();
        } else {
            type = "UNKNOWN";
            params = new double[] {};
        }

        return new SerializableShape(
                type,
                params,
                colorToHex(outlinecolor),
                colorToHex(fillColor),
                filled,
                strokeWidth);
    }

    /**
     * Converts a Color object to its hexadecimal string representation.
     *
     * @param color The Color object to convert.
     * @return The hexadecimal string representation of the color.
     */
    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}

/**
 * Manages a collection of drawable shapes on the canvas.
 * Provides methods for adding, deleting, selecting, and saving/loading shapes.
 */
class Figures {
    /** The collection of drawable shapes managed by this class. */
    private Vector<DrawableShape> shapes = new Vector<>();

    /**
     * Adds a circle to the collection of shapes.
     *
     * @param x            The x-coordinate of the circle's center.
     * @param y            The y-coordinate of the circle's center.
     * @param radius       The radius of the circle.
     * @param colorOutline The outline color of the circle.
     * @param colorFill    The fill color of the circle.
     * @param filled       Whether the circle is filled or only outlined.
     * @param strokeWidth  The stroke width of the circle's outline.
     */
    public void AddCircle(int x, int y, int radius, Color colorOutline, Color colorFill, boolean filled,
            int strokeWidth) {
        Shape shape = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
        shapes.add(new DrawableShape(shape, colorOutline, colorFill, filled, strokeWidth));
    }

    /**
     * Adds a rectangle to the collection of shapes.
     *
     * @param x            The x-coordinate of the rectangle's top-left corner.
     * @param y            The y-coordinate of the rectangle's top-left corner.
     * @param width        The width of the rectangle.
     * @param height       The height of the rectangle.
     * @param colorOutline The outline color of the rectangle.
     * @param colorFill    The fill color of the rectangle.
     * @param filled       Whether the rectangle is filled or only outlined.
     * @param strokeWidth  The stroke width of the rectangle's outline.
     */
    public void AddRectangle(int x, int y, int width, int height, Color colorOutline, Color colorFill, boolean filled,
            int strokeWidth) {
        Shape shape = new Rectangle2D.Double(x, y, width, height);
        shapes.add(new DrawableShape(shape, colorOutline, colorFill, filled, strokeWidth));
    }

    /**
     * Adds a path to the collection of shapes.
     *
     * @param path         The geometric path to add.
     * @param colorOutline The outline color of the path.
     * @param colorFill    The fill color of the path.
     * @param filled       Whether the path is filled or only outlined.
     * @param strokeWidth  The stroke width of the path's outline.
     */
    public void AddPath(Shape path, Color colorOutline, Color colorFill, boolean filled, int strokeWidth) {
        shapes.add(new DrawableShape(path, colorOutline, colorFill, filled, strokeWidth));
    }

    /**
     * Returns the collection of drawable shapes.
     *
     * @return A vector containing all drawable shapes.
     */
    public Vector<DrawableShape> GetFigures() {
        return shapes;
    }

    /**
     * Deletes all selected shapes from the collection.
     */
    public void deleteSelected() {
        shapes.removeIf(shape -> shape.selected);
    }

    /**
     * Deselects all shapes in the collection.
     */
    public void deselectAll() {
        for (DrawableShape shape : shapes) {
            shape.setSelected(false);
        }
    }

    /**
     * Clears all shapes from the collection.
     */
    public void clear() {
        shapes.clear();
    }

    /**
     * Saves the collection of shapes to a JSON file.
     *
     * @param file The file to save the shapes to.
     */
    public void saveToJson(File file) {
        List<SerializableShape> serializableShapes = new ArrayList<>();
        for (DrawableShape ds : shapes) {
            serializableShapes.add(ds.toSerializableShape());
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(serializableShapes, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads shapes from a JSON file into the collection.
     *
     * @param file The file to load the shapes from.
     */
    public void loadFromJson(File file) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(file)) {
            SerializableShape[] serializableShapes = gson.fromJson(reader, SerializableShape[].class);
            shapes.clear();
            for (SerializableShape ss : serializableShapes) {
                DrawableShape ds = fromSerializableShape(ss);
                if (ds != null) {
                    shapes.add(ds);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the collection of shapes to a file.
     *
     * @param file The file to save the shapes to.
     */
    public void saveToFile(File file) {
        try (Writer writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            List<SerializableShape> serializableShapes = new ArrayList<>();
            for (DrawableShape ds : shapes) {
                serializableShapes.add(ds.toSerializableShape());
            }
            gson.toJson(serializableShapes, writer);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving file: " + e.getMessage(), "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads shapes from a file into the collection.
     *
     * @param file The file to load the shapes from.
     */
    public void loadFromFile(File file) {
        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            SerializableShape[] serializableShapes = gson.fromJson(reader, SerializableShape[].class);
            shapes.clear();
            for (SerializableShape ss : serializableShapes) {
                DrawableShape ds = fromSerializableShape(ss);
                if (ds != null) {
                    shapes.add(ds);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading file: " + e.getMessage(), "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Converts a SerializableShape to a DrawableShape.
     *
     * @param ss The SerializableShape to convert.
     * @return The corresponding DrawableShape, or null if the conversion fails.
     */
    private DrawableShape fromSerializableShape(SerializableShape ss) {
        Color outlineColor = Color.decode(ss.outlineColor);
        Color fillColor = Color.decode(ss.fillColor);
        boolean filled = ss.filled;
        int strokeWidth = ss.strokeWidth;

        switch (ss.type) {
            case "CIRCLE":
                if (ss.params.length == 3) {
                    int x = (int) ss.params[0];
                    int y = (int) ss.params[1];
                    int radius = (int) ss.params[2];
                    Shape shape = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
                    return new DrawableShape(shape, outlineColor, fillColor, filled, strokeWidth);
                }
                break;
            case "RECTANGLE":
                if (ss.params.length == 4) {
                    int x = (int) ss.params[0];
                    int y = (int) ss.params[1];
                    int width = (int) ss.params[2];
                    int height = (int) ss.params[3];
                    Shape shape = new Rectangle2D.Double(x, y, width, height);
                    return new DrawableShape(shape, outlineColor, fillColor, filled, strokeWidth);
                }
                break;
            case "PATH":
                if (ss.params.length >= 4) {
                    Path2D.Double path = new Path2D.Double();
                    path.moveTo(ss.params[0], ss.params[1]);
                    for (int i = 2; i < ss.params.length; i += 2) {
                        path.lineTo(ss.params[i], ss.params[i + 1]);
                    }
                    // Close the path explicitly
                    path.closePath();
                    return new DrawableShape(path, outlineColor, fillColor, filled, strokeWidth);
                }
                break;
            default:
                break;
        }
        return null;
    }
}

/**
 * The main class for the Paint application.
 * <p>
 * This class initializes the application window, sets up the canvas, menu bar, 
 * and starts the main event loop for the application.
 * </p>
 */
class Paint {

    /**
     * The entry point of the Paint application.
     * <p>
     * This method creates the main application window, initializes the canvas 
     * and menu bar, and starts a timer to continuously repaint the canvas.
     * </p>
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // Create the main application window
        JFrame frame = new JFrame("Paint");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);

        // Initialize the Figures model to manage shapes
        Figures figures = new Figures();

        // Create the drawing canvas and bind it to the Figures model
        PaintCanvas canvas = new PaintCanvas(figures);

        // Create the menu bar and bind it to the canvas for tool and color selection
        PaintMenuBar menuBar = new PaintMenuBar(
                canvas::setCurrentTool, 
                canvas::setCurrentColor,
                canvas::setCurrentFillColor, 
                canvas::setCurrentStrokeWidth, 
                canvas
        );

        // Set up the menu bar and add the canvas to the frame
        frame.setJMenuBar(menuBar);
        frame.add(canvas);

        // Start a timer to repaint the canvas at regular intervals (60 FPS)
        new Timer(16, e -> canvas.repaint()).start();

        // Make the application window visible
        frame.setVisible(true);
    }
}