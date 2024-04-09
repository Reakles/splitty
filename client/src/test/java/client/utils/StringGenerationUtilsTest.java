package client.utils;

import commons.Event;
import commons.Expense;
import commons.Participant;
import javafx.beans.value.ObservableValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static client.TestObservableUtils.stringToObservable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class StringGenerationUtilsTest {
    @Mock
    Translation translation;
    @InjectMocks
    StringGenerationUtils sut;
    private Participant participant1;
    private Participant participant2;
    private Participant participant3;
    private Participant participant4;
    private Event event;
    private Expense expense1;

    @BeforeEach
    public void setup(){
        participant1 = new Participant(1, "John");
        participant2 = new Participant(2, "Jane");
        participant3 = new Participant(3, "Mike");
        participant4 = new Participant(4, "Bob");
        event = new Event("Title", null);
        event.addParticipant(participant1);
        event.addParticipant(participant2);
        event.addParticipant(participant3);
        event.addParticipant(participant4);
        expense1 = new Expense("Drinks", 12, null, participant1);
        Expense expense2 = new Expense("Food", 20, null, participant2);
        event.addExpense(expense1);
        event.addExpense(expense2);
    }
    @Test
    void generateTextForExpenseLabelAllTest() {
        Set<Participant> payers = new HashSet<>();
        payers.add(participant1);
        payers.add(participant2);
        payers.add(participant3);
        payers.add(participant4);
        expense1.setParticipantToExpense(payers);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("senderName", "John");
        expectedValues.put("amount", "0.12" + FormattingUtils.CURRENCY);
        expectedValues.put("expenseTitle", "Drinks");
        lenient().when(translation.getStringSubstitutionBinding("SGU.String.expenseString", expectedValues))
                .thenReturn(stringToObservable(expectedValues.get("senderName") + " paid "
                        + expectedValues.get("amount") + " for "
                        + expectedValues.get("expenseTitle")));

        ObservableValue<String> result = sut.generateTextForExpenseLabel(expense1, event.getParticipants().size());
        ObservableValue<String> expected =
                stringToObservable("John paid 0.12" + FormattingUtils.CURRENCY + " for Drinks" + "\n(All)");
        assertEquals(expected.getValue(), result.getValue());
    }
    @Test
    void generateTextForExpenseLabelCustomTest() {
        Set<Participant> payers = new HashSet<>();
        payers.add(participant1);
        payers.add(participant2);
        expense1.setParticipantToExpense(payers);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("senderName", "John");
        expectedValues.put("amount", "0.12" + FormattingUtils.CURRENCY);
        expectedValues.put("expenseTitle", "Drinks");
        lenient().when(translation.getStringSubstitutionBinding("SGU.String.expenseString", expectedValues))
                .thenReturn(stringToObservable(expectedValues.get("senderName") + " paid "
                        + expectedValues.get("amount") + " for "
                        + expectedValues.get("expenseTitle")));

        ObservableValue<String> result = sut.generateTextForExpenseLabel(expense1, event.getParticipants().size());
        ObservableValue<String> expected =
                stringToObservable("John paid 0.12" + FormattingUtils.CURRENCY + " for Drinks" + "\n(John, Jane)");
        assertEquals(expected.getValue(), result.getValue());
    }

    @Test
    void generateTextForMoneyTransfer(){
        int amount = 1929;
        Expense transfer = new Expense("Transfer!", -1 * amount, null, participant1);
        transfer.addParticipantToExpense(participant2);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("senderName", "Jane");
        expectedValues.put("amount", FormattingUtils.getFormattedPrice(amount));
        expectedValues.put("receiverName", "John");
        lenient().when(translation.getStringSubstitutionBinding("SGU.String.transferString", expectedValues))
                .thenReturn(stringToObservable(expectedValues.get("senderName") + " paid "
                        + expectedValues.get("amount") + " to "
                        + expectedValues.get("receiverName")));

        ObservableValue<String> result = sut.generateTextForMoneyTransfer(transfer);
        ObservableValue<String> expected =
                stringToObservable("Jane paid 19.29" + FormattingUtils.CURRENCY + " to John");
        assertEquals(expected.getValue(), result.getValue());
    }
}