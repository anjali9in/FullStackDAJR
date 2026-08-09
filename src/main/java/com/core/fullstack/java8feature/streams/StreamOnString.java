package com.core.fullstack.java8feature.streams;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class StreamOnString {

	public static void main(String[] args) {
		List<String> strings = Arrays.asList("apple", "banana", "cherry", "date", "apple", "grapefruit", "date",
				"uiuiu", "rat", "grapefruit");

//		getMaxLengthString();
//		allMethodsList(strings);
		
	}

	public static void allMethodsStream(List<String> strings) {

		// filter returns Stream of data based on condition
		Stream<String> filteredData = strings.stream().filter(string -> string.length() > 5);
		// print stream data
		System.out.println("Filtered Data: filter(string -> string.length() > 5)");
		filteredData.forEach(System.out::println);

		// map transform data based on condition
		Stream<String> transformedData = strings.stream().map(string -> string.toUpperCase());
		System.out.println("Transformed Data: map(string -> string.toUpperCase())");
		// print stream data
		transformedData.forEach(System.out::println);

		// distinct()
		Stream<String> filteredDuplicateData = strings.stream().distinct();
		System.out.println("Filtered Duplicate Data: distinct()");
		filteredDuplicateData.forEach(System.out::println);

		// limit(long maxSize)
		Stream<String> limitedData = strings.stream().limit(3);
		System.out.println("Limited Data: limit(3)");
		limitedData.forEach(System.out::println);

		// skip(long n)
		Stream<String> skipNthData = strings.stream().skip(2);
		System.out.println("Skip Nth Data: skip(2)");
		skipNthData.forEach(System.out::println);

		// takeWhile(Predicate<T> predicate) process until the condition is true and
		// stops when the condition is false
		Stream<String> workedUntilTrueData = strings.stream().takeWhile(d -> d.length() < 7);
		System.out.println("Worked Until True Data: takeWhile(d -> d.length() < 7)");
		workedUntilTrueData.forEach(System.out::println);

		// dropWhile(Predicate<T> predicate) , not takes if condition is true until
		// first false then process rest of the data
		Stream<String> pickedOnFirstFalseData = strings.stream().dropWhile(d -> d.length() < 7);
		System.out.println("Picked On First False Data: dropWhile(d -> d.length() < 7)");
		pickedOnFirstFalseData.forEach(System.out::println);

		// flatMap(Function<T, Stream<R>> mapper):
		Stream<String> flatMappedData = strings.stream().flatMap(d -> Arrays.asList(d).stream());
		System.out.println("Flat Mapped Data: flatMap(d -> Arrays.asList(d).stream())");
		flatMappedData.forEach(System.out::println);

		// sorted(): natural order sorting of stream data by alphabetical order
		Stream<String> sortedData = strings.stream().sorted();
		System.out.println("Sorted Data: sorted()");
		sortedData.forEach(System.out::println);

		// sorted(): natural order sorting of stream data by alphabetical order
		Stream<String> natSortedData = strings.stream().sorted(Comparator.naturalOrder());
		System.out.println("Sorted Data: sorted(Comparator.naturalOrder())");
		natSortedData.forEach(System.out::println);

		// sorted(Comparator<T> comparator) custom order sorting of stream data
		Stream<String> custSortedLenData = strings.stream().sorted(Comparator.comparingInt(String::length));
		System.out.println("Sorted Data: sorted(Comparator.comparingInt(String::length))");
		custSortedLenData.forEach(System.out::println);

		// sorted(Comparator<T> comparator) custom order sorting of stream data
		Stream<String> custSortedData = strings.stream()
				.sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
		System.out.println(
				"Sorted Data: sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))");
		custSortedData.forEach(System.out::println);

		// peek(Consumer<T> action)
		Stream<String> peekData = strings.stream().peek(d -> d.toUpperCase());
		System.out.println(" Data: peek(d -> d.toUpperCase())");
		peekData.forEach(System.out::println);

		// Primitive Mapping Variations:
		IntStream intData = strings.stream().mapToInt(m -> m.length());
		System.out.println(" Data:IntStream mapToInt(m -> m.length())");
		intData.forEach(System.out::println);

		LongStream longData = strings.stream().mapToLong(m -> m.length());
		System.out.println(" Data:LongStream mapToLong(m -> m.length())");
		longData.forEach(System.out::println);
		
		DoubleStream doubleData = strings.stream().mapToDouble(m -> m.length());
		System.out.println("Sorted DoubleStream mapToDouble(m -> m.length())");
		doubleData.forEach(System.out::println);

//			flatMapToInt(), flatMapToLong(), flatMapToDouble()
//mapMulti(BiConsumer<T, Consumer<R>> mapper)

		//count() returns the count of elements in the stream
		long count = strings.stream().count();
		System.out.println("count:" + count);

	}

	public static void allMethodsList(List<String> strings) {

		// filter returns Stream of data based on condition
		List<String> filteredData = strings.stream().filter(string -> string.length() > 5).toList();
		// print stream data
		System.out.println("Filtered Data: filter(string -> string.length() > 5)");
		filteredData.forEach(System.out::println);

		// map transform data based on condition
		List<String> transformedData = strings.stream().map(string -> string.toUpperCase()).toList();
		System.out.println("Transformed Data: map(string -> string.toUpperCase())");
		// print stream data
		
		transformedData = transformedData.stream().toList();
		transformedData.forEach(System.out::println);
		transformedData = transformedData.stream().collect(Collectors.toList());
		transformedData.forEach(System.out::println);

		// distinct()
		List<String> filteredDuplicateData = strings.stream().distinct().toList();
		System.out.println("Filtered Duplicate Data: distinct()");
		filteredDuplicateData.forEach(System.out::println);

		// limit(long maxSize)
		List<String> limitedData = strings.stream().limit(3).toList();
		System.out.println("Limited Data: limit(3)");
		limitedData.forEach(System.out::println);

//		// flatMap(Function<T, Stream<R>> mapper):
//		Stream<String> flatMappedData = strings.stream().flatMap(d -> Arrays.asList(d).stream());
//		System.out.println("Flat Mapped Data: flatMap(d -> Arrays.asList(d).stream())");
//		flatMappedData.forEach(System.out::println);
//
		// sorted(): natural order sorting of stream data by alphabetical order
		List<String> natSortedData = strings.stream().sorted(Comparator.naturalOrder()).toList();
		System.out.println("Sorted Data: sorted(Comparator.naturalOrder())");
		natSortedData.forEach(System.out::println);

		// sorted(Comparator<T> comparator) custom order sorting of stream data
		List<String> custSortedLenData = strings.stream().sorted(Comparator.comparingInt(String::length)).toList();
		System.out.println("Sorted Data: sorted(Comparator.comparingInt(String::length))");
		custSortedLenData.forEach(System.out::println);
//
		// sorted(Comparator<T> comparator) custom order sorting of stream data
		List<String> custSortedData = strings.stream()
				.sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder())).toList();
		System.out.println(
				"Sorted Data: sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))");
		custSortedData.forEach(System.out::println);
//
		// Primitive Mapping Variations:
		int [] intData = strings.stream().mapToInt(m -> m.length()).toArray();	
		System.out.println(" Data:IntStream mapToInt(m -> m.length())" + intData);
		
		List<Long> longData = strings.stream().mapToLong(m -> m.length()).boxed().toList();
		System.out.println(" Data:LongStream mapToLong(m -> m.length())");
		longData.forEach(System.out::println);
		
		List<Double> doubleData = strings.stream().mapToDouble(m -> m.length()).boxed().toList();
		System.out.println(" DoubleStream mapToDouble(m -> m.length())" +doubleData);
		doubleData.forEach(System.out::println);
//
////			flatMapToInt(), flatMapToLong(), flatMapToDouble()
////mapMulti(BiConsumer<T, Consumer<R>> mapper)
//
//		//count() returns the count of elements in the stream
//		long count = strings.stream().count();
//		System.out.println("count:" + count);

	}

	public static void getMaxLengthString() {
		List<String> strings = Arrays.asList("apple", "banana", "cherry", "date", "grapefruit");
		Optional<String> longestString = strings.stream().max(Comparator.comparingInt(String::length));

		System.out.println("Longest string: " + longestString.orElse("No strings found"));
	}

	public static void getMinLengthString() {
		List<String> strings = Arrays.asList("apple", "banana", "cherry", "date", "rat", "grapefruit");
		Optional<String> shortestString = strings.stream().min(Comparator.comparingInt(String::length));
		System.out.println("Shortest string: " + shortestString.orElse("No strings found"));
	}

	public static void getSumOfLengths() {
		List<String> strings = Arrays.asList("apple", "banana", "cherry", "date", "grapefruit");
		int totalLength = strings.stream().mapToInt(String::length).sum();

		System.out.println("Total length of all strings: " + totalLength);
	}

	public static void getAverageLength() {
		List<String> strings = Arrays.asList("apple", "banana", "cherry", "date", "grapefruit");
		double averageLength = strings.stream().mapToInt(String::length).average().orElse(0.0);

		System.out.println("Average length of strings: " + averageLength);
	}

	public static void getAverage() {
		List<String> strings = Arrays.asList("apple", "banana", "cherry", "date", "grapefruit");
		double averageLength = strings.stream().mapToInt(String::length).average().orElse(0.0);

		System.out.println("Average length of strings: " + averageLength);
	}

}
