package com.example;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {
    private JTextPane inputTextPane;
    private JTextArea outputTextArea;
    private JTextField xpathTextField;
    private JTextField amountTextField;
    private JTextField timestampTextField;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String currentJsonText; // 新增：存储当前的JSON文本

    public MainFrame() {
        setTitle("功能合集");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        // 创建左侧按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        String[] buttonLabels = {"JSON提取", "金额转换", "时间戳转换"};
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cardLayout.show(cardPanel, label);
                }
            });
            buttonPanel.add(button);
            buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // 创建卡片面板
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        // JSON提取面板
        JPanel jsonPanel = createJsonPanel();
        cardPanel.add(jsonPanel, "JSON提取");

        // 金额转换面板
        JPanel amountPanel = createAmountPanel();
        cardPanel.add(amountPanel, "金额转换");

        // 时间戳转换面板
        JPanel timestampPanel = createTimestampPanel();
        cardPanel.add(timestampPanel, "时间戳转换");

        // 添加组件到主框架
        add(buttonPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createJsonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        inputTextPane = new JTextPane();
        outputTextArea = new JTextArea();
        xpathTextField = new JTextField();

        JButton formatButton = new JButton("格式化 JSON");
        JButton extractButton = new JButton("提取元素");

        formatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formatJson();
            }
        });
        extractButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extractJsonElement();
            }
        });

        // 添加KeyListener到xpathTextField
        xpathTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    extractJsonElement();
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputTextPane), BorderLayout.CENTER);
        inputPanel.add(formatButton, BorderLayout.SOUTH);

        JPanel xpathPanel = new JPanel(new BorderLayout());
        xpathPanel.add(new JLabel("XPath:"), BorderLayout.WEST);
        xpathPanel.add(xpathTextField, BorderLayout.CENTER);
        xpathPanel.add(extractButton, BorderLayout.EAST);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, outputPanel);
        splitPane.setResizeWeight(0.5);

        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(xpathPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAmountPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        amountTextField = new JTextField();
        JTextArea amountResultArea = new JTextArea();
        amountResultArea.setEditable(false);

        JButton convertButton = new JButton("转换");
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertAmount(amountTextField.getText(), amountResultArea);
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("输入金额（分）:"), BorderLayout.WEST);
        inputPanel.add(amountTextField, BorderLayout.CENTER);
        inputPanel.add(convertButton, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, new JScrollPane(amountResultArea));
        splitPane.setResizeWeight(0.5);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTimestampPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        timestampTextField = new JTextField();
        JTextArea timestampResultArea = new JTextArea();
        timestampResultArea.setEditable(false);

        JButton convertButton = new JButton("转换");
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertTimestamp(timestampTextField.getText(), timestampResultArea);
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("输入时间戳（毫秒）:"), BorderLayout.WEST);
        inputPanel.add(timestampTextField, BorderLayout.CENTER);
        inputPanel.add(convertButton, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, new JScrollPane(timestampResultArea));
        splitPane.setResizeWeight(0.5);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private void formatJson() {
        String input = getPlainText(inputTextPane);
        if (!JsonProcessor.isValidJson(input)) {
            JOptionPane.showMessageDialog(this, "无效的 JSON 格式", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String formattedJson = JsonProcessor.formatJson(input);
            currentJsonText = formattedJson; // 保存格式化后的JSON文本
            inputTextPane.setText(""); // 清空文本
            highlightJson(formattedJson); // 使用带参数的highlightJson方法
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "格式化 JSON 时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void highlightJson(String json) {
        StyledDocument doc = inputTextPane.getStyledDocument();
        
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style keyStyle = inputTextPane.addStyle("Key Style", defaultStyle);
        StyleConstants.setForeground(keyStyle, Color.BLUE);
        Style valueStyle = inputTextPane.addStyle("Value Style", defaultStyle);
        StyleConstants.setForeground(valueStyle, Color.RED);
        Style bracketStyle = inputTextPane.addStyle("Bracket Style", defaultStyle);
        StyleConstants.setForeground(bracketStyle, Color.BLACK);

        String[] lines = json.split("\n");
        for (String line : lines) {
            try {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("{") || trimmedLine.startsWith("}") || 
                    trimmedLine.startsWith("[") || trimmedLine.startsWith("]")) {
                    // 处理括号
                    doc.insertString(doc.getLength(), line, bracketStyle);
                } else {
                    int colonIndex = trimmedLine.indexOf(':');
                    if (colonIndex != -1) {
                        // 处理键值对
                        String key = trimmedLine.substring(0, colonIndex).trim();
                        String value = trimmedLine.substring(colonIndex + 1).trim();
                        
                        // 插入键
                        doc.insertString(doc.getLength(), line.substring(0, line.indexOf(key)), defaultStyle);
                        doc.insertString(doc.getLength(), key, keyStyle);
                        doc.insertString(doc.getLength(), ": ", defaultStyle);
                        
                        // 插入值
                        if (value.startsWith("{") || value.startsWith("[")) {
                            doc.insertString(doc.getLength(), value, bracketStyle);
                        } else {
                            doc.insertString(doc.getLength(), value, valueStyle);
                        }
                    } else {
                        // 处理其他行
                        doc.insertString(doc.getLength(), line, defaultStyle);
                    }
                }
                doc.insertString(doc.getLength(), "\n", defaultStyle);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPlainText(JTextPane textPane) {
        Document doc = textPane.getDocument();
        try {
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void extractJsonElement() {
        String input = currentJsonText != null ? currentJsonText : inputTextPane.getText();
        String xpath = xpathTextField.getText();

        if (!JsonProcessor.isValidJson(input)) {
            JOptionPane.showMessageDialog(this, "无效的 JSON 格式", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String result = JsonProcessor.extractJsonElement(input, xpath);
        outputTextArea.setText(result);
    }

    private void convertAmount(String amountStr, JTextArea resultArea) {
        try {
            long amount = Long.parseLong(amountStr);
            double yuan = amount / 100.0;
            double wan = yuan / 10000.0;
            double yi = wan / 10000.0;

            DecimalFormat df = new DecimalFormat("#,##0.00");
            StringBuilder result = new StringBuilder();
            result.append(df.format(yuan)).append(" 元\n");
            result.append(df.format(wan)).append(" 万元\n");
            result.append(df.format(yi)).append(" 亿元");

            resultArea.setText(result.toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "无效的金额格式", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void convertTimestamp(String timestampStr, JTextArea resultArea) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resultArea.setText(sdf.format(date));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "无效的时间戳格式", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
