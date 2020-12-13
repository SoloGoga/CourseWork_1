package sample;

import sample.memory.MemoryBlock;
import sample.memory.MemoryScheduler;
import sample.queue.PriorityQueue;
import sample.queue.Process;
import sample.queue.Resource;
import sample.pc.CPU;
import sample.pc.Core;
import sample.util.ITicking;

import java.util.ArrayList;
import java.util.Random;

public class TaskScheduler implements ITicking
{
    private CPU cpu;
    private MemoryScheduler memoryScheduler;

    private PriorityQueue processQueue;
    private ArrayList<Process> completedList;
    private ArrayList<Process> rejectsList;

    private Random random = new Random();

    private int lastId = 1;

    private int tasksFinished = 0;
    private int tasksRejected = 0;

    public TaskScheduler(CPU cpu, int systemMemory)
    {
        this.cpu = cpu;
        memoryScheduler = new MemoryScheduler(systemMemory);

        processQueue = new PriorityQueue(Configuration.PROCESS_MAX_PRIORITY);
        completedList = new ArrayList<>();
        rejectsList = new ArrayList<>();

        memoryScheduler.fillMemoryBlock(Configuration.OS_MEMORY_USAGE);
        Main.guiController.updateMemoryUsage();
    }

    @Override
    public void makeTick(int currentTime)
    {
        Process nextProcess = processQueue.peek();
        if(nextProcess != null)
        {
            if (cpu.availableCore())
            {
                processQueue.pop();
                cpu.runProcess(nextProcess);
            }
            else
            {
                Core lowestPriorityCore = cpu.getCore(cpu.getLowestPriorityIndex());
                if(nextProcess.getPriority() < lowestPriorityCore.getCurrentProcess().getPriority())
                {
                    processQueue.front().element().setState(Process.State.FINISHED);
                    lowestPriorityCore.supplantProcess(nextProcess);
                }
            }
        }

        //random task scheduling
        if(Configuration.randomProcessGenerationEnabled())
        {
            if (random.nextInt(10) == 0)
                scheduleRandom();
        }
    }

    public void scheduleTask(String name)
    {
        Process task = new Process(name);
        scheduleTask(task);
    }

    public boolean scheduleTask(Process task)
    {
        if(task.getState() == Process.State.WAITING)
        {
            int additionalTime = task.getTimeRequired() - task.getBurstTime();
            additionalTime = Math.floorDiv(additionalTime, 100) * random.nextInt(16) + 5;
            task.increaseRequiredTime(additionalTime);
            task.setInterruptionReason("");

            processQueue.push(task);
            task.setState(Process.State.READY);
            task.setResource("");
            Main.guiController.updateCPUQueue();

            return true;
        }
        else if(task.getState() == Process.State.READY)
        {
            if(task.getBurstTime() > 0)
            {
                processQueue.push(task);
                Main.guiController.updateCPUQueue();

                return true;
            }
        }

        MemoryBlock memory = memoryScheduler.fillMemoryBlock(task.getMemoryUsage());
        if(memory == null)
        {
            rejectProcess(task);
            return false;
        }

        task.setLocationInMemory(memory);
        processQueue.push(task);
        task.setState(Process.State.READY);
        Main.guiController.updateMemoryUsage();

        return true;
    }

    public void scheduleRandom()
    {
        Process task = new Process();
        scheduleTask(task);
    }

    public void freeMemoryBlock(MemoryBlock block)
    {
        memoryScheduler.releaseMemoryBlock(block);
        Main.guiController.updateMemoryUsage();
    }

    public void addProcessToCompleted(Process process)
    {
        completedList.add(process);
        tasksFinished++;
        Main.guiController.updateTable(Controller.Tables.FINISHED);
        Main.guiController.updateTasksFinished();
    }

    public void rejectProcess(Process process)
    {
        rejectsList.add(process);
        tasksRejected++;
        Main.guiController.updateTable(Controller.Tables.REJECTED);
        Main.guiController.updateTasksRejected();
    }

    public void finishWork()
    {
        for (Resource r : Main.getSystemResources())
            r.finishWork();

        processQueue.clear();
        cpu.finishWork();
    }

    public ArrayList<Process> getCPUTaskList()
    {
        ArrayList<Process> result = new ArrayList<>();
        result.addAll(cpu.getCoresContent());
        result.addAll(processQueue.getList());

        return result;
    }

    public ArrayList<Process> getResourceTaskList(int resourceIndex)
    {
        return Main.getSystemResources().get(resourceIndex).getTaskList();
    }

    public ArrayList<Process> getRejectsList()
    {
        return rejectsList;
    }

    public ArrayList<Process> getCompletedList()
    {
        return completedList;
    }

    public ArrayList<Process> getResourcesContent()
    {
        ArrayList<Process> result = new ArrayList<>();
        result.addAll(cpu.getCoresContent());

        for (Resource r : Main.getSystemResources())
        {
            result.add(r.getCurrentTask());
        }

        return result;
    }

    public int getLastId()
    {
        return lastId;
    }

    public void incrementLastId()
    {
        lastId++;
        Main.guiController.updateTasksTotal();
    }

    public int getTasksFinished() { return tasksFinished; }
    public int getTasksRejected() { return tasksRejected; }
    public int getQueueLength() { return processQueue.getList().size(); }
    public int getCPUInactivity() { return cpu.getInactivityTicks(); }
    public int getMemoryUsage() { return memoryScheduler.getMemoryUsage(); }
}
