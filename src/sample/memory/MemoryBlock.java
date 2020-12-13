package sample.memory;

import java.util.Comparator;

public class MemoryBlock
{
    int start;
    int end;

    public MemoryBlock(int start, int end)
    {
        this.start = start;
        this.end = end;
    }

    public static Comparator<MemoryBlock> byEnd = Comparator.comparingInt(o -> o.end);
}
