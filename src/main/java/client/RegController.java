package client;

import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class RegController {

    Controller controller;

    public TextField loginField;
    public PasswordField passwordField;
    public TextField nickField;

    public void clickOk(ActionEvent actionEvent) {
        controller.tryRegistration(loginField.getText().trim(), passwordField.getText().trim(), nickField.getText().trim());
    }
}
