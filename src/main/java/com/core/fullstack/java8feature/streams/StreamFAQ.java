package com.core.fullstack.java8feature.streams;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class StreamFAQ {

    public static void main(String[] args) {
        // Example usage of String lines() method
      getMaxLengthString();
    }

    public static void getMaxLengthString() {
        List<String> strings = Arrays
                .asList("apple", "banana", "cherry", "date", "grapefruit");
        Optional<String> longestString = strings
                .stream()
                .max(Comparator.comparingInt(String::length));

        System.out.println("Longest string: " + longestString.orElse("No strings found"));
    }

    public static void getMinLengthString() {
        List<String> strings = Arrays
                .asList("apple", "banana", "cherry", "date", "rat", "grapefruit");
        Optional<String> shortestString = strings
                .stream()
                .min(Comparator.comparingInt(String::length));
        System.out.println("Shortest string: " + shortestString.orElse("No strings found"));
    }

    public static void getSumOfLengths() {
        List<String> strings = Arrays
                .asList("apple", "banana", "cherry", "date", "grapefruit");
        int totalLength = strings
                .stream()
                .mapToInt(String::length)
                .sum();

        System.out.println("Total length of all strings: " + totalLength);
    }

    public static void getAverageLength() {
        List<String> strings = Arrays
                .asList("apple", "banana", "cherry", "date", "grapefruit");
        double averageLength = strings
                .stream()
                .mapToInt(String::length)
                .average()
                .orElse(0.0);

        System.out.println("Average length of strings: " + averageLength);
    }
}
