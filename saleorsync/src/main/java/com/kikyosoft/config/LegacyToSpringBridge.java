package com.kikyosoft.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.*;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LegacyToSpringBridge implements ApplicationContextAware, DisposableBean, ApplicationListener<ContextClosedEvent> {

  private static volatile ApplicationContext ctx;
  private static volatile AutowireCapableBeanFactory awbf;

  // Cache for “manually created” fallbacks (non-Spring-managed instances)
  private static final Map<Class<?>, Object> fallbackCache = new ConcurrentHashMap<>();

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ctx = applicationContext;
    awbf = applicationContext.getAutowireCapableBeanFactory();
  }

  /** Preferred: get the real Spring bean by type. Throws if missing. */
  public static <T> T bean(Class<T> type) {
    ApplicationContext c = requireCtx();
    return c.getBean(type);
  }

  /** Optional: get by name + type (useful if multiple beans of same type). */
  public static <T> T bean(String name, Class<T> type) {
    ApplicationContext c = requireCtx();
    return c.getBean(name, type);
  }

  /**
   * Legacy-friendly:
   * 1) Try Spring bean by type.
   * 2) If not present and allowNew==true, new+autowire+initialize+cache a fallback singleton.
   * 3) Return cached fallback thereafter.
   */
  public static <T> T instance(Class<T> type) { return instance(type, true); }

  public static <T> T instance(Class<T> type, boolean allowNew) {
    // 1) Prefer the real Spring bean (includes proxies/AOP)
    ApplicationContext c = ctx;
    if (c != null) {
      try { return c.getBean(type); }
      catch (NoSuchBeanDefinitionException ignore) {}
    }

    // 2) Fallback cache (for non-Spring-managed singletons)
    Object cached = fallbackCache.get(type);
    if (cached != null) return type.cast(cached);

    if (!allowNew) {
      throw new IllegalStateException("No Spring bean of type " + type.getName() +
          " and allowNew=false");
    }

    // 3) Create a new instance, autowire its dependencies if we can
    T created = construct(type);
    AutowireCapableBeanFactory f = awbf;
    if (f != null) {
      f.autowireBean(created);
      // initializeBean runs @PostConstruct and applies BeanPostProcessors
      String beanName = Introspector.decapitalize(type.getSimpleName());
      created = type.cast(f.initializeBean(created, beanName));
    }

    Object prev = fallbackCache.putIfAbsent(type, created);
    return prev != null ? type.cast(prev) : created;
  }

  private static <T> T construct(Class<T> type) {
    try {
      Constructor<T> ctor = type.getDeclaredConstructor();
      ctor.setAccessible(true);
      return ctor.newInstance();
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Cannot create instance of " + type.getName() +
          " (needs no-arg constructor).", e);
    }
  }

  private static ApplicationContext requireCtx() {
    ApplicationContext c = ctx;
    if (c == null) throw new IllegalStateException("Spring ApplicationContext not initialized yet.");
    return c;
  }

  /** Clean up cached fallbacks on shutdown (best effort). */
  @Override public void onApplicationEvent(ContextClosedEvent event) { destroyFallbacks(); }
  @Override public void destroy() { destroyFallbacks(); }

  private static void destroyFallbacks() {
    AutowireCapableBeanFactory f = awbf;
    if (f != null) {
      fallbackCache.values().forEach(obj -> {
        try { f.destroyBean(obj); } catch (Throwable ignored) {}
      });
    }
    fallbackCache.clear();
  }
}
