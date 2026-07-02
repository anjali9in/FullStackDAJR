package com.core.fullstack.designpatterns.architechtural.masterslave;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MasterSlave {

	public static void main(String[] args) {
		List<SlaveNode> slaves = List.of(
			new StableSlave("slave-1"),
			new UnstableSlave("slave-2", 12),
			new StableSlave("slave-3")
		);

		MasterNode master = new MasterNode(slaves);

		List<JobTask> tasks = List.of(
			new JobTask("TASK-1", 10),
			new JobTask("TASK-2", 12),
			new JobTask("TASK-3", 14),
			new JobTask("TASK-4", 16),
			new JobTask("TASK-5", 18)
		);

		BatchResult result = master.executeBatch(tasks);
		printResult(result);
	}

	private static void printResult(BatchResult result) {
		System.out.println("\n================================");
		System.out.println("Batch ID : " + result.batchId());
		System.out.println("Status   : " + result.status());
		System.out.println("Summary  : " + result.summary());
		System.out.println("Outputs  :");

		for (TaskOutput output : result.outputs()) {
			System.out.println(
				" - "
					+ output.taskId()
					+ " -> result="
					+ output.result()
					+ " handledBy="
					+ output.handledBy()
			);
		}

		System.out.println("Events   :");
		for (String event : result.events()) {
			System.out.println(" - " + event);
		}
		System.out.println("================================");
	}

	enum BatchStatus {
		COMPLETED,
		PARTIAL,
		FAILED
	}

	record JobTask(String id, int payload) {
	}

	record TaskOutput(String taskId, int result, String handledBy) {
	}

	record BatchResult(
		String batchId,
		BatchStatus status,
		String summary,
		List<TaskOutput> outputs,
		List<String> events
	) {
	}

	interface SlaveNode {

		String name();

		TaskOutput process(JobTask task);
	}

	static class StableSlave implements SlaveNode {

		private final String name;

		StableSlave(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public TaskOutput process(JobTask task) {
			int computed = task.payload() * task.payload();
			return new TaskOutput(task.id(), computed, name);
		}
	}

	static class UnstableSlave implements SlaveNode {

		private final String name;
		private final int failForPayload;

		UnstableSlave(String name, int failForPayload) {
			this.name = name;
			this.failForPayload = failForPayload;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public TaskOutput process(JobTask task) {
			if (task.payload() == failForPayload) {
				throw new IllegalStateException("Simulated worker failure for payload " + failForPayload);
			}

			int computed = task.payload() * task.payload();
			return new TaskOutput(task.id(), computed, name);
		}
	}

	static class MasterNode {

		private final List<SlaveNode> slaves;
		private int nextSlaveIndex;

		MasterNode(List<SlaveNode> slaves) {
			if (slaves == null || slaves.isEmpty()) {
				throw new IllegalArgumentException("At least one slave is required");
			}
			this.slaves = slaves;
			this.nextSlaveIndex = 0;
		}

		BatchResult executeBatch(List<JobTask> tasks) {
			String batchId = "BATCH-" + UUID.randomUUID();
			List<String> events = new ArrayList<>();
			Map<String, TaskOutput> outputs = new LinkedHashMap<>();

			events.add("Batch started at " + Instant.now());
			events.add("Total tasks: " + tasks.size());

			int failures = 0;
			for (JobTask task : tasks) {
				TaskOutput output = tryProcessWithFailover(task, events);
				if (output != null) {
					outputs.put(task.id(), output);
				} else {
					failures++;
				}
			}

			BatchStatus status;
			String summary;
			if (outputs.isEmpty()) {
				status = BatchStatus.FAILED;
				summary = "No task completed";
			} else if (failures > 0) {
				status = BatchStatus.PARTIAL;
				summary = "Completed " + outputs.size() + " tasks with " + failures + " failures";
			} else {
				status = BatchStatus.COMPLETED;
				summary = "All tasks completed successfully";
			}

			events.add("Batch ended at " + Instant.now());
			return new BatchResult(batchId, status, summary, List.copyOf(outputs.values()), List.copyOf(events));
		}

		private TaskOutput tryProcessWithFailover(JobTask task, List<String> events) {
			int attempts = 0;
			int maxAttempts = slaves.size();

			while (attempts < maxAttempts) {
				SlaveNode selected = nextSlave();
				attempts++;

				events.add("Assigning " + task.id() + " to " + selected.name());

				try {
					TaskOutput output = selected.process(task);
					events.add(task.id() + " completed by " + selected.name());
					return output;
				} catch (RuntimeException exception) {
					events.add(
						task.id()
							+ " failed on "
							+ selected.name()
							+ " with error: "
							+ exception.getMessage()
					);
				}
			}

			events.add("All slaves failed to process " + task.id());
			return null;
		}

		private SlaveNode nextSlave() {
			SlaveNode node = slaves.get(nextSlaveIndex);
			nextSlaveIndex = (nextSlaveIndex + 1) % slaves.size();
			return node;
		}
	}
}
