package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class Main extends JFrame implements ActionListener, KeyListener {

    // ── Palette ──────────────────────────────────────────────
    static final Color BG      = new Color(15, 15, 20),   SURFACE = new Color(24, 24, 32),
            DIGIT   = new Color(42, 42, 58),   OP      = new Color(56, 100, 230),
            EQUAL   = new Color(80, 200, 140), CLEAR   = new Color(210, 60, 70),
            FUNC    = new Color(80, 60, 110),  TEXT    = new Color(240, 240, 255),
            SUBTEXT = new Color(130, 130, 160);

    JTextField display;
    JLabel     exprLabel, histLabel;
    JButton[]  numbers = new JButton[10];
    JButton    add, sub, mul, div, eq, clear, dot, back,
            sqrt, sq, sin, cos, tan, pct, pm;

    String expression = "";
    final ArrayList<String> history = new ArrayList<>();

    Main() {
        setTitle("Calc");
        setSize(420, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // ── Display ──────────────────────────────────────────
        JPanel dp = roundPanel(SURFACE, 18);
        dp.setLayout(new BorderLayout());
        dp.setBorder(new EmptyBorder(16, 20, 16, 20));
        dp.setPreferredSize(new Dimension(0, 120));

        exprLabel = label("", 14f, SUBTEXT, SwingConstants.RIGHT);
        display   = new JTextField("0");
        display.setFont(font(48f)); display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false); display.setOpaque(false);
        display.setForeground(TEXT); display.setBorder(null);

        dp.add(exprLabel, BorderLayout.NORTH);
        dp.add(display,   BorderLayout.CENTER);
        root.add(dp, BorderLayout.NORTH);

        // ── Grid ─────────────────────────────────────────────
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(BG);
        grid.setBorder(new EmptyBorder(14, 0, 0, 0));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(5, 5, 5, 5);
        g.weightx = g.weighty = 1;

        // Build buttons
        sin  = btn("sin", FUNC);  cos  = btn("cos", FUNC);  tan = btn("tan", FUNC);
        sqrt = btn("√",   FUNC);  sq   = btn("x²",  FUNC);
        pct  = btn("%",   FUNC);  pm   = btn("+/-", FUNC);
        clear = btn("C",  CLEAR); back = btn("⌫",   DIGIT);
        div  = btn("÷",   OP);    mul  = btn("×",   OP);
        sub  = btn("−",   OP);    add  = btn("+",   OP);
        eq   = btn("=",   EQUAL);
        dot  = btn(".",   DIGIT);
        for (int i = 0; i < 10; i++) numbers[i] = btn(String.valueOf(i), DIGIT);

        // Row 0 – scientific
        g.ipady = 4;
        JButton[] row0 = {sin, cos, tan, sqrt, sq};
        for (int i = 0; i < row0.length; i++) cell(grid, g, row0[i], i, 0, 1, 1);

        // Rows 1–5 – main pad
        g.ipady = 12;
        Object[][] layout = {
                // {btn, col, row, colspan, rowspan}
                {pct,0,1,1,1}, {pm,1,1,1,1}, {clear,2,1,1,1}, {back,3,1,1,1}, {div,4,1,1,1},
                {numbers[7],0,2,1,1}, {numbers[8],1,2,1,1}, {numbers[9],2,2,1,1},
                {numbers[4],0,3,1,1}, {numbers[5],1,3,1,1}, {numbers[6],2,3,1,1},
                {numbers[1],0,4,1,1}, {numbers[2],1,4,1,1}, {numbers[3],2,4,1,1},
                {numbers[0],0,5,2,1}, {dot,2,5,1,1},
                {mul,3,2,1,1}, {add,3,3,1,1},
                {sub, 3,4,1,2},   // − spans rows 4-5 (same height as =)
                {eq,  4,2,1,4},   // = spans rows 2-5
        };
        for (Object[] e : layout)
            cell(grid, g, (JButton)e[0], (int)e[1], (int)e[2], (int)e[3], (int)e[4]);

        root.add(grid, BorderLayout.CENTER);

        // ── History strip ─────────────────────────────────────
        histLabel = label(" ", 11f, SUBTEXT, SwingConstants.RIGHT);
        histLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        histLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        root.add(histLabel, BorderLayout.SOUTH);

        addKeyListener(this);
        setFocusable(true);
        setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────
    void cell(JPanel p, GridBagConstraints g, JButton b, int x, int y, int w, int h) {
        g.gridx = x; g.gridy = y; g.gridwidth = w; g.gridheight = h;
        p.add(b, g);
        g.gridwidth = g.gridheight = 1;
    }

    JButton btn(String text, Color bg) {
        Color fg = bg == FUNC ? new Color(200, 190, 255) : Color.WHITE;
        float fs = bg == FUNC ? 14f : (bg == OP || bg == EQUAL ? 22f : 20f);
        JButton b = new JButton(text) {
            boolean hot;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hot = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hot = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hot ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()/2f, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        b.setFont(font(fs)); b.setForeground(fg);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(this);
        return b;
    }

    JLabel label(String t, float size, Color fg, int align) {
        JLabel l = new JLabel(t); l.setFont(font(size));
        l.setForeground(fg); l.setHorizontalAlignment(align); return l;
    }

    JPanel roundPanel(Color bg, int r) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
                g2.dispose(); super.paintComponent(g);
            }
            { setOpaque(false); }
        };
    }

    Font font(float size) { return new Font("SansSerif", Font.PLAIN, (int) size); }

    // ── Actions ───────────────────────────────────────────────
    @Override public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        for (int i = 0; i < 10; i++) if (s == numbers[i]) { appendDigit(String.valueOf(i)); return; }
        if (s == dot)   { appendDot();        return; }
        if (s == add)   { appendOp("+");      return; }
        if (s == sub)   { appendOp("−");      return; }
        if (s == mul)   { appendOp("×");      return; }
        if (s == div)   { appendOp("÷");      return; }
        if (s == pct)   { applyFn("pct");     return; }
        if (s == pm)    { applyFn("pm");      return; }
        if (s == clear) { clearAll();         return; }
        if (s == back)  { doBack();           return; }
        if (s == eq)    { calculate();        return; }
        if (s == sqrt)  { applyFn("sqrt");    return; }
        if (s == sq)    { applyFn("sq");      return; }
        if (s == sin)   { applyFn("sin");     return; }
        if (s == cos)   { applyFn("cos");     return; }
        if (s == tan)   { applyFn("tan");     return; }
    }

    void appendDigit(String d) {
        if (expression.equals("0") || expression.equals("Error")) expression = "";
        expression += d;
        display.setText(expression.isEmpty() ? "0" : expression);
        exprLabel.setText("");
    }

    void appendDot() {
        String[] parts = expression.split("[+\\-×÷]");
        String last = parts.length > 0 ? parts[parts.length - 1] : "";
        if (!last.contains(".")) { if (last.isEmpty()) expression += "0"; expression += "."; display.setText(expression); }
    }

    void appendOp(String op) {
        if (expression.isEmpty() || expression.equals("Error")) return;
        if ("+-×÷−".indexOf(expression.charAt(expression.length()-1)) != -1)
            expression = expression.substring(0, expression.length()-1);
        expression += op; display.setText(expression);
    }

    void clearAll()  { expression = ""; display.setText("0"); exprLabel.setText(""); }

    void doBack() {
        if (!expression.isEmpty() && !expression.equals("Error")) {
            expression = expression.substring(0, expression.length()-1);
            display.setText(expression.isEmpty() ? "0" : expression);
        }
    }

    void calculate() {
        try {
            double r = eval(expression.replace("×","*").replace("÷","/").replace("−","-"));
            String entry = expression + " = " + fmt(r);
            history.add(entry);
            histLabel.setText(entry + "  ");
            exprLabel.setText(expression + " =");
            expression = fmt(r); display.setText(expression);
        } catch (Exception ex) { display.setText("Error"); exprLabel.setText(""); expression = "Error"; }
    }

    void applyFn(String fn) {
        try {
            double v = Double.parseDouble(expression.replace("×","*").replace("÷","/").replace("−","-"));
            double r = switch (fn) {
                case "sqrt" -> Math.sqrt(v);
                case "sq"   -> v * v;
                case "sin"  -> Math.sin(Math.toRadians(v));
                case "cos"  -> Math.cos(Math.toRadians(v));
                case "tan"  -> Math.tan(Math.toRadians(v));
                case "pct"  -> v / 100.0;
                case "pm"   -> -v;
                default -> v;
            };
            String lbl = switch (fn) {
                case "sqrt" -> "√(" + expression + ")";
                case "sq"   -> "(" + expression + ")²";
                case "pct"  -> expression + "%";
                case "pm"   -> "+/-(" + expression + ")";
                default     -> fn + "(" + expression + "°)";
            };
            exprLabel.setText(lbl + " =");
            expression = fmt(r); display.setText(expression);
        } catch (Exception ex) { display.setText("Error"); expression = "Error"; }
    }

    // ── Formatter ─────────────────────────────────────────────
    String fmt(double v) {
        if (Double.isNaN(v))      return "Error";
        if (Double.isInfinite(v)) return v > 0 ? "Infinity" : "-Infinity";
        double r = Math.round(v * 1e10) / 1e10;
        if (r == Math.floor(r) && Math.abs(r) < 1e12) return String.valueOf((long) r);
        return String.format("%.10f", r).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    // ── Evaluator ─────────────────────────────────────────────
    double eval(String exp) {
        ArrayList<Double>    nums = new ArrayList<>();
        ArrayList<Character> ops  = new ArrayList<>();
        StringBuilder num = new StringBuilder();
        for (int i = 0; i < exp.length(); i++) {
            char c = exp.charAt(i);
            if (Character.isDigit(c) || c == '.' ||
                    (c == '-' && (i == 0 || "+-*/".indexOf(exp.charAt(i-1)) != -1))) num.append(c);
            else if ("+-*/".indexOf(c) != -1) {
                if (num.length() > 0) { nums.add(Double.parseDouble(num.toString())); num.setLength(0); }
                ops.add(c);
            }
        }
        if (num.length() > 0) nums.add(Double.parseDouble(num.toString()));
        for (int i = 0; i < ops.size(); i++) {
            if (ops.get(i) == '*' || ops.get(i) == '/') {
                double r = ops.get(i) == '*' ? nums.get(i) * nums.get(i+1) : nums.get(i) / nums.get(i+1);
                nums.set(i, r); nums.remove(i+1); ops.remove(i--);
            }
        }
        double r = nums.get(0);
        for (int i = 0; i < ops.size(); i++)
            r = ops.get(i) == '+' ? r + nums.get(i+1) : r - nums.get(i+1);
        return r;
    }

    // ── Keyboard ──────────────────────────────────────────────
    @Override public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (Character.isDigit(c))      { appendDigit(String.valueOf(c)); return; }
        if (c == '+')                  { appendOp("+");  return; }
        if (c == '-')                  { appendOp("−");  return; }
        if (c == '*')                  { appendOp("×");  return; }
        if (c == '/')                  { appendOp("÷");  return; }
        if (c == '.')                    appendDot();
        if (c == '\n' || c == '=')       calculate();
        if (c == '\b')                   doBack();
    }
    @Override public void keyPressed(KeyEvent e)  { if (e.getKeyCode() == KeyEvent.VK_DELETE) clearAll(); }
    @Override public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        try { com.formdev.flatlaf.FlatDarkLaf.setup(); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(Main::new);
    }
}