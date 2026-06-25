package com.kikyosoft.servlet.larva.dashboard;

import java.math.BigDecimal;

public class DashboardMetrics {
  private long totalPageViews;
  private double pageViewsChangePct;

  private long totalUsers;
  private double usersChangePct;

  private long totalOrders;
  private double ordersChangePct;

  private BigDecimal totalSales;
  private double salesChangePct;

  private BigDecimal incomeThisWeek;
  private BigDecimal salesThisWeek;

  // --- getters / setters ---
  public long getTotalPageViews() { return totalPageViews; }
  public void setTotalPageViews(long v) { this.totalPageViews = v; }

  public double getPageViewsChangePct() { return pageViewsChangePct; }
  public void setPageViewsChangePct(double v) { this.pageViewsChangePct = v; }

  public long getTotalUsers() { return totalUsers; }
  public void setTotalUsers(long v) { this.totalUsers = v; }

  public double getUsersChangePct() { return usersChangePct; }
  public void setUsersChangePct(double v) { this.usersChangePct = v; }

  public long getTotalOrders() { return totalOrders; }
  public void setTotalOrders(long v) { this.totalOrders = v; }

  public double getOrdersChangePct() { return ordersChangePct; }
  public void setOrdersChangePct(double v) { this.ordersChangePct = v; }

  public BigDecimal getTotalSales() { return totalSales; }
  public void setTotalSales(BigDecimal v) { this.totalSales = v; }

  public double getSalesChangePct() { return salesChangePct; }
  public void setSalesChangePct(double v) { this.salesChangePct = v; }

  public BigDecimal getIncomeThisWeek() { return incomeThisWeek; }
  public void setIncomeThisWeek(BigDecimal v) { this.incomeThisWeek = v; }

  public BigDecimal getSalesThisWeek() { return salesThisWeek; }
  public void setSalesThisWeek(BigDecimal v) { this.salesThisWeek = v; }
}
