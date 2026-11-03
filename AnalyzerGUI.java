package resumeanalyzer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * AnalyzerGUI — Formal, professional Resume Analyzer
 * Palette: Deep Lilac / Lavender / Slate — no particle animations
 */
public class AnalyzerGUI {

    // ── Colour Palette ────────────────────────────────────────────────────────
    private static final Color C_BG           = new Color(245, 243, 250);   // off-white lavender
    private static final Color C_SURFACE      = new Color(255, 255, 255);   // card white
    private static final Color C_SURFACE_ALT  = new Color(240, 236, 248);   // tinted panel
    private static final Color C_PRIMARY      = new Color(89,  52, 145);    // deep lilac / plum
    private static final Color C_PRIMARY_LT   = new Color(120, 80, 190);    // medium lilac
    private static final Color C_ACCENT       = new Color(162, 116, 228);   // soft violet
    private static final Color C_ACCENT_LT    = new Color(209, 185, 248);   // pale lavender
    private static final Color C_BORDER       = new Color(210, 198, 235);   // muted border
    private static final Color C_TEXT_DARK    = new Color(30,  20,  55);    // near-black
    private static final Color C_TEXT_MED     = new Color(85,  65, 120);    // mid lilac text
    private static final Color C_TEXT_LIGHT   = new Color(150, 130, 180);   // subtle label
    private static final Color C_SUCCESS      = new Color(52, 168, 120);    // emerald
    private static final Color C_WARN         = new Color(220, 160,  50);   // amber
    private static final Color C_DANGER       = new Color(192,  70,  90);   // rose

    // Graph bar colours (ranked 1→N)
    private static final Color[] BAR_COLORS = {
        new Color(89,  52, 145),   // rank 1 – deep plum
        new Color(120, 80, 190),   // rank 2 – medium lilac
        new Color(162, 116, 228),  // rank 3 – violet
        new Color(196, 160, 240),  // rank 4 – soft lavender
        new Color(220, 196, 250),  // rank 5 – pale lavender
    };

    // ── Shared state ──────────────────────────────────────────────────────────
    private static JTextArea     resultAreaRef;
    private static JTextArea     jdInputRef;
    private static JLabel        statusLabelRef;
    private static JLabel        statusBarLabelRef;
    private static JPanel        graphPanel;
    private static List<Double>  scores    = new ArrayList<>();
    private static List<String>  names     = new ArrayList<>();

    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(AnalyzerGUI::buildUI);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BUILD MAIN WINDOW
    // ════════════════════════════════════════════════════════════════════════
    private static void buildUI() {
        JFrame frame = new JFrame("Resume Analyzer — Professional Edition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1160, 800);
        frame.setMinimumSize(new Dimension(960, 680));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        frame.setContentPane(root);

        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ════════════════════════════════════════════════════════════════════════
    private static JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_PRIMARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle horizontal rule at bottom
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 28, 16, 28));

        // Left: logo area
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("RA");
        logo.setFont(new Font("Georgia", Font.BOLD, 18));
        logo.setForeground(C_PRIMARY);
        logo.setOpaque(true);
        logo.setBackground(Color.WHITE);
        logo.setPreferredSize(new Dimension(40, 40));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setVerticalAlignment(SwingConstants.CENTER);
        logo.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel title = new JLabel("Resume Analyzer");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel sep = new JLabel("  |  Professional Edition");
        sep.setFont(new Font("Georgia", Font.PLAIN, 13));
        sep.setForeground(new Color(200, 180, 235));

        left.add(logo);
        left.add(title);
        left.add(sep);

        // Right: pill tags
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(headerPill("Scoring"));
        right.add(headerPill("Suggestions"));
        right.add(headerPill("Job Match"));
        right.add(headerPill("Ranking"));

        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private static JLabel headerPill(String text) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(new Color(230, 215, 255));
        l.setBorder(new EmptyBorder(5, 12, 5, 12));
        l.setOpaque(false);
        return l;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CENTER SPLIT
    // ════════════════════════════════════════════════════════════════════════
    private static JComponent buildCenter() {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            buildSidebar(),
            buildMainArea()
        );
        split.setDividerLocation(280);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(C_BORDER);
        split.setOpaque(true);
        return split;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ════════════════════════════════════════════════════════════════════════
    private static JPanel buildSidebar() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(C_SURFACE);
        outer.setBorder(new MatteBorder(0, 0, 0, 1, C_BORDER));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(C_SURFACE);
        inner.setBorder(new EmptyBorder(22, 20, 20, 20));

        // ── Job Description ───────────────────────────────────────────────
        inner.add(sectionHeader("Job Description"));
        inner.add(vGap(8));

        JTextArea jdInput = new JTextArea(
            "Senior Java Developer with Spring Boot, Microservices, and SQL expertise.");
        jdInput.setFont(new Font("SansSerif", Font.PLAIN, 12));
        jdInput.setForeground(C_TEXT_DARK);
        jdInput.setBackground(C_SURFACE_ALT);
        jdInput.setCaretColor(C_PRIMARY);
        jdInput.setLineWrap(true);
        jdInput.setWrapStyleWord(true);
        jdInput.setRows(5);
        jdInput.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane jdScroll = plainScroll(jdInput);
        jdScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        jdScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        jdInputRef = jdInput;
        inner.add(jdScroll);
        inner.add(vGap(20));

        // ── Upload ────────────────────────────────────────────────────────
        inner.add(sectionHeader("Resume Files"));
        inner.add(vGap(8));

        JButton uploadBtn = formalButton("Upload PDF Resumes…");
        uploadBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        uploadBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        uploadBtn.addActionListener(e -> handleUpload());
        inner.add(uploadBtn);
        inner.add(vGap(8));

        JLabel statusLabel = new JLabel("No files loaded.");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        statusLabel.setForeground(C_TEXT_LIGHT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabelRef = statusLabel;
        inner.add(statusLabel);
        inner.add(vGap(24));

        // ── Legend ────────────────────────────────────────────────────────
        inner.add(sectionHeader("Legend"));
        inner.add(vGap(8));
        inner.add(legendCard());
        inner.add(vGap(20));

        // ── Keywords ──────────────────────────────────────────────────────
        inner.add(sectionHeader("Tracked Keywords"));
        inner.add(vGap(8));
        inner.add(keywordsCard());

        inner.add(Box.createVerticalGlue());
        outer.add(inner, BorderLayout.CENTER);
        return outer;
    }

    private static JPanel legendCard() {
        JPanel card = surfaceCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(legendRow("■", "Matched keywords",     C_SUCCESS));
        card.add(vGap(5));
        card.add(legendRow("■", "Missing keywords",     C_DANGER));
        card.add(vGap(5));
        card.add(legendRow("■", "Improvement tips",     C_WARN));
        card.add(vGap(5));
        card.add(legendRow("■", "Job recommendations",  C_ACCENT));
        return card;
    }

    private static JPanel keywordsCard() {
        JPanel card = surfaceCard();
        card.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
        String[] kws = {"java","spring boot","microservices","sql","rest api","hibernate","maven","docker"};
        for (String kw : kws) card.add(keyword(kw));
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MAIN AREA
    // ════════════════════════════════════════════════════════════════════════
    private static JPanel buildMainArea() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(C_BG);

        // Results card
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setForeground(C_TEXT_DARK);
        resultArea.setBackground(C_SURFACE);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setMargin(new Insets(16, 18, 16, 18));
        resultArea.setText(
            "\n  Welcome to Resume Analyzer — Professional Edition\n\n" +
            "  1.  Enter or edit the Job Description in the left panel.\n" +
            "  2.  Click 'Upload PDF Resumes…' to select one or more PDF files.\n" +
            "  3.  Analysis results will appear here; the ranking graph updates below.\n\n" +
            "  Resumes are ranked best-to-weakest by match score."
        );
        resultAreaRef = resultArea;

        JPanel resultCard = titledCard("Analysis Results");
        resultCard.setLayout(new BorderLayout());
        resultCard.add(plainScroll(resultArea), BorderLayout.CENTER);

        // Graph card
        graphPanel = buildGraphCard();

        JSplitPane vSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultCard, graphPanel);
        vSplit.setDividerLocation(420);
        vSplit.setDividerSize(1);
        vSplit.setBorder(null);
        vSplit.setBackground(C_BORDER);
        vSplit.setResizeWeight(0.6);
        vSplit.setOpaque(true);

        outer.add(vSplit, BorderLayout.CENTER);
        return outer;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RANKING GRAPH CARD
    // ════════════════════════════════════════════════════════════════════════
    private static JPanel buildGraphCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(C_SURFACE);
        card.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDER));

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(C_SURFACE);
        titleRow.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, C_BORDER),
            new EmptyBorder(10, 18, 10, 18)
        ));
        JLabel title = new JLabel("Candidate Ranking Comparison");
        title.setFont(new Font("Georgia", Font.BOLD, 13));
        title.setForeground(C_PRIMARY);
        JLabel subtitle = new JLabel("Score (%) by resume — best to weakest");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setForeground(C_TEXT_LIGHT);
        titleRow.add(title,    BorderLayout.WEST);
        titleRow.add(subtitle, BorderLayout.EAST);

        // Drawing area
        JPanel barArea = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                if (scores.isEmpty()) drawEmptyState(g2, getWidth(), getHeight());
                else                  drawGraph(g2, getWidth(), getHeight());
            }
        };
        barArea.setBackground(C_SURFACE);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(barArea,  BorderLayout.CENTER);
        return card;
    }

    private static void drawEmptyState(Graphics2D g2, int w, int h) {
        g2.setColor(C_TEXT_LIGHT);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
        String msg = "Ranking graph will appear after analysis.";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2 + 4);
    }

    private static void drawGraph(Graphics2D g2, int w, int h) {
        int n    = scores.size();
        int padL = 64, padR = 32, padT = 28, padB = 56;
        int cW   = w - padL - padR;
        int cH   = h - padT - padB;

        // Background grid
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            1f, new float[]{4, 4}, 0));
        for (int pct = 0; pct <= 100; pct += 20) {
            int y = padT + cH - (int)(cH * pct / 100.0);
            g2.setColor(C_BORDER);
            g2.drawLine(padL, y, padL + cW, y);
            // Y-axis label
            g2.setColor(C_TEXT_LIGHT);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String lbl = pct + "%";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, padL - fm.stringWidth(lbl) - 6, y + 4);
        }

        // Axes
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(C_BORDER);
        g2.drawLine(padL, padT,       padL,       padT + cH);  // Y-axis
        g2.drawLine(padL, padT + cH, padL + cW,  padT + cH);  // X-axis

        // Slot width per candidate
        int slotW = cW / n;
        int barW  = Math.min(56, slotW - 24);

        for (int i = 0; i < n; i++) {
            double score  = scores.get(i);
            int    barH   = (int)(cH * score / 100.0);
            int    x      = padL + i * slotW + (slotW - barW) / 2;
            int    yTop   = padT + cH - barH;

            Color barColor = BAR_COLORS[Math.min(i, BAR_COLORS.length - 1)];

            // Drop shadow (subtle)
            g2.setColor(new Color(0, 0, 0, 12));
            g2.setStroke(new BasicStroke(1f));
            g2.fillRect(x + 3, yTop + 3, barW, barH);

            // Bar fill
            g2.setColor(barColor);
            g2.fillRect(x, yTop, barW, barH);

            // Top highlight stripe
            g2.setColor(new Color(255, 255, 255, 45));
            g2.fillRect(x, yTop, barW, Math.min(6, barH));

            // Bar border
            g2.setColor(barColor.darker());
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(x, yTop, barW, barH);

            // Rank badge (circle above bar)
            int bx = x + barW / 2;
            int by = yTop - 18;
            g2.setColor(barColor);
            g2.fillOval(bx - 11, by - 11, 22, 22);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Georgia", Font.BOLD, 11));
            FontMetrics fmB = g2.getFontMetrics();
            String rank = String.valueOf(i + 1);
            g2.drawString(rank, bx - fmB.stringWidth(rank) / 2, by + 4);

            // Score label (above badge)
            g2.setColor(C_TEXT_MED);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fmS = g2.getFontMetrics();
            String scoreStr = String.format("%.1f%%", score);
            g2.drawString(scoreStr, bx - fmS.stringWidth(scoreStr) / 2, by - 15);

            // Candidate name (below x-axis)
            String cname = i < names.size() ? names.get(i) : ("R" + (i + 1));
            if (cname.length() > 12) cname = cname.substring(0, 11) + "…";
            g2.setColor(C_TEXT_MED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics fmN = g2.getFontMetrics();
            g2.drawString(cname, bx - fmN.stringWidth(cname) / 2, padT + cH + 18);

            // Thin separator line at slot boundary (except last)
            if (i < n - 1) {
                g2.setColor(new Color(C_BORDER.getRed(), C_BORDER.getGreen(), C_BORDER.getBlue(), 100));
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawLine(padL + (i + 1) * slotW, padT, padL + (i + 1) * slotW, padT + cH);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STATUS BAR
    // ════════════════════════════════════════════════════════════════════════
    private static JPanel buildStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_PRIMARY);
        p.setBorder(new EmptyBorder(7, 20, 7, 20));
        JLabel lbl = new JLabel("Ready  —  Upload resumes to begin analysis.");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(new Color(210, 195, 240));
        p.add(lbl, BorderLayout.WEST);
        statusBarLabelRef = lbl;
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UPLOAD HANDLER
    // ════════════════════════════════════════════════════════════════════════
    private static void handleUpload() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

        File[] files = chooser.getSelectedFiles();
        statusLabelRef.setText(files.length + " file(s) selected.");
        statusBarLabelRef.setText("Analyzing " + files.length + " resume(s)…");
        resultAreaRef.setText("  Processing " + files.length + " file(s), please wait…\n");

        SwingWorker<String, Void> worker = new SwingWorker<String,Void>() {
            @Override protected String doInBackground() {
                return CoreResumeAnalyzer.getAnalysisResults(files, jdInputRef.getText());
            }
            @Override protected void done() {
                try {
                    String result = get();
                    resultAreaRef.setText(result);
                    resultAreaRef.setCaretPosition(0);
                    statusBarLabelRef.setText("Analysis complete — " + files.length + " resume(s) processed.");
                    parseScores(result, files);
                    graphPanel.repaint();
                } catch (Exception ex) {
                    resultAreaRef.setText("Error: " + ex.getMessage());
                    statusBarLabelRef.setText("Error during analysis.");
                }
            }
        };
        worker.execute();
    }

    private static void parseScores(String result, File[] files) {
        scores.clear();
        names.clear();
        int fileIdx = 0;
        for (String line : result.split("\n")) {
            if (line.contains("MATCH SCORE:")) {
                try {
                    double score = Double.parseDouble(line.replaceAll("[^0-9.]", ""));
                    scores.add(score);
                    String n = fileIdx < files.length
                        ? files[fileIdx].getName().replace(".pdf", "") : ("R" + (fileIdx + 1));
                    names.add(n);
                    fileIdx++;
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private static JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(C_TEXT_LIGHT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 2, 0));
        return l;
    }

    private static JPanel legendRow(String icon, String desc, Color color) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel i = new JLabel(icon);
        i.setFont(new Font("SansSerif", Font.BOLD, 13));
        i.setForeground(color);
        JLabel d = new JLabel(desc);
        d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        d.setForeground(C_TEXT_MED);
        row.add(i); row.add(d);
        return row;
    }

    private static JLabel keyword(String text) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_ACCENT_LT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(C_ACCENT);
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("Monospaced", Font.PLAIN, 11));
        l.setForeground(C_PRIMARY);
        l.setBorder(new EmptyBorder(3, 8, 3, 8));
        l.setOpaque(false);
        return l;
    }

    private static JPanel surfaceCard() {
        JPanel card = new JPanel();
        card.setBackground(C_SURFACE_ALT);
        card.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        return card;
    }

    private static JPanel titledCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(C_SURFACE);
        JLabel lbl = new JLabel("  " + title);
        lbl.setFont(new Font("Georgia", Font.BOLD, 13));
        lbl.setForeground(C_PRIMARY);
        lbl.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, C_BORDER),
            new EmptyBorder(10, 10, 10, 10)
        ));
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private static JButton formalButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? C_PRIMARY_LT : C_PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JScrollPane plainScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(null);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        return sp;
    }

    private static Box.Filler vGap(int h) {
        return (Box.Filler) Box.createVerticalStrut(h);
    }
}

// ── WrapLayout (unchanged utility) ───────────────────────────────────────────
class WrapLayout extends FlowLayout {
    WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
    @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true);  }
    @Override public Dimension minimumLayoutSize(Container target)   { return layoutSize(target, false); }
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int tw = target.getSize().width;
            if (tw == 0) tw = Integer.MAX_VALUE;
            int hgap = getHgap(), vgap = getVgap();
            Insets ins = target.getInsets();
            int maxW = tw - ins.left - ins.right - hgap * 2;
            int x = 0, y = ins.top + vgap, rowH = 0;
            for (int i = 0; i < target.getComponentCount(); i++) {
                Component c = target.getComponent(i);
                if (!c.isVisible()) continue;
                Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                if (x + d.width > maxW && x > 0) { y += rowH + vgap; x = 0; rowH = 0; }
                x += d.width + hgap;
                rowH = Math.max(rowH, d.height);
            }
            y += rowH + ins.bottom + vgap;
            return new Dimension(tw, y);
        }
    }
}