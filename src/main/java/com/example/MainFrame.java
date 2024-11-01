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
import java.util.Calendar;

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
        String[] buttonLabels = {"JSON提取", "XML提取", "金额转换", "时间戳转换", "批量加引号"};
        
        // 计算最长按钮文本的宽度
        int maxWidth = 0;
        for (String label : buttonLabels) {
            JButton tempButton = new JButton(label);
            maxWidth = Math.max(maxWidth, tempButton.getPreferredSize().width);
        }
        
        // 设置所有按钮相同的尺寸
        Dimension buttonSize = new Dimension(maxWidth + 20, 30);
        
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setPreferredSize(buttonSize);
            button.setMinimumSize(buttonSize);
            button.setMaximumSize(buttonSize);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(e -> cardLayout.show(cardPanel, label));
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

        // 添加XML提取面板
        JPanel xmlPanel = createXmlPanel();
        cardPanel.add(xmlPanel, "XML提取");

        // 金额转换面板
        JPanel amountPanel = createAmountPanel();
        cardPanel.add(amountPanel, "金额转换");

        // 时间戳转换面板
        JPanel timestampPanel = createTimestampPanel();
        cardPanel.add(timestampPanel, "时间戳转换");

        // 添加批量加引号面板
        JPanel quotePanel = createQuotePanel();
        cardPanel.add(quotePanel, "批量加引号");

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
        JButton helpButton = new JButton("帮助");
        helpButton.addActionListener(e -> showJsonHelp());

        formatButton.addActionListener(e -> formatJson());
        extractButton.addActionListener(e -> extractJsonElement());

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
        xpathPanel.add(new JLabel("JsonPath:"), BorderLayout.WEST);
        xpathPanel.add(xpathTextField, BorderLayout.CENTER);
        
        // 创建一个面板包含提取按钮和帮助按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(extractButton);
        buttonPanel.add(helpButton);
        xpathPanel.add(buttonPanel, BorderLayout.EAST);

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
        amountTextField = new JTextField(15);  // 设置输入框宽度
        JTextArea amountResultArea = new JTextArea(8, 30);  // 修改为3行高度
        amountResultArea.setEditable(false);
        amountResultArea.setBorder(BorderFactory.createEtchedBorder());  // 添加边框
        amountResultArea.setLineWrap(true);  // 启用自动换行
        amountResultArea.setWrapStyleWord(true);  // 按单词换行

        JButton convertButton = new JButton("转换");
        convertButton.addActionListener(e -> convertAmount(amountTextField.getText(), amountResultArea));

        // 创建一个水平布局的面板
        JPanel horizontalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        horizontalPanel.add(new JLabel("输入金额（分）:"));
        horizontalPanel.add(amountTextField);
        horizontalPanel.add(convertButton);
        horizontalPanel.add(new JScrollPane(amountResultArea));  // 添加滚动面板

        panel.add(horizontalPanel, BorderLayout.NORTH);
        // 添加一个空面板填充剩余空间
        panel.add(new JPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTimestampPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建一个面板来包含所有内容，使用GridBagLayout以实现更好的对齐
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;  // 修改为BOTH，允许组件在两个方向上调整大小
        gbc.anchor = GridBagConstraints.WEST;  // 设置组件靠左对齐
        
        // 时间戳转日期时间的面板
        JLabel timestampLabel = new JLabel("输入时间戳（毫秒）:", SwingConstants.LEFT);
        timestampLabel.setPreferredSize(new Dimension(150, 25));
        JTextField timestampTextField = new JTextField();
        timestampTextField.setPreferredSize(new Dimension(200, 25));  // 设置输入框大小
        JTextArea timestampResultArea = new JTextArea();
        timestampResultArea.setPreferredSize(new Dimension(200, 25));  // 设置输出框大小
        timestampResultArea.setEditable(false);
        timestampResultArea.setBorder(BorderFactory.createEtchedBorder());
        
        JButton convertButton = new JButton("转换");
        convertButton.setPreferredSize(new Dimension(100, 25));
        convertButton.addActionListener(e -> convertTimestamp(timestampTextField.getText(), timestampResultArea));
        
        // 日期时间转时间戳的面板
        JLabel dateLabel = new JLabel("输入日期时间:", SwingConstants.LEFT);
        dateLabel.setPreferredSize(new Dimension(150, 25));
        JTextField dateTextField = new JTextField();
        dateTextField.setPreferredSize(new Dimension(200, 25));  // 设置输入框大小
        JTextArea dateResultArea = new JTextArea();
        dateResultArea.setPreferredSize(new Dimension(200, 25));  // 设置输出框大小
        dateResultArea.setEditable(false);
        dateResultArea.setBorder(BorderFactory.createEtchedBorder());
        
        JButton convertDateButton = new JButton("转换为时间戳");
        convertDateButton.setPreferredSize(new Dimension(100, 25));
        convertDateButton.addActionListener(e -> convertDateStringToTimestamp(dateTextField.getText(), dateResultArea));
        
        // 使用GridBagLayout添加组件
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0;  // 标签不需要额外空间
        contentPanel.add(timestampLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;  // 输入框获得额外的水平空间
        contentPanel.add(timestampTextField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;  // 按钮不需要额外空间
        contentPanel.add(convertButton, gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 1.0;  // 输出框获得额外的水平空间
        contentPanel.add(timestampResultArea, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.0;
        contentPanel.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        contentPanel.add(dateTextField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        contentPanel.add(convertDateButton, gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        contentPanel.add(dateResultArea, gbc);
        
        // 添加到主面板
        panel.add(contentPanel, BorderLayout.NORTH);
        panel.add(new JPanel(), BorderLayout.CENTER);
        
        // 添加提示文本
        dateTextField.setToolTipText("格式: yyyy-MM-dd HH:mm:ss");
        
        return panel;
    }

    // 新的转换方法，处理日期字符串转时间戳
    private void convertDateStringToTimestamp(String dateStr, JTextArea resultArea) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(dateStr);
            long timestamp = date.getTime();
            resultArea.setText(String.valueOf(timestamp));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "请输入正确的日期格式(yyyy-MM-dd HH:mm:ss)\n例如: 2024-01-01 12:00:00", 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        }
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

    private JPanel createXmlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextPane xmlInputTextPane = new JTextPane();
        JTextArea xmlOutputTextArea = new JTextArea();
        JTextField xmlXpathTextField = new JTextField();

        JButton formatButton = new JButton("格式化 XML");
        JButton extractButton = new JButton("提取元素");
        JButton helpButton = new JButton("帮助");
        helpButton.addActionListener(e -> showXmlHelp());

        formatButton.addActionListener(e -> formatXml(xmlInputTextPane));
        extractButton.addActionListener(e -> extractXmlElement(xmlInputTextPane, xmlXpathTextField, xmlOutputTextArea));

        xmlXpathTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    extractXmlElement(xmlInputTextPane, xmlXpathTextField, xmlOutputTextArea);
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(xmlInputTextPane), BorderLayout.CENTER);
        inputPanel.add(formatButton, BorderLayout.SOUTH);

        JPanel xpathPanel = new JPanel(new BorderLayout());
        xpathPanel.add(new JLabel("XPath:"), BorderLayout.WEST);
        xpathPanel.add(xmlXpathTextField, BorderLayout.CENTER);
        
        // 创建一个面板包含提取按钮和帮助按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(extractButton);
        buttonPanel.add(helpButton);
        xpathPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(new JScrollPane(xmlOutputTextArea), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, outputPanel);
        splitPane.setResizeWeight(0.5);

        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(xpathPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void formatXml(JTextPane inputPane) {
        String input = getPlainText(inputPane);
        if (!XMLProcessor.isValidXml(input)) {
            JOptionPane.showMessageDialog(this, "无效的 XML 格式", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String formattedXml = XMLProcessor.formatXml(input);
            inputPane.setText(formattedXml);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "格式化 XML 时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void extractXmlElement(JTextPane inputPane, JTextField xpathField, JTextArea outputArea) {
        String input = getPlainText(inputPane);
        String xpath = xpathField.getText();

        if (!XMLProcessor.isValidXml(input)) {
            JOptionPane.showMessageDialog(this, "无效的 XML 格式", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String result = XMLProcessor.extractXmlElement(input, xpath);
        outputArea.setText(result);
    }

    private JPanel createQuotePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextPane inputTextPane = new JTextPane();
        JTextArea outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);

        JButton convertButton = new JButton("转换");
        convertButton.addActionListener(e -> addQuotes(inputTextPane, outputTextArea));

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputTextPane), BorderLayout.CENTER);
        inputPanel.add(convertButton, BorderLayout.SOUTH);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, outputPanel);
        splitPane.setResizeWeight(0.5);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private void addQuotes(JTextPane inputPane, JTextArea outputArea) {
        String input = getPlainText(inputPane);
        if (input.trim().isEmpty()) {
            return;
        }

        String[] lines = input.split("\n");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                result.append("'").append(line).append("'");
                // 如果不是最后一行，添加逗号和换行符
                if (i < lines.length - 1) {
                    result.append(",");
                }
                result.append("\n");
            }
        }
        
        outputArea.setText(result.toString());
    }

    // 添加显示JSON帮助的方法
    private void showJsonHelp() {
        String helpText = 
            "JsonPath 语法说明：\n\n" +
            "1. $ : 根节点\n" +
            "2. . : 子节点\n" +
            "3. [] : 数组索引\n" +
            "4. * : 通配符\n" +
            "5. [start:end] : 数组切片\n\n" +
            "常用示例：\n" +
            "$.store.book[0].title : 获取第一本书的标题\n" +
            "$.store.book[*].author : 获取所有书的作者\n" +
            "$.store.book[?(@.price < 10)] : 获取价格小于10的书\n\n" +
            "示例JSON：\n" +
            "{\n" +
            "  \"store\": {\n" +
            "    \"book\": [\n" +
            "      {\n" +
            "        \"title\": \"Java编程\",\n" +
            "        \"price\": 29.99\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setRows(20);
        textArea.setColumns(50);
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "JsonPath 语法帮助", JOptionPane.INFORMATION_MESSAGE);
    }

    // 添加显示XML帮助的方法
    private void showXmlHelp() {
        String helpText = 
            "XPath 语法说明：\n\n" +
            "1. / : 从根节点选取\n" +
            "2. // : 从匹配选择的当前节点选择文档中的节点，不考虑它们的位置\n" +
            "3. . : 选取当前节点\n" +
            "4. .. : 选取当前节点的父节点\n" +
            "5. @ : 选取属性\n\n" +
            "常用示例：\n" +
            "/bookstore/book[1]/title : 选取第一个book元素的title\n" +
            "//book[@category='计算机']/title : 选取category属性为'计算机'的book的title\n" +
            "//title[@*] : 选取所有带有属性的title元素\n\n" +
            "示例XML：\n" +
            "<bookstore>\n" +
            "  <book category=\"计算机\">\n" +
            "    <title>Java编程</title>\n" +
            "    <author>张三</author>\n" +
            "    <price>29.99</price>\n" +
            "  </book>\n" +
            "</bookstore>";

        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setRows(20);
        textArea.setColumns(50);
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "XPath 语法帮助", JOptionPane.INFORMATION_MESSAGE);
    }
}
