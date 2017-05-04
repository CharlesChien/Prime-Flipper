package com.cchien.sieveoferatosthenes;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by Chiapeng on 5/2/2017.
 * Inspired by C++ code from http://www.geeksforgeeks.org/segmented-sieve/
 */

public class SegmentedSieveOfEratosthenes {
    static LinkedHashSet<Integer> primes = null;
    static int max_num_calculated;
    static int limit;

    public final static int max_num_limit = 30000000; // 30 million
    // On a Samsung S4 it gets OutOfMemoryError at 33,541,489
    // On LG 5X, it's at                           52,649,801

    public static int last_number = 2;

    static final String DEBUG_TAG = "SOE - SSOE";

    public SegmentedSieveOfEratosthenes(int n) {
        int number = n > max_num_limit? max_num_limit : n;
        if (primes == null) {
            primes = new LinkedHashSet<Integer>();
            max_num_calculated = 0;
            segmentedSieve(number);
        } else if (number > max_num_calculated) {
            incrementalSegmentedSieve(n);
        }
    }

    public Iterator<Integer> getPrimeDivisors(int number) {
        // DEBUG Log.d(DEBUG_TAG, String.format("getPrimeDivisors(%d) - limit: %d, max_num_calculated: %d", number, limit, max_num_calculated));
        last_number = number;
        int sqrt_value = (new Double(Math.floor(Math.sqrt(number)))).intValue() + 1;
        if (sqrt_value > max_num_calculated) {
            simpleSieve(sqrt_value);
            // DEBUG Log.d(DEBUG_TAG, String.format("getPrimeDivisors(%d) - limit: %d, max_num_calculated: %d, sqrt_value: %d", number, limit, max_num_calculated, sqrt_value));
        }
        ArrayList<Integer> prime_devisors = new ArrayList<Integer>();
        Iterator<Integer> prime = primes.iterator();
        int current_num = number;
        while (current_num > 1 && prime.hasNext()) {
            int prime_number = prime.next().intValue();
            if (current_num < prime_number) {
                // DEBUG Log.d(DEBUG_TAG, String.format("getPrimeDivisors(%d) - current_num %d < prime_number %d", current_num, prime_number));
                break;
            }
            if (current_num % prime_number == 0) {
                do {
                    // DEBUG Log.d(DEBUG_TAG, String.format("getPrimeDivisors(%d) - prime number %d is a divisor of %d", number, prime_number, current_num));
                    prime_devisors.add(new Integer(prime_number));
                    current_num /= prime_number;
                } while (current_num > 1 && current_num % prime_number == 0);
            }
        }

        // This is to catch where a number is a composite of multiple primes and at least one
        // is greater than the square root of number.
        if (current_num > sqrt_value) {
            // DEBUG Log.d(DEBUG_TAG, String.format("getPrimeDivisors(%d) - prime number %d is a divisor of %d", number, current_num, number));
            prime_devisors.add(new Integer(current_num));
        }

        return prime_devisors.iterator();
    }

    public boolean isPrime(int number) {
        if (number > max_num_calculated) {
            // DEBUG Log.d(DEBUG_TAG, String.format("isPrime(%d) - limit: %d, max_num_calculated: %d", number, limit, max_num_calculated));
            int sqrt_value = (new Double(Math.floor(Math.sqrt(number)))).intValue() + 1;
            if (sqrt_value > max_num_calculated) {
                simpleSieve(sqrt_value);
                // DEBUG Log.d(DEBUG_TAG, String.format("isPrime(%d) - limit: %d, max_num_calculated: %d", number, limit, max_num_calculated));
            }

            // Experiment a way to not using all the memory.
            // Use the found primes by simpleSieve() to find
            // primes in current range
            Iterator<Integer> prime = primes.iterator();
            while (prime.hasNext()) {
                int prime_number = prime.next().intValue();

                if (number % prime_number == 0) {
                    // DEBUG Log.d(DEBUG_TAG, String.format("isPrime(%d) - prime number %d is a divisor", number, prime_number));
                    return false;
                }
            }
            return true;
        }
        return primes.contains(new Integer(number));
    }

    public boolean isPrimeOrg(int number) {
        if (number > max_num_calculated) {
            incrementalSegmentedSieve(number);
        }
        return primes.contains(new Integer(number));
    }

    private void simpleSieve(int max_number) {
        // DEBUG Log.d(DEBUG_TAG, String.format("Initializing Array, size %d...", max_number + 1));
        int[] arr = new int[max_number + 1];

        // DEBUG Log.d(DEBUG_TAG, "Looping through sqrt()");
        for (int i = 2; i <= Math.sqrt(max_number); i++) {
            if (arr[i] == 0) {
                for (int j = i * i; j <= max_number; j += i) {
                    arr[j] = 1;
                }
            }
        }

        for (int i = 2; i < max_number; i++) {
            if (arr[i] == 0) {
                primes.add(i);
            }
        }
    }

    private void incrementalSegmentedSieve(int max_number) {
        limit = (new Double(Math.floor(Math.sqrt(max_number)))).intValue() + 1;

        // Divide the range [0..n-1] in different segments
        // We have chosen segment size as sqrt(n).
        int low  = limit > max_num_calculated? max_num_calculated:limit;
        int high = low + limit;
        // DEBUG Log.d(DEBUG_TAG, String.format("incrementalSegmentedSieve() - limit: %d, max_num_calculated: %d, low: %d, high: %d", limit, max_num_calculated, low, high));

        LinkedHashSet<Integer> newly_found_primes = new LinkedHashSet<Integer>();

        // While all segments of range [0..n-1] are not processed,
        // process one segment at a time
        while (low < max_number) {
            // To mark primes in current range. A value in mark[i]
            // will finally be false if 'i-low' is Not a prime,
            // else true.
            int[] mark = new int[limit + 1];

            // Use the found primes by simpleSieve() to find
            // primes in current range
            Iterator<Integer> prime = primes.iterator();
            while (prime.hasNext()) {
                int prime_number = prime.next().intValue();

                // Find the minimum number in [low..high] that is
                // a multiple of prime[i] (divisible by prime[i])
                // For example, if low is 31 and prime[i] is 3,
                // we start with 33.
                int loLim = (new Double(Math.floor(low / prime_number))).intValue() * prime_number;
                if (loLim < low)
                    loLim += prime_number;

                    /*  Mark multiples of prime[i] in [low..high]:
                        We are marking j - low for j, i.e. each number
                        in range [low, high] is mapped to [0, high-low]
                        so if range is [50, 100]  marking 50 corresponds
                        to marking 0, marking 51 corresponds to 1 and
                        so on. In this way we need to allocate space only
                        for range  */
                for (int j = loLim; j < high; j += prime_number) {
                    mark[j - low] = 1;
                }
            }

            // Numbers which are not marked as false are prime
            for (int i = low; i < high; i++)
                if (mark[i - low] == 0) {
                    newly_found_primes.add(i);
                    // DEBUG Log.d(DEBUG_TAG, String.format("incrementalSegmentedSieve() - Add %d as prime", i));
                }
            // Update low and high for next segment
            low = low + limit;
            high = high + limit;
            if (high >= max_number)
                high = max_number;
        }

        // Adds all primes back to primes.
        primes.addAll(newly_found_primes);
        max_num_calculated = max_number;
        // DEBUG Log.d(DEBUG_TAG, String.format("incrementalSegmentedSieve() - max_num_calculated: %d", max_num_calculated));
    }

    private void segmentedSieve(int max_number) {
        limit = (new Double(Math.floor(Math.sqrt(max_number)))).intValue() + 1;
        simpleSieve(limit);

        // Divide the range [0..n-1] in different segments
        // We have chosen segment size as sqrt(n).
        int low  = limit;
        int high = 2*limit;
        // DEBUG Log.d(DEBUG_TAG, String.format("segmentedSieve() - limit: %d, max_num_calculated: %d, low: %d, high: %d", limit, max_num_calculated, low, high));

        LinkedHashSet<Integer> newly_found_primes = new LinkedHashSet<Integer>();

        // While all segments of range [0..n-1] are not processed,
        // process one segment at a time
        while (low < max_number) {
            // To mark primes in current range. A value in mark[i]
            // will finally be false if 'i-low' is Not a prime,
            // else true.
            int[] mark = new int[limit + 1];

            // Use the found primes by simpleSieve() to find
            // primes in current range
            Iterator<Integer> prime = primes.iterator();
            while (prime.hasNext()) {
                int prime_number = prime.next().intValue();

                // Find the minimum number in [low..high] that is
                // a multiple of prime[i] (divisible by prime[i])
                // For example, if low is 31 and prime[i] is 3,
                // we start with 33.
                int loLim = (new Double(Math.floor(low/prime_number))).intValue() * prime_number;
                if (loLim < low)
                    loLim += prime_number;

                /*  Mark multiples of prime[i] in [low..high]:
                    We are marking j - low for j, i.e. each number
                    in range [low, high] is mapped to [0, high-low]
                    so if range is [50, 100]  marking 50 corresponds
                    to marking 0, marking 51 corresponds to 1 and
                    so on. In this way we need to allocate space only
                    for range  */
                for (int j = loLim; j< high; j += prime_number) {
                    mark[j - low] = 1;
                }
            }

            // Numbers which are not marked as false are prime
            for (int i = low; i < high; i++)
                if (mark[i - low] == 0) {
                    newly_found_primes.add(i);
                    // DEBUG Log.d(DEBUG_TAG, String.format("segmentedSieve() - Add %d as prime, length: %d, limit: %d", i, newly_found_primes.size(), limit));
                }
            // Update low and high for next segment
            low  = low + limit;
            high = high + limit;
            if (high >= max_number)
                high = max_number;
        }


        // Adds all primes back to primes.
        primes.addAll(newly_found_primes);
        max_num_calculated = max_number;
        // DEBUG Log.d(DEBUG_TAG, String.format("segmentedSieve() - max_num_calculated: %d", max_num_calculated));
    }

}
