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

import java.time.LocalDate;
import java.time.Month;
import java.time.chrono.IsoChronology;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@Measurement(batchSize = 2000, iterations = 8, time = 2, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Threads(value = 1)
@Fork(value = 1)
@State(Scope.Benchmark)
public class LocalDateBenchmark {

    private static final LocalDate DATE = LocalDate.of(2014, 6, 1);

    @Param({"1", "27", "30", "90", "-1"})
    public int arg;

    @Benchmark
    public LocalDate bmkPlusDays1() {
        return DATE.plusDays(arg);
    }

    @Benchmark
    public LocalDate bmkPlusDaysInline() {
        return plusDaysInline(DATE, arg);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better1() {
        return plusDaysBetter1(DATE, arg);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better2() {
        return plusDaysBetter2(DATE, arg);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better3a() {
        return plusDaysBetter3a(DATE, arg);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better3b() {
        return plusDaysBetter3b(DATE, arg);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better3c() {
        return plusDaysBetter3c(DATE, arg);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better4() {
        return plusDaysBetter4(DATE, arg);
    }

    @Benchmark
    public LocalDate bmkPlusDays1Better5() {
        return plusDaysBetter5(DATE, arg);
    }

    //-------------------------------------------------------------------------
    public LocalDate plusDaysInline(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

    // 4.1x speedup (463 vs 113) for same month
    // little change non-optimized path (114 vs 113)
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

    // 3.7x speedup (415 vs 113) for same month
    // little change non-optimized path (111 vs 113)
    public LocalDate plusDaysBetter2(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long dom = input.getDayOfMonth() + daysToAdd;
        if (dom > 0 && dom <= input.lengthOfMonth()) {
            return LocalDate.of(input.getYear(), input.getMonthValue(), (int) dom);
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

    // 3.7x speedup (422 vs 113) for same month
    // 3.7x speedup (417 vs 113) for next month
    // little change non-optimized path (113 vs 113)
    public LocalDate plusDaysBetter3a(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long dom = input.getDayOfMonth() + daysToAdd;
        if (dom > 0 && dom <= 59) { // 59th Jan is 28th Feb
            int monthLen = input.lengthOfMonth();
            int month = input.getMonthValue();
            int year = input.getYear();
            if (dom <= monthLen) {
                return LocalDate.of(year, month, (int) dom);
            } else if (month < 12) {
                return LocalDate.of(year, month + 1, (int) (dom - monthLen));
            } else {
                return LocalDate.of(year + 1, 1, (int) (dom - monthLen));
            }
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

    // faster than 3a for same month, little change otherwise
    public LocalDate plusDaysBetter3b(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long dom = input.getDayOfMonth() + daysToAdd;
        if (dom > 0 && dom <= 28) { // same month
            return input.withDayOfMonth((int) dom);
        }
        if (dom > 0 && dom <= 59) { // 59th Jan is 28th Feb
            int monthLen = input.lengthOfMonth();
            int month = input.getMonthValue();
            int year = input.getYear();
            if (dom <= monthLen) {
                return LocalDate.of(year, month, (int) dom);
            } else if (month < 12) {
                return LocalDate.of(year, month + 1, (int) (dom - monthLen));
            } else {
                return LocalDate.of(year + 1, 1, (int) (dom - monthLen));
            }
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

    // marginally faster than 3b for same month, little change otherwise
    public LocalDate plusDaysBetter3c(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long dom = input.getDayOfMonth() + daysToAdd;
        if (dom > 0) {
            if (dom <= 28) { // same month
                return input.withDayOfMonth((int) dom);
            }
            if (dom <= 59) { // 59th Jan is 28th Feb
                int monthLen = input.lengthOfMonth();
                int month = input.getMonthValue();
                int year = input.getYear();
                if (dom <= monthLen) {
                    return LocalDate.of(year, month, (int) dom);
                } else if (month < 12) {
                    return LocalDate.of(year, month + 1, (int) (dom - monthLen));
                } else {
                    return LocalDate.of(year + 1, 1, (int) (dom - monthLen));
                }
            }
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

    // 3.9x speedup (444 vs 113) for same month
    // 2.9x speedup (344 vs 113) for next/previous month
    // marginally slower for non-optimized path (109 vs 113)
    public LocalDate plusDaysBetter4(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long dom = input.getDayOfMonth() + daysToAdd;
        if (dom > 0) {
            int monthLen = input.lengthOfMonth();
            int month = input.getMonthValue();
            int year = input.getYear();
            if (dom <= monthLen) {
                // same month
                return LocalDate.of(year, month, (int) dom);
            } else if (dom <= monthLen + 28) {
                // next month (28 guarantees only one month later)
                dom -= monthLen;
                if (month == 12) {
                    return LocalDate.of(year + 1, 1, (int) dom);
                } else {
                    return LocalDate.of(year, month + 1, (int) dom);
                }
            }
        } else if (dom > -28) {
            // previous month (28 guarantees only one month earlier)
            int month = input.getMonthValue();
            int year = input.getYear();
            if (month == 1) {
                return LocalDate.of(year - 1, 12, (int) dom + 31);
            } else {
                int monthLen = Month.of(month - 1).length(input.isLeapYear());
                return LocalDate.of(year, month - 1, (int) dom + monthLen);
            }
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

    // 3.6x speedup (406 vs 113) for same month
    // 2.7x speedup (305 vs 113) for next month
    // 2.4x speedup (271 vs 113) for 90 days
    // little change non-optimized path (118 vs 113)
    public LocalDate plusDaysBetter5(LocalDate input, long daysToAdd) {
        if (daysToAdd == 0) {
            return input;
        }
        long dom = input.getDayOfMonth() + daysToAdd;
        if (dom > 0 && dom <= 180) {
            int month = input.getMonthValue();
            int year = input.getYear();
            while (true) {
                switch (month) {
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                    case 8:
                    case 10: {
                        if (dom <= 31) {
                            return LocalDate.of(year, month, (int) dom);
                        } else {
                            month++;
                            dom -= 31;
                        }
                    }
                    case 4:
                    case 6:
                    case 9:
                    case 11: {
                        if (dom <= 30) {
                            return LocalDate.of(year, month, (int) dom);
                        } else {
                            month++;
                            dom -= 30;
                        }
                    }
                    case 2: {
                        if (IsoChronology.INSTANCE.isLeapYear(year)) {
                            if (dom <= 29) {
                                return LocalDate.of(year, month, (int) dom);
                            } else {
                                month++;
                                dom -= 29;
                            }
                        } else {
                            if (dom <= 28) {
                                return LocalDate.of(year, month, (int) dom);
                            } else {
                                month++;
                                dom -= 29;
                            }
                        }
                    }
                    case 12: {
                        if (dom <= 31) {
                            return LocalDate.of(year, month, (int) dom);
                        } else {
                            year++;
                            month = 1;
                            dom -= 31;
                        }
                    }
                }
            }
        }
        long epDay = Math.addExact(input.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(epDay);
    }

}
