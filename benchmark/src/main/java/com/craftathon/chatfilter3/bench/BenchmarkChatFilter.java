/*
 * ChatFilter
 * Copyright (C) 2019 Craftathon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
