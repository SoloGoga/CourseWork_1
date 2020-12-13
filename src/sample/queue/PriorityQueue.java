package sample.queue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class PriorityQueue
{
    private final ArrayList<Queue<Process>> queue;

    public PriorityQueue(final int maxPrior){
        queue = new ArrayList<>();

        for(int i = 0; i < maxPrior; ++i)
            queue.add(new LinkedList<>());
    }

    public boolean push(Process process){
        int prior = process.getPriority();

        if(prior < queue.size()){
            queue.get(queue.size() - prior - 1).add(process);
            return true;
        }

        return false;
    }

    public Process pop(){
        for(Queue<Process> q : queue){
            if(!q.isEmpty())
                return q.poll();
        }

        return null;
    }

    public Process peek() {
        for (Queue<Process> q : queue) {
            if (!q.isEmpty()) {
                return q.peek();
            }
        }
        return null;
    }

    public void clear() {
        for (Queue<Process> q : queue) {
            q.clear();
        }
    }

    public Queue<Process> front(){
        return queue.get(0);
    }

    public ArrayList<Process> getList() {
        ArrayList<Process> result = new ArrayList<>();
        for (Queue<Process> q : queue) {
            for (Process p : q) {
                result.add(p);
            }
        }
        return result;
    }

}
