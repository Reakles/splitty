package client.scenes;

import client.utils.ManagementOverviewUtils;
import client.utils.ServerUtils;
import client.utils.Translation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Event;
import jakarta.ws.rs.BadRequestException;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ManagementOverviewScreenCtrlTest {
    private Translation translation;
    private ServerUtils server;
    private ManagementOverviewUtils utils;
    private MainCtrl mainCtrl;
    private TestManagementOverviewScreenCtrl managementOverviewScreenCtrl;
    private ObjectMapper objectMapper;
    private File file;
    @BeforeEach
    public void setUp(){
        translation = mock(Translation.class);
        server = mock(ServerUtils.class);
        utils = new ManagementOverviewUtils(translation, server);
        mainCtrl = mock(MainCtrl.class);
        objectMapper = mock(ObjectMapper.class);
        file = mock(File.class);
        managementOverviewScreenCtrl = new TestManagementOverviewScreenCtrl(server, mainCtrl, null, utils);
        managementOverviewScreenCtrl.setObjectMapper(objectMapper);
    }

    @Test
    public void exportInvalidEventID(){
        when(server.getEvent(anyString())).thenThrow(new BadRequestException());
        managementOverviewScreenCtrl.textBoxText = "invalidcode";
        managementOverviewScreenCtrl.exportButtonClicked();
        assertTrue(managementOverviewScreenCtrl.bindings.contains("MOSCtrl.EventNotFound"));
    }

    @Test
    public void exportValidEventID(){
        Event event = new Event("title", new Date());
        when(server.getEvent(anyString())).thenReturn(event);

        try {
            doNothing().when(objectMapper).writeValue(any(File.class), any());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        managementOverviewScreenCtrl.textBoxText = "AAAAAA";
        managementOverviewScreenCtrl.exportButtonClicked();
        assertTrue(managementOverviewScreenCtrl.bindings.contains("MOSCtrl.SuccessExport"));
    }

    @Test
    public void exportIOException(){
        Event event = new Event("title", new Date());
        when(server.getEvent(anyString())).thenReturn(event);

        try {
            doThrow(new IOException()).when(objectMapper).writeValue(any(File.class), (Class<Event>) any());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        managementOverviewScreenCtrl.setObjectMapper(objectMapper);
        managementOverviewScreenCtrl.textBoxText = "AAAAAA";
        managementOverviewScreenCtrl.exportButtonClicked();
        assertTrue(managementOverviewScreenCtrl.bindings.contains("MOSCtrl.ErrorExportingEvent"));
    }

    @Test
    public void importInvalidEventID(){
        managementOverviewScreenCtrl.textBoxText = "invalidcode";
        try {
            when(objectMapper.readValue(any(File.class), (Class<Event>) any())).thenThrow(new IOException());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        managementOverviewScreenCtrl.setObjectMapper(objectMapper);
        managementOverviewScreenCtrl.importButtonClicked();
        assertTrue(managementOverviewScreenCtrl.bindings.contains("MOSCtrl.ErrorImportingEvent"));
        assertFalse(managementOverviewScreenCtrl.bindings.contains("MOSCtrl.SuccessImport"));
    }

    @Test
    public void importValid(){
        managementOverviewScreenCtrl.textBoxText = "AAAAAA";
        Event event = new Event("test", new Date());
        try {
            when(objectMapper.readValue(any(File.class), (Class<Event>) any())).thenReturn(event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        managementOverviewScreenCtrl.setObjectMapper(objectMapper);
        assertEquals(0, managementOverviewScreenCtrl.initializeAllEventsCalls);
        managementOverviewScreenCtrl.importButtonClicked();
        assertTrue(managementOverviewScreenCtrl.bindings.contains("MOSCtrl.SuccessImport"));
        assertEquals(1, managementOverviewScreenCtrl.initializeAllEventsCalls);
    }


    private class TestManagementOverviewScreenCtrl extends ManagementOverviewScreenCtrl{
        public String textBoxText;
        public ArrayList<String> bindings;
        public int initializeAllEventsCalls;

        /**
         * Constructor
         *
         * @param server      the ServerUtils instance
         * @param mainCtrl    the MainCtrl instance
         * @param translation the Translation to use
         * @param utils       the ManagementOverviewUtils to use
         */
        public TestManagementOverviewScreenCtrl(ServerUtils server, MainCtrl mainCtrl, Translation translation, ManagementOverviewUtils utils) {
            super(server, mainCtrl, translation, utils);
            bindings = new ArrayList<>();
            initializeAllEventsCalls = 0;
        }

        @Override
        public void bindButton(Button button, String str){
            bindings.add(str);
            return;
        }
        @Override
        public void bindTextField(TextField button, String str){
            bindings.add(str);
            return;
        }

        @Override
        public void bindLabel(Label button, String str){
            bindings.add(str);
            return;
        }

        @Override
        public String getTextBoxText(TextField textBox){
            return textBoxText;
        }

        public void setObjectMapper(ObjectMapper objectMapper){
            this.objectMapper = objectMapper;
        }
        @Override
        public void initializeAllEvents(){
            initializeAllEventsCalls+=1;
            return;
        }
    }
}