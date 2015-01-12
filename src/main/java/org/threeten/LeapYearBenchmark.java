/*
 *  Copyright 2015 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.threeten;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@Measurement(batchSize = 2000, iterations = 5)
@Warmup(iterations = 5)
@Threads(value = 1)
@Fork(value = 1)
@State(Scope.Benchmark)
public class LeapYearBenchmark {
    // Benchmark numbers indicate no difference between the two algorithms

    @Param({"-5", "-4", "0", "1999", "2000", "2001", "2004", "2025", "2099", "2100"})
    public int arg;

    @Benchmark
    public boolean bmkLeapYear1() {
        return isLeapYear1(arg);
    }

    @Benchmark
    public boolean bmkLeapYear2() {
        return isLeapYear2(arg);
    }

    //-----------------------------------------------------------------------
    public boolean isLeapYear1(long year) {
        return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
    }

    public boolean isLeapYear2(long year) {
        return ((year & 3) == 0) && ((year % 25) != 0 || (year & 15) == 0);
    }

}
