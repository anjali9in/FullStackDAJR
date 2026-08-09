package com.core.fullstack.java8feature.streams;

import java.util.Arrays;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamTest {

	public static void main(String[] args) {
		List<MyData<Integer>> scores = Arrays.asList(new MyData<>(1, 100), new MyData<>(2, 200), new MyData<>(3, 300),
				new MyData<>(4, 400), new MyData<>(5, 500));

		List<String> words = Arrays.asList("apple", "", "banana", "pear", "", "grape", "kiwi", "date");
//		List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5, 11, 13, 8);

//		demoMapFilterSortReduce(scores);
//		demoStringOperations(words);
//		demoNumericOperations(numbers);
//		demoFindAndMatch(words);
		demoGrouping(words);
//		getMultiLineStringDemo();
	}

	private static void getMultiLineStringDemo() {
		var cities = """
				  San Francisco
				  Casablanca
				  Antwerp
				  New Delhi
				  Osaka
				""";

		Stream<String> lines = cities.lines();
		String hh = "";
		System.out.println(lines.toList());

		var message = """
				{"name" : "%s", "language" : "Java"}""";

		var bbb = "{\"name\" : \"%s\", \"language\" : \"Java\"}";
		System.out.println(message.formatted("Duke"));
		System.out.println(bbb.formatted("Duke"));
		System.out.println(bbb == message);

		String cities2 = """
				New York   \s
				Paris      \s
				Bangalore  \s
				Singapore  \s
				Tokyo      \s
				""";

		cities2.lines().map(line -> "|" + line + "|").forEach(System.out::println);
	}

	private static void demoMapFilterSortReduce(List<MyData<Integer>> scores) {
		System.out.println("\n=== Basic map/filter/sort/reduce ===");

		List<Integer> values = scores.stream().map(MyData::getData).collect(Collectors.toList());
		System.out.println("Mapped values: " + values);

		List<MyData<Integer>> filtered = scores.stream().filter(item -> item.getData() > 250)
				.collect(Collectors.toList());
		System.out.println("Filtered (>250): " + filtered);

		List<MyData<Integer>> sorted = scores.stream().sorted(Comparator.comparingInt(MyData::getData))
				.collect(Collectors.toList());
		System.out.println("Sorted ascending: " + sorted);

		int sum = scores.stream().mapToInt(MyData::getData).sum();
		System.out.println("Reduced sum: " + sum);
	}

	private static void demoStringOperations(List<String> words) {
		System.out.println("\n=== String stream operations ===");

		long emptyCount = words.stream().filter(String::isEmpty).count();
		System.out.println("Empty strings count: " + emptyCount);

		List<String> nonEmpty = words.stream().filter(word -> !word.isEmpty()).collect(Collectors.toList());
		System.out.println("Non-empty strings: " + nonEmpty);

		String merged = words.stream().filter(word -> !word.isEmpty()).collect(Collectors.joining(", "));
		System.out.println("Joined non-empty strings: " + merged);
	}

	private static void demoNumericOperations(List<Integer> numbers) {
		System.out.println("\n=== Numeric stream operations ===");

		List<Integer> distinctSquares = numbers.stream().map(n -> n * n).distinct().sorted()
				.collect(Collectors.toList());
		System.out.println("Distinct squares: " + distinctSquares);

		IntSummaryStatistics stats = numbers.stream().mapToInt(Integer::intValue).summaryStatistics();
		System.out.println("Min: " + stats.getMin());
		System.out.println("Max: " + stats.getMax());
		System.out.println("Sum: " + stats.getSum());
		System.out.println("Average: " + stats.getAverage());
	}

	private static void demoFindAndMatch(List<String> words) {
		System.out.println("\n=== findFirst and match examples ===");

		Optional<String> startsWithB = words.stream().filter(word -> word.startsWith("b")).findFirst();
		System.out.println("First word starting with 'b': " + startsWithB.orElse("Not found"));

		boolean anyLengthGt5 = words.stream().anyMatch(word -> word.length() > 5);
		boolean allNonEmpty = words.stream().allMatch(word -> !word.isEmpty());
		System.out.println("Any word length > 5: " + anyLengthGt5);
		System.out.println("All words non-empty: " + allNonEmpty);
	}

	private static void demoGrouping(List<String> words) {
		System.out.println("\n=== Grouping example ===");

		Map<Integer, List<String>> groupedByLength = words.stream().filter(word -> !word.isEmpty())
				.collect(Collectors.groupingBy(String::length));
		System.out.println("Grouped by length: " + groupedByLength);
	}

}

class MyData<T> {
	private int id;
	private T data;

	public MyData(int id, T data) {
		this.id = id;
		this.data = data;
	}

	public int getId() {
		return id;
	}

	public T getData() {
		return data;
	}

	@Override
	public String toString() {
		return "MyData{" + "id=" + id + ", data=" + data + '}';
	}
}