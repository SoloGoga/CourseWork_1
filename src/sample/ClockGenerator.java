package sample;

import sample.util.ITicking;

import java.util.ArrayList;

public class ClockGenerator extends Thread
{
    ArrayList<ITicking> attachedComponents;

    private int currentTick = 0;

    private boolean running = false;

    public void attachSystemComponent(ITicking component)
    {
        attachedComponents.add(component);
    }

    @Override
    public void run()
    {
        running = true;

        System.out.println("System clock is running.");

        while(running)
        {
            if(!Main.pauseActive())
            {
                try
                {
                    Thread.sleep(Math.floorDiv(1000, Configuration.getClockTps()));
                    for (ITicking item : attachedComponents)
                    {
                        item.makeTick(currentTick);
                    }
                    currentTick++;
                    Main.guiController.updateTicks();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("System clock is stopped.");
    }

    public ClockGenerator(ITicking... attachedComponents)
    {
        this.attachedComponents = new ArrayList<>();

        for (ITicking item : attachedComponents)
        {
            this.attachedComponents.add(item);
        }
    }

    public int getTime()
    {
        return currentTick;
    }

    public void finishWork() { running = false; }
}
