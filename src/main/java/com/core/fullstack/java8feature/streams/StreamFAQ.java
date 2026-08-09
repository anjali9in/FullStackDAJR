package com.core.fullstack.java8feature.streamsOptional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class StreamFAQ {

    public static void main(String[] args) {
        // Example usage of String lines() method
        //      getMaxLengthString();

        // getMinLengthString();
        findStartWithString(Arrays.asList("apple", "banana", "cherry", "date", "grapefruit"), "b");
    }
    
    public static void findStartWithString(List<String> dataList, String startWith) {
        Optional<String> foundString = dataList
                .stream()
                .filter(s -> s.startsWith(startWith))
                .findFirst();

                System.out.println("Found string: " + foundString.orElse("No string found starting with " + startWith)
        );
    }

        public static void findEndsWithString(List<String> dataList, String startWith) {
        Optional<String> foundString = dataList
                .stream()
                .filter(s -> s.startsWith(startWith))
                .findFirst();

        System.out.println("Found string: " + foundString.orElse("No string found starting with " + startWith));
    }

    public static void getMaxLengthString() {
        List<String> strings = Arrays.asList("apple", "banana", "cherry", "date", "grapefruit");
        Optional<String> longestString = strings
                .stream()
                .max(Comparator.comparingInt(String::length));

        System.out.println("Longest string: " + longestString.orElse("No strings found"));
    }

    public static void getMinLengthString() {
        List<String> strings = Arrays.asList("apple", "banana", "cherry", "date", "rat", "grapefruit");

        strings.stream()
                .forEach(b -> System.out.println("with forEach data:" + b));

        strings.stream()
                .map(b -> {
            System.out.println("with unused map data:" + b);
            return b;
        });

        Stream<String> shortestStringStream = strings.stream()
                .map(b -> {
            System.out.println("with used map data:" + b);
            return b;
        });

        Optional<String> shortestStringOpt = shortestStringStream
                .min(Comparator.comparingInt(String::length));

        System.out.println("Shortest string shortestStringOpt: " + shortestStringOpt);

        String shortestString = shortestStringOpt.orElse("No strings found");

        System.out.println("Shortest string: " + shortestString);
    }

    public static void getSumOfLengths() {
        List<String> strings = Arrays.asList("apple", "banana", "cherry", "date", "grapefruit");
        int totalLength = strings
                .stream()
                .mapToInt(String::length)
                .sum();

        System.out.println("Total length of all strings: " + totalLength);
    }

    public static void getAverageLength() {
        List<String> strings = Arrays.asList("apple", "banana", "cherry", "date", "grapefruit");
        double averageLength = strings
                .stream()
                .mapToInt(String::length)
                .average()
                .orElse(0.0);

        System.out.println("Average length of strings: " + averageLength);
    }


}
