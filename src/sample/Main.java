package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.pc.CPU;
import sample.queue.Resource;

import java.util.ArrayList;

public class Main extends Application
{
    public static Controller guiController;

    private static ClockGenerator systemClock;
    private static CPU cpu;
    private static TaskScheduler taskScheduler;
    private static ArrayList<Resource> resources;

    private static boolean running = false;
    private static boolean firstRun = true;
    private static boolean pause = false;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Kyrsovay");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setResizable(false);

        guiController = loader.getController();
        guiController.initBaseTabs();
        guiController.initControlButtons();
        guiController.initTextFields();
        guiController.initSliders();

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();
        finishWork();
    }

    public static void main(String[] args)
    {
        launch(args);
    }

    public static void setupSystem()
    {
        System.out.println("Starting system.");

        cpu = new CPU(4);
        taskScheduler = new TaskScheduler(cpu, Configuration.getMemoryVolume());
        systemClock = new ClockGenerator(cpu, taskScheduler);
        resources = new ArrayList<>();
        for (int i = 0; i < Configuration.getResourcesCount(); i++)
        {
            Resource r = new Resource("R" + (i + 1));
            resources.add(r);
            systemClock.attachSystemComponent(r);
        }

        guiController.initResourcesBar(resources);

        systemClock.start();
        running = true;

        System.out.println("System setup completed!");

        firstRun = false;
    }

    public static void finishWork()
    {
        if(taskScheduler != null) taskScheduler.finishWork();
        if(systemClock != null) systemClock.finishWork();
        running = false;
        System.out.println("System shutdown.");
    }

    public static int getSystemTime()
    {
        return systemClock.getTime();
    }

    public static ClockGenerator getSystemClock() { return systemClock; }

    public static TaskScheduler getTaskScheduler()
    {
        return taskScheduler;
    }

    public static ArrayList<Resource> getSystemResources()
    {
        return resources;
    }

    public static boolean isRunning() { return running; }

    public static boolean isFirstRun() { return firstRun; }

    /*--Cross-Thread methods--*/

    public static synchronized boolean pauseActive() { return pause; }

    public static synchronized void setPause(boolean value) { pause = value; }
}
