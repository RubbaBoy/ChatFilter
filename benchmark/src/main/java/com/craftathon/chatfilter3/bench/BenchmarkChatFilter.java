package com.craftathon.chatfilter3.bench;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 20)
@Measurement(iterations = 40)
@OutputTimeUnit(value = TimeUnit.MICROSECONDS)
@Fork(value = 1, warmups = 1)
public class BenchmarkChatFilter {

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void filterSingleShort(SingleFilterPlan singleFilterPlan) {
        singleFilterPlan.chatFilter.clean(singleFilterPlan.shortLine);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void filterSingleLong(SingleFilterPlan singleFilterPlan) {
        singleFilterPlan.chatFilter.clean(singleFilterPlan.longLine);
    }

}
