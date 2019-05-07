package sample;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import sample.server.KillServer;

public class Controller {
    public TextField port;
    public TextArea area;

    public void begin(ActionEvent actionEvent) {
        String s = port.getText();
        new KillServer(s, area).init();
    }
}
