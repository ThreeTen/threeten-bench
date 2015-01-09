/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.threeten;

import java.time.LocalDate;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@Measurement(batchSize = 2000, iterations = 10)
@Warmup(iterations = 6)
@Threads(value = 1)
@Fork(value = 1)
public class LocalDateBenchmark {

    private static final LocalDate DATE = LocalDate.of(2014, 6, 1);

    @Benchmark
    public LocalDate bmkPlusDays1() {
        return DATE.plusDays(1);
    }

    @Benchmark
    public LocalDate bmkPlusDaysInline1() {
        return plusDaysInline(DATE, 1);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better1() {
        return plusDaysBetter1(DATE, 1);
    }

    @Benchmark
    public LocalDate bmkPlusDays30Better1() {
        return plusDaysBetter1(DATE, 30);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better2() {
        return plusDaysBetter2(DATE, 1);
    }

    @Benchmark
    public LocalDate bmkPlusDays30Better2() {
        return plusDaysBetter2(DATE, 30);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better3() {
        return plusDaysBetter3(DATE, 1);
    }

    @Benchmark
    public LocalDate bmkPlusDays30Better3() {
        return plusDaysBetter3(DATE, 30);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better4() {
        return plusDaysBetter4(DATE, 1);
    }

    @Benchmark
    public LocalDate bmkPlusDays30Better4() {
        return plusDaysBetter4(DATE, 30);
    }

    //-------------------------------------------------------------------------
    public LocalDate plusDaysInline(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

    public LocalDate plusDaysBetter1(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long dom = input.getDayOfMonth() + daysToAdd;
        if (dom > 0 && dom <= 28) {
            return LocalDate.of(input.getYear(), input.getMonthValue(), (int) dom);
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

    public LocalDate plusDaysBetter2(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long dom = input.getDayOfMonth() + daysToAdd;
        if (dom > 0) {
            if (dom <= input.lengthOfMonth()) {
                return LocalDate.of(input.getYear(), input.getMonthValue(), (int) dom);
            }
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

    public LocalDate plusDaysBetter3(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long dom = input.getDayOfMonth() + daysToAdd;
        if (dom > 0 && dom <= 59) {
            int monthLen = input.lengthOfMonth();
            if (dom <= monthLen) {
                return LocalDate.of(input.getYear(), input.getMonthValue(), (int) dom);
            } else {
                return LocalDate.of(input.getYear(), input.getMonthValue() + 1, (int) (dom - monthLen));
            }
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

    public LocalDate plusDaysBetter4(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        int monthLen = input.lengthOfMonth();
        long dom = input.getDayOfMonth() + daysToAdd;
        if (dom >= 1) {
            if (dom <= monthLen) {
                // same month
                return LocalDate.of(input.getYear(), input.getMonthValue(), (int) dom);
            } else if (dom <= monthLen + 28) {
                // next month (28 guarantees only one month later)
                dom -= monthLen;
                int month = input.getMonthValue();
                int year = input.getYear();
                if (month == 12) {
                    return LocalDate.of(year + 1 , 1, (int) dom);
                } else {
                    return LocalDate.of(year, month + 1, (int) dom);
                }
            }
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }
}
