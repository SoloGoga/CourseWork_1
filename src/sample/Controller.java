package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import sample.queue.Process;
import sample.queue.Resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Controller
{
    @FXML
    TabPane queuesPane;
    @FXML
    BorderPane runningPane;

    @FXML
    Tab tabRejected;
    @FXML
    Tab tabFinished;

    @FXML
    ToolBar barResources;

    @FXML
    ToggleButton btnPause;
    @FXML
    Button btnRun;
    @FXML
    Button btnCreate;

    @FXML
    TextField txtMemory;
    @FXML
    TextField txtResources;

    @FXML
    Slider sldTps;

    @FXML
    Label lblTps;

    @FXML
    TableView tblResources;
    TableView tblRunning;
    TableView tblRejected;
    TableView tblFinished;

    @FXML
    Label lblTicks;
    @FXML
    Label lblFinished;
    @FXML
    Label lblRejected;
    @FXML
    Label lblTotal;
    @FXML
    Label lblQueue;
    @FXML
    Label lblInactivity;
    @FXML
    Label lblMemory;


    ObservableList<Process> tblDataRunning = FXCollections.observableArrayList();
    ObservableList<Process> tblDataRejected = FXCollections.observableArrayList();
    ObservableList<Process> tblDataFinished = FXCollections.observableArrayList();
    ObservableList<Process> tblDataResources = FXCollections.observableArrayList();

    ArrayList<ToggleButton> resourceButtons = new ArrayList<>();

    EventHandler handleResourceTableSwitch = new EventHandler()
    {
        @Override
        public void handle(Event event)
        {
            updateTable(Tables.RUNNING);
        }
    };

    @FXML
    private void initialize()
    {
        queuesPane.setTabMinWidth(64);
    }

    public void initControlButtons()
    {
        btnRun.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!Main.isRunning())
                {
                    initSystemResources();
                    Main.setupSystem();
                    btnPause.setSelected(false);
                    Main.setPause(false);
                    updateTable(Tables.RUNNING);
                    updateTable(Tables.REJECTED);
                    updateTable(Tables.FINISHED);
                    btnRun.setDisable(true);
                    btnPause.setDisable(false);
                    btnCreate.setDisable(false);

                    updateMemoryUsage();
                    updateCPUQueue();
                    updateTicks();
                    updateCPUInactivity();
                    updateTasksFinished();
                    updateTasksRejected();
                    updateTasksTotal();
                }
            }
        });

        btnPause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!Main.isFirstRun())
                {
                    Main.setPause(btnPause.isSelected());
                }
            }
        });

        btnCreate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openCreateProcessWindow();
            }
        });

        btnPause.setDisable(true);
        btnCreate.setDisable(true);
    }

    public void initTextFields()
    {
        txtMemory.setTextFormatter(new TextFormatter<Number>(change -> numberFilter(change)));
        txtMemory.setOnAction(event -> validateNumberField(txtMemory, 1024, 16384, 2048));
        txtMemory.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                validateNumberField(txtMemory, 1024, 16384, 2048);
            }
        });

        txtResources.setTextFormatter(new TextFormatter<Number>(change -> numberFilter(change)));
        txtResources.setOnAction(event -> validateNumberField(txtResources, 3, 5, 3));
        txtResources.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                validateNumberField(txtResources, 3, 5, 3);
            }
        });
    }

    public void initSliders()
    {
        sldTps.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                Configuration.setClockTps(newValue.intValue());
                lblTps.setText(String.valueOf(Configuration.getClockTps()));
            }
        });
    }

    private void initSystemResources()
    {
        try
        {
            Configuration.setMemoryVolume(Integer.parseInt(txtMemory.getText()));
            Configuration.setResourcesCount(Integer.parseInt(txtResources.getText()));
        }
        catch (NumberFormatException e)
        {
            Configuration.setDefaultResources();
        }
    }

    private void openCreateProcessWindow()
    {
        TextInputDialog dialog = new TextInputDialog("New Process");
        dialog.setTitle("Creating New Process");
        dialog.setHeaderText("Enter the name of a new process.\nUse only letters, numbers, '-' and '_'");
        dialog.setContentText("Process name:");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button btnCancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        dialog.setOnCloseRequest(event -> {
            event.consume();
        });

        dialog.show();

        btnOk.addEventFilter(ActionEvent.ACTION, event -> {
            String name = dialog.getEditor().getText();
            if (!name.isEmpty())
            {
                boolean correct = true;
                char[] arr = name.toCharArray();
                for (char c : arr)
                {
                    if (!Character.isLetter(c) && !Character.isDigit(c) && c != '-' && c != '_' && c != ' ')
                    {
                        correct = false;
                        break;
                    }
                }
                if (correct)
                {
                    dialog.setOnCloseRequest(e -> { });
                    dialog.close();
                    Main.getTaskScheduler().scheduleTask(name);
                }
                else
                {
                    dialog.setHeaderText("You should use only\nletters, numbers, '-' and '_'");
                }
            }
            else
            {
                dialog.setHeaderText("You should use only\nletters, numbers, '-' and '_'");
            }
        });

        btnCancel.addEventFilter(ActionEvent.ACTION, event -> {
            dialog.setOnCloseRequest(e -> { });
            dialog.close();
        });
    }

    private TextFormatter.Change numberFilter(TextFormatter.Change change)
    {
        if (change.isDeleted())
        {
            return change;
        }

        String text = change.getControlNewText();
        if (!text.matches("[0-9]+"))
        {
            return null;
        }

        return change;
    }

    private void validateNumberField(TextField field, int minVal, int maxVal, int defVal)
    {
        try
        {
            String oldText = field.getText();
            int old = Integer.parseInt(oldText);
            if(old > maxVal) field.setText(String.valueOf(maxVal));
            else if(old < minVal) field.setText(String.valueOf(minVal));
            else field.setText(oldText);
        }
        catch (Exception e) {field.setText(String.valueOf(defVal));}
    }

    public void initResourcesBar(ArrayList<Resource> resources)
    {
        resourceButtons.clear();
        resourceButtons.add(new ToggleButton("CPU"));
        resourceButtons.get(0).setOnAction(handleResourceTableSwitch);
        for (Resource item : resources)
        {
            resourceButtons.add(new ToggleButton(item.getName()));
            resourceButtons.get(resourceButtons.size() - 1).setOnAction(handleResourceTableSwitch);
        }

        barResources.getItems().setAll(resourceButtons);
        ToggleGroup group = new ToggleGroup();
        barResources.getItems().forEach(item ->
        {
            ((ToggleButton) item).setToggleGroup(group);
            ((ToggleButton) item).setMinWidth(64);
        });

        if(resourceButtons.size() > 0) resourceButtons.get(0).setSelected(true);
    }

    public void initBaseTabs()
    {
        tblRunning = generateProcessTable();
        runningPane.setCenter(tblRunning);

        tblRejected = generateTableRejected();
        tabRejected.setContent(tblRejected);

        tblFinished = generateTableFinished();
        tabFinished.setContent(tblFinished);

        generateTableResources();
    }

    public int getCurrentResourceTableIndex()
    {
        for (int i = 0; i < resourceButtons.size(); i++)
        {
            if(resourceButtons.get(i).isSelected())
            {
                return i;
            }
        }

        return 0;
    }

    public void updateTable(Tables table)
    {
        if(table == Tables.RUNNING)
        {
            int currentResourceTableIndex = getCurrentResourceTableIndex();

            if(currentResourceTableIndex == 0)
            {
                //requesting CPU task list
                tblDataRunning.setAll(Main.getTaskScheduler().getCPUTaskList());
                tblRunning.setItems(tblDataRunning);
            }
            else
            {
                //requesting current resource task list
                for (int i = 1; i <= Configuration.getResourcesCount(); i++)
                {
                    if(currentResourceTableIndex == i)
                    {
                        tblDataRunning.setAll(Main.getTaskScheduler().getResourceTaskList(i-1));
                    }
                }
            }
        }
        else if(table == Tables.REJECTED)
        {
            tblDataRejected.setAll(Main.getTaskScheduler().getRejectsList());
            tblRejected.setItems(tblDataRejected);
        }
        else if(table == Tables.FINISHED)
        {
            tblDataFinished.setAll(Main.getTaskScheduler().getCompletedList());
            tblFinished.setItems(tblDataFinished);

        }
        else if(table == Tables.RESOURCES)
        {
            tblDataResources.setAll(Main.getTaskScheduler().getResourcesContent());
            for (Object r: tblDataResources.toArray())
            {
                if(r == null)
                {
                    tblDataResources.remove(r);
                    continue;
                }
                String res = ((Process)r).getResource();
                if(res == "" || res.isEmpty()) tblDataResources.remove(r);
            }
            Set<Process> set = new HashSet<>(tblDataResources);
            tblDataResources.clear();
            tblDataResources.setAll(set);
            tblDataResources.sort(new Comparator<Process>() {
                @Override
                public int compare(Process o1, Process o2) {
                    return o1.getResource().compareTo(o2.getResource());
                }
            });

            tblResources.setItems(tblDataResources);
        }
    }

    private static TableView<Process> generateProcessTable()
    {
        TableColumn<Process, Integer> tblColId;
        TableColumn<Process, String> tblColName;
        TableColumn<Process, Integer> tblColPriority;
        TableColumn<Process, String> tblColStatus;
        TableColumn<Process, Integer> tblColMemory;

        TableView<Process> tbl = new TableView<>();

        tblColId = new TableColumn<>("ID");
        tblColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tbl.getColumns().add(tblColId);

        tblColName = new TableColumn<>("Name");
        tblColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tbl.getColumns().add(tblColName);

        tblColPriority = new TableColumn<>("Priority");
        tblColPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        tbl.getColumns().add(tblColPriority);

        tblColStatus = new TableColumn<>("Status");
        tblColStatus.setCellValueFactory(new PropertyValueFactory<>("state"));
        tbl.getColumns().add(tblColStatus);

        tblColMemory = new TableColumn<>("Memory Usage");
        tblColMemory.setCellValueFactory(new PropertyValueFactory<>("memoryUsage"));
        tbl.getColumns().add(tblColMemory);

        return tbl;
    }

    private static TableView<Process> generateTableRejected()
    {
        TableColumn<Process, Integer> tblColId;
        TableColumn<Process, String> tblColName;
        TableColumn<Process, Integer> tblColMemory;

        TableView<Process> tbl = new TableView<>();

        tblColId = new TableColumn<>("ID");
        tblColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tbl.getColumns().add(tblColId);

        tblColName = new TableColumn<>("Name");
        tblColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tbl.getColumns().add(tblColName);

        tblColMemory = new TableColumn<>("Memory Usage");
        tblColMemory.setCellValueFactory(new PropertyValueFactory<>("memoryUsage"));
        tbl.getColumns().add(tblColMemory);

        return tbl;
    }

    private static TableView<Process> generateTableFinished()
    {
        TableColumn<Process, Integer> tblColId;
        TableColumn<Process, String> tblColName;
        TableColumn<Process, Integer> tblColMemory;
        TableColumn<Process, Integer> tblColTime;
        TableColumn<Process, String> tblColStatus;

        TableView<Process> tbl = new TableView<>();

        tblColId = new TableColumn<>("ID");
        tblColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tbl.getColumns().add(tblColId);

        tblColName = new TableColumn<>("Name");
        tblColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tbl.getColumns().add(tblColName);

        tblColMemory = new TableColumn<>("Memory Usage");
        tblColMemory.setCellValueFactory(new PropertyValueFactory<>("memoryUsage"));
        tbl.getColumns().add(tblColMemory);

        tblColTime = new TableColumn<>("Run Time");
        tblColTime.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        tbl.getColumns().add(tblColTime);

        tblColStatus = new TableColumn<>("Status");
        tblColStatus.setCellValueFactory(new PropertyValueFactory<>("state"));
        tbl.getColumns().add(tblColStatus);

        return tbl;
    }

    private void generateTableResources()
    {
        TableColumn<Process, String> tblColResource;
        TableColumn<Process, Integer> tblColId;
        TableColumn<Process, String> tblColName;
        TableColumn<Process, Integer> tblColMemory;
        TableColumn<Process, Integer> tblColTime;
        TableColumn<Process, String> tblColStatus;

        tblColResource = new TableColumn<>("Resource");
        tblColResource.setCellValueFactory(new PropertyValueFactory<>("resource"));
        tblResources.getColumns().add(tblColResource);

        tblColId = new TableColumn<>("ID");
        tblColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tblResources.getColumns().add(tblColId);

        tblColName = new TableColumn<>("Name");
        tblColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tblResources.getColumns().add(tblColName);

        tblColMemory = new TableColumn<>("Memory Usage");
        tblColMemory.setCellValueFactory(new PropertyValueFactory<>("memoryUsage"));
        tblResources.getColumns().add(tblColMemory);

        tblColTime = new TableColumn<>("Run Time");
        tblColTime.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        tblResources.getColumns().add(tblColTime);

        tblColStatus = new TableColumn<>("Status");
        tblColStatus.setCellValueFactory(new PropertyValueFactory<>("state"));
        tblResources.getColumns().add(tblColStatus);
    }

    public void updateTicks()
    {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lblTicks.setText(String.valueOf(Main.getSystemTime()));
            }
        });
    }
    public void updateTasksFinished()
    {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lblFinished.setText(String.valueOf(Main.getTaskScheduler().getTasksFinished()));
            }
        });
    }
    public void updateTasksRejected()
    {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lblRejected.setText(String.valueOf(Main.getTaskScheduler().getTasksRejected()));
            }
        });
    }
    public void updateTasksTotal()
    {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lblTotal.setText(String.valueOf(Main.getTaskScheduler().getLastId()));
            }
        });
    }
    public void updateCPUQueue()
    {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lblQueue.setText(String.valueOf(Main.getTaskScheduler().getQueueLength()));
            }
        });
    }
    public void updateCPUInactivity()
    {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lblInactivity.setText(String.valueOf(Main.getTaskScheduler().getCPUInactivity()));
            }
        });
    }
    public void updateMemoryUsage()
    {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                lblMemory.setText(String.valueOf(Main.getTaskScheduler().getMemoryUsage()));
            }
        });
    }

    public enum Tables
    {
        RUNNING,
        REJECTED,
        FINISHED,
        RESOURCES
    }
}
