package client.scenes;

import client.utils.AppStateManager;
import client.utils.ScreenInfo;
import client.utils.Translation;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class MainCtrl {

    private Stage primaryStage;
    private StartupScreenCtrl startupScreenCtrl;
    private ExpenseScreenCtrl expenseScreenCtrl;
    private Scene startupScene;
    private Scene eventScene;
    private Scene participantScene;
    private ParticipantScreenCtrl participantScreenCtrl;
    private Scene managementOvervirewPasswordScene;
    private Scene managementOverviewScreenScene;
    private ManagementOverviewScreenCtrl managementOverviewScreenCtrl;
    private DeleteEventsScreenCtrl deleteEventsScreenCtrl;
    private Scene deleteEventsScene;
    private final Translation translation;
    private HashMap<Class<?>, ScreenInfo> screenMap;
    private final String serverURL;
    private final String language;
    private final AppStateManager manager;

    /**
     * Constructor
     * @param translation the translation
     * @param manager the app state manager
     * @param serverURL the URl of the server
     * @param language the language tag used in the client
     */
    @Inject
    public MainCtrl(Translation translation, AppStateManager manager,
                    @Named("connection.URL") String serverURL,
                    @Named("client.language") String language) {
        this.translation = translation;
        this.manager = manager;
        this.serverURL = serverURL;
        this.language = language;
    }

    /**
     * Initialize the main controller
     * @param primaryStage the primary stage
     * @param overview the startup screen
     * @param eventUI the event screen
     * @param expenseUI the expense screen
     * @param participantUI the participant screen
     * @param editTitlePair the edit title pair
     * @param managementOverviewPasswordUI the management overview password UI
     * @param managementOverviewScreenUI the management overview screen UI
     * @param settleDebtsUI the settle debts UI
     * @param deleteEventsScreenUI the delete events screen UI
     * @param participantListUI the participant list UI
     * @param transferMoneyUI the money transfer UI
     * @param addTagUI the add tag UI
     * @param emailInviteUI the email invite UI
     * @param statisticsScreenUI the statistics screen UI
     */
    public void initialize(Stage primaryStage, Pair<StartupScreenCtrl, Parent> overview,
                           Pair<EventScreenCtrl, Parent> eventUI,
                           Pair<ExpenseScreenCtrl, Parent> expenseUI,
                           Pair<ParticipantScreenCtrl, Parent> participantUI,
                           Pair<EditTitleCtrl, Parent> editTitlePair,
                           Pair<ManagementOverviewPasswordCtrl, Parent> managementOverviewPasswordUI,
                           Pair<ManagementOverviewScreenCtrl, Parent> managementOverviewScreenUI,
                           Pair<SettleDebtsScreenCtrl, Parent> settleDebtsUI,
                           Pair<DeleteEventsScreenCtrl, Parent> deleteEventsScreenUI,
                           Pair<ParticipantListScreenCtrl, Parent> participantListUI,
                           Pair<TransferMoneyCtrl, Parent> transferMoneyUI,
                           Pair<AddTagCtrl, Parent> addTagUI,
                           Pair<EmailInviteCtrl, Parent> emailInviteUI,
                           Pair<StatisticsScreenCtrl, Parent> statisticsScreenUI){
        translation.changeLanguage(Locale.forLanguageTag(language));
        this.primaryStage = primaryStage;
        this.startupScreenCtrl = overview.getKey();
        this.startupScene = new Scene(overview.getValue());
        this.eventScene = new Scene(eventUI.getValue());
        EventScreenCtrl eventScreenCtrl = eventUI.getKey();
        Scene expenseScene = new Scene(expenseUI.getValue());
        this.expenseScreenCtrl = expenseUI.getKey();
        Scene participantListScene = new Scene(participantListUI.getValue());
        ParticipantListScreenCtrl participantListScreenCtrl = participantListUI.getKey();
        this.participantScene = new Scene(participantUI.getValue());
        this.participantScreenCtrl = participantUI.getKey();
        TransferMoneyCtrl transferMoneyCtrl = transferMoneyUI.getKey();
        Scene transferMoneyScene = new Scene(transferMoneyUI.getValue());

        EditTitleCtrl editTitleCtrl = editTitlePair.getKey();
        Scene editTitleScene = new Scene(editTitlePair.getValue());
        showMainScreen();
        this.managementOvervirewPasswordScene = new Scene(managementOverviewPasswordUI.getValue());
        this.managementOverviewScreenScene = new Scene(managementOverviewScreenUI.getValue());
        this.managementOverviewScreenCtrl = managementOverviewScreenUI.getKey();

        SettleDebtsScreenCtrl settleDebtsScreenCtrl = settleDebtsUI.getKey();
        Scene settleDebtsScene = new Scene(settleDebtsUI.getValue());
        this.deleteEventsScene = new Scene(deleteEventsScreenUI.getValue());
        this.deleteEventsScreenCtrl = deleteEventsScreenUI.getKey();
        Scene addTagScene = new Scene(addTagUI.getValue());
        AddTagCtrl addTagCtrl = addTagUI.getKey();
        Scene statisticsScreenScene = new Scene(statisticsScreenUI.getValue());
        StatisticsScreenCtrl statisticsScreenCtrl = statisticsScreenUI.getKey();
        //initialize stylesheets
        this.startupScene.getStylesheets().add("stylesheets/main.css");
        this.managementOvervirewPasswordScene.getStylesheets().add("stylesheets/main.css");
        EmailInviteCtrl emailInviteCtrl = emailInviteUI.getKey();
        Scene emailInviteScene = new Scene(emailInviteUI.getValue());
        this.screenMap = new HashMap<>();
        screenMap.put(EventScreenCtrl.class,
                new ScreenInfo(eventScreenCtrl, true, eventScene, "Event.Window.title"));
        screenMap.put(ExpenseScreenCtrl.class,
                new ScreenInfo(expenseScreenCtrl, false, expenseScene, "Expense.Window.title"));
        screenMap.put(EditTitleCtrl.class,
                new ScreenInfo(editTitleCtrl, false, editTitleScene, "editTitle.Window.title"));
        screenMap.put(ParticipantScreenCtrl.class,
                new ScreenInfo(participantScreenCtrl, false, participantScene, "Participants.Window.title"));
        screenMap.put(ParticipantListScreenCtrl.class,
                new ScreenInfo(participantListScreenCtrl, true, participantListScene, "ParticipantList.Window.title"));
        screenMap.put(SettleDebtsScreenCtrl.class,
                new ScreenInfo(settleDebtsScreenCtrl, true, settleDebtsScene, "SettleDebts.Window.title"));
        screenMap.put(AddTagCtrl.class,
                new ScreenInfo(addTagCtrl,true, addTagScene, "AddTag.Window.title"));
        screenMap.put(EmailInviteCtrl.class,
                new ScreenInfo(emailInviteCtrl, false, emailInviteScene, "Email.TitleLabel"));
        screenMap.put(TransferMoneyCtrl.class,
                new ScreenInfo(transferMoneyCtrl, true, transferMoneyScene, "TransferMoney.title"));
        screenMap.put(StatisticsScreenCtrl.class,
                new ScreenInfo(statisticsScreenCtrl, true, statisticsScreenScene, "Statistics.Screen.Window.Title"));
        manager.setScreenInfoMap(screenMap);

        Runnable connectionErrorCallback = (()-> Platform.runLater(this::onConnectionError));
        primaryStage.setOnCloseRequest(e -> manager.onStop());
        manager.setStartupScreen(startupScreenCtrl);
        manager.setOnCurrentEventDeletedCallback(this::showMainScreen);
        manager.subscribeToUpdates(connectionErrorCallback);
        primaryStage.show();
    }

    /***
     * Executes screen switching to the target's instance
     * @param target the Class of the target screen
     */
    public void switchScreens(Class<?> target){
        ScreenInfo screenInfo = screenMap.get(target);
        manager.onSwitchScreens(target);
        ObservableValue<String> title = translation.getStringBinding(screenInfo.titleBinding());
        primaryStage.titleProperty().bind(title);
        primaryStage.setScene(screenInfo.scene());
    }

    /***
     * Switches back to the Startup screen
     */
    public void showMainScreen() {
        manager.closeOpenedEvent();
        startupScreenCtrl.refreshLanguageOnSwitchback();
        primaryStage.titleProperty().bind(translation.getStringBinding("Startup.Window.title"));
        primaryStage.setScene(startupScene);
    }

    /***
     * Executed when there is a connection error/interruption, generates a popup for the user
     */
    public void onConnectionError() {
        showMainScreen();
        String reconnectString = translation.getStringBinding("Main.ConnectionError.Reconnect").getValue();
        String exitString = translation.getStringBinding("Main.ConnectionError.Exit").getValue();
        ButtonType buttonTypeReconnect = new ButtonType(reconnectString, ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeExit = new ButtonType(exitString, ButtonBar.ButtonData.NO);
        var connectionErrorPopup = generateConnectionErrorPopup(serverURL, buttonTypeReconnect, buttonTypeExit);

        Optional<ButtonType> result = connectionErrorPopup.showAndWait();
        if(result.isPresent() && result.get().equals(buttonTypeReconnect)){
            manager.subscribeToUpdates(()-> Platform.runLater(this::onConnectionError));
        } else {
            Platform.exit();
        }
    }

    /***
     * Generates an alert popup on connection failure
     * @param serverURL the URL of the server
     * @param buttonTypeReconnect the ButtonType of the Reconnect button
     * @param buttonTypeExit the ButtonType of the Exit button
     * @return an Alert popup telling the user to either reconnect or exit the app
     */
    public Alert generateConnectionErrorPopup(String serverURL, ButtonType buttonTypeReconnect, ButtonType buttonTypeExit){
        Map<String, String> substitutionMap = new HashMap<>();
        substitutionMap.put("serverURL", serverURL);

        Alert connectionErrorPopup = new Alert(Alert.AlertType.ERROR);
        String errorTitle = translation.getStringBinding("Main.ConnectionError.Title").getValue();
        String errorHeader = translation.getStringSubstitutionBinding("Main.ConnectionError.Header", substitutionMap)
                .getValue();
        String errorContent = translation.getStringSubstitutionBinding("Main.ConnectionError.Content", substitutionMap)
                .getValue();

        connectionErrorPopup.setTitle(errorTitle);
        connectionErrorPopup.setHeaderText(errorHeader);
        connectionErrorPopup.setContentText(errorContent);

        connectionErrorPopup.getButtonTypes().setAll(buttonTypeExit, buttonTypeReconnect);

        return connectionErrorPopup;
    }

    /**
     * Gets startup screen
     * @return the startup screen
     */
    public Scene getMainMenuScene(){
        return startupScene;
    }

    /**
     * Gets the EventScreen
     * @return the Event screen
     */
    public Scene getEventScene() {
        return eventScene;
    }

    /**
     * Gets the ExpenseScreen
     * @return the Expense screen
     */
    public Scene getParticipantScene(){
        return participantScene;
    }

    /**
     * Switches to the edit expense screen
     * @param expenseId  the expense id of the expense to edit
     */
    public void switchToEditExpense(long expenseId) {
        switchScreens(ExpenseScreenCtrl.class);
        expenseScreenCtrl.setExpense(expenseId);
    }

    /**
     * Switches to the edit participant screen
     * @param participantId the participant id of the participant to edit
     */
    public void switchToEditParticipant(long participantId) {
        participantScreenCtrl.saveId(participantId);
        switchScreens(ParticipantScreenCtrl.class);
        participantScreenCtrl.setParticipant(participantId);
    }
    /**
     * switch to the login page for the management overview
     */
    public void switchToManagementOverviewPasswordScreen(){
        primaryStage.setScene(managementOvervirewPasswordScene);
        primaryStage.titleProperty().bind(translation.getStringBinding("MOPCtrl.Window.title"));
    }

    /**
     * go to the management overview screen
     */
    public void switchToManagementOverviewScreen(){
        primaryStage.setScene(managementOverviewScreenScene);
        primaryStage.titleProperty().bind(translation.getStringBinding("MOSCtrl.Window.title"));
        managementOverviewScreenCtrl.initializeAllEvents();
    }

    /**
     * Switches to the settle debts screen
     */
    public void switchToDeleteEventsScreen(){
        primaryStage.setScene(deleteEventsScene);
        primaryStage.titleProperty().bind(translation.getStringBinding("DES.Window.title"));
        deleteEventsScreenCtrl.initializeEventsCheckList();
    }

    /***
     * Replace the event being viewed
     * @param eventCode the event code to use
     */
    public void switchEvents(String eventCode) {
        manager.switchClientEvent(eventCode);
    }

    /**
     * Shows an alert that tells the user if the email was sent successfully
     * @param wasSuccessful true if the email was sent successfully, false otherwise
     */
    public void showEmailPrompt(boolean wasSuccessful) {
        Alert a;
        if (wasSuccessful){
            System.out.println("Successfully sent email!");
            a = new Alert(Alert.AlertType.INFORMATION);
            a.contentTextProperty().bind(translation.getStringBinding("Event.Label.EmailFeedback.Success"));
            a.titleProperty().bind(translation.getStringBinding("Email.SuccessTitle"));
        }else{
            System.out.println("Error while sending email!");
            a = new Alert(Alert.AlertType.ERROR);
            a.contentTextProperty().bind(translation.getStringBinding("Event.Label.EmailFeedback.Fail"));
            a.titleProperty().bind(translation.getStringBinding("Email.ErrorTitle"));
        }
        a.headerTextProperty().bind(translation.getStringBinding("Email.EmailFeedback"));
        a.show();
    }
}
