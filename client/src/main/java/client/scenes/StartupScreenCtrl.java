package client.scenes;

import client.utils.AppStateManager;
import client.utils.ServerUtils;
import client.utils.Translation;
import com.google.inject.Inject;
import commons.Event;
import jakarta.ws.rs.BadRequestException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;

import static javafx.geometry.Pos.CENTER_LEFT;
public class StartupScreenCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final AppStateManager appStateManager;
    @FXML
    private TextField eventTitleTextBox;
    @FXML
    private TextField inviteCodeTextBox;
    @FXML
    private Label createEventFeedback;
    @FXML
    private Label joinEventFeedback;
    @FXML
    private VBox recentlyViewedEventsVBox;
    @FXML
    private Button joinEventButton;
    @FXML
    private Button createEventButton;
    @FXML
    private Button managementOverviewButton;
    @FXML
    private Label createEventLabel;
    @FXML
    private Label joinEventLabel;
    @FXML
    private ComboBox<Locale> languageIndicator;
    private HashMap<String, HBox> eventsAndHBoxes;
    private Translation translation;
    private final LanguageIndicatorCtrl languageCtrl;

    /**
     * Constructor
     * @param server the ServerUtils instance
     * @param mainCtrl the MainCtrl instance
     * @param translation the Translation to use
     * @param appStateManager the AppStateManager to use
     */
    @Inject
    public StartupScreenCtrl(ServerUtils server, MainCtrl mainCtrl, Translation translation,
                             LanguageIndicatorCtrl languageCtrl, AppStateManager appStateManager) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.translation = translation;
        this.languageCtrl = languageCtrl;
        this.appStateManager = appStateManager;
        eventsAndHBoxes = new HashMap<>();
    }

    /**
     * Binds the fields to their matching binding
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bindTextBox(eventTitleTextBox, "Startup.TextBox.EventTitle");
        bindTextBox(inviteCodeTextBox, "Startup.TextBox.EventCode");
        bindLabel(joinEventLabel, "Startup.Label.JoinEvent");
        bindLabel(createEventLabel, "Startup.Label.CreateEvent");
        bindButton(joinEventButton, "Startup.Button.JoinEvent");
        bindButton(createEventButton, "Startup.Button.CreateEvent");
        bindLabel(joinEventFeedback, "empty");
        bindLabel(createEventFeedback, "empty");
        bindButton(managementOverviewButton, "Startup.Button.Management.Overview");
        languageCtrl.initializeLanguageIndicator(languageIndicator);
        languageCtrl.refresh(languageIndicator);
    }

    /**
     * Creates and joins the event specified by the user in the text box
     */
    public void createEvent(){
        bindLabel(createEventFeedback, "empty");
        String title = getTextBoxText(eventTitleTextBox);
        if (title.isEmpty()){
            bindLabel(createEventFeedback, "Startup.Label.UnspecifiedTitle");
            return;
        }
        Event event = server.createEvent(title);
        String newEventID = event.getId();
        addToHistory(newEventID, event.getTitle());
        switchToEvent(newEventID);
    }

    /**
     * Joins the event specified by the user in the text box
     */
    public void joinEventClicked(){
        bindLabel(joinEventFeedback, "empty");
        String inviteCode = getTextBoxText(inviteCodeTextBox);
        if (inviteCode.length() != 6){
            bindLabel(joinEventFeedback, "Startup.Label.InvalidCode");
            return;
        }
        try{
            Event event = server.getEvent(inviteCode);
            addToHistory(inviteCode, event.getTitle());
            switchToEvent(inviteCode);
        }catch (BadRequestException exception){
            bindLabel(joinEventFeedback, "Startup.Label.InvalidCode");
        }
    }
    /**
     * Joins the given event
     * @param eventId the ID of the event to join
     */
    public void switchToEvent(String eventId){
        mainCtrl.switchEvents(eventId);
        mainCtrl.switchToEventScreen();
        moveHistoryToTop(eventId);
    }

    /**
     * Adds the event to the history
     * @param eventId the ID of the event to add to the history
     * @param eventName the title of the event
     */
    public void addToHistory(String eventId, String eventName) {
        Label eventLabel = generateLabelForEvent(eventId, eventName);
        ImageView imageView = generateRemoveButton(eventLabel, eventId);
        HBox hbox = generateHBox(eventLabel, imageView);
        removeFromHistoryIfExists(eventId);
        appStateManager.addSubscription(eventId);
        List<Node> recentlyViewedEvents = getHistoryNodes();
        recentlyViewedEvents.addFirst(hbox);
        eventsAndHBoxes.put(eventId, hbox);
        if (recentlyViewedEvents.size() > 5){
            HBox lastHBox = (HBox) recentlyViewedEvents.getLast();
            String removedEventId = findKeyByValue(lastHBox);
            removeFromHistoryIfExists(removedEventId);
        }
    }

    private String findKeyByValue(HBox hBox) {
        List<Map.Entry<String, HBox>> matching = eventsAndHBoxes.entrySet().stream().
                filter(entry->entry.getValue().equals(hBox)).toList();
        if(matching.size()>1) throw new IllegalStateException("One Event ID bound to multiple HBoxes");
        if(matching.isEmpty()) return null;
        return matching.get(0).getKey();
    }

    private void moveHistoryToTop(String eventId) {
        var relevantHBox = eventsAndHBoxes.get(eventId);
        var recentlyViewed = getHistoryNodes();
        recentlyViewed.remove(relevantHBox);
        recentlyViewed.addFirst(relevantHBox);
    }

    /**
     * Getter for eventsAndHBoxes
     *
     * @return eventsAndHBoxes
     */
    public HashMap<String, HBox> getEventsAndHBoxes() {
        return eventsAndHBoxes;
    }

    /**
     * Removes the HBox containing the event given if it exists already in the history
     * @param eventId the ID of the event to remove the history of
     */
    public void removeFromHistoryIfExists(String eventId){
        var hBox = eventsAndHBoxes.getOrDefault(eventId, null);
        if (hBox!=null){
            eventsAndHBoxes.remove(eventId);
            appStateManager.removeSubscription(eventId);
            removeFromVBox(hBox);
        }
    }

    /**
     * Generates the label for the history
     * @param eventId the ID of the event to generate the label for
     * @param eventName the title of the vent
     * @return the label generated
     */
    public Label generateLabelForEvent(String eventId, String eventName){
        Label label = new Label();
        String labelString = eventName + " (" +
                eventId +
                ")";
        label.setText(labelString);
        Tooltip joinEventTooltip = new Tooltip();
        joinEventTooltip.textProperty().bind(translation.getStringBinding("Startup.Tooltip.JoinEvent"));
        label.setTooltip(joinEventTooltip);
        label.setOnMouseClicked(
                mouseEvent -> {
                    switchToEvent(eventId);
                    joinEventFeedback.textProperty().bind(translation.getStringBinding("empty"));
                });
        label.setOnMouseEntered(
                mouseEvent -> {
                    label.setUnderline(true);
                    mainCtrl.getMainMenuScene().setCursor(Cursor.HAND);
                }
        );
        label.setOnMouseExited(
                mouseEvent -> {
                    label.setUnderline(false);
                    mainCtrl.getMainMenuScene().setCursor(Cursor.DEFAULT);
                }
        );
        return label;
    }
    /**
     * Generates the HBox for the history (which will contain the label and image)
     * @param label the label to display
     * @param imageView the image to display
     * @return HBox generated
     */

    public HBox generateHBox(Label label, ImageView imageView){
        HBox hbox = new HBox(imageView, label);
        hbox.setSpacing(10);
        hbox.setAlignment(CENTER_LEFT);
        return hbox;
    }
    /**
     * Generates an image which when clicked will remove the HBox which contains it.
     * It will also change the cursor of the user while hovering to indicate clickability.
     * @param label the label which will be contained with the image in the HBox
     * @param eventId the ID of the event to remove
     * @return ImageView with the image
     */
    public ImageView generateRemoveButton(Label label, String eventId) {
        FileInputStream input = null;
        try {
            input = new FileInputStream("client/src/main/resources/images/x_remove.png");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Image image = new Image(input);
        ImageView imageView = new ImageView(image);
        int imgSize = 15;
        imageView.setFitHeight(imgSize);
        imageView.setFitWidth(imgSize);
        imageView.setPickOnBounds(true);
        Tooltip deleteEventToolTip = new Tooltip();
        deleteEventToolTip.textProperty().bind(translation.getStringBinding("Startup.Tooltip.DeleteEvent"));
        Tooltip.install(imageView, deleteEventToolTip);
        imageView.setOnMouseEntered(
                mouseEvent -> {
                    mainCtrl.getMainMenuScene().setCursor(Cursor.HAND);
                }
        );
        imageView.setOnMouseExited(
                mouseEvent -> {
                    mainCtrl.getMainMenuScene().setCursor(Cursor.DEFAULT);
                }
        );
        imageView.setOnMouseClicked(
                mouseEvent -> {
                    removeFromHistoryIfExists(eventId);
                }
        );

        return imageView;
    }
    /**
     * Removes from the VBox the corresponding HBox child
     * @param hBox the HBox to remove
     */
    public void removeFromVBox(HBox hBox){
        List<Node> allNodes = getHistoryNodes();
        allNodes.remove(hBox);
    }

    /**
     * Returns the history nodes that are in recentlyViewedEventsVBox
     * @return a list with the nodes
     */
    public List<Node> getHistoryNodes(){
        return recentlyViewedEventsVBox.getChildren();
    }

    /**
     * Gets the text from a given textfield
     * @param textBox the textfield to get the text from
     * @return String the text from the textfield
     */
    public String getTextBoxText(TextField textBox){
        return textBox.getText();
    }

    /**
     * Binds a label to a given string binding
     * @param label the label to bind
     * @param key the key for the translator
     */

    public void bindLabel(Label label, String key){
        label.textProperty().bind(translation.getStringBinding(key));
    }

    /**
     * Binds a textfield to a given string binding
     * @param textBox the textfield to bind
     * @param key the translator key
     */
    public void bindTextBox(TextField textBox, String key) {
        textBox.promptTextProperty().bind(translation.getStringBinding(key));
    }

    /**
     * Binds a button to a given string binding
     * @param button the button to bind
     * @param key the translator key
     */
    public void bindButton(Button button, String key) {
        button.textProperty().bind(translation.getStringBinding(key));
    }

    /**
     * switch to the management overview password (log in) scene
     * @param actionEvent on button press go to another scene
     */
    public void goToTheManagementOverview(ActionEvent actionEvent) {
        mainCtrl.switchToMnagamentOverviewPasswordScreen();
    }
}
