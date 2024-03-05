package client.scenes;

import client.utils.ServerUtils;
import client.utils.Translation;
import commons.Event;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StartupScreenCtrlTest{

    private TestStartupScreenCtrl sut;
    private TestServerUtils testServerUtils;
    private TestMainController testMainController;
    @BeforeEach
    public void setup() {
        this.testServerUtils = new TestServerUtils();
        this.testMainController =  new TestMainController();
        sut = new TestStartupScreenCtrl(this.testServerUtils, this.testMainController, null);

    }
    @Test
    public void testCreateEventEmptyTitle(){
        String title = "";
        sut.textBoxText = title;
        sut.createEvent();
        assertTrue(sut.joinEventCalls.isEmpty());
        assertTrue(testServerUtils.calls.isEmpty());
    }

    @Test
    public void testCreateEventSuccess(){
        String title = "title";
        sut.textBoxText = title;
        sut.createEvent();
        assertEquals(testServerUtils.calls.size(), 1);
        assertEquals(sut.joinEventCalls.size(), 1);
    }
    @Test
    public void testJoinEventInvalidLength(){
        String inviteCode = "invalid";
        sut.textBoxText = inviteCode;
        sut.joinEventClicked();
        assertEquals(0, testServerUtils.calls.size());
        assertEquals(0, testMainController.calls.size());
        assertFalse(sut.labelBindings.isEmpty());
        assertTrue(sut.labelBindings.contains("Startup.Label.InvalidCode"));
    }
//
//    @Test
//    public void testJoinEventInvalidCode(){
//        String inviteCode = "aaaaab";
//        inviteCodeTextBox.setText(inviteCode);
//        sut.joinEventClicked();
//        assertEquals(1, testServerUtils.calls.size());
//        //assertEquals(1, testMainController.calls.size());
//        //assertEquals("Invalid invitation code!",joinEventFeedback.getText());
//    }
//
//    @Test
//    public void testJoinEventValidCode(){
//        String inviteCode = "aaaaaa";
//        inviteCodeTextBox.setText(inviteCode);
//        sut.joinEventClicked();
//        assertEquals(1, testServerUtils.calls.size());
//        assertEquals(1, testMainController.calls.size());
//    }

    private class TestServerUtils extends ServerUtils{
        public List<String> calls = new LinkedList<>();
        @Override
        public Event getEvent(String inviteCode){
            calls.add("getEvent: " + inviteCode);
            //valid code
            if(inviteCode.equals("aaaaaa")){
                return new Event();
            }
            //invalid code
            throw new jakarta.ws.rs.BadRequestException();
        }

        @Override
        public Event createEvent(String inviteCode){
            calls.add("createEvent: " + inviteCode);
            return new Event();
        }
    }

    private class TestMainController extends MainCtrl{
        public List<String> calls = new LinkedList<>();
        public Event lastEvent;
        @Override
        public void joinEvent(Event event){
            lastEvent = event;
            calls.add("join");
        }
    }

    private class TestStartupScreenCtrl extends StartupScreenCtrl{
        public String textBoxText;
        public List<String> joinEventCalls = new ArrayList<>();

        public List<String> labelBindings = new ArrayList<>();
        /**
         * Constructor
         *
         * @param server      the ServerUtils instance
         * @param mainCtrl    the MainCtrl instance
         * @param translation the Translation to use
         */
        public TestStartupScreenCtrl(ServerUtils server, MainCtrl mainCtrl, Translation translation) {
            super(server, mainCtrl, translation);
        }

        @Override
        public String getTextBoxText(TextField textBox){
            return textBoxText;
        }

        @Override
        public void bindLabel(Label label,String str){
            labelBindings.add(str);
            return;
        }

        @Override
        public void joinEvent(Event event){
            joinEventCalls.add(event.toString());
        }
    }
}
