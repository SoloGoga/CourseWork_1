package sample.queue;

import sample.Configuration;
import sample.Controller;
import sample.Main;
import sample.memory.MemoryBlock;
import sample.util.AppGenerator;

import java.util.Random;

public class Process
{
    public enum State
    {
        READY,
        WAITING,
        RUNNING,
        FINISHED,
        TERMINATED
    }

    private final int id;
    private final String name;
    private final int priority;
    private final int memoryUsage;
    private final int startTime;

    private int requiredTime;
    private int burstTime;
    private State state;
    private String interruptionReason = "";

    private MemoryBlock memoryBlock;

    private String resource = "";

    private Random random = new Random();

    public Process(String name)
    {
        this.name = name;
        this.id = Main.getTaskScheduler().getLastId();
        Main.getTaskScheduler().incrementLastId();
        this.startTime = Main.getSystemTime();

        requiredTime = random.nextInt(90) + 10;
        memoryUsage = random.nextInt(Configuration.PROCESS_MAX_MEMORY_USAGE - Configuration.PROCESS_MIN_MEMORY_USAGE) + Configuration.PROCESS_MIN_MEMORY_USAGE;
        priority = random.nextInt(Configuration.PROCESS_MAX_PRIORITY - 1) + 1;

        burstTime = 0;
        state = State.READY;
    }

    public Process() { this(AppGenerator.generate()); }

    public boolean isFinished()
    {
        return this.state == State.FINISHED;
    }

    public void increaseBurstTime(int ticks)
    {
        burstTime += ticks;
    }

    public void increaseRequiredTime(int ticks)
    {
        this.requiredTime += ticks;
    }

    @Override
    public String toString()
    {
        return  "Process " + id +
                '{' +
                "name='" + name + '\'' +
                '}';
    }

    public void setBurstTime(int burstTime)
    {
        this.burstTime = burstTime;
    }

    public void setState(State state)
    {
        if(state == State.FINISHED || state == State.TERMINATED)
        {
            Main.getTaskScheduler().freeMemoryBlock(memoryBlock);
            Main.getTaskScheduler().addProcessToCompleted(this);
        }
        else if(state == State.RUNNING || state == State.READY)
        {
            Main.guiController.updateTable(Controller.Tables.RUNNING);
        }

        this.state = state;
    }

    public void setLocationInMemory(MemoryBlock memoryBlock) { this.memoryBlock = memoryBlock; }

    public void setInterruptionReason(String value)
    {
        this.interruptionReason = value;
    }

    public void setResource(String resourceName) { resource = resourceName; }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getPriority()
    {
        return priority;
    }

    public int getMemoryUsage() { return memoryUsage; }

    public int getTimeRequired()
    {
        return requiredTime;
    }

    public int getStartTime()
    {
        return startTime;
    }

    public int getBurstTime()
    {
        return burstTime;
    }

    public State getState()
    {
        return state;
    }

    public MemoryBlock getLocationInMemory() { return memoryBlock; }

    public String getInterruptionReason()
    {
        return interruptionReason;
    }

    public String getResource() { return resource; }
}
