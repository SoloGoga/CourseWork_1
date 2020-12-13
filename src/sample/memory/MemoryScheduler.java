package sample.memory;

import sample.Configuration;

import java.util.ArrayList;

public class MemoryScheduler
{
    private final int accessibleMemory;
    private ArrayList<MemoryBlock> memoryBlocks;

    public MemoryScheduler(int accessibleMemory)
    {
        this.accessibleMemory = accessibleMemory;
        memoryBlocks = new ArrayList<>();
    }

    private int findFreeBlock(int size)
    {
        if(size > accessibleMemory) return -1;

        if(memoryBlocks.isEmpty())
        {
            return 0;
        }

        if(memoryBlocks.size() == 1)
        {
            return memoryBlocks.get(0).end + 1;
        }

        memoryBlocks.sort(MemoryBlock.byEnd);


        int freeSize = -1;

        for (int i = 0; i < memoryBlocks.size()-1; i++)
        {
            if(memoryBlocks.get(i+1).start - memoryBlocks.get(i).end >= size)
            {
                freeSize = memoryBlocks.get(i).end + 1;

                for (++i; i < memoryBlocks.size()-1; i++)
                {
                    if(freeSize > (memoryBlocks.get(i+1).start - memoryBlocks.get(i).end) && (memoryBlocks.get(i+1).start - memoryBlocks.get(i).end) >= size)
                    {
                        freeSize = memoryBlocks.get(i).end + 1;
                    }
                }
            }

            if( i == memoryBlocks.size()-2 && (Configuration.getMemoryVolume() - memoryBlocks.get(i+1).end) >= size )
                return memoryBlocks.get(i+1).end + 1;

            if(i == memoryBlocks.size()-1)
                return freeSize;
        }

        return -1;
    }

    public MemoryBlock fillMemoryBlock(int size)
    {
        if(size > accessibleMemory) return null;

        int start = findFreeBlock(size);
        if(start == -1) return null;

        MemoryBlock result = new MemoryBlock(start, start + size - 1);
        if(addBlock(result)) return result;

        return null;
    }

    public void releaseMemoryBlock(MemoryBlock block)
    {
        if(block == null) return;
        memoryBlocks.remove(block);
    }

    public boolean addBlock(MemoryBlock block)
    {
        if(block == null) return false;
        return memoryBlocks.add(block);
    }

    public int getMemoryUsage()
    {
        int busy = 0;
        for (MemoryBlock block : memoryBlocks)
        {
            busy += block.end - block.start;
        }
        return busy;
    }

}
