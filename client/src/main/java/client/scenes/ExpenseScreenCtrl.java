package client.scenes;

import client.utils.ImageUtils;
import client.utils.ServerUtils;
import client.utils.Styling;
import client.utils.Translation;
import commons.Event;
import commons.Expense;
import commons.Participant;
import commons.Tag;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ExpenseScreenCtrl implements Initializable, SimpleRefreshable {
    private final ServerUtils server;
    @FXML
    private Label addEditExpense;
    @FXML
    private Label paidBy;
    @FXML
    private ComboBox<String> choosePayer;
    @FXML
    private Label purpose;
    @FXML
    private TextField expensePurpose;
    @FXML
    private Label amount;
    @FXML
    private TextField sum;
    @FXML
    private ComboBox<String> currency;
    @FXML
    private Label date;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label splitMethod;
    @FXML
    private Label expenseType;
    @FXML
    private CheckBox splitBetweenAllCheckBox;
    @FXML
    private CheckBox splitBetweenCustomCheckBox;
    @FXML
    private Button cancel;
    @FXML
    private Button confirm;
    @FXML
    private Label errorParticipants;
    @FXML
    private Label errorNoPurpose;
    @FXML
    private Label errorAmount;
    @FXML
    private Label errorDate;
    @FXML
    private Label errorSplitMethod;
    @FXML
    private VBox participantsVBox;
    @FXML
    private ComboBox<Tag> tagComboBox;
    private final MainCtrl mainCtrl;
    private Event currentEvent;
    private final Translation translation;
    private long expenseId;
    private List<CheckBox> participantCheckBoxes;
    private final ImageUtils imageUtils;
    private final AddTagCtrl addTagCtrl;
    private final Styling styling;

    /**
     *
     * @param server the server to which the client is connected
     * @param mainCtrl the main controller
     * @param imageUtils Utilities for image loading
     * @param addTagCtrl Controller for adding/editing tags
     * @param translation the class that manages translations
     */
    @Inject
    public ExpenseScreenCtrl (ServerUtils server, MainCtrl mainCtrl,
                              Translation translation, ImageUtils imageUtils, AddTagCtrl addTagCtrl, Styling styling) {
        this.mainCtrl = mainCtrl;
        this.translation = translation;
        this.server = server;
        this.imageUtils = imageUtils;
        this.addTagCtrl = addTagCtrl;
        this.styling = styling;
    }

    /**
     * responsible for setting up the fields that need to be translated
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currency.setItems(FXCollections.observableArrayList("EUR"));
        participantCheckBoxes = new ArrayList<>();
        choosePayer.setItems(getParticipantList());
        binds();
        splitBetweenAllCheckBox.setOnAction(event -> {
            if (splitBetweenAllCheckBox.isSelected()) {
                splitBetweenCustomCheckBox.setSelected(false);
                participantsVBox.getChildren().clear();
            }
        });

        splitBetweenCustomCheckBox.setOnAction(event -> {
            if (splitBetweenCustomCheckBox.isSelected()) {
                splitBetweenAllCheckBox.setSelected(false);
                addParticipants();
            }
            if(!splitBetweenCustomCheckBox.isSelected()) {
                participantsVBox.getChildren().clear();
            }
        });
    }

    /**
     * initialize the available tags in the existent event
     * selects by default the "default" tag, so an event cannot be created without a tag
     * if forgotten or wrongly chosen, it can be edited
     */
    public void initializeTagComboBox(){
        tagComboBox.getItems().clear();
        Set<Tag> tags = currentEvent.getEventTags();
        tags.removeIf(tag -> tag.getTagName().equals("money transfer"));
        tagComboBox.getItems().addAll(tags);
        tagComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item.getTagName());
                    label.setAlignment(Pos.CENTER);
                    label.setStyle("-fx-background-color: " + item.getColorCode() + ";" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 5 10 5 10;" +
                            "-fx-text-fill: white;");
                    Button editButton = new Button("", imageUtils.generateImageView("editing.png", 15));
                    styling.applyStyling(editButton, "positiveButton");
                    editButton.setOnMousePressed(event -> {
                        mainCtrl.switchScreens(AddTagCtrl.class);
                        addTagCtrl.fillInput(item);
                    });
                    AnchorPane pane = new AnchorPane(label, editButton);
                    AnchorPane.setRightAnchor(editButton, 0.0);
                    setGraphic(pane);
                }
            }
        });
        tagComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item.getTagName());
                    label.setAlignment(Pos.CENTER);
                    label.setStyle("-fx-background-color: " + item.getColorCode() + ";" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 5 10 5 10;" +
                            "-fx-text-fill: white;");

                    setGraphic(label);
                }
            }
        });

        Tag defaultTag = findDefaultTag(tags);
        if (!tags.isEmpty()) {
            tagComboBox.getSelectionModel().select(defaultTag);
        }
    }

    /**
     * this method searches for the default tag
     * @param tags the tags in the current event
     * @return the default tag
     */
    private Tag findDefaultTag(Set<Tag> tags) {
        for (Tag tag : tags) {
            if ("default".equals(tag.getTagName())) {
                return tag;
            }
        }
        return null;
    }

    /**
     * Method used for getting the participants that will be added
     * in the combobox for the Paid by field
     * @return the participant list
     */
    public ObservableList<String> getParticipantList() {
        Set<Participant> participants;
        if(currentEvent == null || currentEvent.getParticipants() == null)
            participants = new HashSet<>();
        else participants = currentEvent.getParticipants();
        Iterator<Participant> iterator = participants.iterator();
        List<String> names = new ArrayList<>();
        while(iterator.hasNext()) {
            Participant participant = iterator.next();
            names.add(participant.getName());
        }
        return FXCollections.observableArrayList(names);
    }

    /**
     * Binds each text to a key in order to be used for translation
     */
    private void binds() {
        addEditExpense.textProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.Add"));
        paidBy.textProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.paid"));
        choosePayer.promptTextProperty()
                .bind(translation.getStringBinding("Expense.ComboBox.payer"));
        purpose.textProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.purpose"));
        expensePurpose.promptTextProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.purpose"));
        amount.textProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.amount"));
        sum.promptTextProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.amount"));
        date.textProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.date"));
        datePicker.promptTextProperty()
                .bind(translation.getStringBinding("Expense.DatePicker.Display.date"));
        splitMethod.textProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.split"));
        splitBetweenAllCheckBox.textProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.splitAll"));
        splitBetweenCustomCheckBox.textProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.splitCustom"));
        cancel.textProperty()
            .bind(translation.getStringBinding("Expense.Button.Cancel"));
        confirm.textProperty()
            .bind(translation.getStringBinding("Expense.Button.Confirm"));
        expenseType.textProperty()
            .bind(translation.getStringBinding("Expense.Label.Display.expenseType"));
        bindToEmpty();
    }

    /**
     * Binds the error fields to empty
     */
    public void bindToEmpty() {
        errorParticipants.textProperty()
            .bind(translation.getStringBinding("empty"));
        errorNoPurpose.textProperty()
            .bind(translation.getStringBinding("empty"));
        errorAmount.textProperty()
            .bind(translation.getStringBinding("empty"));
        errorDate.textProperty()
            .bind(translation.getStringBinding("empty"));
        errorSplitMethod.textProperty()
            .bind(translation.getStringBinding("empty"));
    }

    /**
     * Assign the event corresponding to the current expense
     * @param event the event
     */
    public void refresh(Event event) {
        this.currentEvent = event;
        currency.setItems(FXCollections.observableArrayList("", "EUR"));
        choosePayer.setItems(getParticipantList());
        initializeTagComboBox();
        bindToEmpty();
    }


    /**
     * When pressing the Cancel button it takes the user
     * back to the Event Screen
     */
    public void switchToEventScreen() {
        resetAll();
        mainCtrl.switchScreens(EventScreenCtrl.class);
    }

    /**
     * resets all the fields in the expenseScreen
     */
    public void resetAll() {
        resetPaidBy();
        resetAmount();
        resetPurpose();
        resetDate();
        resetCurrency();
        resetSplitMethod();
    }

    /**
     * resets the text from the Paid by field
     */
    public void resetPaidBy() {
        choosePayer.setValue("");
    }

    /**
     * resets the amount inserted in the amount TextField
     */
    public void resetAmount() {
        this.sum.clear();
    }

    /**
     * resets the text inserted in the purpose TextField
     */
    public void resetPurpose() {
        this.expensePurpose.clear();
    }

    /**
     * resets the date chosen for the datePicker field
     */
    public void resetDate() {
        this.datePicker.getEditor().clear();
    }

    /**
     * (Theoretically) resets the currency inserted in the currency
     * ComboBox
     */
    public void resetCurrency() {
        this.currency.setValue("");
    }

    /**
     * resets the checkboxes for the split methods
     */
    public void resetSplitMethod() {
        splitBetweenAllCheckBox.setSelected(false);
        splitBetweenCustomCheckBox.setSelected(false);
        participantsVBox.getChildren().clear();
    }

    /**
     * Creates a new expense based on the information provided
     * in the ExpenseScreen
     * @return the newly created expense
     */
    public Expense createNewExpense() {
        String name = getTextFieldText(expensePurpose);
        String priceInMoney = getTextFieldText(sum);
        double price = 0;
        try {
            price = Double.parseDouble(priceInMoney);
        }
        catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid number");
        }
        String curr = getComboBox(currency);
        int priceInCents = (int) Math.ceil(price * 100);
        //change in case of wanting to implement another date system
        LocalDate date = getLocalDate(datePicker);
        Date expenseDate = null;
        if(date != null) {
            expenseDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        String participantName = getComboBox(choosePayer);
        Iterator<Participant> participantIterator = currentEvent.getParticipants().iterator();
        Participant participant = null;
        while(participantIterator.hasNext()){
            Participant current = participantIterator.next();
            if(current.getName().equals(participantName)) {
                participant = current;
                break;
            }
        }
        Expense resultExpense = new Expense(name, priceInCents, expenseDate, participant);
        Set<Participant> participantSet = getParticipantsForExpense();
        for(Participant part: participantSet) {
            resultExpense.addParticipantToExpense(part);
        }
        resultExpense.setCurrency(curr);
        Tag selectedTag = getTagComboBox(tagComboBox);
        resultExpense.setExpenseTag(selectedTag);
        return resultExpense;
    }

    /**
     *
     * @param textField a text field from the screen
     * @return the text inside the text field
     */
    public String getTextFieldText(TextField textField) {
        return textField.getText();
    }

    /**
     *
     * @param datePicker a chosen date picker
     * @return the date from the date picker
     */
    public LocalDate getLocalDate(DatePicker datePicker) {
        return datePicker.getValue();
    }

    /**
     *
     * @param comboBox the specified comboBox
     * @return the text from the comboBox
     */
    public String getComboBox(ComboBox<String> comboBox) {
        return comboBox.getValue();
    }

    /**
     * gets the selected tag in the combobox
     * @param tagComboBox the combobox which holds tags
     * @return the tag stored inside the combo box
     */
    public Tag getTagComboBox(ComboBox<Tag> tagComboBox){
        return tagComboBox.getValue();
    }
    /**
     * Adds the specified expense to the server
     * @param expense the provided expense
     */
    public void addExpenseToTheServer(Expense expense) {
        server.addExpense(currentEvent.getId(), expense);
    }

    /**
     * when editing an expense this method makes sure that
     * the fields are filled according to the expense that is being edited
     * @param id the id of the expense
     */
    public void setExpense(long id) {
        Set<Expense> expenses = currentEvent.getExpenses();
        Expense expense = null;
        for(Expense exp: expenses){
            if(exp.getId() == id) {
                expense = exp;
                break;
            }
        }
        if(expense == null)
            return;
        expensePurpose.setText(expense.getName());
        double price = expense.getPriceInCents() / 100.;
        if(price == (int) price)
            sum.setText(Integer.toString((int)price));
        sum.setText(String.valueOf(price));
        choosePayer.getEditor().setText(expense.getOwedTo().getName());
        Date expenseDate = expense.getDate();
        currency.setValue(expense.getCurrency());
        datePicker.getEditor()
            .setText((expenseDate.getMonth() + 1) + "/"
                + expenseDate.getDate() + "/" +
                expenseDate.getYear()); //needs revision
        if(expense.getParticipantsInExpense()
            .containsAll(currentEvent.getParticipants())) {
            splitBetweenAllCheckBox.setSelected(true);
            splitBetweenCustomCheckBox.setSelected(false);
            participantsVBox.getChildren().clear();
        }
        else {
            participantsVBox.getChildren().clear();
            splitBetweenCustomCheckBox.setSelected(true);
            splitBetweenAllCheckBox.setSelected(false);
            addParticipants();
            for(Participant participant: expense.getParticipantsInExpense()) {
                for(CheckBox checkBox: participantCheckBoxes) {
                    if(checkBox.getText().equals(participant.getName())) {
                        checkBox.setSelected(true);
                        break;
                    }
                }
            }
        }
        tagComboBox.getSelectionModel().select(expense.getExpenseTag());
        expenseId = id;
    }

    /**
     * Edits the expense with the provided id
     * @param expenseId the id of the expense that is edited
     * @param expense the expense we want to replace the current
     * expense with
     */
    public void editExpenseOnServer(long expenseId, Expense expense) {
        server.editExpense(currentEvent.getId(), expenseId, expense);
    }
    /**
     * Fires on clicking the confirm button
     */
    public void addExpenseToEvenScreen() {
        boolean toAdd = true;
        Expense expense = createNewExpense();
        bindToEmpty();
        if(expense.getOwedTo() == null) {
            errorParticipants.textProperty()
                .bind(translation.getStringBinding("Expense.Label.NoParticipants"));
            toAdd = false;
        }
        if(expense.getName() == null || expense.getName().isEmpty()) {
            errorNoPurpose.textProperty()
                .bind(translation.getStringBinding("Expense.Label.NoPurpose"));
            toAdd = false;
        }
        if(expense.getPriceInCents() <= 0) {
            errorAmount.textProperty()
                .bind(translation.getStringBinding("Expense.Label.InvalidAmount"));
            toAdd = false;
        }
        if(expense.getPriceInCents() > 0 &&
            (expense.getCurrency() == null || !expense.getCurrency().equals("EUR"))) {
            errorAmount.textProperty()
                .bind(translation.getStringBinding("Expense.Label.InvalidCurrency"));
            toAdd = false;
        }
        if(expense.getDate() == null) {
            errorDate.textProperty()
                .bind(translation.getStringBinding("Expense.Label.InvalidDate"));
            toAdd = false;
        }
        if(!splitBetweenCustomCheckBox.isSelected()
            && !splitBetweenAllCheckBox.isSelected()){
            errorSplitMethod.textProperty()
                .bind(translation.getStringBinding("Expense.Label.InvalidSplitMethod"));
            toAdd = false;
        }
        if(toAdd) {
            if(expenseId == 0)
                addExpenseToTheServer(expense);
            else {
                editExpenseOnServer(expenseId, expense);
                expenseId = 0;
            }
            resetAll();
            mainCtrl.switchScreens(EventScreenCtrl.class);
        }
    }

    /**
     * Adds the selected participants to the current expense
     * @return a set of all participants for the expense
     */
    public Set<Participant> getParticipantsForExpense() {
        if(splitBetweenAllCheckBox.isSelected())
            return currentEvent.getParticipants();
        Set<Participant> result = new HashSet<>();
        Set<Participant> participants = currentEvent.getParticipants();
        for(CheckBox checkBox: participantCheckBoxes) {
            boolean found = false;
            if(checkBox.isSelected()) {
                String name = checkBox.getText();
                for(Participant participant: participants) {
                    if(participant.getName().equals(name)) {
                        result.add(participant);
                        found = true;
                        break;
                    }
                }
                if(!found)
                    throw new EntityNotFoundException("The participant doesn't exist anymore");
            }
        }
        return result;
    }

    /**
     * Generates a list of checkboxes with the names of
     * the event participants
     */
    public void addParticipants() {
        Set<Participant> participants = currentEvent.getParticipants();
        participantCheckBoxes.clear();
        if(participants.size() < 4)
            participantsVBox.setPrefHeight((double)participants.size() / 4 * 100);
        ListView<CheckBox> participantsListView = new ListView<>();
        participantsVBox.getChildren().add(participantsListView);
        for(Participant participant: participants) {
            CheckBox participantToPay = new CheckBox(participant.getName());
            participantsListView.getItems().add(participantToPay);
            participantCheckBoxes.add(participantToPay);
        }
    }

    /**
     * Setter for the current event
     * @param event the event
     */
    public void setCurrentEvent(Event event) {
        this.currentEvent = event;
    }
}
