package com.uniinformation.utils;

import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.JvmAttributeGaugeSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadDump;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

public class HealthCheckUtil {
	public static void run() {
		MetricRegistry registry = new MetricRegistry();
		registry.register("gc", new GarbageCollectorMetricSet());
		registry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
		registry.register("memory", new MemoryUsageGaugeSet());
		
		registry.register("jvm.attribute", new JvmAttributeGaugeSet());
		registry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory
                                                                               .getPlatformMBeanServer()));
        registry.register("jvm.classloader", new ClassLoadingGaugeSet());
        registry.register("jvm.filedescriptor", new FileDescriptorRatioGauge());
        registry.register("jvm.gc", new GarbageCollectorMetricSet());
        registry.register("jvm.memory", new MemoryUsageGaugeSet());
        registry.register("jvm.threads", new ThreadStatesGaugeSet());
		
		ScheduledReporter reporter;

		reporter = ConsoleReporter.forRegistry(registry)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
		/*
		reporter = CsvReporter.forRegistry(registry)
                .formatFor(Locale.US)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(new File("/tmp/healthcheck/));
        reporter.start(5, TimeUnit.SECONDS);
        */
		reporter.report();
		
		//test threaddump
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ThreadDump(ManagementFactory.getThreadMXBean()).dump(baos);
        UniLog.log1("threaddump:\n============\n%s============", baos.toString());
		
	}
	public static void main (String args[]) throws Exception{
		HealthCheckUtil.run();
		HealthCheckUtil.run();
		HealthCheckUtil.run();
		HealthCheckUtil.run();
		HealthCheckUtil.run();
	}

}
