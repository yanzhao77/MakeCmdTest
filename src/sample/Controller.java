package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    private static Controller instance;
    private String prefix = " >";
    private int KEY_PRESSEDNum;//监控命令行回滚
    private int cliTextNum;//命令行数量
    private List<String> cliText = new ArrayList<>();//命令行数据
    private LinkedList<String> newCliText = new LinkedList<>();
    private String command;
    private StringBuilder textCount = new StringBuilder();


    @FXML
    private InlineCssTextArea loggerArea;


    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        loggerArea.appendText(prefix);//加载初始值
        loggerArea.applyCss();//加载样式
        loggerArea.requestFocus();//把焦点放在这里
    }

    /**
     * 鼠标监控
     * //实现鼠标点击可以选择，但是不可改变光标位置
     *
     * @param mouseEvent
     */
    public void onMouseClickListener(MouseEvent mouseEvent) {
    }

    /**
     * 键盘监控
     *
     * @param event
     */
    public void onKeyPressedListener(KeyEvent event) {

       /* if (loggerArea.getCaretPosition() != loggerArea.getLength()) {
            if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.ENTER) {
                event.consume();//使键盘事件失效（然而并没有什么卵用）
                loggerArea.moveTo(prefix.length());
                loggerArea.appendText("");
            }
        }*/

        //如果是退格键,就删到prefix停下
        if (event.getCode() == KeyCode.BACK_SPACE && loggerArea.getLength() != 0) {
            if (loggerArea.getCaretPosition() != loggerArea.getLength()) {
                loggerArea.deleteText(0, loggerArea.getLength());
                loggerArea.appendText(String.valueOf(textCount));
                loggerArea.moveTo(prefix.length());
                loggerArea.appendText("");
                return;
            } else if (loggerArea.getCaretColumn() < prefix.length()) {
                loggerArea.replaceText(loggerArea.getCaretPosition() - prefix.length() + 1, loggerArea.getCaretPosition(), prefix);
                loggerArea.moveTo(prefix.length());
                textCount = new StringBuilder(loggerArea.getText());
                loggerArea.appendText("");
                return;
            }
        } else if (event.getCode() == KeyCode.ENTER) {//监控回车键
            if (loggerArea.getCaretPosition() != loggerArea.getLength()) {
                loggerArea.deleteText(0, loggerArea.getLength());
                loggerArea.appendText(String.valueOf(textCount));
                loggerArea.moveTo(prefix.length());
                loggerArea.appendText("");
                return;
            } else {
                   /* Object dd = loggerArea.getText();//文本域中所有的内容
            Object dd2 = loggerArea.getStylesheets();//样式
            Object dd23 = loggerArea.getCaretColumn();//显示当前是第几行
            Object dd4 = loggerArea.getCaretPosition();//显示当前光标
            Object dd5 = loggerArea.getCurrentParagraph();//显示当前光标所在位置
            System.out.println(dd);*/
                String[] command2 = loggerArea.getText(loggerArea.getCurrentParagraph() - 1).replace(prefix, "").split(" ");
                command = loggerArea.getText(loggerArea.getCurrentParagraph() - 1).replace(prefix, "");
                System.out.println(command);
                ListListener(command);
                if (command.equals("") || command == null) {
                    loggerArea.appendText(prefix);
                    loggerArea.moveTo(prefix.length());
                    textCount = new StringBuilder(loggerArea.getText());
                    loggerArea.appendText("");
                    return;
                }
                //<-- 解析为cmd执行
                try {
                    Process ps = Runtime.getRuntime().exec(command);
                    InputStream inputStream = ps.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("GBK"));//设置输出的字体格式
                    BufferedReader br = new BufferedReader(reader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String message;
                    while ((message = br.readLine()) != null) {
                        stringBuilder.append(message + "\n");
                    }
                    // System.out.println(stringBuilder);
                    loggerArea.appendText(String.valueOf(stringBuilder) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //-->cmd执行结束

                loggerArea.appendText(prefix);
                loggerArea.moveTo(prefix.length());
                textCount = new StringBuilder(loggerArea.getText());
                loggerArea.appendText("");
            }


        } else if (event.getCode() == KeyCode.DELETE) {//监控删除键
            if (loggerArea.getCaretPosition() != loggerArea.getLength()) {
                loggerArea.deleteText(0, loggerArea.getLength());
                loggerArea.appendText(String.valueOf(textCount));
                loggerArea.moveTo(prefix.length());
                loggerArea.appendText("");
                return;
            }
        } /*else if (loggerArea.getCaretColumn() < prefix.length()) {//监听组合键
            if (event.isShiftDown()) {
                loggerArea.appendText("");
                return;
            }
            textCount.append(loggerArea.getText());
        }*/
        loggerArea.appendText("");
        KeyEventRecord(event);

    }

    /**
     * 实现循环数组
     *
     * @param command
     */
    public void ListListener(String command) {
        this.command = command;
        cliTextNumListener();
        if (cliText.size() >= cliTextNum) {
            newCliText.add(command);
            cliText.remove(newCliText.lastIndexOf(command));
            cliText.add(newCliText.lastIndexOf(command), String.valueOf(newCliText.getLast()));
        } else {
            cliText.add(command);
        }
        if (newCliText.size() > cliTextNum) {
            newCliText.clear();
        }
        cliText = ListDupliacateRemova14(cliText);
    }


    /**
     * 去重
     *
     * @param cliText
     * @return
     */
    private List<String> ListDupliacateRemova14(List<String> cliText) {
        cliText = cliText.stream().distinct().collect(Collectors.toList());
        return cliText;
    }


    /**
     * 命令行回滚
     */
    public void KeyEventRaback() {
        String cliTextStr = loggerArea.getText();
        loggerArea.replaceText(cliTextStr.lastIndexOf(prefix) + prefix.length(), loggerArea.getCaretPosition(), cliText.get(KEY_PRESSEDNum));
    }

    //命令行回滚
    void KeyEventRecord(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {//设置up键响应
            KeyEventRaback();
            KEY_PRESSEDNum++;
        } else if (event.getCode() == KeyCode.DOWN) {//设置down键响应
            KeyEventRaback();
            KEY_PRESSEDNum--;
        }
        numLister();
    }

    /**
     * 初始化命令行数
     */
    private void cliTextNumListener() {
        if (cliTextNum == 0 || cliTextNum < 0) {
            cliTextNum = 20;
        }
    }

    /**
     * 监听按键次数
     *
     * @return
     */
    public int numLister() {
        if (KEY_PRESSEDNum > cliText.size() - 1) {
            KEY_PRESSEDNum = cliText.size() - 1;
        } else if (KEY_PRESSEDNum < 0) {
            KEY_PRESSEDNum = 0;
        }
        return KEY_PRESSEDNum;
    }

    //设置光标位置（不可用）
    private void cursorListenter() {
        //loggerArea.positionCaret(loggerArea.getLength());
    }
}
