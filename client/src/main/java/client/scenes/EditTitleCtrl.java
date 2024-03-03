package client.scenes;

import client.utils.ServerUtils;
import client.utils.Translation;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class EditTitleCtrl implements Initializable {
    @FXML
    private TextField title;
    @FXML
    private Label label;
    @FXML
    private Button confirm;
    @FXML
    private Button cancel;

    ServerUtils server;
    MainCtrl mainCtrl;
    Translation translation;

    @Inject
    public EditTitleCtrl(MainCtrl mainCtrl, ServerUtils server, Translation translation) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.translation = translation;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.label.textProperty().bind(translation.getStringBinding("editTitle.label"));
        this.title.textProperty().bind(translation.getStringBinding("editTitle.promptText"));
        this.cancel.textProperty().bind(translation.getStringBinding("editTitle.cancelButton"));
        this.confirm.textProperty().bind(translation.getStringBinding("editTitle.confirmButton"));
    }

    public void confirm() {

    }

    public void cancel() {

    }
}
